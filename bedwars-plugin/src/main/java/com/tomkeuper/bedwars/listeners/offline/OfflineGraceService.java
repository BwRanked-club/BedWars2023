package com.tomkeuper.bedwars.listeners.offline;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.listeners.dropshandler.PlayerDrops;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OfflineGraceService {

    private OfflineGraceService() {
    }

    private static final int GRACE_SECONDS = 60;

    private static final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    private static final class Entry {
        final IArena arena;
        final ITeam team;
        final Player snapshot;
        final long expiresAt;
        BukkitTask task;

        Entry(IArena arena, ITeam team, Player snapshot, long expiresAt) {
            this.arena = arena;
            this.team = team;
            this.snapshot = snapshot;
            this.expiresAt = expiresAt;
        }
    }

    public static void startGrace(IArena arena, ITeam team, Player player) {
        if (arena == null || team == null || player == null) return;
        cancelGrace(player.getUniqueId());

        long expires = System.currentTimeMillis() + GRACE_SECONDS * 1000L;
        Entry e = new Entry(arena, team, player, expires);
        e.task = Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> expireNow(player.getUniqueId()), GRACE_SECONDS * 20L);
        entries.put(player.getUniqueId(), e);
    }

    public static void cancelGrace(UUID playerId) {
        Entry e = entries.remove(playerId);
        if (e != null && e.task != null) e.task.cancel();
    }

    public static boolean hasActiveForTeam(IArena arena, ITeam team) {
        if (arena == null || team == null) return false;
        long now = System.currentTimeMillis();
        for (Entry e : entries.values()) {
            if (e.arena == arena && e.team == team && e.expiresAt > now) return true;
        }
        return false;
    }

    public static void onBedBroken(IArena arena, ITeam victimTeam, Player breaker) {
        if (arena == null || victimTeam == null) return;

        long now = System.currentTimeMillis();
        List<Map.Entry<UUID, Entry>> list = entries.entrySet().stream()
                .filter(en -> en.getValue().arena == arena
                        && en.getValue().team == victimTeam
                        && en.getValue().expiresAt > now)
                .toList();

        if (list.isEmpty()) return;

        for (Map.Entry<UUID, Entry> kv : list) {
            Entry e = kv.getValue();
            Player victim = e.snapshot;

            PlayerKillEvent.PlayerKillCause cause = PlayerKillEvent.PlayerKillCause.PLAYER_DISCONNECT_FINAL;
            PlayerKillEvent event = new PlayerKillEvent(
                    e.arena, victim, breaker,
                    player -> Language.getMsg(player, Messages.PLAYER_DIE_PVP_LOG_OUT_FINAL),
                    cause
            );
            Bukkit.getPluginManager().callEvent(event);

            Player realKiller = event.getKiller();
            ITeam killerTeam = realKiller == null ? null : e.arena.getTeam(realKiller);

            if (realKiller != null) {
                e.arena.addPlayerKill(realKiller, true, victim);
                if (event.playSound()) {
                    com.tomkeuper.bedwars.configuration.Sounds.playSound(ConfigPath.SOUNDS_KILL, realKiller);
                }
            }

            for (Player on : e.arena.getPlayers()) {
                Language lang = Language.getPlayerLanguage(on);
                on.sendMessage(event.getMessage().apply(on)
                        .replace("%bw_team_name%", e.team.getDisplayName(lang))
                        .replace("%bw_player_color%", e.team.getColor().chat().toString())
                        .replace("%bw_player%", victim.getDisplayName())
                        .replace("%bw_playername%", victim.getName())
                        .replace("%bw_killer_color%", killerTeam == null ? "" : killerTeam.getColor().chat().toString())
                        .replace("%bw_killer_name%", realKiller == null ? "" : realKiller.getDisplayName())
                        .replace("%bw_killer_team_name%", killerTeam == null ? "" : killerTeam.getDisplayName(lang)));
            }
            for (Player on : e.arena.getSpectators()) {
                Language lang = Language.getPlayerLanguage(on);
                on.sendMessage(event.getMessage().apply(on)
                        .replace("%bw_team_name%", e.team.getDisplayName(lang))
                        .replace("%bw_player_color%", e.team.getColor().chat().toString())
                        .replace("%bw_player%", victim.getDisplayName())
                        .replace("%bw_playername%", victim.getName())
                        .replace("%bw_killer_color%", killerTeam == null ? "" : killerTeam.getColor().chat().toString())
                        .replace("%bw_killer_name%", realKiller == null ? "" : realKiller.getDisplayName())
                        .replace("%bw_killer_team_name%", killerTeam == null ? "" : killerTeam.getDisplayName(lang)));
            }

            PlayerDrops.handlePlayerDrops(e.arena, victim, realKiller, e.team, killerTeam, cause, new ArrayList<ItemStack>());
            e.arena.addPlayerDeath(victim);

            e.arena.checkWinner();
            cancelGrace(kv.getKey());
        }
    }

    /**
     * Expire grace:
     * - Se a cama do time já foi quebrada, credita FINAL KILL ao bedDestroyer.
     * - Em todos os casos, registra a morte do jogador que kicou.
     * - Sem broadcast; apenas estatísticas e verificação de vitória.
     */
    private static void expireNow(UUID playerId) {
        Entry e = entries.remove(playerId);
        if (e == null) return;

        IArena a = e.arena;
        if (a == null || a.getStatus() != GameState.playing) return;

        if (e.task != null) {
            try {
                e.task.cancel();
            } catch (Throwable ignored) {
            }
        }

        boolean bedDestroyed = e.team.isBedDestroyed();
        if (bedDestroyed) {
            Player bedDestroyer = e.team.getBedDestroyer();
            if (bedDestroyer != null
                    && e.arena.isPlayer(bedDestroyer)
                    && !bedDestroyer.getUniqueId().equals(e.snapshot.getUniqueId())) {
                e.arena.addPlayerKill(bedDestroyer, true, e.snapshot);
            }
        }

        e.arena.addPlayerDeath(e.snapshot);

        e.arena.checkWinner();
    }

}
