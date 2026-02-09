package com.tomkeuper.bedwars.commands.history;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.configuration.Permissions;
import com.tomkeuper.bedwars.history.HistoryMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public class HistoryCommand extends BukkitCommand {

    private static final java.util.concurrent.ConcurrentHashMap<UUID, Long> historyCoolDown = new java.util.concurrent.ConcurrentHashMap<>();

    public HistoryCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender s, String st, String[] args) {
        if (s instanceof ConsoleCommandSender) {
            s.sendMessage("This command is for players!");
            return true;
        }

        Player p = (Player) s;
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ENABLED)) {
            p.sendMessage(getMsg(p, Messages.PLAYER_HISTORY_NO_DATA));
            return true;
        }

        if (!canOpenHistory(p)) {
            p.sendMessage(getMsg(p, Messages.COMMAND_NOT_ALLOWED_IN_GAME));
            return true;
        }

        if (onCooldown(p)) {
            return true;
        }

        UUID targetId = p.getUniqueId();
        if (args.length > 0) {
            if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ALLOW_OTHERS) || !p.hasPermission(Permissions.PERMISSION_HISTORY_VIEW_OTHERS)) {
                p.sendMessage(getMsg(p, Messages.PLAYER_HISTORY_VIEW_OTHER_DENIED));
                return true;
            }
            UUID resolved = resolveTarget(args[0]);
            if (resolved == null) {
                p.sendMessage(getMsg(p, Messages.PLAYER_HISTORY_PLAYER_NOT_FOUND));
                return true;
            }
            targetId = resolved;
        }

        HistoryMenu.open(p, targetId, 1);
        return true;
    }

    private boolean canOpenHistory(Player p) {
        IArena a = Arena.getArenaByPlayer(p);
        if (a == null) return true;
        if (a.isSpectator(p)) return true;
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ALLOW_IN_GAME)) return true;
        return a.getStatus() == GameState.starting || a.getStatus() == GameState.waiting;
    }

    private UUID resolveTarget(String name) {
        if (name == null || name.isBlank()) return null;
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online.getUniqueId();
        OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
        if (offline != null && (offline.isOnline() || offline.hasPlayedBefore())) {
            return offline.getUniqueId();
        }
        return null;
    }

    private boolean onCooldown(Player p) {
        long cooldownMs = BedWars.config.getYml().getLong(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_COOLDOWN_MS);
        if (cooldownMs <= 0) return false;

        Long last = historyCoolDown.get(p.getUniqueId());
        long now = System.currentTimeMillis();
        if (last == null || now - last >= cooldownMs) {
            historyCoolDown.put(p.getUniqueId(), now);
            return false;
        }
        return true;
    }
}
