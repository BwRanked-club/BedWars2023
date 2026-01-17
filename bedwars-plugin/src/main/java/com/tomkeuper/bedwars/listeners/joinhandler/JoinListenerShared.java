package com.tomkeuper.bedwars.listeners.joinhandler;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListenerShared implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        JoinHandlerCommon.displayCustomerDetails(p);

        // Show commands if player is op and there is no set arenas
        if (p.isOp()) {
            if (Arena.getArenas().isEmpty()) {
                p.performCommand(BedWars.mainCmd);
            }
        }

        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
            // Hide new player to players and spectators, and vice versa
            for (Player inArena : Arena.getArenaByPlayer().keySet()) {
                if (inArena.equals(p)) continue;
                BedWars.nms.spigotHidePlayer(p, inArena);
                BedWars.nms.spigotHidePlayer(inArena, p);
            }
        }, 14L);
    }
}

