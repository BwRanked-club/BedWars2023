package com.tomkeuper.bedwars.arena.tasks;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.gameplay.EggBridgeBuildEvent;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.configuration.Sounds;
import com.tomkeuper.bedwars.listeners.EggBridgeListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import static com.tomkeuper.bedwars.BedWars.config;

@SuppressWarnings("WeakerAccess")
public class EggBridgeTask implements Runnable {

    private Egg projectile;
    private TeamColor teamColor;
    private Player player;
    private IArena arena;
    private BukkitTask task;

    public EggBridgeTask(Player player, Egg projectile, TeamColor teamColor) {
        IArena a = Arena.getArenaByPlayer(player);
        if (a == null) return;
        this.arena = a;
        this.projectile = projectile;
        this.teamColor = teamColor;
        this.player = player;
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, 0, 1);
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public Egg getProjectile() {
        return projectile;
    }

    public Player getPlayer() {
        return player;
    }

    public IArena getArena() {
        return arena;
    }

    @Override
    public void run() {

        Location loc = getProjectile().getLocation();
        if (loc.getBlockY() >= getArena().getConfig().getInt(ConfigPath.ARENA_CONFIGURATION_MAX_BUILD_Y)) {
            return;
        }

        if (getProjectile().isDead()
                || !arena.isPlayer(getPlayer())
                || getPlayer().getLocation().distance(getProjectile().getLocation()) > config.getInt(ConfigPath.GENERAL_EGGBRIDGE_MAX_LENGTH)
                || getPlayer().getLocation().getY() - getProjectile().getLocation().getY() > config.getInt(ConfigPath.GENERAL_EGGBRIDGE_MAX_HEIGHT)) {
            EggBridgeListener.removeEgg(projectile);
            return;
        }

        if (getPlayer().getLocation().distance(loc) > config.getDouble(ConfigPath.GENERAL_EGGBRIDGE_MIN_DISTANCE_FROM_PLAYER)) {

            Block b2 = loc.clone().subtract(0.0D, 2.0D, 0.0D).getBlock();
            if (!Misc.isBuildProtected(b2.getLocation(), getArena())) {
                if (b2.getType() == Material.AIR) {
                    b2.setType(BedWars.nms.woolMaterial());
                    BedWars.nms.setBlockTeamColor(b2, getTeamColor());
                    getArena().addPlacedBlock(b2);
                    Bukkit.getPluginManager().callEvent(new EggBridgeBuildEvent(getTeamColor(), getArena(), b2));
                    loc.getWorld().playEffect(b2.getLocation(), BedWars.nms.eggBridge(), 3);
                    Sounds.playSound("egg-bridge-block", getPlayer());
                }
            }

            Block b3 = loc.clone().subtract(1.0D, 2.0D, 0.0D).getBlock();
            if (!Misc.isBuildProtected(b3.getLocation(), getArena())) {
                if (b3.getType() == Material.AIR) {
                    b3.setType(BedWars.nms.woolMaterial());
                    BedWars.nms.setBlockTeamColor(b3, getTeamColor());
                    getArena().addPlacedBlock(b3);
                    Bukkit.getPluginManager().callEvent(new EggBridgeBuildEvent(getTeamColor(), getArena(), b3));
                    loc.getWorld().playEffect(b3.getLocation(), BedWars.nms.eggBridge(), 3);
                    Sounds.playSound("egg-bridge-block", getPlayer());
                }
            }

            Block b4 = loc.clone().subtract(0.0D, 2.0D, 1.0D).getBlock();
            if (!Misc.isBuildProtected(b4.getLocation(), getArena())) {
                if (b4.getType() == Material.AIR) {
                    b4.setType(BedWars.nms.woolMaterial());
                    BedWars.nms.setBlockTeamColor(b4, getTeamColor());
                    getArena().addPlacedBlock(b4);
                    Bukkit.getPluginManager().callEvent(new EggBridgeBuildEvent(getTeamColor(), getArena(), b4));
                    loc.getWorld().playEffect(b4.getLocation(), BedWars.nms.eggBridge(), 3);
                    Sounds.playSound("egg-bridge-block", getPlayer());
                }
            }
        }
    }

    public void cancel(){
        task.cancel();
    }
}
