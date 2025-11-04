package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerPingListener implements Listener {
    @EventHandler
    public void onPing(ServerListPingEvent e){
        if (!Arena.getArenas().isEmpty()){
            IArena a = Arena.getArenas().get(0);
            if (a != null){
                e.setMaxPlayers(a.getMaxPlayers());
                e.setMotd(a.getDisplayStatus(Language.getDefaultLanguage()));
            }
        }
    }
}
