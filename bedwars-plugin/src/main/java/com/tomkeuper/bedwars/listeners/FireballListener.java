package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.LastHit;
import com.tomkeuper.bedwars.arena.team.BedWarsTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class FireballListener implements Listener {

    private static final double MAX_KB = 4.0;

    private final double fireballExplosionSize;
    private final double fireballHorizontalSelf;
    private final double fireballHorizontalOthers;
    private final double fireballVerticalSelf;
    private final double fireballVerticalOthers;
    private final double damageSelf;
    private final double damageEnemy;
    private final double damageTeammates;
    private final double fireballSpeedMultiplier;
    private final double fireballCooldown;
    private final double jumpTolerance;
    private final boolean fireballMakeFire;
    private final Set<Material> explosionProof;

    public FireballListener() {
        YamlConfiguration yml = BedWars.config.getYml();
        fireballExplosionSize = yml.getDouble(ConfigPath.GENERAL_FIREBALL_EXPLOSION_SIZE);
        fireballMakeFire = yml.getBoolean(ConfigPath.GENERAL_FIREBALL_MAKE_FIRE);
        fireballHorizontalSelf = yml.getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_HORIZONTAL_SELF) * -1;
        fireballHorizontalOthers = yml.getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_HORIZONTAL_OTHERS) * -1;
        fireballVerticalSelf = yml.getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_VERTICAL_SELF);
        fireballVerticalOthers = yml.getDouble(ConfigPath.GENERAL_FIREBALL_KNOCKBACK_VERTICAL_OTHERS);
        damageSelf = yml.getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_SELF);
        damageEnemy = yml.getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_ENEMY);
        damageTeammates = yml.getDouble(ConfigPath.GENERAL_FIREBALL_DAMAGE_TEAMMATES);
        fireballSpeedMultiplier = yml.getDouble(ConfigPath.GENERAL_FIREBALL_SPEED_MULTIPLIER);
        fireballCooldown = yml.getDouble(ConfigPath.GENERAL_FIREBALL_COOLDOWN);
        jumpTolerance = yml.getDouble(ConfigPath.GENERAL_FIREBALL_JUMP_TOLERANCE);

        explosionProof = EnumSet.noneOf(Material.class);
        for (String type : BedWars.config.getList(ConfigPath.GENERAL_FIREBALL_EXPLOSION_PROOF_BLOCKS)) {
            try {
                explosionProof.add(Material.valueOf(type));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFireballInteract(PlayerInteractEvent e) {
        ItemStack handItem = e.getItem();
        Action action = e.getAction();
        if ((action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) || handItem == null) return;

        Player player = e.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || arena.getStatus() != GameState.playing || handItem.getType() != BedWars.nms.materialFireball())
            return;

        e.setCancelled(true);

        long now = System.currentTimeMillis();
        long last = arena.getFireballCooldowns().getOrDefault(player.getUniqueId(), 0L);
        long cdMs = (long) (fireballCooldown * 1000);
        long left = cdMs - (now - last);
        if (left > 0) {
            if (fireballCooldown >= 1.0) {
                long secs = (left + 999) / 1000;
                player.sendMessage(Language.getMsg(player, Messages.ARENA_FIREBALL_COOLDOWN)
                        .replace("%bw_cooldown%", String.valueOf(secs)));
            }
            return;
        }
        arena.getFireballCooldowns().put(player.getUniqueId(), now);

        Fireball fireball = player.launchProjectile(Fireball.class);
        Vector direction = player.getEyeLocation().getDirection();
        fireball = BedWars.nms.setFireballDirection(fireball, direction);
        fireball.setVelocity(fireball.getDirection().multiply(fireballSpeedMultiplier));
        fireball.setYield((float) fireballExplosionSize);
        fireball.setIsIncendiary(fireballMakeFire);
        fireball.setMetadata("bw2023", new FixedMetadataValue(BedWars.plugin, "1"));

        BedWars.nms.minusAmount(player, handItem, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void fireballHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Fireball)) return;

        ProjectileSource projectileSource = e.getEntity().getShooter();
        if (!(projectileSource instanceof Player source)) return;

        IArena arena = Arena.getArenaByPlayer(source);
        if (arena == null || arena.getStatus() != GameState.playing) return;

        Location loc = e.getEntity().getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        Vector center = loc.toVector();
        Collection<Entity> nearby = world.getNearbyEntities(loc, fireballExplosionSize, fireballExplosionSize, fireballExplosionSize);

        for (Entity entity : nearby) {
            if (!(entity instanceof Player player)) continue;
            if (!Arena.isInArena(player) || arena.isSpectator(player) || arena.isReSpawning(player)) continue;

            UUID pid = player.getUniqueId();
            long invuln = BedWarsTeam.reSpawnInvulnerability.getOrDefault(pid, 0L);
            if (invuln > System.currentTimeMillis()) continue;
            BedWarsTeam.reSpawnInvulnerability.remove(pid);

            Vector playerVec = player.getLocation().toVector();
            Vector unit = safeNormalize(center.clone().subtract(playerVec));
            double baseY = unit.getY() < 0 ? unit.getY() + 1.5 : unit.getY();
            baseY = Math.max(baseY, 0);

            boolean self = player.getUniqueId().equals(source.getUniqueId());
            double y = (baseY <= jumpTolerance)
                    ? (self ? fireballVerticalSelf : fireballVerticalOthers) * 1.5
                    : baseY * (self ? fireballVerticalSelf : fireballVerticalOthers) * 1.5;

            Vector horiz = unit.multiply(self ? fireballHorizontalSelf : fireballHorizontalOthers);
            Vector kb = clamp(horiz.setY(y));

            if (isFinite(kb)) {
                try {
                    player.setVelocity(kb);
                } catch (IllegalArgumentException ex) {
                    Bukkit.getLogger().warning("Invalid KB vector for " + player.getName() + " at " + player.getLocation() + " vec=" + kb);
                }
            }

            touch(player, source);

            if (self) {
                if (damageSelf > 0) damageFromExplosion(player, damageSelf, source);
            } else {
                ITeam pt = arena.getTeam(player);
                ITeam st = arena.getTeam(source);
                double amount = (pt != null && pt.equals(st)) ? damageTeammates : damageEnemy;
                damageFromExplosion(player, amount, source);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireballExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Fireball)) return;

        ProjectileSource projectileSource = ((Fireball) event.getEntity()).getShooter();
        if (!(projectileSource instanceof Player source)) return;

        IArena arena = Arena.getArenaByPlayer(source);
        if (arena == null || arena.getStatus() != GameState.playing) return;

        event.blockList().removeIf(block -> explosionProof.contains(block.getType()));
    }

    @EventHandler(ignoreCancelled = true)
    public void fireballDirectHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Fireball) || !(e.getEntity() instanceof Player player)) return;
        if (!Arena.isInArena(player)) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void fireballPrime(ExplosionPrimeEvent e) {
        if (!(e.getEntity() instanceof Fireball fireball)) return;
        ProjectileSource shooter = fireball.getShooter();
        if (!(shooter instanceof Player shooterPlayer) || !Arena.isInArena(shooterPlayer)) return;
        e.setFire(fireballMakeFire);
    }

    private static boolean isFinite(Vector v) {
        return Double.isFinite(v.getX()) && Double.isFinite(v.getY()) && Double.isFinite(v.getZ());
    }

    private static Vector safeNormalize(Vector v) {
        double len = v.length();
        if (len < 1.0e-6) return new Vector(0, 0.1, 0);
        return v.multiply(1.0 / len);
    }

    private static Vector clamp(Vector v) {
        double len = v.length();
        if (len > FireballListener.MAX_KB) v.multiply(FireballListener.MAX_KB / len);
        return v;
    }

    private static void damageFromExplosion(Player victim, double amount, Player source) {
        if (amount <= 0) return;
        if (source != null) victim.damage(amount, source);
        else victim.damage(amount);
    }

    private static void touch(Player victim, Player damager) {
        LastHit lh = LastHit.getLastHit(victim);
        if (lh != null) {
            lh.setDamager(damager);
            lh.setTime(System.currentTimeMillis());
        } else {
            new LastHit(victim, damager, System.currentTimeMillis());
        }
    }
}
