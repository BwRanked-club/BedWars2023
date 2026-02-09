package com.tomkeuper.bedwars.commands.bedwars.subcmds.sensitive;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.commands.bedwars.MainCommand;
import com.tomkeuper.bedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DisableArena extends SubCommand {

    public DisableArena(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(6);
        showInList(true);
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " " + getSubCommandName() + " §6<worldName>", "§fDisable an arena.\nThis will remove the players \n§ffrom the arena before disabling.",
                "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
        setPermission(Permissions.PERMISSION_ARENA_DISABLE);
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!MainCommand.isLobbySet()) {
            s.sendMessage("§c▪ §7You have to set the lobby location first!");
            return true;
        }
        if (args.length != 1) {
            s.sendMessage("§c▪ §7Usage: §o/" + getParent().getName() + " " + getSubCommandName() + " <mapName>");
            return true;
        }

        String rawName = args[0];
        IArena byIdentifier = Arena.getArenaByIdentifier(rawName);
        String template = byIdentifier != null ? byIdentifier.getArenaName() : rawName;
        if (byIdentifier == null && rawName.startsWith("bw_temp_")) {
            String derived = rawName.replaceAll("bw_temp_\\w+_", "");
            if (!derived.equals(rawName)) {
                template = derived;
            }
        }

        boolean rawIsWorld = BedWars.getAPI().getRestoreAdapter().isWorld(rawName);
        boolean templateIsWorld = BedWars.getAPI().getRestoreAdapter().isWorld(template);

        List<IArena> toDisable = new ArrayList<>();
        for (IArena arena : Arena.getArenas()) {
            if (matchesTemplate(arena, template, rawName)) {
                toDisable.add(arena);
            }
        }

        List<IArena> toDequeue = new ArrayList<>();
        for (IArena arena : Arena.getEnableQueue()) {
            if (matchesTemplate(arena, template, rawName)) {
                toDequeue.add(arena);
            }
        }

        if (toDisable.isEmpty() && toDequeue.isEmpty()) {
            if (!rawIsWorld && !templateIsWorld) {
                s.sendMessage("§c▪ §7" + rawName + " is a world and not an arena!");
                return true;
            }
            s.sendMessage("§c▪ §7This has already been disabled or doesnt exist!");
            return true;
        }

        for (IArena arena : toDisable) {
            if (arena.getStatus() == GameState.playing) {
                s.sendMessage("§6 ▪ §7There is a game running on this Arena, please disable after the game!");
                return true;
            }
        }

        s.sendMessage("§6 ▪ §7Disabling arena...");
        for (IArena arena : toDisable) {
            arena.disable();
        }
        for (IArena arena : toDequeue) {
            Arena.removeFromEnableQueue(arena);
        }
        return true;
    }

    private boolean matchesTemplate(IArena arena, String template, String rawName) {
        if (arena == null) return false;
        String arenaName = arena.getArenaName();
        String worldName = arena.getWorldName();

        if (arenaName.equalsIgnoreCase(template) || arenaName.equalsIgnoreCase(rawName)) return true;
        if (worldName != null && worldName.equalsIgnoreCase(rawName)) return true;

        String templateLower = template.toLowerCase();
        String arenaLower = arenaName.toLowerCase();
        if (arenaLower.startsWith("bw_temp_") && arenaLower.endsWith(templateLower)) return true;
        if (worldName != null) {
            String worldLower = worldName.toLowerCase();
            return worldLower.startsWith("bw_temp_") && worldLower.endsWith(templateLower);
        }
        return false;
    }

    @Override
    public List<String> getTabComplete() {
        List<String> tab = new ArrayList<>();
        for (IArena a : Arena.getArenas()) {
            tab.add(a.getArenaName());
        }
        return tab;
    }

    @Override
    public boolean canSee(CommandSender s, com.tomkeuper.bedwars.api.BedWars api) {
        if (s instanceof Player p) {
            if (Arena.isInArena(p)) return false;
            if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        }
        return hasPermission(s);
    }
}
