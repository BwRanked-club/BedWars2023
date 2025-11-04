package com.tomkeuper.bedwars.api.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ParentCommand {

    /**
     * Check if a parent command has the target sub-command
     */
    boolean hasSubCommand(String name);

    /**
     * Add a subCommand
     */
    void addSubCommand(SubCommand subCommand);

    /**
     * Send sub-commands list to a player
     * This includes subCommands with showInList true only
     * Can only see commands which they have permission for
     */
    void sendSubCommands(CommandSender p);

    /**
     * Get available subCommands
     */
    List<SubCommand> getSubCommands();

    /**
     * Get parent name
     */
    String getName();
}
