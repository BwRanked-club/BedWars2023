package com.tomkeuper.bedwars.popuptower;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.gameplay.PopUpTowerBuildEvent;
import com.tomkeuper.bedwars.api.region.Region;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class NewPlaceBlock {

    public NewPlaceBlock(Block baseBlock, String xyz, TeamColor color, Player player, boolean ladder, int ladderData) {
        final IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) return;

        final String[] parts = xyz.split(",");
        if (parts.length < 3) return;

        final int dx = parseInt(parts[0]);
        final int dy = parseInt(parts[1]);
        final int dz = parseInt(parts[2]);

        final Block target = baseBlock.getRelative(dx, dy, dz);
        final int by = target.getY();

        final int maxBuildY = arena.getConfig().getInt(ConfigPath.ARENA_CONFIGURATION_MAX_BUILD_Y);
        if (by >= maxBuildY) return;

        if (target.getType() != Material.AIR) return;

        for (Region r : arena.getRegionsList()) {
            if (r.isInRegion(target.getLocation())) return;
        }

        Block placed = ladder
                ? BedWars.nms.placeLadder(baseBlock, dx, dy, dz, arena, ladderData)
                : BedWars.nms.placeTowerBlocks(baseBlock, arena, color, dx, dy, dz);

        PopUpTowerBuildEvent event = new PopUpTowerBuildEvent(color, arena, placed);
        Bukkit.getPluginManager().callEvent(event);
    }

    private static int parseInt(String s) {
        try {
            return (int) Double.parseDouble(s.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }
}
