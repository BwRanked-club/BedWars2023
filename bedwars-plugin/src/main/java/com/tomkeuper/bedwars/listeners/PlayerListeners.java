package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.shop.ShopHolo;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.entity.Despawnable;
import com.tomkeuper.bedwars.api.entity.GeneratorHolder;
import com.tomkeuper.bedwars.api.events.player.PlayerInvisibilityPotionEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.events.team.TeamEliminatedEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.LastHit;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.arena.team.BedWarsTeam;
import com.tomkeuper.bedwars.configuration.Sounds;
import com.tomkeuper.bedwars.listeners.dropshandler.PlayerDrops;
import com.tomkeuper.bedwars.stats.AssistTracker;
import com.tomkeuper.bedwars.support.paper.PaperSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tomkeuper.bedwars.BedWars.plugin;
import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public class PlayerListeners implements Listener {

    private double tntJumpStrengthReductionConstant;
    private double tntJumpYAxisReductionConstant;
    private double tntJumpHorizontalForgiveness;
    private double tntDamageSelf;
    private double tntDamageTeammates;
    private double tntDamageOthers;
    private final Map<UUID, Location> deathSpectateLocations = new HashMap<>();

    public PlayerListeners() {
        reloadCombatSettings();
    }

    public void reloadCombatSettings() {
        YamlConfiguration yml = BedWars.config.getYml();
        this.tntJumpStrengthReductionConstant = yml.getDouble(ConfigPath.GENERAL_TNT_JUMP_STRENGTH_REDUCTION);
        this.tntJumpYAxisReductionConstant = yml.getDouble(ConfigPath.GENERAL_TNT_JUMP_Y_REDUCTION);
        this.tntJumpHorizontalForgiveness = yml.getDouble(ConfigPath.GENERAL_TNT_JUMP_HORIZONTAL_FORGIVENESS);
        this.tntDamageSelf = yml.getDouble(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_SELF);
        this.tntDamageTeammates = yml.getDouble(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_TEAMMATES);
        this.tntDamageOthers = yml.getDouble(ConfigPath.GENERAL_TNT_JUMP_DAMAGE_OTHERS);
    }

    private static boolean isPlaying(IArena arena) {
        return arena != null && arena.getStatus() == GameState.playing;
    }

    private static boolean isAliveInArena(Player p, IArena arena) {
        return p != null && arena != null && arena.isPlayer(p) && !arena.isSpectator(p);
    }

    private static boolean sameTeam(IArena arena, Player a, Player b) {
        if (arena == null || a == null || b == null) return false;
        ITeam ta = arena.getTeam(a);
        ITeam tb = arena.getTeam(b);
        return ta != null && ta.equals(tb);
    }

    private static void updateLastHit(Player victim, Object damagerEntity) {
        if (victim == null || damagerEntity == null) return;
        LastHit lh = LastHit.getLastHit(victim);
        if (lh != null) {
            lh.setDamager((Entity) damagerEntity);
            lh.setTime(System.currentTimeMillis());
        } else {
            new LastHit(victim, (Entity) damagerEntity, System.currentTimeMillis());
        }
    }

    private static void removeInvisibilityAndShow(Player p, IArena a) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player on : a.getWorld().getPlayers()) BedWars.nms.showArmor(p, on);
            a.getShowTime().remove(p);
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
            ITeam team = a.getTeam(p);
            p.sendMessage(getMsg(p, Messages.INTERACT_INVISIBILITY_REMOVED_DAMGE_TAKEN));
            Bukkit.getPluginManager().callEvent(new PlayerInvisibilityPotionEvent(PlayerInvisibilityPotionEvent.Type.REMOVED, team, p, a));
        });
    }

    private static Vector centerXZ(Vector v) {
        v.setX(Math.floor(v.getX()) + 0.5);
        v.setZ(Math.floor(v.getZ()) + 0.5);
        return v;
    }

    private Location resolveDeathSpectateLocation(Player victim, IArena arena, EntityDamageEvent damageEvent) {
        Location location = victim.getLocation().clone();
        World world = location.getWorld();
        if (world == null) {
            return arena.getSpectatorLocation();
        }

        boolean diedInVoid = damageEvent != null && damageEvent.getCause() == EntityDamageEvent.DamageCause.VOID;
        if (diedInVoid) {
            int x = location.getBlockX();
            int z = location.getBlockZ();
            int highestY = world.getHighestBlockYAt(x, z);
            if (highestY > 1) {
                location.setY(highestY + 2.0);
            } else {
                Location fallback = arena.getSpectatorLocation();
                location = fallback == null ? location : fallback.clone();
            }
        } else {
            location.add(0, 5, 0);
        }

        double minY = 1.0;
        double maxY = Math.max(minY, world.getMaxHeight() - 1.0);
        if (location.getY() < minY) location.setY(minY);
        if (location.getY() > maxY) location.setY(maxY);
        return location;
    }

    private static void spawnUtility(String s, Location loc, ITeam t, Player p) {
        if ("silverfish".equalsIgnoreCase(s)) {
            BedWars.nms.spawnSilverfish(
                    loc,
                    t,
                    BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_SPEED),
                    BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_HEALTH),
                    BedWars.shop.getInt(ConfigPath.SHOP_SPECIAL_SILVERFISH_DESPAWN),
                    BedWars.shop.getYml().getDouble(ConfigPath.SHOP_SPECIAL_SILVERFISH_DAMAGE),
                    BedWars.shop.getInt(ConfigPath.SHOP_SPECIAL_SILVERFISH_PATH_FINDING_TICKS)
            );
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        World world = e.getEntity().getLocation().getWorld();

        if (BedWars.getServerType() == ServerType.MULTIARENA && world.getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
            e.setCancelled(true);
            return;
        }

        if (!(e.getEntity() instanceof Player player)) return;

        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) return;

        if (arena.isSpectator(player) || arena.isReSpawning(player) || arena.getStatus() != GameState.playing) {
            e.setCancelled(true);
            return;
        }

        // invulnerabilidade pós-respawn (mantém comportamento existente)
        if (BedWarsTeam.reSpawnInvulnerability.containsKey(player.getUniqueId())) {
            if (BedWarsTeam.reSpawnInvulnerability.get(player.getUniqueId()) > System.currentTimeMillis())
                e.setCancelled(true);
            else BedWarsTeam.reSpawnInvulnerability.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBowHit(EntityDamageByEntityEvent e) {
        if (e.isCancelled() || e.getEntity().getType() != EntityType.PLAYER || !(e.getDamager() instanceof Projectile projectile))
            return;
        if (!(projectile.getShooter() instanceof Player damager)) return;

        Player player = (Player) e.getEntity();
        IArena arena = Arena.getArenaByPlayer(player);
        if (!isPlaying(arena)) return;

        // msg de hit por arco
        ITeam team = arena.getTeam(player);
        Language lang = Language.getPlayerLanguage(damager);
        String fmt = lang.m(Messages.PLAYER_HIT_BOW);
        if (fmt.isEmpty()) return;

        double remaining = Math.max(((Player) e.getEntity()).getHealth() - e.getFinalDamage(), 0);
        String msg = fmt
                .replace("%bw_damage_amount%", new DecimalFormat("#.#").format(((Player) e.getEntity()).getHealth() - e.getFinalDamage()))
                .replace("%bw_player%", Misc.getPlayerName(player))
                .replace("%bw_team%", team.getColor().chat() + team.getDisplayName(lang))
                .replace("%bw_health_remaining%", new DecimalFormat("#.#").format(remaining));
        damager.sendMessage(msg);

        updateLastHit(player, damager);
        AssistTracker.recordHit(player, damager, arena);
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed tnt)) return;
        if (!(tnt.getSource() instanceof Player owner)) return;

        double r = tnt.getYield();
        for (Entity ent : tnt.getNearbyEntities(r, r, r)) {
            if (!(ent instanceof Player victim) || !victim.equals(owner)) continue;

            EntityDamageByEntityEvent fake = new EntityDamageByEntityEvent(tnt, victim, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 4.0);
            Bukkit.getPluginManager().callEvent(fake);
            if (!fake.isCancelled()) {
                AssistTracker.recordHit(victim, owner, Arena.getArenaByPlayer(victim));
                victim.damage(fake.getFinalDamage(), tnt);
                updateLastHit(victim, owner);
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player p) {
            IArena a = Arena.getArenaByPlayer(p);
            if (a == null) return;

            if (!isPlaying(a) || a.isSpectator(p) || a.isReSpawning(p)) {
                e.setCancelled(true);
                return;
            }

            Player damager = null;
            Object damagerSource = null;

            if (e.getDamager() instanceof Player d) {
                damager = d;
                damagerSource = d;
                if (a.isReSpawning(damager)) {
                    e.setCancelled(true);
                    return;
                }
            } else if (e.getDamager() instanceof Projectile proj) {
                ProjectileSource shooter = proj.getShooter();
                if (!(shooter instanceof Player d)) return;
                damager = d;
                damagerSource = d;
            } else if (e.getDamager() instanceof TNTPrimed tnt) {
                if (!(tnt.getSource() instanceof Player d)) return;
                damager = d;
                damagerSource = tnt;

                if (damager == p) {
                    if (tntDamageSelf > -1) e.setDamage(tntDamageSelf);
                    applyTntJumpVelocity((LivingEntity) e.getEntity(), tnt);
                } else {
                    ITeam currentTeam = a.getTeam(p);
                    ITeam damagerTeam = a.getTeam(damager);
                    if (currentTeam != null && currentTeam.equals(damagerTeam)) {
                        if (tntDamageTeammates > -1) e.setDamage(tntDamageTeammates);
                    } else if (tntDamageOthers > -1) e.setDamage(tntDamageOthers);
                }
            } else if (e.getDamager() instanceof Silverfish || e.getDamager() instanceof IronGolem) {
                damagerSource = e.getDamager();
                updateLastHit(p, e.getDamager());
                if (a.getShowTime().containsKey(p)) {
                    if ((e.getDamager() instanceof Silverfish && BedWars.shop.getBoolean(ConfigPath.SHOP_SPECIAL_SILVERFISH_REMOVES_INVISIBILITY))
                            || (e.getDamager() instanceof IronGolem && BedWars.shop.getBoolean(ConfigPath.SHOP_SPECIAL_IRON_GOLEM_REMOVES_INVISIBILITY))) {
                        removeInvisibilityAndShow(p, a);
                    }
                }
            }

            if (damager != null) {
                if (a.isSpectator(damager) || a.isReSpawning(damager.getUniqueId())) {
                    e.setCancelled(true);
                    return;
                }
                if (sameTeam(a, p, damager) && !(e.getDamager() instanceof TNTPrimed)) {
                    e.setCancelled(true);
                    return;
                }

                BedWarsTeam.reSpawnInvulnerability.remove(damager.getUniqueId());

                updateLastHit(p, damagerSource);
                AssistTracker.recordHit(p, damager, a);

                if (a.getShowTime().containsKey(p)) removeInvisibilityAndShow(p, a);
            }
        } else if (BedWars.nms.isDespawnable(e.getEntity())) {
            Player damager;

            if (e.getDamager() instanceof Player p) damager = p;
            else if (e.getDamager() instanceof Projectile proj) {
                if (!(proj.getShooter() instanceof Player p)) return;
                damager = p;
            } else if (e.getDamager() instanceof TNTPrimed tnt) {
                if (!(tnt.getSource() instanceof Player p)) return;
                damager = p;
            } else return;

            IArena a = Arena.getArenaByPlayer(damager);
            if (a == null) return;

            if (a.isPlayer(damager)) {
                Despawnable d = BedWars.nms.getDespawnablesList().get(e.getEntity().getUniqueId());
                if (d != null && a.getTeam(damager) == d.getTeam()) e.setCancelled(true);
            } else e.setCancelled(true);
        }
    }

    private void applyTntJumpVelocity(LivingEntity damaged, TNTPrimed tnt) {
        Vector tntLoc = centerXZ(tnt.getLocation().toVector());
        Vector playerLoc = damaged.getLocation().toVector();

        Vector dirToPlayer = playerLoc.clone().subtract(tntLoc);
        double originalDistance = dirToPlayer.length();
        Vector forgiveness = dirToPlayer.clone().normalize().multiply(tntJumpHorizontalForgiveness / Math.max(originalDistance, 0.0001));

        Vector adjusted = playerLoc.clone().add(forgiveness);
        Vector distance = adjusted.clone().subtract(tntLoc);
        Vector direction = distance.clone().normalize();

        double force = ((tnt.getYield() * tnt.getYield()) / (tntJumpStrengthReductionConstant + originalDistance));
        Vector resulting = direction.multiply(force);
        resulting.setY(resulting.getY() / (originalDistance + tntJumpYAxisReductionConstant));

        damaged.setVelocity(resulting);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = e.getEntity().getKiller();
        IArena a = Arena.getArenaByPlayer(victim);

        if (a == null) {
            if (BedWars.getServerType() == ServerType.MULTIARENA && BedWars.getLobbyWorld().equals(victim.getWorld().getName())) {
                e.setDeathMessage(null);
            }
            return;
        }

        e.setDeathMessage(null);

        if (a.isSpectator(victim) || a.getStatus() != GameState.playing) {
            victim.spigot().respawn();
            return;
        }

        EntityDamageEvent dmg = victim.getLastDamageCause();
        ITeam victimsTeam = a.getTeam(victim);
        if (victimsTeam == null) {
            victim.spigot().respawn();
            return;
        }

        deathSpectateLocations.put(victim.getUniqueId(), resolveDeathSpectateLocation(victim, a, dmg));

        BedWars.nms.clearArrowsFromPlayerBody(victim);

        boolean victimsBedDestroyed = victimsTeam.isBedDestroyed();
        String message = victimsBedDestroyed ? Messages.PLAYER_DIE_UNKNOWN_REASON_FINAL_KILL : Messages.PLAYER_DIE_UNKNOWN_REASON_REGULAR;
        PlayerKillEvent.PlayerKillCause cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.UNKNOWN_FINAL_KILL : PlayerKillEvent.PlayerKillCause.UNKNOWN;

        Player bedDestroyer = victimsTeam.getBedDestroyer();
        ITeam killersTeam = null;

        if (dmg != null) {
            switch (dmg.getCause()) {
                case ENTITY_EXPLOSION -> {
                    Player lhKiller = extractPlayerFromLastHit(victim);
                    if (lhKiller != null && !lhKiller.getUniqueId().equals(victim.getUniqueId())) killer = lhKiller;
                    else if (killer != null && killer.getUniqueId().equals(victim.getUniqueId())) killer = null;

                    if (killer == null) {
                        message = victimsBedDestroyed ? Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL : Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR;
                    } else {
                        message = (killer != victim)
                                ? (victimsBedDestroyed ? Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_FINAL_KILL : Messages.PLAYER_DIE_EXPLOSION_WITH_SOURCE_REGULAR_KILL)
                                : (victimsBedDestroyed ? Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_FINAL_KILL : Messages.PLAYER_DIE_EXPLOSION_WITHOUT_SOURCE_REGULAR);
                    }
                    cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.EXPLOSION_FINAL_KILL : PlayerKillEvent.PlayerKillCause.EXPLOSION;
                }
                case VOID -> {
                    Player lhKiller = extractPlayerFromLastHit(victim);
                    if (lhKiller != null && !lhKiller.getUniqueId().equals(victim.getUniqueId())) {
                        killer = lhKiller;
                    } else {
                        if (isAliveInArena(bedDestroyer, a) && !bedDestroyer.getUniqueId().equals(victim.getUniqueId())) {
                            killer = bedDestroyer;
                        }
                    }

                    if (killer == null) {
                        message = victimsBedDestroyed ? Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL : Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL;
                    } else {
                        message = (killer != victim)
                                ? (victimsBedDestroyed ? Messages.PLAYER_DIE_KNOCKED_IN_VOID_FINAL_KILL : Messages.PLAYER_DIE_KNOCKED_IN_VOID_REGULAR_KILL)
                                : (victimsBedDestroyed ? Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL : Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL);
                    }
                    cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.VOID_FINAL_KILL : PlayerKillEvent.PlayerKillCause.VOID;
                }
                case ENTITY_ATTACK -> {
                    if (killer == null) {
                        Object damagerObj = extractDamagerObjectFromLastHit(victim);
                        if (damagerObj instanceof Entity ent && BedWars.nms.isDespawnable(ent)) {
                            Despawnable d = BedWars.nms.getDespawnablesList().get(ent.getUniqueId());
                            if (d != null) {
                                killersTeam = d.getTeam();
                                message = ent.getType() == EntityType.IRON_GOLEM
                                        ? (victimsBedDestroyed ? Messages.PLAYER_DIE_IRON_GOLEM_FINAL_KILL : Messages.PLAYER_DIE_IRON_GOLEM_REGULAR)
                                        : (victimsBedDestroyed ? Messages.PLAYER_DIE_DEBUG_FINAL_KILL : Messages.PLAYER_DIE_DEBUG_REGULAR);
                                cause = victimsBedDestroyed ? d.getDeathFinalCause() : d.getDeathRegularCause();
                            }
                        } else {
                            Player lhKiller = extractPlayerFromLastHit(victim);
                            if (lhKiller != null && !lhKiller.getUniqueId().equals(victim.getUniqueId())) {
                                killer = lhKiller;
                                message = victimsBedDestroyed ? Messages.PLAYER_DIE_PVP_FINAL_KILL : Messages.PLAYER_DIE_PVP_REGULAR_KILL;
                                cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.PVP_FINAL_KILL : PlayerKillEvent.PlayerKillCause.PVP;
                            }
                        }
                    } else {
                        message = victimsBedDestroyed ? Messages.PLAYER_DIE_PVP_FINAL_KILL : Messages.PLAYER_DIE_PVP_REGULAR_KILL;
                        cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.PVP_FINAL_KILL : PlayerKillEvent.PlayerKillCause.PVP;
                    }
                }
                case PROJECTILE -> {
                    if (killer != null) {
                        message = victimsBedDestroyed ? Messages.PLAYER_DIE_SHOOT_FINAL_KILL : Messages.PLAYER_DIE_SHOOT_REGULAR;
                        cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.PLAYER_SHOOT_FINAL_KILL : PlayerKillEvent.PlayerKillCause.PLAYER_SHOOT;
                    } else {
                        Player lhKiller = extractPlayerFromLastHit(victim);
                        if (lhKiller != null && !lhKiller.getUniqueId().equals(victim.getUniqueId())) {
                            killer = lhKiller;
                            message = victimsBedDestroyed ? Messages.PLAYER_DIE_SHOOT_FINAL_KILL : Messages.PLAYER_DIE_SHOOT_REGULAR;
                            cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.PLAYER_SHOOT_FINAL_KILL : PlayerKillEvent.PlayerKillCause.PLAYER_SHOOT;
                        }
                    }
                }
                case FALL -> {
                    Player lhKiller = extractPlayerFromLastHit(victim);
                    if (lhKiller != null && !lhKiller.getUniqueId().equals(victim.getUniqueId())) {
                        killer = lhKiller;
                        message = (victimsBedDestroyed ? Messages.PLAYER_DIE_KNOCKED_BY_FINAL_KILL : Messages.PLAYER_DIE_KNOCKED_BY_REGULAR_KILL);
                        cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.PLAYER_PUSH_FINAL : PlayerKillEvent.PlayerKillCause.PLAYER_PUSH;
                    } else if (isAliveInArena(bedDestroyer, a) && !bedDestroyer.getUniqueId().equals(victim.getUniqueId())) {
                        killer = bedDestroyer;
                        message = (victimsBedDestroyed ? Messages.PLAYER_DIE_KNOCKED_BY_FINAL_KILL : Messages.PLAYER_DIE_KNOCKED_BY_REGULAR_KILL);
                        cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.PLAYER_PUSH_FINAL : PlayerKillEvent.PlayerKillCause.PLAYER_PUSH;
                    } else {
                        message = victimsBedDestroyed ? Messages.PLAYER_DIE_VOID_FALL_FINAL_KILL : Messages.PLAYER_DIE_VOID_FALL_REGULAR_KILL;
                        cause = victimsBedDestroyed ? PlayerKillEvent.PlayerKillCause.PLAYER_PUSH_FINAL : PlayerKillEvent.PlayerKillCause.PLAYER_PUSH;
                    }
                }
                default -> {
                }
            }
        }

        String finalMessage = message;
        PlayerKillEvent playerKillEvent = new PlayerKillEvent(a, victim, killer, player -> Language.getMsg(player, finalMessage), cause);
        Bukkit.getPluginManager().callEvent(playerKillEvent);

        killer = playerKillEvent.getKiller();
        cause = playerKillEvent.getCause();

        if (killer != null) killersTeam = a.getTeam(killer);
        if (killer != null && playerKillEvent.playSound()) Sounds.playSound(ConfigPath.SOUNDS_KILL, killer);

        for (Player on : a.getPlayers()) {
            Language lang = Language.getPlayerLanguage(on);
            on.sendMessage(playerKillEvent.getMessage().apply(on)
                    .replace("%bw_player_color%", victimsTeam.getColor().chat().toString())
                    .replace("%bw_player%", Misc.getPlayerName(victim))
                    .replace("%bw_playername%", victim.getName())
                    .replace("%bw_team%", victimsTeam.getDisplayName(lang))
                    .replace("%bw_killer_color%", killersTeam == null ? "" : killersTeam.getColor().chat().toString())
                    .replace("%bw_killer_playername%", killer == null ? "" : killer.getName())
                    .replace("%bw_killer_name%", killer == null ? "" : Misc.getPlayerName(killer))
                    .replace("%bw_killer_team_name%", killersTeam == null ? "" : killersTeam.getDisplayName(lang)));
        }
        for (Player on : a.getSpectators()) {
            Language lang = Language.getPlayerLanguage(on);
            on.sendMessage(playerKillEvent.getMessage().apply(on)
                    .replace("%bw_player_color%", victimsTeam.getColor().chat().toString())
                    .replace("%bw_player%", Misc.getPlayerName(victim))
                    .replace("%bw_playername%", victim.getName())
                    .replace("%bw_team%", victimsTeam.getDisplayName(lang))
                    .replace("%bw_killer_color%", killersTeam == null ? "" : killersTeam.getColor().chat().toString())
                    .replace("%bw_killer_playername%", killer == null ? "" : killer.getName())
                    .replace("%bw_killer_name%", killer == null ? "" : Misc.getPlayerName(killer))
                    .replace("%bw_killer_team_name%", killersTeam == null ? "" : killersTeam.getDisplayName(lang)));
        }

        if (killer != null && !victim.equals(killer) && !victimsTeam.equals(killersTeam)) {
            a.addPlayerKill(killer, cause.isFinalKill(), victim);
        }

        if (PlayerDrops.handlePlayerDrops(a, victim, killer, victimsTeam, killersTeam, cause, e.getDrops())) {
            e.getDrops().clear();
        } else {
            for (ItemStack item : e.getDrops()) {
                victim.getWorld().dropItemNaturally(victim.getLocation(), item);
            }
            e.getDrops().clear();
        }

        Bukkit.getScheduler().runTask(plugin, () -> victim.spigot().respawn());
        a.addPlayerDeath(victim);

        if (victimsTeam.isBedDestroyed() && victimsTeam.getSize() == 1 && a.getConfig().getBoolean(ConfigPath.ARENA_DISABLE_GENERATOR_FOR_EMPTY_TEAMS)) {
            for (IGenerator g : victimsTeam.getGenerators()) g.disable();
            victimsTeam.getGenerators().clear();
        }
    }

    private Player extractPlayerFromLastHit(Player victim) {
        LastHit lh = LastHit.getLastHit(victim);
        if (lh == null) return null;
        Object obj = lh.getDamager();
        if (obj instanceof Player p) return p;

        if (obj instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        if (obj instanceof TNTPrimed tnt && tnt.getSource() instanceof Player p) return p;

        return null;
    }

    private Object extractDamagerObjectFromLastHit(Player victim) {
        LastHit lh = LastHit.getLastHit(victim);
        return lh != null ? lh.getDamager() : null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(player);

        if (a == null) {
            SetupSession ss = SetupSession.getSession(player.getUniqueId());
            if (ss != null) e.setRespawnLocation(player.getWorld().getSpawnLocation());
            return;
        }

        if (a.isSpectator(player)) {
            String iso = Language.getPlayerLanguage(player).getIso();
            e.setRespawnLocation(a.getSpectatorLocation());
            for (IGenerator o : a.getOreGenerators()) {
                GeneratorHolder holder = o.getHologramHolder();
                o.updateHolograms(player);
                if (holder != null) holder.update(player);
            }
            for (ITeam t : a.getTeams()) {
                for (IGenerator o : t.getGenerators()) {
                    GeneratorHolder holder = o.getHologramHolder();
                    o.updateHolograms(player);
                    if (holder != null) holder.update(player);
                }
            }
            List<ShopHolo> shopHolograms = a.getShopHolograms(iso);
            if (shopHolograms != null) {
                for (ShopHolo sh : shopHolograms) {
                    if (sh != null) {
                        sh.update(player);
                    }
                }
            }

            a.sendSpectatorCommandItems(player);
            return;
        }
        ITeam t = a.getTeam(player);
        if (t == null) {
            e.setRespawnLocation(a.getReSpawnLocation());
            plugin.getLogger().severe(e.getPlayer().getName() + " re-spawn error on " + a.getArenaName() + "[" + a.getWorldName() + "] because the team was NULL and he was not spectating!");
            plugin.getLogger().severe("This is caused by one of your plugins: remove or configure any re-spawn related plugins.");
            a.removePlayer(player, false);
            a.removeSpectator(player, false);
            return;
        }
        if (t.isBedDestroyed()) {
            Location spectateLoc = deathSpectateLocations.remove(player.getUniqueId());
            if (spectateLoc == null) spectateLoc = a.getSpectatorLocation();
            e.setRespawnLocation(spectateLoc);
            a.addSpectator(player, true, spectateLoc);
            t.getMembers().remove(player);
            player.sendMessage(getMsg(player, Messages.PLAYER_DIE_ELIMINATED_CHAT));
            if (t.getMembers().isEmpty()) {
                Bukkit.getPluginManager().callEvent(new TeamEliminatedEvent(a, t));
                for (Player p : a.getWorld().getPlayers()) {
                    p.sendMessage(getMsg(p, Messages.TEAM_ELIMINATED_CHAT).replace("%bw_team_color%", t.getColor().chat().toString()).replace("%bw_team_name%", t.getDisplayName(Language.getPlayerLanguage(p))));
                }
                Bukkit.getScheduler().runTask(plugin, a::checkWinner); //Does not really need to be async but since intensive better safe than sorry
            }
        } else {
            //respawn session
            int respawnTime = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_RE_SPAWN_COUNTDOWN);
            if (respawnTime > 1) {
                e.setRespawnLocation(a.getReSpawnLocation());
                a.startReSpawnSession(player, respawnTime);
            } else {
                // instant respawn configuration
                e.setRespawnLocation(t.getSpawn());
                t.respawnMember(player);
            }
        }

        deathSpectateLocations.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        deathSpectateLocations.remove(event.getPlayer().getUniqueId());
        AssistTracker.clearPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (Arena.isInArena(player)) {
            IArena a = Arena.getArenaByPlayer(player);
            if (e.getFrom().getChunk() != e.getTo().getChunk()) {
                /* update armor-stands hidden by nms */
                // hide armor for those with invisibility potions
                if (!a.getShowTime().isEmpty()) {
                    // generic hide packets
                    for (Map.Entry<Player, Integer> entry : a.getShowTime().entrySet()) {
                        if (entry.getValue() > 1) {
                            if (!a.getTeam(entry.getKey()).equals(a.getTeam(player))) {
                                BedWars.nms.hideArmor(entry.getKey(), player);
                            }
                        }
                    }
                    // if the moving player has invisible armor
                    if (a.getShowTime().containsKey(player)) {
                        for (Player p : a.getPlayers()) {
                            if (a.getTeam(player).equals(a.getTeam(p))) continue;
                            BedWars.nms.hideArmor(player, p);
                        }
                    }
                    /* hide players from spectators */
                    if (a.getShowTime().containsKey(player)) {
                        for (Player p : a.getSpectators()) {
                            BedWars.nms.hideArmor(player, p);
                        }
                    }
                }
            }

            if (a.isSpectator(player) || a.isReSpawning(player)) {
                if (e.getTo().getY() < 0) {
                    PaperSupport.teleportC(player, a.isSpectator(player) ? a.getSpectatorLocation() : a.getReSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    // how to remove fall velocity?
                }
            } else {
                if (a.getStatus() == GameState.playing) {
                    if (player.getLocation().getBlockY() <= a.getYKillHeight()) {
                        BedWars.nms.voidKill(player);
                    }
                    if (e.getFrom() != e.getTo()) {
                        Arena.afkCheck.remove(player.getUniqueId());
                        BedWars.getAPI().getAFKUtil().setPlayerAFK(player, false);
                    }
                } else {
                    if (player.getLocation().getBlockY() <= 0) {
                        ITeam bwt = a.getTeam(player);
                        if (bwt != null) {
                            PaperSupport.teleportC(player, bwt.getSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        } else {
                            PaperSupport.teleportC(player, a.getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                    }
                }
            }
        } else {
            if (BedWars.config.getBoolean(ConfigPath.LOBBY_VOID_TELEPORT_ENABLED) && player.getWorld().getName().equalsIgnoreCase(BedWars.config.getLobbyWorldName()) && BedWars.getServerType() == ServerType.MULTIARENA) {
                if (e.getTo().getY() < BedWars.config.getInt(ConfigPath.LOBBY_VOID_TELEPORT_HEIGHT)) {
                    PaperSupport.teleportC(player, BedWars.config.getConfigLoc("lobbyLoc"), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }
    }

    @EventHandler
    public void onProjHit(ProjectileHitEvent e) {
        Projectile proj = e.getEntity();
        if (!(proj.getShooter() instanceof Player shooter)) return;

        IArena a = Arena.getArenaByPlayer(shooter);
        if (a == null || !a.isPlayer(shooter)) return;

        String utility = (proj instanceof Snowball) ? "silverfish" : "";
        if (!utility.isEmpty()) {
            Location spawn = proj.getLocation().clone();
            if (spawn.getBlock() != null && spawn.getBlock().getType() != Material.AIR) {
                spawn.add(0, 1, 0);
            }
            Vector centered = centerXZ(spawn.toVector());
            spawn.setX(centered.getX());
            spawn.setZ(centered.getZ());
            spawnUtility(utility, spawn, a.getTeam(shooter), shooter);
        }
    }

    @EventHandler
    public void onItemFrameDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() != EntityType.ITEM_FRAME) return;

        IArena a = Arena.getArenaByIdentifier(e.getEntity().getWorld().getName());
        if (a != null || (BedWars.getServerType() == ServerType.MULTIARENA && BedWars.getLobbyWorld().equals(e.getEntity().getWorld().getName()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (Arena.getArenaByIdentifier(e.getEntity().getLocation().getWorld().getName()) != null) {
            if (e.getEntityType() == EntityType.IRON_GOLEM || e.getEntityType() == EntityType.SILVERFISH) {
                BedWars.debug("Clearing Drops");
                e.getDrops().clear();
                e.setDroppedExp(0);
            }
        }
        BedWars.nms.getDespawnablesList().remove(e.getEntity().getUniqueId());
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == BedWars.nms.materialCake()) {
            if (Arena.getArenaByIdentifier(e.getPlayer().getWorld().getName()) != null) e.setCancelled(true);
        }
    }
}
