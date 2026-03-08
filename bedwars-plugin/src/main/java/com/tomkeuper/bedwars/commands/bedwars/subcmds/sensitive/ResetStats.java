package com.tomkeuper.bedwars.commands.bedwars.subcmds.sensitive;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.arena.SetupSession;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ResetStats extends SubCommand {

    public ResetStats(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(12);
        showInList(true);
        setDisplayInfo(Misc.msgHoverClick(
                ChatColor.GOLD + " - " + ChatColor.GRAY + "/" + getParent().getName() + " " + getSubCommandName() + "    " + ChatColor.DARK_GRAY + " - &breset stats + restart",
                ChatColor.WHITE + "Reset all player stats and restart the server.",
                "/" + getParent().getName() + " " + getSubCommandName(),
                ClickEvent.Action.RUN_COMMAND
        ));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (args.length != 0) {
            s.sendMessage(ChatColor.RED + "Usage: /" + getParent().getName() + " " + getSubCommandName());
            return true;
        }

        s.sendMessage(ChatColor.YELLOW + "Resetting player stats table...");
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> {
            boolean success = BedWars.getRemoteDatabase().resetStatsTable();
            Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
                if (!success) {
                    s.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "ERRO! " + ChatColor.RED + "Could not reset the stats table.");
                    return;
                }

                s.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "SUCESSO! " + ChatColor.GREEN + "Stats table reset. Restarting server...");

                String restartCommand = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_RESTART_CMD);
                if (restartCommand == null || restartCommand.trim().isEmpty()) {
                    restartCommand = "restart";
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), restartCommand.trim());
            });
        });
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, com.tomkeuper.bedwars.api.BedWars api) {
        if (!(s instanceof Player p)) return false;
        if (Arena.isInArena(p)) return false;
        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        return p.isOp();
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || (sender instanceof Player && ((Player) sender).isOp());
    }
}
