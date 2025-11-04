package com.tomkeuper.bedwars.connectionmanager.redis;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaEnableEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RedisArenaListeners implements Listener {

    RedisConnection redisConnection;

    public RedisArenaListeners(RedisConnection redisConnection) {
        this.redisConnection = redisConnection;
    }

    @EventHandler
    public void onPlayerJoinArena(PlayerJoinArenaEvent e) {
        if (e == null) return;
        final IArena a = e.getArena();
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, ()-> {
            if (!redisConnection.storeArenaInformation(a)) Bukkit.getLogger().severe("An error occurred while trying to store arena information!");
        });
    }

    @EventHandler
    public void onPlayerLeaveArena(PlayerLeaveArenaEvent e){
        if (e == null) return;
        final IArena a = e.getArena();
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, ()-> {
            if (!redisConnection.storeArenaInformation(a)) Bukkit.getLogger().severe("An error occurred while trying to store arena information!");
        });
    }

    @EventHandler
    public void onArenaStatusChange(GameStateChangeEvent e){
        if (e == null) return;
        final IArena a = e.getArena();
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, ()-> {
            if (!redisConnection.storeArenaInformation(a)) Bukkit.getLogger().severe("An error occurred while trying to store arena information!");
        });
    }

    @EventHandler
    public void onArenaLoad(ArenaEnableEvent e){
        if (e == null) return;
        final IArena a = e.getArena();
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, ()-> {
            if (!redisConnection.storeArenaInformation(a)) Bukkit.getLogger().severe("An error occurred while trying to store arena information!");
        });
    }
}
