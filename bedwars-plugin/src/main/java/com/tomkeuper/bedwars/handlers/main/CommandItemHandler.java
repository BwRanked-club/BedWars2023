package com.tomkeuper.bedwars.handlers.main;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.items.handlers.HandlerType;
import com.tomkeuper.bedwars.api.items.handlers.IPermanentItem;
import com.tomkeuper.bedwars.api.items.handlers.PermanentItemHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static com.tomkeuper.bedwars.BedWars.config;

public class CommandItemHandler extends PermanentItemHandler {
    public CommandItemHandler(String id, Plugin plugin, BedWars api) {
        super(id, plugin, api);
    }

    @Override
    public void handleUse(Player player, IArena arena, IPermanentItem lobbyItem) {
        String command;
        if (arena == null) {
            command = config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_COMMAND.replace("%path%", lobbyItem.getIdentifier()));
        } else {
            if (arena.isSpectator(player)) {
                command = config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_COMMAND.replace("%path%", lobbyItem.getIdentifier()));
            } else {
                command = config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_COMMAND.replace("%path%", lobbyItem.getIdentifier()));
            }
        }
        if (command == null) {
            Bukkit.getLogger().warning("Command for item `" + lobbyItem.getIdentifier() + "` is not set.");
            return;
        }
        player.performCommand(command);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.COMMAND;
    }
}