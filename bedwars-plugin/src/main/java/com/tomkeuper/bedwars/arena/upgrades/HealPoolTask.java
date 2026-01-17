package com.tomkeuper.bedwars.arena.upgrades;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class HealPoolTask extends BukkitRunnable {

    // --- Static task registry (O(1) membership / removal) ---
    private static final Set<HealPoolTask> TASKS = new HashSet<>();

    // --- Configurable sampling (tune to balance CPU vs. visuals) ---
    private static final int CHANCE_DIVISOR = 250;      // keep the original "1 in 250" feeling
    private static final int MAX_SAMPLES_PER_TICK = 512; // safety cap per run
    private static final int TICK_PERIOD = 30;          // original period

    private final ITeam team;
    private final IArena arena;
    private final World world;

    private final int minX, maxX, minY, maxY, minZ, maxZ;

    public HealPoolTask(ITeam team) {
        this.team = team;

        if (team == null || team.getSpawn() == null || team.getArena() == null) {
            removeForTeam(team);
            cancel();
            this.arena = null;
            this.world = null;
            this.minX = this.maxX = this.minY = this.maxY = this.minZ = this.maxZ = 0;
            return;
        }

        this.arena = team.getArena();
        this.world = arena.getWorld();

        int radius = arena.getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS);
        Location spawn = team.getSpawn();

        this.minX = spawn.getBlockX() - radius;
        this.maxX = spawn.getBlockX() + radius;
        this.minY = spawn.getBlockY() - radius;
        this.maxY = spawn.getBlockY() + radius;
        this.minZ = spawn.getBlockZ() - radius;
        this.maxZ = spawn.getBlockZ() + radius;

        // Keep async scheduling but do world-touch in a sync subtask.
        this.runTaskTimerAsynchronously(BedWars.plugin, 0L, TICK_PERIOD);
        TASKS.add(this);
    }

    public static boolean exists(IArena arena, ITeam team) {
        if (TASKS.isEmpty()) return false;
        for (HealPoolTask task : TASKS) {
            if (task != null && task.getArena() == arena && task.getTeam() == team) return true;
        }
        return false;
    }

    // --- Static helpers (API-compatible) ---

    public static void removeForArena(IArena arena) {
        if (arena == null || TASKS.isEmpty()) return;
        for (Iterator<HealPoolTask> it = TASKS.iterator(); it.hasNext(); ) {
            HealPoolTask task = it.next();
            if (task == null) continue;
            IArena a = task.getArena();
            if (a != null && a.equals(arena)) {
                task.cancel();
                it.remove();
            }
        }
    }

    public static void removeForArena(String worldName) {
        if (worldName == null || TASKS.isEmpty()) return;
        for (Iterator<HealPoolTask> it = TASKS.iterator(); it.hasNext(); ) {
            HealPoolTask task = it.next();
            if (task == null) continue;
            IArena a = task.getArena();
            if (a != null && worldName.equals(a.getWorldName())) {
                task.cancel();
                it.remove();
            }
        }
    }

    public static void removeForTeam(ITeam team) {
        if (team == null || TASKS.isEmpty()) return;
        for (Iterator<HealPoolTask> it = TASKS.iterator(); it.hasNext(); ) {
            HealPoolTask task = it.next();
            if (task == null) continue;
            ITeam t = task.getTeam();
            if (t != null && t.equals(team)) {
                task.cancel();
                it.remove();
            }
        }
    }

    @Override
    public void run() {
        // Basic null/validity guards
        if (team == null || arena == null || world == null || team.getSpawn() == null) {
            TASKS.remove(this);
            cancel();
            return;
        }

        // Snapshot lightweight data that is safe off-thread (primitive bounds and config reads)
        final boolean seenByTeamOnly = BedWars.config.getBoolean(
                ConfigPath.GENERAL_CONFIGURATION_HEAL_POOL_SEEN_TEAM_ONLY
        );

        // Compute sampling size based on volume (no world access here)
        long volume = (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        int samples = (int) Math.min(MAX_SAMPLES_PER_TICK, Math.max(16, volume / CHANCE_DIVISOR));

        // Pre-generate random coordinates off-thread (no Bukkit calls)
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        final List<Location> candidateLocations = new ArrayList<>(samples);
        for (int i = 0; i < samples; i++) {
            int x = rnd.nextInt(minX, maxX + 1);
            int y = rnd.nextInt(minY, maxY + 1);
            int z = rnd.nextInt(minZ, maxZ + 1);
            // Center on block for nicer particles
            candidateLocations.add(new Location(world, x + 0.5, y + 0.5, z + 0.5));
        }

        // Build recipients snapshot off-thread (no iteration over live collections later)
        final List<Player> recipientsSnapshot;
        if (seenByTeamOnly) {
            recipientsSnapshot = team.getMembers().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            recipientsSnapshot = arena.getPlayers().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        // Apply on main thread: block queries and particle effects
        BedWars.plugin.getServer().getScheduler().runTask(BedWars.plugin, () -> {
            // Defensive checks again before touching world state
            if (team.getSpawn() == null) return;

            for (Location loc : candidateLocations) {
                // Touching blocks must be sync
                if (loc.getBlock().getType() != Material.AIR) continue;

                // Preserve the probabilistic nature (roughly 1 in CHANCE_DIVISOR)
                if (rnd.nextInt(CHANCE_DIVISOR) != 0) continue;

                // Play effect to the chosen audience
                for (Player p : recipientsSnapshot) {
                    if (p == null || !p.isOnline()) continue;
                    BedWars.nms.playVillagerEffect(p, loc);
                }
            }
        });
    }

    // --- Accessors with clearer names ---

    public ITeam getTeam() {
        return team;
    }

    public IArena getArena() {
        return arena;
    }
}
