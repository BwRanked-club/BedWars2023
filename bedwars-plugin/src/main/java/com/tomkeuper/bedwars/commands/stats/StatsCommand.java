package com.tomkeuper.bedwars.commands.stats;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

public class StatsCommand extends BukkitCommand {

    public StatsCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return StatsCommandSupport.execute(sender, args);
    }
}
