package com.tomkeuper.bedwars.commands.bedwars.subcmds.sensitive;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ApplyTabConfig extends SubCommand {

    private static final String RESOURCE_FILE = "tab_config.yml"; // File inside BedWars JAR
    private static final String TAB_CONFIG_FILENAME = "config.yml"; // Target name in TAB folder
    private static final String TAB_PLUGIN_NAME = "TAB"; // TAB plugin name

    public ApplyTabConfig(ParentCommand parent, String name) {
        super(parent, name);
        showInList(false);
    }

    @Override
    public boolean execute(String[] args, CommandSender sender) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("§cThis command can only be executed from the console.");
            return true;
        }

        // Check if TAB plugin is installed
        Plugin tabPlugin = Bukkit.getPluginManager().getPlugin(TAB_PLUGIN_NAME);
        if (tabPlugin == null) {
            sender.sendMessage("§cError: The TAB plugin is not installed or not enabled.");
            return true;
        }

        // Get TAB plugin's config folder
        File tabConfigFolder = tabPlugin.getDataFolder();
        if (!tabConfigFolder.exists() && !tabConfigFolder.mkdirs()) {
            sender.sendMessage("§cError: Could not create TAB plugin's config folder.");
            return true;
        }

        File destinationFile = new File(tabConfigFolder, TAB_CONFIG_FILENAME);

        // Copy file from BedWars JAR to TAB config folder
        try (InputStream input = BedWars.plugin.getResource(RESOURCE_FILE)) {
            if (input == null) {
                sender.sendMessage("§cError: Could not find " + RESOURCE_FILE + " inside the BedWars JAR.");
                return true;
            }

            Files.copy(input, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            sender.sendMessage("§aSuccessfully applied the TAB plugin configuration to " + destinationFile.getPath());
        } catch (IOException e) {
            sender.sendMessage("§cError while copying the TAB config: " + e.getMessage());
        }

        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }
}
