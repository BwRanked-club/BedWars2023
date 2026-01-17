package com.tomkeuper.bedwars.arena.despawnables;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class TargetListener implements Listener {

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e) {
        if (!(e.getTarget() instanceof Player p)) return;
        IArena arena = Arena.getArenaByIdentifier(e.getEntity().getWorld().getName());
        if (arena == null) return;
        if (!arena.isPlayer(p)) {
            e.setCancelled(true);
            return;
        }
        if (arena.getStatus() != GameState.playing) {
            e.setCancelled(true);
            return;
        }
        if (BedWars.nms.isDespawnable(e.getEntity())) {
            if (arena.getTeam(p) == BedWars.nms.getDespawnablesList().get(e.getEntity().getUniqueId()).getTeam()) {
                e.setCancelled(true);
            }
        }
    }
}
