package com.tomkeuper.bedwars.commands.bedwars.subcmds.regular;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.commands.bedwars.MainCommand;
import com.tomkeuper.bedwars.configuration.Permissions;
import com.tomkeuper.bedwars.history.HistoryMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public class CmdHistory extends SubCommand {

    private static final ConcurrentHashMap<UUID, Long> historyCoolDown = new ConcurrentHashMap<>();

    public CmdHistory(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(17);
        showInList(false);
        setDisplayInfo(MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " " + getSubCommandName(), "/" + getParent().getName() + " " + getSubCommandName(), "§fOpens the match history GUI."));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
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

    @Override
    public List<String> getTabComplete() {
        return new ArrayList<>();
    }

    @Override
    public boolean canSee(CommandSender s, com.tomkeuper.bedwars.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (Arena.isInArena(p)) return false;

        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        return hasPermission(s);
    }

    private boolean canOpenHistory(Player p) {
        IArena a = Arena.getArenaByPlayer(p);
        if (a == null) return true;
        if (a.isSpectator(p)) return true;
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ALLOW_IN_GAME)) return true;
        return a.getStatus() == GameState.starting || a.getStatus() == GameState.waiting;
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
}
