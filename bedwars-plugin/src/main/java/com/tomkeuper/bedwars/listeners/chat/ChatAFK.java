package com.tomkeuper.bedwars.listeners.chat;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatAFK implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Arena.afkCheck.remove(event.getPlayer().getUniqueId());
        if (BedWars.getAPI().getAFKUtil().isPlayerAFK(event.getPlayer())) {
            // go sync
            Bukkit.getScheduler().runTask(BedWars.plugin, () ->
                    BedWars.getAPI().getAFKUtil().setPlayerAFK(event.getPlayer(), false)
            );
        }
    }
}
