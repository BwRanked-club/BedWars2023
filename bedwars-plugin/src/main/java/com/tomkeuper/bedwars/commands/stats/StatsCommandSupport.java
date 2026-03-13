package com.tomkeuper.bedwars.commands.stats;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public final class StatsCommandSupport {

    private static final ConcurrentHashMap<UUID, Long> STATS_COOLDOWN = new ConcurrentHashMap<>();

    private StatsCommandSupport() {
    }

    public static boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("This command is for players!");
            return true;
        }

        Player viewer = (Player) sender;
        if (!canOpenStats(viewer)) {
            viewer.sendMessage(getMsg(viewer, Messages.COMMAND_NOT_ALLOWED_IN_GAME));
            return true;
        }

        if (onCooldown(viewer)) {
            return true;
        }

        ResolvedTarget target = resolveTarget(viewer, args);
        if (target == null) {
            viewer.sendMessage(getMsg(viewer, Messages.PLAYER_STATS_PLAYER_NOT_FOUND));
            return true;
        }

        openStats(viewer, target);
        return true;
    }

    public static void clearCooldown(UUID playerId) {
        STATS_COOLDOWN.remove(playerId);
    }

    private static boolean canOpenStats(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) {
            return true;
        }
        if (arena.isSpectator(player)) {
            return true;
        }
        return arena.getStatus() == GameState.starting || arena.getStatus() == GameState.waiting;
    }

    private static boolean onCooldown(Player player) {
        Long last = STATS_COOLDOWN.get(player.getUniqueId());
        long now = System.currentTimeMillis();
        if (last == null || now - last >= 3000L) {
            STATS_COOLDOWN.put(player.getUniqueId(), now);
            return false;
        }
        return true;
    }

    @Nullable
    private static ResolvedTarget resolveTarget(Player viewer, String[] args) {
        if (args.length == 0 || args[0] == null || args[0].isBlank()) {
            return new ResolvedTarget(viewer.getUniqueId(), viewer.getName());
        }

        String name = args[0];
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return new ResolvedTarget(online.getUniqueId(), online.getName());
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline != null && (offline.isOnline() || offline.hasPlayedBefore())) {
            String targetName = offline.getName() == null ? name : offline.getName();
            return new ResolvedTarget(offline.getUniqueId(), targetName);
        }
        return null;
    }

    private static void openStats(Player viewer, ResolvedTarget target) {
        PlayerStats cached = BedWars.getStatsManager().getMutable(target.uuid());
        if (cached != null) {
            if (cached.getName() == null || cached.getName().isBlank()) {
                cached.setName(target.name());
            }
            Misc.openStatsGUI(viewer, cached, target.uuid(), target.name());
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> {
            PlayerStats loaded = (PlayerStats) BedWars.getRemoteDatabase().fetchStats(target.uuid());
            if (loaded.getName() == null || loaded.getName().isBlank()) {
                loaded.setName(target.name());
            }
            Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
                if (!viewer.isOnline()) {
                    return;
                }
                Misc.openStatsGUI(viewer, loaded, target.uuid(), target.name());
            });
        });
    }

    private record ResolvedTarget(UUID uuid, String name) {
    }
}
