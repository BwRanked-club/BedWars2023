package com.tomkeuper.bedwars.history;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.NextEvent;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.gameplay.*;
import com.tomkeuper.bedwars.api.events.player.*;
import com.tomkeuper.bedwars.api.events.shop.ShopBuyEvent;
import com.tomkeuper.bedwars.api.events.shop.ShopOpenEvent;
import com.tomkeuper.bedwars.api.events.team.TeamEliminatedEvent;
import com.tomkeuper.bedwars.api.events.upgrades.UpgradeBuyEvent;
import com.tomkeuper.bedwars.api.language.Language;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MatchHistoryEventListener implements Listener {

    private boolean isEnabled() {
        return BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ENABLED)
                && BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_EVENTS_ENABLED);
    }

    private boolean allowResourceDrop() {
        return false;
    }

    private boolean allowResourceCollect() {
        return BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_EVENTS_LOG_RESOURCE_COLLECT);
    }

    private boolean allowBuildEvents() {
        return BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_EVENTS_LOG_BUILD_EVENTS);
    }

    private boolean allowProgressionEvents() {
        return BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_EVENTS_LOG_PROGRESSION_EVENTS);
    }

    private boolean allowStatEvents() {
        return BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_EVENTS_LOG_STAT_EVENTS);
    }

    private boolean allowShopOpen() {
        return BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_EVENTS_LOG_SHOP_OPEN);
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        if (!isEnabled()) return;
        IArena arena = event.getArena();
        if (arena == null) return;

        if (event.getNewState() == GameState.starting) {
            MatchHistoryRegistry.getOrCreate(arena);
        }

        if (event.getNewState() == GameState.waiting && event.getOldState() == GameState.starting) {
            UUID matchId = MatchHistoryRegistry.clear(arena);
            if (matchId != null) {
                MatchHistoryEventService.deleteEvents(matchId);
            }
        }

        if (event.getNewState() == GameState.playing) {
            Map<String, String> meta = new HashMap<>();
            meta.put("state", "playing");
            logEvent(arena, MatchHistoryEventType.GAME_START, null, null, null, meta);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(GameEndEvent event) {
        if (!isEnabled()) return;
        IArena arena = event.getArena();
        if (arena == null) return;
        Map<String, String> meta = new HashMap<>();
        if (event.getTeamWinner() != null) {
            meta.put("winner_team", event.getTeamWinner().getName());
        }
        meta.put("winners_count", String.valueOf(event.getWinners().size()));
        meta.put("losers_count", String.valueOf(event.getLosers().size()));
        logEvent(arena, MatchHistoryEventType.GAME_END, null, null, event.getTeamWinner(), meta);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinArenaEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("spectator", String.valueOf(event.isSpectator()));
        logEvent(event.getArena(), MatchHistoryEventType.PLAYER_JOIN, event.getPlayer(), null, event.getArena().getTeam(event.getPlayer()), meta);
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveArenaEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("spectator", String.valueOf(event.isSpectator()));
        if (event.getLastDamager() != null) {
            meta.put("last_damager", event.getLastDamager().getName());
        }
        logEvent(event.getArena(), MatchHistoryEventType.PLAYER_LEAVE, event.getPlayer(), event.getLastDamager(), event.getArena().getTeam(event.getPlayer()), meta);
    }

    @EventHandler
    public void onPlayerRejoin(PlayerReJoinEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("respawn_time", String.valueOf(event.getRespawnTime()));
        logEvent(event.getArena(), MatchHistoryEventType.PLAYER_REJOIN, event.getPlayer(), null, event.getArena().getTeam(event.getPlayer()), meta);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerReSpawnEvent event) {
        if (!isEnabled()) return;
        logEvent(event.getArena(), MatchHistoryEventType.PLAYER_RESPAWN, event.getPlayer(), null, event.getTeam(), null);
    }

    @EventHandler
    public void onPlayerFirstSpawn(PlayerFirstSpawnEvent event) {
        if (!isEnabled()) return;
        logEvent(event.getArena(), MatchHistoryEventType.PLAYER_FIRST_SPAWN, event.getPlayer(), null, event.getTeam(), null);
    }

    @EventHandler
    public void onPlayerKill(PlayerKillEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        Player killer = event.getKiller();
        Player victim = event.getVictim();
        PlayerKillEvent.PlayerKillCause cause = event.getCause();
        meta.put("cause", cause != null ? cause.name() : "UNKNOWN");
        meta.put("final_kill", String.valueOf(cause != null && cause.isFinalKill()));
        meta.put("despawnable", String.valueOf(cause != null && cause.isDespawnable()));
        meta.put("pvp_logout", String.valueOf(cause != null && cause.isPvpLogOut()));
        ITeam killerTeam = killer != null ? event.getArena().getTeam(killer) : null;
        ITeam victimTeam = victim != null ? event.getArena().getTeam(victim) : null;
        if (victimTeam != null) {
            meta.put("victim_team", victimTeam.getName());
        }
        logEvent(event.getArena(), MatchHistoryEventType.PLAYER_KILL, killer != null ? killer : victim, victim, killerTeam != null ? killerTeam : victimTeam, meta);
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        if (event.getVictimTeam() != null) {
            meta.put("victim_team", event.getVictimTeam().getName());
        }
        logEvent(event.getArena(), MatchHistoryEventType.BED_BREAK, event.getPlayer(), null, event.getPlayerTeam(), meta);
    }

    @EventHandler
    public void onTeamEliminated(TeamEliminatedEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("team", event.getTeam().getName());
        logEvent(event.getArena(), MatchHistoryEventType.TEAM_ELIMINATED, null, null, event.getTeam(), meta);
    }

    @EventHandler
    public void onTeamAssign(TeamAssignEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("team", event.getTeam().getName());
        logEvent(event.getArena(), MatchHistoryEventType.TEAM_ASSIGN, event.getPlayer(), null, event.getTeam(), meta);
    }

    @EventHandler(ignoreCancelled = true)
    public void onUpgradeBuy(UpgradeBuyEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        if (event.getTeamUpgrade() != null) {
            meta.put("upgrade", event.getTeamUpgrade().getName());
        }
        meta.put("team", event.getTeam().getName());
        logEvent(event.getArena(), MatchHistoryEventType.UPGRADE_BUY, event.getPlayer(), null, event.getTeam(), meta);
    }

    @EventHandler(ignoreCancelled = true)
    public void onShopBuy(ShopBuyEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        if (event.getCategoryContent() != null) {
            meta.put("item", event.getCategoryContent().getIdentifier());
            meta.put("category", event.getCategoryContent().getCategoryIdentifier());
        }
        meta.put("slot", String.valueOf(event.getSlot()));
        logEvent(event.getArena(), MatchHistoryEventType.SHOP_BUY, event.getBuyer(), null, event.getArena().getTeam(event.getBuyer()), meta);
    }

    @EventHandler
    public void onShopOpen(ShopOpenEvent event) {
        if (!isEnabled() || !allowShopOpen()) return;
        logEvent(event.getArena(), MatchHistoryEventType.SHOP_OPEN, event.getPlayer(), null, event.getArena().getTeam(event.getPlayer()), null);
    }

    @EventHandler
    public void onGeneratorUpgrade(GeneratorUpgradeEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("generator", event.getGenerator().getType().name());
        meta.put("amount", String.valueOf(event.getGenerator().getAmount()));
        meta.put("delay", String.valueOf(event.getGenerator().getDelay()));
        logEvent(event.getGenerator().getArena(), MatchHistoryEventType.GENERATOR_UPGRADE, null, null, null, meta);
    }

    @EventHandler
    public void onNextEvent(NextEventChangeEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        NextEvent next = event.getNewEvent();
        NextEvent prev = event.getOldEvent();
        if (next != null) meta.put("next_event", next.name());
        if (prev != null) meta.put("previous_event", prev.name());
        logEvent(event.getArena(), MatchHistoryEventType.NEXT_EVENT, null, null, null, meta);
    }

    @EventHandler
    public void onDreamDefender(PlayerDreamDefenderSpawnEvent event) {
        if (!isEnabled()) return;
        logEvent(event.getArena(), MatchHistoryEventType.DREAM_DEFENDER, event.getPlayer(), null, event.getPlayerTeam(), null);
    }

    @EventHandler
    public void onBedBug(PlayerBedBugSpawnEvent event) {
        if (!isEnabled()) return;
        logEvent(event.getArena(), MatchHistoryEventType.BED_BUG, event.getPlayer(), null, event.getPlayerTeam(), null);
    }

    @EventHandler
    public void onInvisibility(PlayerInvisibilityPotionEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("type", event.getType().name());
        if (event.getPlayer() != null) {
            logEvent(event.getArena(), MatchHistoryEventType.INVISIBILITY, event.getPlayer(), null, event.getTeam(), meta);
        }
    }

    @EventHandler
    public void onEggBridgeThrow(EggBridgeThrowEvent event) {
        if (!isEnabled() || !allowBuildEvents()) return;
        ITeam team = event.getArena() != null && event.getPlayer() != null ? event.getArena().getTeam(event.getPlayer()) : null;
        logEvent(event.getArena(), MatchHistoryEventType.EGG_BRIDGE_THROW, event.getPlayer(), null, team, null);
    }

    @EventHandler
    public void onEggBridgeBuild(EggBridgeBuildEvent event) {
        if (!isEnabled() || !allowBuildEvents()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("block", event.getBlock().getType().name());
        if (event.getTeamColor() != null) {
            meta.put("team_color", event.getTeamColor().chat().toString());
        }
        logEvent(event.getArena(), MatchHistoryEventType.EGG_BRIDGE_BUILD, null, null, null, meta);
    }

    @EventHandler
    public void onPopUpTowerPlace(PopUpTowerPlaceEvent event) {
        if (!isEnabled() || !allowBuildEvents()) return;
        ITeam team = event.getArena() != null && event.getPlayer() != null ? event.getArena().getTeam(event.getPlayer()) : null;
        logEvent(event.getArena(), MatchHistoryEventType.POPUP_TOWER_PLACE, event.getPlayer(), null, team, null);
    }

    @EventHandler
    public void onPopUpTowerBuild(PopUpTowerBuildEvent event) {
        if (!isEnabled() || !allowBuildEvents()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("block", event.getBlock().getType().name());
        if (event.getTeamColor() != null) {
            meta.put("team_color", event.getTeamColor().chat().toString());
        }
        logEvent(event.getArena(), MatchHistoryEventType.POPUP_TOWER_BUILD, null, null, null, meta);
    }

    @EventHandler
    public void onItemDeposit(PlayerItemDepositEvent event) {
        if (!isEnabled()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("container", event.getContainerType().name());
        meta.put("item", event.getItem().getType().name());
        meta.put("amount", String.valueOf(event.getItem().getAmount()));
        logEvent(event.getArena(), MatchHistoryEventType.ITEM_DEPOSIT, event.getPlayer(), null, event.getArena().getTeam(event.getPlayer()), meta);
    }

    @EventHandler
    public void onBaseEnter(PlayerBaseEnterEvent event) {
        if (!isEnabled()) return;
        logEvent(event.getTeam().getArena(), MatchHistoryEventType.BASE_ENTER, event.getPlayer(), null, event.getTeam(), null);
    }

    @EventHandler
    public void onBaseLeave(PlayerBaseLeaveEvent event) {
        if (!isEnabled()) return;
        logEvent(event.getTeam().getArena(), MatchHistoryEventType.BASE_LEAVE, event.getPlayer(), null, event.getTeam(), null);
    }

    @EventHandler
    public void onResourceCollect(PlayerGeneratorCollectEvent event) {
        if (!isEnabled() || !allowResourceCollect()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("amount", String.valueOf(event.getAmount()));
        meta.put("item", event.getItemStack().getType().name());
        logEvent(event.getArena(), MatchHistoryEventType.RESOURCE_COLLECT, event.getPlayer(), null, event.getArena().getTeam(event.getPlayer()), meta);
    }

    @EventHandler
    public void onResourceDrop(GeneratorDropEvent event) {
        if (!isEnabled() || !allowResourceDrop()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("generator", event.getGenerator().getType().name());
        meta.put("amount", String.valueOf(event.getGenerator().getAmount()));
        if (event.getGenerator().getOre() != null) {
            meta.put("item", event.getGenerator().getOre().getType().name());
        }
        logEvent(event.getGenerator().getArena(), MatchHistoryEventType.RESOURCE_DROP, null, null, null, meta);
    }

    @EventHandler
    public void onStatChange(PlayerStatChangeEvent event) {
        if (!isEnabled() || !allowStatEvents()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("stat", event.getStatType().name());
        IArena arena = event.getArena();
        if (arena != null) {
            logEvent(arena, MatchHistoryEventType.STAT_CHANGE, event.getPlayer(), null, arena.getTeam(event.getPlayer()), meta);
        }
    }

    @EventHandler
    public void onXpGain(PlayerXpGainEvent event) {
        if (!isEnabled() || !allowProgressionEvents()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("xp", String.valueOf(event.getAmount()));
        meta.put("source", event.getXpSource().name());
        IArena arena = BedWars.getAPI().getArenaUtil().getArenaByPlayer(event.getPlayer());
        if (arena != null) {
            logEvent(arena, MatchHistoryEventType.XP_GAIN, event.getPlayer(), null, arena.getTeam(event.getPlayer()), meta);
        }
    }

    @EventHandler
    public void onMoneyGain(PlayerMoneyGainEvent event) {
        if (!isEnabled() || !allowProgressionEvents()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("money", String.valueOf(event.getAmount()));
        meta.put("source", event.getMoneySource().name());
        IArena arena = BedWars.getAPI().getArenaUtil().getArenaByPlayer(event.getPlayer());
        if (arena != null) {
            logEvent(arena, MatchHistoryEventType.MONEY_GAIN, event.getPlayer(), null, arena.getTeam(event.getPlayer()), meta);
        }
    }

    @EventHandler
    public void onLevelUp(PlayerLevelUpEvent event) {
        if (!isEnabled() || !allowProgressionEvents()) return;
        Map<String, String> meta = new HashMap<>();
        meta.put("level", String.valueOf(event.getNewLevel()));
        meta.put("next_xp", String.valueOf(event.getNewXpTarget()));
        IArena arena = BedWars.getAPI().getArenaUtil().getArenaByPlayer(event.getPlayer());
        if (arena != null) {
            logEvent(arena, MatchHistoryEventType.LEVEL_UP, event.getPlayer(), null, arena.getTeam(event.getPlayer()), meta);
        }
    }

    private void logEvent(IArena arena,
                          MatchHistoryEventType type,
                          Player actor,
                          Player target,
                          ITeam team,
                          Map<String, String> meta) {
        if (arena == null || type == null) return;
        UUID matchId = MatchHistoryRegistry.getOrCreate(arena);
        if (matchId == null) return;

        String teamName = team != null ? team.getDisplayName(Language.getDefaultLanguage()) : "";
        String teamColor = team != null && team.getColor() != null ? team.getColor().chat().toString() : "";

        MatchHistoryEventRecord record = new MatchHistoryEventRecord(
                0L,
                matchId,
                System.currentTimeMillis(),
                type.getKey(),
                actor != null ? actor.getUniqueId() : null,
                actor != null ? actor.getName() : "",
                target != null ? target.getUniqueId() : null,
                target != null ? target.getName() : "",
                teamName,
                teamColor,
                MatchHistoryEventService.serializeMeta(meta)
        );
        MatchHistoryEventService.saveEventAsync(record);
    }
}
