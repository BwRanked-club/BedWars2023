package com.tomkeuper.bedwars.arena.tasks;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.shop.ShopHolo;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.tomkeuper.bedwars.api.tasks.RestartingTask;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.configuration.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GameRestartingTask implements Runnable, RestartingTask {

    private final BukkitTask task;
    private Arena arena;
    private int restarting = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_RESTART) + 3;

    public GameRestartingTask(@NotNull Arena arena) {
        this.arena = arena;
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0, 20L);
        Sounds.playSound("game-end", arena.getPlayers());
        Sounds.playSound("game-end", arena.getSpectators());
    }

    /**
     * Get task ID
     */
    public int getTask() {
        return task.getTaskId();
    }

    @Override
    public int getRestarting() {
        return restarting;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public BukkitTask getBukkitTask() {
        return task;
    }

    @Override
    public void run() {

        restarting--;

        if (getArena().getPlayers().isEmpty() && restarting > 9) restarting = 9;
        if (restarting == 7) {
            for (Player on : new ArrayList<>(getArena().getPlayers())) {
                getArena().removePlayer(on, BedWars.getServerType() == ServerType.BUNGEE, true);
            }
            for (Player on : new ArrayList<>(getArena().getSpectators())) {
                getArena().removeSpectator(on, BedWars.getServerType() == ServerType.BUNGEE, true);
            }
        } else if (restarting == 4) {
            for (Language lang : Language.getLanguages()) {
                List<ShopHolo> holos = getArena().getShopHolograms(lang.getIso());
                if (holos != null) {
                    for (ShopHolo holo : holos) {
                        if (holo != null) holo.clear();
                    }
                }
            }
            for (Entity e : getArena().getWorld().getEntities()) {
                if (e.getType() == EntityType.PLAYER) {
                    Player p = (Player) e;
                    Misc.moveToLobbyOrKick(p, getArena(), true);
                    if (getArena().isSpectator(p)) getArena().removeSpectator(p, false);
                    if (getArena().isPlayer(p)) getArena().removePlayer(p, false);
                }
            }
            for (IGenerator eg : getArena().getOreGenerators()) {
                eg.disable();
            }
            for (ITeam t : getArena().getTeams()) {
                for (IGenerator eg : t.getGenerators()) {
                    eg.disable();
                }
            }
        } else if (restarting == 0) {
            getArena().restart();
            task.cancel();
            arena = null;
        }
    }

    public void cancel() {
        task.cancel();
    }

}
