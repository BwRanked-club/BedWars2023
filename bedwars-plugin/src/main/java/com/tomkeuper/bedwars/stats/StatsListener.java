package com.tomkeuper.bedwars.stats;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerStatChangeEvent;
import com.tomkeuper.bedwars.api.party.Party;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class StatsListener implements Listener {

    private PlayerStats getCachedStats(UUID uuid, String context) {
        PlayerStats stats = BedWars.getStatsManager().getMutable(uuid);
        if (stats == null) {
            BedWars.debug("Skipping stats update (" + context + ") for unloaded player: " + uuid);
        }
        return stats;
    }

    private void apply(PlayerStats stats, IArena arena, Consumer<ModeStats> updater) {
        stats.applyToOverallAndMode(StatsModeResolver.resolve(arena), updater);
    }

    private boolean sameParty(Player a, Player b) {
        if (a == null || b == null) return false;
        Party util = BedWars.getAPI().getPartyUtil();
        if (util == null) return false;
        if (!util.hasParty(a) || !util.hasParty(b)) return false;
        Player ownerA = util.getOwner(a);
        Player ownerB = util.getOwner(b);
        return ownerA != null && ownerA.equals(ownerB);
    }

    private boolean sharesPartyWithVictimTeam(Player attacker, IArena arena, ITeam victimTeam) {
        if (attacker == null || arena == null || victimTeam == null) return false;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (arena.isPlayer(p)) {
                ITeam t = arena.getTeam(p);
                if (t != null && t.equals(victimTeam) && sameParty(attacker, p)) return true;
            }
        }
        return false;
    }

    private void awardAssists(Player victim, IArena arena, Player killer, boolean finalKill) {
        List<Player> assistants = AssistTracker.consumeAssistants(victim, arena, killer);
        for (Player assistant : assistants) {
            if (sameParty(assistant, victim)) continue;

            PlayerStats assistantStats = getCachedStats(assistant.getUniqueId(), finalKill ? "final-assist" : "assist");
            if (assistantStats == null) continue;

            PlayerStatChangeEvent evAssist = new PlayerStatChangeEvent(
                    assistant,
                    arena,
                    finalKill ? PlayerStatChangeEvent.StatType.FINAL_ASSISTS : PlayerStatChangeEvent.StatType.ASSISTS
            );
            Bukkit.getPluginManager().callEvent(evAssist);
            if (evAssist.isCancelled()) continue;

            apply(assistantStats, arena, bucket -> {
                if (finalKill) {
                    bucket.setFinalAssists(bucket.getFinalAssists() + 1);
                } else {
                    bucket.setAssists(bucket.getAssists() + 1);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        PlayerStats stats = (PlayerStats) BedWars.getRemoteDatabase().fetchStats(event.getUniqueId());
        stats.setName(event.getName());
        BedWars.getStatsManager().put(event.getUniqueId(), stats);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            BedWars.getStatsManager().remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent event) {
        Player breaker = event.getPlayer();
        if (sharesPartyWithVictimTeam(breaker, event.getArena(), event.getVictimTeam())) return;

        PlayerStats stats = getCachedStats(breaker.getUniqueId(), "bed-break");
        if (stats == null) return;
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(breaker, event.getArena(), PlayerStatChangeEvent.StatType.BEDS_DESTROYED);
        Bukkit.getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            apply(stats, event.getArena(), bucket -> bucket.setBedsDestroyed(bucket.getBedsDestroyed() + 1));
        }

        for (Player victim : event.getVictimTeam().getMembers()) {
            PlayerStats victimStats = getCachedStats(victim.getUniqueId(), "bed-loss");
            if (victimStats == null) continue;

            PlayerStatChangeEvent evBedLost = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.BEDS_LOST);
            Bukkit.getPluginManager().callEvent(evBedLost);
            if (evBedLost.isCancelled()) continue;

            apply(victimStats, event.getArena(), bucket -> bucket.setBedsLost(bucket.getBedsLost() + 1));
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerKillEvent event) {
        Player victim = event.getVictim();
        Player killer = event.getKiller();

        if (sameParty(killer, victim)) {
            AssistTracker.clearVictim(victim.getUniqueId());
            return;
        }

        PlayerStats victimStats = getCachedStats(victim.getUniqueId(), "kill-victim");
        if (victimStats == null) {
            AssistTracker.clearVictim(victim.getUniqueId());
            return;
        }
        PlayerStats killerStats = (killer != null && !victim.equals(killer))
                ? getCachedStats(killer.getUniqueId(), "kill-killer")
                : null;

        PlayerStatChangeEvent evKill = new PlayerStatChangeEvent(killer, event.getArena(), PlayerStatChangeEvent.StatType.KILLS);
        PlayerStatChangeEvent evFinalKill = new PlayerStatChangeEvent(killer, event.getArena(), PlayerStatChangeEvent.StatType.FINAL_KILLS);
        PlayerStatChangeEvent evFinalDeaths = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.FINAL_DEATHS);
        PlayerStatChangeEvent evLosses = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.LOSSES);
        PlayerStatChangeEvent evGamesPlayed = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.GAMES_PLAYED);
        PlayerStatChangeEvent evDeaths = new PlayerStatChangeEvent(victim, event.getArena(), PlayerStatChangeEvent.StatType.DEATHS);

        if (event.getCause().isFinalKill()) {
            Bukkit.getPluginManager().callEvent(evFinalDeaths);
            if (!evFinalDeaths.isCancelled()) {
                apply(victimStats, event.getArena(), bucket -> bucket.setFinalDeaths(bucket.getFinalDeaths() + 1));
            }

            Bukkit.getPluginManager().callEvent(evLosses);
            if (!evLosses.isCancelled()) {
                apply(victimStats, event.getArena(), bucket -> bucket.setLosses(bucket.getLosses() + 1));
            }

            Bukkit.getPluginManager().callEvent(evGamesPlayed);
            if (!evGamesPlayed.isCancelled()) {
                apply(victimStats, event.getArena(), bucket -> bucket.setGamesPlayed(bucket.getGamesPlayed() + 1));
            }

            Bukkit.getPluginManager().callEvent(evFinalKill);
            if (!evFinalKill.isCancelled() && killerStats != null) {
                apply(killerStats, event.getArena(), bucket -> bucket.setFinalKills(bucket.getFinalKills() + 1));
            }

            awardAssists(victim, event.getArena(), killer, true);
        } else {
            Bukkit.getPluginManager().callEvent(evDeaths);
            if (!evDeaths.isCancelled()) {
                apply(victimStats, event.getArena(), bucket -> bucket.setDeaths(bucket.getDeaths() + 1));
            }

            Bukkit.getPluginManager().callEvent(evKill);
            if (!evKill.isCancelled() && killerStats != null) {
                apply(killerStats, event.getArena(), bucket -> bucket.setKills(bucket.getKills() + 1));
            }

            awardAssists(victim, event.getArena(), killer, false);
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        for (UUID uuid : event.getWinners()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            PlayerStats stats = getCachedStats(uuid, "game-end-winner");
            if (stats == null) continue;

            PlayerStatChangeEvent evWins = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.WINS);
            PlayerStatChangeEvent evGamesPlayed = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.GAMES_PLAYED);

            Bukkit.getPluginManager().callEvent(evWins);
            if (!evWins.isCancelled()) {
                apply(stats, event.getArena(), bucket -> bucket.setWins(bucket.getWins() + 1));
            }

            IArena playerArena = Arena.getArenaByPlayer(player);
            if (playerArena != null && playerArena.equals(event.getArena())) {
                Bukkit.getPluginManager().callEvent(evGamesPlayed);
                if (!evGamesPlayed.isCancelled()) {
                    apply(stats, event.getArena(), bucket -> bucket.setGamesPlayed(bucket.getGamesPlayed() + 1));
                }
            }
        }
    }

    @EventHandler
    public void onArenaLeave(PlayerLeaveArenaEvent event) {
        Player player = event.getPlayer();
        ITeam team = event.getArena().getExTeam(player.getUniqueId());
        if (team == null) {
            AssistTracker.clearVictim(player.getUniqueId());
            return;
        }
        if (event.getArena().getStatus() == GameState.starting || event.getArena().getStatus() == GameState.waiting) {
            AssistTracker.clearVictim(player.getUniqueId());
            return;
        }

        PlayerStats playerStats = getCachedStats(player.getUniqueId(), "arena-leave-player");
        if (playerStats == null) return;

        PlayerStatChangeEvent evFirst = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.FIRST_PLAY);
        PlayerStatChangeEvent evLast = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.LAST_PLAY);
        PlayerStatChangeEvent evFinalDeaths = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.FINAL_DEATHS);
        PlayerStatChangeEvent evLosses = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.LOSSES);

        Instant now = Instant.now();
        Bukkit.getPluginManager().callEvent(evLast);
        if (!evLast.isCancelled()) {
            apply(playerStats, event.getArena(), bucket -> bucket.setLastPlay(now));
        }
        Bukkit.getPluginManager().callEvent(evFirst);
        if (!evFirst.isCancelled()) {
            apply(playerStats, event.getArena(), bucket -> {
                if (bucket.getFirstPlay() == null) {
                    bucket.setFirstPlay(now);
                }
            });
        }

        if (event.getArena().getStatus() == GameState.playing) {
            if (team.isBedDestroyed()) {
                if (event.getArena().isPlayer(player)) {
                    Bukkit.getPluginManager().callEvent(evFinalDeaths);
                    if (!evFinalDeaths.isCancelled()) {
                        apply(playerStats, event.getArena(), bucket -> bucket.setFinalDeaths(bucket.getFinalDeaths() + 1));
                    }
                    Bukkit.getPluginManager().callEvent(evLosses);
                    if (!evLosses.isCancelled()) {
                        apply(playerStats, event.getArena(), bucket -> bucket.setLosses(bucket.getLosses() + 1));
                    }
                }

                Player damager = event.getLastDamager();
                if (damager != null && event.getArena().isPlayer(damager) && event.getArena().getTeam(damager) != null) {
                    if (!sameParty(damager, player)) {
                        PlayerStatChangeEvent evFinalKill = new PlayerStatChangeEvent(damager, event.getArena(), PlayerStatChangeEvent.StatType.FINAL_KILLS);
                        Bukkit.getPluginManager().callEvent(evFinalKill);
                        if (!evFinalKill.isCancelled()) {
                            PlayerStats damagerStats = getCachedStats(damager.getUniqueId(), "arena-leave-final-kill");
                            if (damagerStats != null) {
                                apply(damagerStats, event.getArena(), bucket -> bucket.setFinalKills(bucket.getFinalKills() + 1));
                                event.getArena().addPlayerKill(damager, true, player);
                            }
                        }
                        awardAssists(player, event.getArena(), damager, true);
                    }
                }
            } else {
                Player damager = event.getLastDamager();
                if (damager != null && event.getArena().isPlayer(damager) && event.getArena().getTeam(damager) != null) {
                    if (!sameParty(damager, player)) {
                        PlayerStatChangeEvent evDeaths = new PlayerStatChangeEvent(player, event.getArena(), PlayerStatChangeEvent.StatType.DEATHS);
                        PlayerStatChangeEvent evKills = new PlayerStatChangeEvent(damager, event.getArena(), PlayerStatChangeEvent.StatType.KILLS);

                        Bukkit.getPluginManager().callEvent(evDeaths);
                        if (!evDeaths.isCancelled()) {
                            apply(playerStats, event.getArena(), bucket -> bucket.setDeaths(bucket.getDeaths() + 1));
                        }
                        event.getArena().addPlayerDeath(player);

                        event.getArena().addPlayerKill(damager, false, player);
                        Bukkit.getPluginManager().callEvent(evKills);
                        if (!evKills.isCancelled()) {
                            PlayerStats damagerStats = getCachedStats(damager.getUniqueId(), "arena-leave-kill");
                            if (damagerStats != null) {
                                apply(damagerStats, event.getArena(), bucket -> bucket.setKills(bucket.getKills() + 1));
                            }
                        }
                        awardAssists(player, event.getArena(), damager, false);
                    }
                }
            }
        }

        AssistTracker.clearVictim(player.getUniqueId());
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> BedWars.getRemoteDatabase().saveStats(playerStats));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        AssistTracker.clearPlayer(event.getPlayer().getUniqueId());
        BedWars.getStatsManager().remove(event.getPlayer().getUniqueId());
    }
}
