package com.tomkeuper.bedwars.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Locale;

public class ChunkListener implements Listener {

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();
        final Entity[] entities = chunk.getEntities();

        for (Entity entity : entities) {
            if (!(entity instanceof ArmorStand as)) continue;

            if (entity.hasMetadata("bw2023-setup")) {
                entity.remove();
                continue;
            }

            if (as.isVisible()) continue;
            if (!as.isMarker()) continue;
            if (!entity.isCustomNameVisible()) continue;

            final String customName = entity.getCustomName();
            if (customName == null || customName.isEmpty()) continue;

            final String plain = ChatColor.stripColor(customName).toLowerCase(Locale.ROOT);

            if (plain.contains(" set")) {
                entity.remove();
            }
        }
    }
}
