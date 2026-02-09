package com.tomkeuper.bedwars.shop.listeners;

import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.entity.Player;

public class ShopOpenListener implements Listener {

    @EventHandler
    public void onShopOpen(PlayerInteractEntityEvent e) {
        IArena a = Arena.getArenaByPlayer(e.getPlayer());
        if (a == null) return;
        if (!a.getStatus().equals(GameState.playing)) return;
        Location l = e.getRightClicked().getLocation();
        for (ITeam t : a.getTeams()) {
            Location l2 = t.getShop();
            if (l.getBlockX() == l2.getBlockX() && l.getBlockY() == l2.getBlockY() && l.getBlockZ() == l2.getBlockZ()) {
                e.setCancelled(true);
                if (a.isPlayer(e.getPlayer()) && a.getLinkedShop() != null) {
                    a.getLinkedShop().open(e.getPlayer(), PlayerQuickBuyCache.getInstance().getQuickBuyCache(e.getPlayer().getUniqueId()), true);
                }
            }
        }
    }

    @EventHandler
    public void onShopOpenLeftClick(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        IArena a = Arena.getArenaByPlayer(player);
        if (a == null) return;
        if (!a.getStatus().equals(GameState.playing)) return;
        Location l = e.getEntity().getLocation();
        for (ITeam t : a.getTeams()) {
            Location l2 = t.getShop();
            if (l.getBlockX() == l2.getBlockX() && l.getBlockY() == l2.getBlockY() && l.getBlockZ() == l2.getBlockZ()) {
                e.setCancelled(true);
                if (a.isPlayer(player) && a.getLinkedShop() != null) {
                    a.getLinkedShop().open(player, PlayerQuickBuyCache.getInstance().getQuickBuyCache(player.getUniqueId()), true);
                }
                return;
            }
        }
    }
}
