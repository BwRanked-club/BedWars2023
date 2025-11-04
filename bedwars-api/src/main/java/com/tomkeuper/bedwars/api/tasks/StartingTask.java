package com.tomkeuper.bedwars.api.tasks;

import com.tomkeuper.bedwars.api.arena.IArena;
import org.bukkit.scheduler.BukkitTask;

/**
 * The StartingTask interface represents a task that handles the starting process of a game in a BedWars arena.
 */
public interface StartingTask {

    /**
     * Get the countdown value for the starting process.
     *
     * @return The countdown value.
     */
    int getCountdown();

    /**
     * Set the countdown value for the starting process.
     *
     * @param countdown The countdown value to set.
     */
    void setCountdown(int countdown);

    /**
     * Get the arena associated with the task.
     *
     * @return The arena object.
     */
    IArena getArena();

    /**
     * Get the task ID of the task.
     *
     * @return The task ID.
     */
    int getTask();

    /**
     * Get the BukkitTask associated with the task.
     *
     * @return The BukkitTask object.
     */
    BukkitTask getBukkitTask();

    /**
     * Cancel the task.
     */
    void cancel();
}

