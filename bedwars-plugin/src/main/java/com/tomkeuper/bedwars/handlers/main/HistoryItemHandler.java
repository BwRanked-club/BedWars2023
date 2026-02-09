package com.tomkeuper.bedwars.handlers.main;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.items.handlers.HandlerType;
import com.tomkeuper.bedwars.api.items.handlers.IPermanentItem;
import com.tomkeuper.bedwars.api.items.handlers.PermanentItemHandler;
import com.tomkeuper.bedwars.history.HistoryMenu;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HistoryItemHandler extends PermanentItemHandler {
    public HistoryItemHandler(String id, Plugin plugin, BedWars api) {
        super(id, plugin, api);
    }

    @Override
    public void handleUse(Player player, IArena arena, IPermanentItem item) {
        HistoryMenu.open(player, player.getUniqueId(), 1);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.VIEW_HISTORY;
    }
}

