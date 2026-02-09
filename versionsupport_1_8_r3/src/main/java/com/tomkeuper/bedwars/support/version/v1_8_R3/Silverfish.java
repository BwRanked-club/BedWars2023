package com.tomkeuper.bedwars.support.version.v1_8_R3;

import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.support.version.common.VersionCommon;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public class Silverfish extends EntitySilverfish {

    private ITeam team;

    public Silverfish(World world, ITeam bedWarsTeam, int pathFindingTicks) {
        super(world);
        if (bedWarsTeam == null) return;
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(this.goalSelector, new UnsafeList());
            bField.set(this.targetSelector, new UnsafeList());
            cField.set(this.goalSelector, new UnsafeList());
            cField.set(this.targetSelector, new UnsafeList());
        } catch (IllegalAccessException | NoSuchFieldException e1) {
            e1.printStackTrace();
        }
        this.team = bedWarsTeam;
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this,1.9D, false));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.goalSelector.a(3, new PathfinderGoalRandomStroll(this, 2D));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, pathFindingTicks, true, false, player -> {
            if (player == null) return false;
            return ((EntityHuman)player).isAlive() && !team.wasMember(((EntityHuman)player).getUniqueID()) && !team.getArena().isReSpawning(((EntityHuman)player).getUniqueID())
                    && !team.getArena().isSpectator(((EntityHuman)player).getUniqueID());
        }));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, IGolem.class, pathFindingTicks, true, false, golem -> {
            if (golem == null) return false;
            return ((IGolem)golem).getTeam() != team;
        }));
        this.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget(this, Silverfish.class, pathFindingTicks, true, false, sf -> {
            if (sf == null) return false;
            return ((Silverfish)sf).getTeam() != team;
        }));
    }

    public ITeam getTeam() {
        return team;
    }

    public static LivingEntity spawn(Location loc, ITeam team, double speed, double health, int despawn, double damage, int pathFindingTicks) {
        WorldServer mcWorld = ((CraftWorld)loc.getWorld()).getHandle();
        Silverfish customEnt = new Silverfish(mcWorld, team, pathFindingTicks);
        customEnt.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        customEnt.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        customEnt.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        customEnt.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(damage);
        ((CraftLivingEntity)customEnt.getBukkitEntity()).setRemoveWhenFarAway(false);
        customEnt.setCustomNameVisible(true);
        mcWorld.addEntity(customEnt, CreatureSpawnEvent.SpawnReason.CUSTOM);

        Player initialTarget = findInitialTarget(loc, team);
        if (initialTarget != null && customEnt.getBukkitEntity() instanceof Creature creature) {
            creature.setTarget(initialTarget);
        }
        return (LivingEntity) customEnt.getBukkitEntity();
    }

    private static Player findInitialTarget(Location loc, ITeam team) {
        if (loc == null || team == null || team.getArena() == null || loc.getWorld() == null) return null;
        Player closest = null;
        double best = Double.MAX_VALUE;
        for (Player player : loc.getWorld().getPlayers()) {
            if (player == null || !player.isOnline()) continue;
            if (team.wasMember(player.getUniqueId())) continue;
            if (team.getArena().isReSpawning(player.getUniqueId()) || team.getArena().isSpectator(player)) continue;
            double dist = player.getLocation().distanceSquared(loc);
            if (dist < best) {
                best = dist;
                closest = player;
            }
        }
        return closest;
    }

    @Override
    public void die(DamageSource damagesource) {
        super.die(damagesource);
        team = null;
        VersionCommon.api.getVersionSupport().getDespawnablesList().remove(this.getUniqueID());
    }

    @Override
    public void die() {
        super.die();
        team = null;
        VersionCommon.api.getVersionSupport().getDespawnablesList().remove(this.getUniqueID());
    }
}
