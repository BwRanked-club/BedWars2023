package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AutoscaleListener implements Listener {

    private static final Set<String> PENDING = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @EventHandler
    public void onState(GameStateChangeEvent e) {
        if (!BedWars.autoscale) return;
        BedWars.debug(e.getNewState().name());
        if (e.getNewState() != GameState.playing) return;

        String template = e.getArena().getArenaName();

        if (Arena.hasStandbyForTemplate(template)) return;

        if (!PENDING.add(template)) return;

        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            try {
                if (!Arena.hasStandbyForTemplate(template)) {
                    new Arena(template, null);
                    BedWars.debug("[AutoScale] Spawned standby for template " + template +
                            " (standbys now: " + Arena.countStandbyForTemplate(template) + ")");
                }
            } finally {
                PENDING.remove(template);
            }
        });
    }

    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        if (!BedWars.autoscale) return;

        String arenaName = e.getArena().getArenaName();
        String template = arenaName.startsWith("bw_temp_")
                ? arenaName.replaceAll("bw_temp_\\w+_", "")
                : arenaName;

        BedWars.debug("[AutoScaleEnd] Checking template " + template);

        Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
            boolean hasStandby = Arena.hasStandbyForTemplate(template);
            boolean standbyIsPlaying = Arena.getArenas().stream()
                    .anyMatch(a -> a.getArenaName().startsWith("bw_temp_")
                            && a.getArenaName().endsWith(template)
                            && a.getStatus() == GameState.playing);

            if (!hasStandby || standbyIsPlaying) {
                BedWars.debug("[AutoScaleEnd] Creating new standby for " + template);
                new Arena(template, null);
                BedWars.debug("[AutoScaleEnd] Spawned standby for template " + template +
                        " (standbys now: " + Arena.countStandbyForTemplate(template) + ")");
            }
        });
    }
}
