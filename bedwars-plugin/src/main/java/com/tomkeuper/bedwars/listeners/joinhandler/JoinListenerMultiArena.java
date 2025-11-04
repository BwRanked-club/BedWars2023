package com.tomkeuper.bedwars.listeners.joinhandler;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.ReJoin;
import com.tomkeuper.bedwars.support.paper.PaperSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class JoinListenerMultiArena implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        final Player p = e.getPlayer();
        p.getInventory().setArmorContents(null);

        JoinHandlerCommon.displayCustomerDetails(p);

        // Show commands if player is op and there is no set arenas
        if (p.isOp()) {
            if (Arena.getArenas().isEmpty()) {
                p.performCommand(BedWars.mainCmd);
            }
        }

        ReJoin reJoin = ReJoin.getPlayer(p);

        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
            // Hide new player to players and spectators, and vice versa
            // Players from lobby will remain visible
            for (Player online : Bukkit.getOnlinePlayers()){
                if (Arena.isInArena(online)) {
                    BedWars.nms.spigotHidePlayer(online, p);
                    BedWars.nms.spigotHidePlayer(p, online);
                } else {
                    BedWars.nms.spigotShowPlayer(online, p);
                    BedWars.nms.spigotShowPlayer(p, online);
                }
            }

            // To prevent invisibility issues handle ReJoin after sending invisibility packets
            if (reJoin != null) {
                if (reJoin.canReJoin()) {
                    reJoin.reJoin(p);
                    return;
                }
                reJoin.destroy(false);
            }
        }, 14L);

        if (reJoin != null && reJoin.canReJoin()) return;

        // Teleport to lobby location
        Location lobbyLocation = BedWars.config.getConfigLoc("lobbyLoc");
        if (lobbyLocation != null && lobbyLocation.getWorld() != null) {
            PaperSupport.teleportC(p, lobbyLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        // Send items
        Arena.sendLobbyCommandItems(p);

        p.setHealthScale(p.getMaxHealth());
        p.setExp(0);
        p.setHealthScale(20);
        p.setFoodLevel(20);
    }
}

