package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaDisableEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaEnableEvent;
import com.tomkeuper.bedwars.arena.ArenaGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RefreshGUI implements Listener {

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e == null) return;
        int size = e.getArena().getPlayers().size();
        for (Player p : Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, e.getArena(), size);
        }
    }

    @EventHandler
    public void onPlayerJoinArena(PlayerJoinArenaEvent e) {
        if (e == null) return;
        int size = e.getArena().getPlayers().size();
        if (!e.isSpectator()) {
            size++;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, e.getArena(), size);
        }
    }

    @EventHandler
    public void onPlayerLeaveArena(PlayerLeaveArenaEvent e) {
        if (e == null) return;
        int size = e.getArena().getPlayers().size();
        if (!e.isSpectator()) {
            size--;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, e.getArena(), size);
        }
    }

    @EventHandler
    public void onArenaEnable(ArenaEnableEvent e) {
        if (e == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, e.getArena(), 0);
        }
    }

    @EventHandler
    public void onArenaDisable(ArenaDisableEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            ArenaGUI.refreshInv(p, null, 0);
        }
    }
}
