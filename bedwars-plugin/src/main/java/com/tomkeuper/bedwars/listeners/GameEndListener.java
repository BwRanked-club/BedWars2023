package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.arena.team.BedWarsTeam;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GameEndListener implements Listener {

    @EventHandler
    public void cleanInventoriesAndDroppedItems(@NotNull GameEndEvent event) {
        if (event.getArena().getPlayers().isEmpty()) {
            return;
        }

        // clear inventories
        for (UUID p : event.getAliveWinners()) {
            Bukkit.getPlayer(p).getInventory().clear();
        }

        // clear Ender Chests for everyone currently in the arena (players and spectators)
        for (Player pl : event.getArena().getPlayers()) {
            try {
                pl.getEnderChest().clear();
            } catch (Throwable ignored) {
            }
        }
        for (Player pl : event.getArena().getSpectators()) {
            try {
                pl.getEnderChest().clear();
            } catch (Throwable ignored) {
            }
        }
        for (ITeam team : event.getArena().getTeams()) {
            if (team instanceof BedWarsTeam) {
                ((BedWarsTeam) team).clearSharedEnderChest();
            }
        }

        // clear dropped items
        World game = event.getArena().getWorld();
        for (Entity item : game.getEntities()) {
            if (item instanceof Item || item instanceof ItemStack) {
                item.remove();
            }
        }
    }
}
