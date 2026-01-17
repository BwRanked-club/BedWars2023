package com.tomkeuper.bedwars.listeners.blockstatus;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaEnableEvent;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BlockStatusListener implements Listener {

    /**
     * Update sign block
     */
    public static void updateBlock(Arena a) {
        if (a == null) return;
        for (Block s : a.getSigns()) {
            if (!(s.getState() instanceof Sign)) continue;
            String path = "", data = "";
            data = switch (a.getStatus()) {
                case waiting -> {
                    path = ConfigPath.SIGNS_STATUS_BLOCK_WAITING_MATERIAL;
                    yield ConfigPath.SIGNS_STATUS_BLOCK_WAITING_DATA;
                }
                case playing -> {
                    path = ConfigPath.SIGNS_STATUS_BLOCK_PLAYING_MATERIAL;
                    yield ConfigPath.SIGNS_STATUS_BLOCK_PLAYING_DATA;
                }
                case starting -> {
                    path = ConfigPath.SIGNS_STATUS_BLOCK_STARTING_MATERIAL;
                    yield ConfigPath.SIGNS_STATUS_BLOCK_STARTING_DATA;
                }
                case restarting -> {
                    path = ConfigPath.SIGNS_STATUS_BLOCK_RESTARTING_MATERIAL;
                    yield ConfigPath.SIGNS_STATUS_BLOCK_RESTARTING_DATA;
                }
                default -> throw new IllegalStateException("Unhandled game status!");
            };
            BedWars.nms.setJoinSignBackground(s.getState(), Material.valueOf(BedWars.signs.getString(path)));
            BedWars.nms.setJoinSignBackgroundBlockData(s.getState(), (byte) BedWars.signs.getInt(data));
        }
    }

    @EventHandler
    public void onArenaEnable(ArenaEnableEvent e) {
        if (e == null) return;
        updateBlock((Arena) e.getArena());
    }

    @EventHandler
    public void onStatusChange(GameStateChangeEvent e) {
        if (e == null) return;
        updateBlock((Arena) e.getArena());
    }
}
