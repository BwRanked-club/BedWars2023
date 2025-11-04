package com.tomkeuper.bedwars.api.tasks;

import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.scheduler.BukkitTask;

/**
 * The RestartingTask interface represents a task that handles the restarting process of a game in a BedWars arena.
 */
public interface RestartingTask {

    /**
     * Get the arena associated with the task.
     *
     * @return The arena object.
     */
    IArena getArena();

    /**
     * Get the BukkitTask associated with the task.
     *
     * @return The BukkitTask object.
     */
    BukkitTask getBukkitTask();

    /**
     * Get the task ID of the task.
     *
     * @return The task ID.
     */
    int getTask();

    /**
     * Get the remaining time for the restarting process.
     *
     * @return The remaining time in seconds.
     */
    int getRestarting();

    /**
     * Cancel the task.
     */
    void cancel();
}