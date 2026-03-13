package com.tomkeuper.bedwars.commands.bedwars.subcmds.regular;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.commands.bedwars.MainCommand;
import com.tomkeuper.bedwars.commands.stats.StatsCommandSupport;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CmdStats extends SubCommand {

    public CmdStats(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(16);
        showInList(false);
        setDisplayInfo(MainCommand.createTC("&a- &f/stats", "/stats", "&fOpens the stats GUI."));
    }

    @Override
    public boolean execute(String[] args, CommandSender sender) {
        return StatsCommandSupport.execute(sender, args);
    }

    @Override
    public List<String> getTabComplete() {
        return new ArrayList<>();
    }

    @Override
    public boolean canSee(CommandSender sender, BedWars api) {
        if (sender instanceof ConsoleCommandSender) return false;

        Player player = (Player) sender;
        if (Arena.isInArena(player)) return false;

        if (SetupSession.isInSetupSession(player.getUniqueId())) return false;
        return hasPermission(sender);
    }
}
