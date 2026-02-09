package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.gameplay.PopUpTowerPlaceEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerBedBreakEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.region.Region;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.configuration.Sounds;
import com.tomkeuper.bedwars.listeners.offline.OfflineGraceService;
import com.tomkeuper.bedwars.popuptower.TowerEast;
import com.tomkeuper.bedwars.popuptower.TowerNorth;
import com.tomkeuper.bedwars.popuptower.TowerSouth;
import com.tomkeuper.bedwars.popuptower.TowerWest;
import com.tomkeuper.bedwars.support.paper.PaperSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.tomkeuper.bedwars.BedWars.*;
import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public class PlayerBlockListeners implements Listener {

    private static final List<Player> buildSession = new ArrayList<>();
    private static final BlockFace[] FACES = {
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST,
            BlockFace.UP, BlockFace.DOWN
    };
    private final boolean allowFireBreak;

    public PlayerBlockListeners() {
        allowFireBreak = config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ALLOW_FIRE_EXTINGUISH);
    }

    public static boolean isBuildSession(Player p) {
        return buildSession.contains(p);
    }

    public static void addBuildSession(Player p) {
        buildSession.add(p);
    }

    public static void removeBuildSession(Player p) {
        buildSession.remove(p);
    }

    @EventHandler
    public void onIceMelt(BlockFadeEvent e) {
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (Objects.requireNonNull(e.getBlock().getLocation().getWorld()).getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
                e.setCancelled(true);
                return;
            }
        }
        if (e.getBlock().getType() == Material.ICE) {
            if (Arena.getArenaByIdentifier(e.getBlock().getWorld().getName()) != null) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCactus(BlockPhysicsEvent e) {
        if (e.getBlock().getType() == Material.CACTUS) {
            if (Arena.getArenaByIdentifier(e.getBlock().getWorld().getName()) != null) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWaterFlow(BlockFromToEvent e) {
        IArena arena = Arena.getArenaByIdentifier(e.getBlock().getWorld().getName());
        if (arena == null) return;
        if (!e.getBlock().getType().toString().equalsIgnoreCase("WATER")) return;
        if (e.getToBlock().isEmpty()) return;

        for (Region region : arena.getRegionsList()) {
            if (region.isInRegion(e.getToBlock().getLocation()) && region.isProtected()) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(@NotNull BlockBurnEvent event) {
        IArena arena = Arena.getArenaByIdentifier(event.getBlock().getWorld().getName());
        if (arena == null) return;
        if (!arena.isAllowMapBreak()) {
            event.setCancelled(true);
            return;
        }
        if (arena.isTeamBed(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;

        //Prevent player from placing during the removal from the arena
        IArena arena = Arena.getArenaByIdentifier(e.getBlock().getWorld().getName());
        if (arena != null) {
            if (arena.getStatus() != GameState.playing) {
                e.setCancelled(true);
                return;
            }
            if (e.getItemInHand().getType().equals(nms.materialFireball()) && e.getBlockPlaced().getType().equals(Material.FIRE)) {
                e.setCancelled(true);
            }
        }
        Player p = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(p);
        if (a != null) {
            if (a.isSpectator(p)) {
                e.setCancelled(true);
                return;
            }
            if (a.getRespawnSessions().containsKey(p)) {
                e.setCancelled(true);
                return;
            }
            if (a.getStatus() != GameState.playing) {
                e.setCancelled(true);
                return;
            }
            if (e.getBlockPlaced().getLocation().getBlockY() >= a.getConfig().getInt(ConfigPath.ARENA_CONFIGURATION_MAX_BUILD_Y)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(getMsg(e.getPlayer(), Messages.ARENA_MAX_BUILD_LIMIT_REACHED));
                return;
            }
            if (e.getBlockPlaced().getLocation().getBlockY() <= a.getConfig().getInt(ConfigPath.ARENA_CONFIGURATION_MIN_BUILD_Y)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(getMsg(e.getPlayer(), Messages.ARENA_MIN_BUILD_LIMIT_REACHED));
                return;
            }

            for (Region r : a.getRegionsList()) {
                if (r.isInRegion(e.getBlock().getLocation()) && r.isProtected()) {
                    e.setCancelled(true);
                    p.sendMessage(getMsg(p, Messages.INTERACT_CANNOT_PLACE_BLOCK));
                    return;
                }
            }

            // prevent modifying wood if protected
            // issue #531
            if (e.getBlockPlaced().getType().toString().contains("STRIPPED_") && e.getBlock().getType().toString().contains("_WOOD")) {
                if (null != arena && !arena.isAllowMapBreak()) {
                    e.setCancelled(true);
                    return;
                }
            }

            a.addPlacedBlock(e.getBlock());
            if (e.getBlock().getType() == Material.TNT) {
                if (config.getBoolean(ConfigPath.GENERAL_TNT_AUTO_IGNITE)) {
                    e.getBlockPlaced().setType(Material.AIR);
                    TNTPrimed tnt = Objects.requireNonNull(e.getBlock().getLocation().getWorld()).spawn(e.getBlock().getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
                    tnt.setFuseTicks(config.getInt(ConfigPath.GENERAL_TNT_FUSE_TICKS));
                    nms.setSource(tnt, p);
                    return;
                }
            } else if (BedWars.shop.getBoolean(ConfigPath.SHOP_SPECIAL_TOWER_ENABLE)) {
                if (e.getBlock().getType() == Material.valueOf(shop.getString(ConfigPath.SHOP_SPECIAL_TOWER_MATERIAL))) {

                    e.setCancelled(true);
                    Location loc = e.getBlock().getLocation();
                    IArena a1 = Arena.getArenaByPlayer(p);
                    TeamColor col = a1.getTeam(p).getColor();
                    Block block = loc.getWorld().getBlockAt(loc.clone().subtract(0, -1, 0));

                    PopUpTowerPlaceEvent event = new PopUpTowerPlaceEvent(p, loc, block, a1);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) return;

                    double rotation = (p.getLocation().getYaw() - 90.0F) % 360.0F;
                    if (rotation < 0.0D) {
                        rotation += 360.0D;
                    }
                    if (45.0D <= rotation && rotation < 135.0D) {
                        new TowerSouth(event.getLocation(), e.getBlockPlaced(), col, p);
                    } else if (225.0D <= rotation && rotation < 315.0D) {
                        new TowerNorth(event.getLocation(), e.getBlockPlaced(), col, p);
                    } else if (135.0D <= rotation && rotation < 225.0D) {
                        new TowerWest(event.getLocation(), e.getBlockPlaced(), col, p);
                    } else if (0.0D <= rotation && rotation < 45.0D) {
                        new TowerEast(event.getLocation(), e.getBlockPlaced(), col, p);
                    } else if (315.0D <= rotation && rotation < 360.0D) {
                        new TowerEast(event.getLocation(), e.getBlockPlaced(), col, p);
                    }
                }
            }
            return;
        }
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (Objects.requireNonNull(e.getBlock().getLocation().getWorld()).getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
                if (!isBuildSession(p)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (BedWars.getServerType() == ServerType.MULTIARENA
                && player.getWorld().getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getRelative(BlockFace.UP).getType() == Material.FIRE) {
                if (!isBuildSession(player)) {
                    event.setCancelled(true);
                    //return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakMonitor(BlockBreakEvent event) {
        IArena a = Arena.getArenaByPlayer(event.getPlayer());
        if (a != null) {
            a.removePlacedBlock(event.getBlock());
        }
    }

    @EventHandler
    public void onBlockDrop(ItemSpawnEvent event) {
        //WHEAT_SEEDS AND BEDs
        IArena arena = Arena.getArenaByIdentifier(event.getEntity().getWorld().getName());
        if (arena == null) return;
        Material material = event.getEntity().getItemStack().getType();
        if (nms.isBed(material) || material.toString().equalsIgnoreCase("SEEDS") || material.toString().equalsIgnoreCase("WHEAT_SEEDS")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        final Player player = e.getPlayer();
        final Block block = e.getBlock();

        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            World world = block.getWorld();
            if (world != null && world.getName().equalsIgnoreCase(BedWars.getLobbyWorld()) && !isBuildSession(player)) {
                e.setCancelled(true);
                return;
            }
        }

        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) return;

        if (!arena.isPlayer(player) || arena.getRespawnSessions().containsKey(player) || arena.getStatus() != GameState.playing) {
            e.setCancelled(true);
            return;
        }

        if (isSoftBreak(block.getType())) return;
        if (block.getType() == Material.FIRE && allowFireBreak) return;

        if (BedWars.nms.isBed(block.getType())) {
            ITeam bedTeam = arena.getBedsTeam(block.getLocation());
            if (bedTeam != null && !bedTeam.isBedDestroyed()) {
                if (bedTeam.isMember(player)) {
                    player.sendMessage(Language.getMsg(player, Messages.INTERACT_CANNOT_BREAK_OWN_BED));
                    e.setCancelled(true);

                    if (player.getLocation().getBlock().getType().toString().contains("BED")) {
                        PaperSupport.teleport(player, player.getLocation().add(0, 0.5, 0));
                    }
                    return;
                }

                e.setCancelled(false);
                bedTeam.setBedDestroyed(true);
                bedTeam.setBedDestroyer(player);
                arena.addPlayerBedDestroyed(player);

                PlayerBedBreakEvent breakEvent = new PlayerBedBreakEvent(
                        player, arena.getTeam(player), bedTeam, arena,
                        viewer -> bedTeam.isMember(viewer)
                                ? Language.getMsg(viewer, Messages.INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT_TO_VICTIM)
                                : Language.getMsg(viewer, Messages.INTERACT_BED_DESTROY_CHAT_ANNOUNCEMENT),
                        viewer -> bedTeam.isMember(viewer)
                                ? Language.getMsg(viewer, Messages.INTERACT_BED_DESTROY_TITLE_ANNOUNCEMENT)
                                : null,
                        viewer -> bedTeam.isMember(viewer)
                                ? Language.getMsg(viewer, Messages.INTERACT_BED_DESTROY_SUBTITLE_ANNOUNCEMENT)
                                : null
                );
                Bukkit.getPluginManager().callEvent(breakEvent);

                OfflineGraceService.onBedBroken(arena, bedTeam, player);

                for (Player viewer : arena.getWorld().getPlayers()) {
                    if (breakEvent.getMessage() != null) {
                        Language lang = Language.getPlayerLanguage(viewer);
                        viewer.sendMessage(breakEvent.getMessage().apply(viewer)
                                .replace("%bw_team_color%", bedTeam.getColor().chat().toString())
                                .replace("%bw_team_name%", bedTeam.getDisplayName(lang))
                                .replace("%bw_player_color%", arena.getTeam(player).getColor().chat().toString())
                                .replace("%bw_player%", Misc.getPlayerName(player))
                                .replace("%bw_playername%", player.getName()));
                    }

                    if (breakEvent.getTitle() != null && breakEvent.getSubTitle() != null) {
                        BedWars.nms.sendTitle(viewer, breakEvent.getTitle().apply(viewer),
                                breakEvent.getSubTitle().apply(viewer), 0, 40, 10);
                    }

                    if (bedTeam.isMember(viewer)) {
                        Sounds.playSound(ConfigPath.SOUNDS_BED_DESTROY_OWN, viewer);
                    } else {
                        Sounds.playSound(ConfigPath.SOUNDS_BED_DESTROY, viewer);
                    }
                }
                return;
            }
        }

        for (Region r : arena.getRegionsList()) {
            if (r.isInRegion(block.getLocation()) && r.isProtected()) {
                e.setCancelled(true);
                player.sendMessage(Language.getMsg(player, Messages.INTERACT_CANNOT_BREAK_BLOCK));
                return;
            }
        }

        if (!arena.isMapBreakable() && !arena.isBlockPlaced(block)) {
            e.setCancelled(true);
            player.sendMessage(Language.getMsg(player, Messages.INTERACT_CANNOT_BREAK_BLOCK));
        }
    }

    /**
     * update game signs
     */
    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (e == null) return;
        Player p = e.getPlayer();
        if (Objects.requireNonNull(e.getLine(0)).equalsIgnoreCase("[" + mainCmd + "]")) {
            File dir = new File(plugin.getDataFolder(), "/Arenas");
            boolean exists = false;
            if (dir.exists()) {
                for (File f : Objects.requireNonNull(dir.listFiles())) {
                    if (f.isFile()) {
                        if (f.getName().contains(".yml")) {
                            if (Objects.equals(e.getLine(1), f.getName().replace(".yml", ""))) {
                                exists = true;
                            }
                        }
                    }
                }
                List<String> s;
                if (signs.getYml().get("locations") == null) {
                    s = new ArrayList<>();
                } else {
                    s = new ArrayList<>(signs.getYml().getStringList("locations"));
                }
                if (exists) {
                    s.add(e.getLine(1) + "," + signs.stringLocationConfigFormat(e.getBlock().getLocation()));
                    signs.set("locations", s);
                }
                IArena a = Arena.getArenaByName(e.getLine(1));
                if (a != null) {
                    p.sendMessage("§a▪ §7Sign saved for arena: " + e.getLine(1));
                    a.addSign(e.getBlock().getLocation());
                    Sign b = (Sign) e.getBlock().getState();
                    int line = 0;
                    for (String string : BedWars.signs.getList("format")) {
                        e.setLine(line, string.replace("[on]", String.valueOf(a.getPlayers().size())).replace("[max]",
                                        String.valueOf(a.getMaxPlayers())).replace("[arena]", a.getDisplayName()).replace("[status]", a.getDisplayStatus(Language.getDefaultLanguage()))
                                .replace("[type]", String.valueOf(a.getMaxInTeam())));
                        line++;
                    }
                    b.update(true);
                }
            } else {
                p.sendMessage("§c▪ §7You didn't set any arena yet!");
            }
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (e.isCancelled()) return;
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (Objects.requireNonNull(e.getPlayer().getLocation().getWorld()).getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
                if (!isBuildSession(e.getPlayer())) {
                    e.setCancelled(true);
                }
            }
        }
        IArena a = Arena.getArenaByPlayer(e.getPlayer());
        if (a != null) {
            if (a.isSpectator(e.getPlayer()) || a.getStatus() != GameState.playing || a.getRespawnSessions().containsKey(e.getPlayer()))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) return;
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (Objects.requireNonNull(e.getPlayer().getLocation().getWorld()).getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
                if (!isBuildSession(e.getPlayer())) {
                    e.setCancelled(true);
                }
            }
        }
        //Prevent player from placing during the removal from the arena
        IArena arena = Arena.getArenaByIdentifier(e.getBlockClicked().getWorld().getName());
        if (arena != null) {
            if (arena.getStatus() != GameState.playing) {
                e.setCancelled(true);
                return;
            }
        }
        Player p = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(p);
        if (a != null) {
            if (a.isSpectator(p)) {
                e.setCancelled(true);
                return;
            }
            if (a.getRespawnSessions().containsKey(p)) {
                e.setCancelled(true);
                return;
            }
            if (a.getStatus() != GameState.playing) {
                e.setCancelled(true);
                return;
            }
            if (e.getBlockClicked().getRelative(e.getBlockFace()).getLocation().getBlockY() >= a.getConfig().getInt(ConfigPath.ARENA_CONFIGURATION_MAX_BUILD_Y)) {
                p.sendMessage(Language.getMsg(p, Messages.ARENA_MAX_BUILD_LIMIT_REACHED));
                e.setCancelled(true);
                return;
            }
            if (e.getBlockClicked().getRelative(e.getBlockFace()).getLocation().getBlockY() <= a.getConfig().getInt(ConfigPath.ARENA_CONFIGURATION_MIN_BUILD_Y)) {
                e.setCancelled(true);
                p.sendMessage(getMsg(p, Messages.ARENA_MIN_BUILD_LIMIT_REACHED));
            }

            for (Region r : a.getRegionsList()) {
                if (r.isInRegion(e.getBlockClicked().getRelative(e.getBlockFace()).getLocation()) && r.isProtected()) {
                    e.setCancelled(true);
                    p.sendMessage(getMsg(p, Messages.INTERACT_CANNOT_PLACE_BLOCK));
                    return;
                }
            }
            /* Remove empty bucket */
            Bukkit.getScheduler().runTaskLater(plugin, () -> nms.minusAmount(e.getPlayer(), e.getItemStack(), 1), 3L);
        }
    }

    @EventHandler
    public void onBlow(@NotNull EntityExplodeEvent e) {
        if (e.isCancelled()) return;
        if (e.blockList().isEmpty()) return;

        IArena a = Arena.getArenaByIdentifier(e.getLocation().getWorld().getName());
        if (a != null) {
            if (a.getStatus() == GameState.playing) {
                if (e.getEntity().getType() == EntityType.ENDER_DRAGON && a.isAllowEnderDragonDestroy()) {
                    return;
                }
                Location explosionSource = e.getLocation(); // The block where the explosion started
                e.blockList().removeIf(b ->
                        a.isProtected(b.getLocation()) ||
                                a.isTeamBed(b.getLocation()) ||
                                (!a.isBlockPlaced(b) && !a.isAllowMapBreak()) ||
                                isProtectedByBlastResistanceClose(b, explosionSource) ||
                                isProtectedByBlastResistance(b, explosionSource)
                );
            }
        }
    }

    @EventHandler
    public void onBlockExplode(@NotNull BlockExplodeEvent e) {
        if (e.isCancelled()) return;
        if (e.blockList().isEmpty()) return;

        IArena a = Arena.getArenaByIdentifier(e.blockList().get(0).getWorld().getName());
        if (a != null && a.getStatus() == GameState.playing) {
            Location explosionSource = e.getBlock().getLocation(); // The block where the explosion started
            e.blockList().removeIf(b ->
                    a.isProtected(b.getLocation()) ||
                            a.isTeamBed(b.getLocation()) ||
                            (!a.isBlockPlaced(b) && !a.isAllowMapBreak()) ||
                            isProtectedByBlastResistanceClose(b, explosionSource) ||
                            isProtectedByBlastResistance(b, explosionSource)
            );
        }
    }

    private boolean isProtectedByBlastResistance(Block targetBlock, Location explosionLoc) {
        // Add offset to location vectors, prevent ray from going between blocks
        Vector explosionVec = explosionLoc.toVector().add(new Vector(0.15, 0.15, 0.1));
        Vector targetCenter = targetBlock.getLocation().toVector().add(new Vector(0.2, 0.1, 0.25));

        // Direction vector from offset explosion location to target center
        Vector direction = targetCenter.clone().subtract(explosionVec);
        double distance = direction.length();
        direction.normalize();

        Vector current = explosionVec.clone();
        // Step size: smaller = more accurate but more CPU
        double step = 0.1;

        for (double d = 0; d < distance; d += step) {
            current.add(direction.clone().multiply(step));

            Block b = current.toLocation(explosionLoc.getWorld()).getBlock();

            if (b.getLocation().equals(targetBlock.getLocation())) continue;
            if (b.getType() == Material.AIR) continue;

            float resistance = nms.getBlastResistance(b);
            if (resistance >= 20.0F) {
                return true;
            }
        }
        return false;
    }

    private boolean isProtectedByBlastResistanceClose(Block target, Location explosionLocation) {
        // Get direction from explosion source to target
        Vector direction = explosionLocation.toVector().subtract(target.getLocation().toVector()).normalize();

        // Get the block on the face where the explosion hits
        BlockFace face = getClosestFace(direction);
        Block adjacent = target.getRelative(face);

        // Define a blast resistance threshold (e.g., > 20 is strong)
        return adjacent.getType().isSolid() && nms.getBlastResistance(adjacent) > 20;
    }

    private BlockFace getClosestFace(Vector direction) {
        BlockFace closest = BlockFace.NORTH;
        double highestDot = -Double.MAX_VALUE;

        for (BlockFace face : FACES) {
            Vector faceVec = new Vector(face.getModX(), face.getModY(), face.getModZ());
            double dot = faceVec.normalize().dot(direction);
            if (dot > highestDot) {
                highestDot = dot;
                closest = face;
            }
        }
        return closest;
    }

    @EventHandler
    public void onPaintingRemove(HangingBreakByEntityEvent e) {
        IArena a = Arena.getArenaByIdentifier(e.getEntity().getWorld().getName());
        if (a == null) {
            if (BedWars.getServerType() == ServerType.SHARED) return;
            if (!BedWars.getLobbyWorld().equals(e.getEntity().getWorld().getName())) return;
        }
        if (e.getEntity().getType() == EntityType.PAINTING || e.getEntity().getType() == EntityType.ITEM_FRAME) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockCanBuildEvent(BlockCanBuildEvent e) {
        if (e.isBuildable()) return;
        IArena a = Arena.getArenaByIdentifier(e.getBlock().getWorld().getName());
        if (a != null) {
            boolean bed = false;
            for (ITeam t : a.getTeams()) {
                for (int x = e.getBlock().getX() - 1; x < e.getBlock().getX() + 1; x++) {
                    for (int z = e.getBlock().getZ() - 1; z < e.getBlock().getZ() + 1; z++) {

                        //Check bed block
                        if (t.getBed().getBlockX() == x && t.getBed().getBlockY() == e.getBlock().getY() && t.getBed().getBlockZ() == z) {
                            e.setBuildable(false);
                            bed = true;
                            break;
                        }
                    }
                }
                //Check bed hologram
                if (t.getBed().getBlockX() == e.getBlock().getX() && t.getBed().getBlockY() + 1 == e.getBlock().getY() && t.getBed().getBlockZ() == e.getBlock().getZ()) {
                    if (!bed) {
                        e.setBuildable(true);
                        break;
                    }
                }
            }
        }
    }

    //prevent farm breaking farm stuff
    @EventHandler
    public void soilChangeEntity(EntityChangeBlockEvent e) {
        if (e.getTo() == Material.DIRT) {
            if (e.getBlock().getType().toString().equals("FARMLAND") || e.getBlock().getType().toString().equals("SOIL")) {
                if ((Arena.getArenaByIdentifier(e.getBlock().getWorld().getName()) != null) || (e.getBlock().getWorld().getName().equals(BedWars.getLobbyWorld())))
                    e.setCancelled(true);
            }
        }
    }

    private boolean isSoftBreak(Material m) {
        String name = m.name();
        return m == Material.BROWN_MUSHROOM
                || m == Material.RED_MUSHROOM
                || name.equals("LONG_GRASS")
                || name.equals("TALL_GRASS")
                || name.equals("SEAGRASS")
                || name.equals("TALL_SEAGRASS")
                || name.equals("DOUBLE_PLANT")
                || name.equals("GRASS_PATH")
                || name.equals("SUGAR_CANE")
                || name.equals("SUGAR_CANE_BLOCK")
                || name.equalsIgnoreCase("RED_MUSHROOM")
                || name.equalsIgnoreCase("BROWN_MUSHROOM");
    }
}
