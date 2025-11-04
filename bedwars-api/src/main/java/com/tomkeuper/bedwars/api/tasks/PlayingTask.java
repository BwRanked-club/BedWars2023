package com.tomkeuper.bedwars.api.tasks;

import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.scheduler.BukkitTask;

/**
 * The PlayingTask interface represents a task that is executed during the gameplay of a BedWars mini-game.
 * It provides methods to access the associated arena, Bukkit task, task ID, countdowns, and to cancel the task.
 */
public interface PlayingTask {

    /**
     * Get the arena associated with the playing task.
     *
     * @return The arena object.
     */
    IArena getArena();

    /**
     * Get the Bukkit task associated with the playing task.
     *
     * @return The BukkitTask object representing the task.
     */
    BukkitTask getBukkitTask();

    /**
     * Get the task ID of the playing task.
     *
     * @return The task ID.
     */
    int getTask();

    /**
     * Get the countdown value for destroying beds in the game.
     *
     * @return The countdown value for beds destruction.
     */
    int getBedsDestroyCountdown();

    /**
     * Get the countdown value for spawning the ender dragon in the game.
     *
     * @return The countdown value for ender dragon spawn.
     */
    int getDragonSpawnCountdown();

    /**
     * Get the countdown value for the end of the game.
     *
     * @return The countdown value for game end.
     */
    int getGameEndCountdown();

    /**
     * Cancel the playing task and associated tasks.
     */
    void cancel();
}