package com.tomkeuper.bedwars.popuptower;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.configuration.Sounds;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

@Slf4j
abstract class BaseTowerTask {

    private BukkitTask task;

    protected BaseTowerTask(Location soundLocation,
                            Block baseChestBlock,
                            TeamColor color,
                            Player player,
                            List<String> rawSteps) {

        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) return;

        final ItemStack hand = player.getInventory().getItemInHand();
        if (hand != null) {
            if (hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1);
            else player.getInventory().setItemInHand(null);
        }

        final List<RelativeStep> steps = new ArrayList<>(rawSteps.size());
        for (String s : rawSteps) {
            steps.add(parseStep(s));
        }

        final int[] index = {0};
        this.task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            Sounds.playsoundArea("pop-up-tower-build", soundLocation, 1.0F, 0.5F);

            if (index[0] >= steps.size()) {
                cancel();
                return;
            }
            placeOne(baseChestBlock, color, player, steps.get(index[0]++));

            if (index[0] >= steps.size()) {
                cancel();
                return;
            }
            placeOne(baseChestBlock, color, player, steps.get(index[0]++));
        }, 0L, 1L);
    }

    private static String toKey(RelativeStep s) {
        return s.dx + ", " + s.dy + ", " + s.dz;
    }

    private static RelativeStep parseStep(String raw) {
        String[] parts = raw.split(",");
        if (parts.length < 3) return new RelativeStep(0, 0, 0, -1);

        int dx = parseInt(parts[0]);
        int dy = parseInt(parts[1]);
        int dz = parseInt(parts[2]);

        int ladder = -1;
        if (parts.length >= 4) {
            String last = parts[3].trim();
            int idx = last.indexOf("ladder");
            if (idx >= 0) {
                try {
                    ladder = Integer.parseInt(last.substring(idx + "ladder".length()).trim());
                } catch (Exception ignored) {
                    ladder = -1;
                }
            }
        }
        return new RelativeStep(dx, dy, dz, ladder);
    }

    private static int parseInt(String s) {
        try {
            return (int) Double.parseDouble(s.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void placeOne(Block base, TeamColor color, Player p, RelativeStep step) {
        if (step.isLadder()) {
            new NewPlaceBlock(base,
                    toKey(step),
                    color, p, true, step.ladderData());
        } else {
            new NewPlaceBlock(base,
                    toKey(step),
                    color, p, false, 0);
        }
    }

    private void cancel() {
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
            }
            task = null;
        }
    }

    protected record RelativeStep(int dx, int dy, int dz, int ladderData) {
        boolean isLadder() {
            return ladderData >= 0;
        }
    }
}
