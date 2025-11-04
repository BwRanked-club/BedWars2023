package com.tomkeuper.bedwars.commands.bedwars.subcmds.sensitive.setup;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.configuration.Permissions;
import com.tomkeuper.bedwars.support.paper.PaperSupport;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class Save extends SubCommand {

    public Save(ParentCommand parent, String name) {
        super(parent, name);
        setArenaSetupCommand(true);
        setPermission(Permissions.PERMISSION_SETUP_ARENA);
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        SetupSession ss = SetupSession.getSession(p.getUniqueId());
        if (ss == null) {
            //s.sendMessage("§c ▪ §7You're not in a setup session!");
            return false;
        }

        //Clear setup armor-stands
        for (Entity e : p.getWorld().getEntities()) {
            if (e.getType() == EntityType.ARMOR_STAND) {
                e.remove();
            }
        }

        if (Bukkit.getWorld(BedWars.getLobbyWorld()) != null) {
            PaperSupport.teleportC(p, Bukkit.getWorld(BedWars.getLobbyWorld()).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
        } else {
            PaperSupport.teleportC(p, Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
        }
        ss.done();
        p.sendMessage(ss.getPrefix() + "Arena changes saved!");
        p.sendMessage(ss.getPrefix() + "You can now enable it using:");
        p.spigot().sendMessage(Misc.msgHoverClick(ChatColor.GOLD + "/" + getParent().getName() + " enableArena " + ss.getWorldName() + ChatColor.GRAY +" (click to enable)", ChatColor.GREEN + "Enable this arena.", "/" + getParent().getName() + " enableArena " + ss.getWorldName(), ClickEvent.Action.RUN_COMMAND));
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, com.tomkeuper.bedwars.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (!SetupSession.isInSetupSession(p.getUniqueId())) return false;

        return hasPermission(s);
    }
}
