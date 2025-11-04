package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.LinkedList;

public class WorldLoadListener implements Listener {

    @EventHandler
    public void onLoad(WorldLoadEvent e) {
        for (IArena a : new LinkedList<>(Arena.getEnableQueue())) {
            if (a.getWorldName().equalsIgnoreCase(e.getWorld().getName())) {
                if (a.isInitialized()) {
                    BedWars.debug("Ignorando init duplicado via WorldLoadListener para " + a.getArenaName());
                    return;
                }
                a.init(e.getWorld());
                return;
            }
        }
    }
}
