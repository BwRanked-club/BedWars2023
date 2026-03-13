package com.tomkeuper.bedwars.api.server;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.generator.IGeneratorAnimation;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.api.entity.Despawnable;
import com.tomkeuper.bedwars.api.entity.GeneratorHolder;
import com.tomkeuper.bedwars.api.exceptions.InvalidEffectException;
import com.tomkeuper.bedwars.api.hologram.containers.IHoloLine;
import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.arena.shop.ShopHolo;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class VersionSupport {

    private static final ConcurrentHashMap<UUID, Despawnable> despawnables = new ConcurrentHashMap<>();
    public static String PLUGIN_TAG_GENERIC_KEY = "BedWars2023";
    public static String PLUGIN_TAG_TIER_KEY = "tierIdentifier";
    private static String name2;
    private final Plugin plugin;
    private Effect eggBridge;

    public VersionSupport(Plugin plugin, String versionName) {
        name2 = versionName;
        this.plugin = plugin;
    }

    public static String getName() {
        return name2;
    }

    protected void loadDefaultEffects() {
        try {
            setEggBridgeEffect("MOBSPAWNER_FLAMES");
        } catch (InvalidEffectException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a new command as bukkit command
     */
    public abstract void registerCommand(String name, Command cmd);

    /**
     * Send title, subtitle. null for empty
     */
    public abstract void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    /**
     * Send action-bar message
     */
    public abstract void playAction(Player p, String text);

    /**
     * Check if bukkit command is registered
     */
    public abstract boolean isBukkitCommandRegistered(String command);

    /**
     * Get in had item-stack
     */
    public abstract ItemStack getItemInHand(Player p);

    /**
     * Hide an entity
     */
    public abstract void hideEntity(Entity e, Player p);

    /**
     * Legacy hook kept for old version-support modules.
     */
    public void fakeDamagePlayer(Player player) {
    }

    /**
     * Check if item-stack is armor
     */
    public abstract boolean isArmor(ItemStack itemStack);

    /**
     * Check if item-stack is a tool
     */
    public abstract boolean isTool(ItemStack itemStack);

    /**
     * Check if item-stack is sword
     */
    public abstract boolean isSword(ItemStack itemStack);

    /**
     * Check if item-stack is axe
     */
    public abstract boolean isAxe(ItemStack itemStack);

    /**
     * Check if item-stack is bow
     */
    public abstract boolean isBow(ItemStack itemStack);

    /**
     * Check if itemstack is Projectile
     */
    public abstract boolean isProjectile(ItemStack itemStack);

    /**
     * Check if itemstack is Invisibility Potion
     */
    public abstract boolean isInvisibilityPotion(ItemStack itemStack);

    /**
     * Check if type is a Glass type material
     */
    public boolean isGlass(Material type) {
        return type != Material.AIR && (type == Material.GLASS || type.toString().contains("_GLASS"));
    }

    /**
     * Register custom entities
     */
    public abstract void registerEntities();

    /**
     * Spawn shop NPC
     */
    public void spawnShop(Location loc, String name1, List<Player> players, IArena arena) {
        spawnShop(loc, name1, (Iterable<Player>) players, arena);
    }

    /**
     * Legacy shop NPC hook kept for old version-support modules.
     */
    @Deprecated
    public void spawnShop(Location loc, String name1, Iterable<Player> players, IArena arena) {
        throw new UnsupportedOperationException("Legacy spawnShop hook must be implemented by the version support module.");
    }

    /**
     * Spawn shop hologram
     */
    public void spawnShopHologram(Location loc, String name1, List<Player> players, IArena arena, ITeam team) {
        if (loc == null || players == null || players.isEmpty() || arena == null) return;

        Map<String, List<Player>> playersByIso = new HashMap<>();
        for (Player player : players) {
            if (player == null || !player.isOnline()) continue;

            playersByIso.computeIfAbsent(Language.getPlayerLanguage(player).getIso(), key -> new ArrayList<>()).add(player);
        }

        for (Map.Entry<String, List<Player>> entry : playersByIso.entrySet()) {
            List<Player> localizedPlayers = entry.getValue();
            if (localizedPlayers.isEmpty()) continue;

            Player sample = localizedPlayers.get(0);
            List<String> lines = Language.getList(sample, name1);
            if (lines == null || lines.isEmpty()) {
                lines = Language.getList(sample, name1.replace(name1.split("\\.")[2], "default"));
            }
            if (lines == null || lines.isEmpty()) continue;

            ShopHolo existing = arena.findShopHologram(entry.getKey(), team, loc);
            if (existing != null) {
                for (Player localizedPlayer : localizedPlayers) {
                    existing.getHologram().addPlayer(localizedPlayer);
                }
                existing.update();
                continue;
            }

            IHologram hologram = createHologram(localizedPlayers, loc, lines.toArray(new String[0]));
            ShopHolo shopHolo = new ShopHolo(hologram, arena, team);
            arena.registerShopHologram(entry.getKey(), shopHolo);
            shopHolo.update();
        }
    }

    /**
     * Legacy shop hologram hook kept for source compatibility with old modules.
     */
    @Deprecated
    public void spawnShopHologram(Location loc, String name1, Iterable<Player> players, ITeam team) {
        throw new UnsupportedOperationException("Legacy spawnShopHologram hook is no longer supported without an arena context.");
    }

    /**
     * Get item-stack damage amount
     */
    public abstract double getDamage(ItemStack i);

    /**
     * Spawn silverfish for a team
     */
    public abstract void spawnSilverfish(Location loc, ITeam team, double speed, double health, int despawn, double damage, int pathFindingTicks);

    /**
     * Spawn a iron-golem for a team
     */
    public abstract void spawnIronGolem(Location loc, ITeam team, double speed, double health, int despawn, int pathFindingTicks);

    /**
     * Is despawnable entity
     */
    public boolean isDespawnable(Entity e) {
        if (e == null) return false;
        return despawnables.get(e.getUniqueId()) != null;
    }

    /**
     * Change item amount
     */
    public abstract void minusAmount(Player p, ItemStack i, int amount);

    /**
     * Set tnt source
     */
    public abstract void setSource(TNTPrimed tnt, Player owner);

    /**
     * Void damage with cause
     */
    public abstract void voidKill(Player p);

    /**
     * Hide player armor to a player
     */
    public abstract void hideArmor(Player victim, Player receiver);

    /**
     * Show a player armor
     */
    public abstract void showArmor(Player victim, Player receiver);

    /**
     * Spawn ender dragon
     */
    public abstract EnderDragon spawnDragon(Location l, ITeam team);

    /**
     * Color a bed 1.12+
     */
    public abstract void colorBed(ITeam team);

    /**
     * Modify and register block blast resistance.
     */
    public abstract void registerTntWhitelist(float endStoneBlast, float glassBlast);

    /**
     * Get blast resistance of a block.
     * This will return the default blast resistance if not modified.
     *
     * @param block the block to get blast resistance for
     * @return the blast resistance of the block
     */
    public abstract float getBlastResistance(Block block);

    /**
     * Egg bridge particles
     */
    public Effect eggBridge() {
        return eggBridge;
    }

    @SuppressWarnings("WeakerAccess")
    public void setEggBridgeEffect(String eggBridge) throws InvalidEffectException {
        try {
            this.eggBridge = Effect.valueOf(eggBridge);
        } catch (Exception e) {
            throw new InvalidEffectException(eggBridge);
        }
    }

    /**
     * Set block data
     * For 1.13 support
     */
    public abstract void setBlockTeamColor(Block block, TeamColor teamColor);

    /**
     * Disable collisions in 1.9+
     */
    public abstract void setCollide(Player p, IArena a, boolean value);

    /**
     * Add custom data to an ItemStack
     */
    public abstract ItemStack addCustomData(ItemStack i, String data);

    public abstract ItemStack setTag(ItemStack itemStack, String key, String value);

    /**
     * Get a custom item tag.
     *
     * @return null if not present.
     */
    @SuppressWarnings("unused")
    public abstract String getTag(ItemStack itemStack, String key);

    /**
     * Check if an item has a BedWars2023 NBTTag
     */
    public abstract boolean isCustomBedWarsItem(ItemStack i);

    /**
     * Get the NBTTag from a BedWars2023 item
     */
    public abstract String getCustomData(ItemStack i);

    /**
     * Color an item if possible with the team's color
     */
    public abstract ItemStack colourItem(ItemStack itemStack, ITeam bedWarsTeam);

    public abstract ItemStack createItemStack(String material, int amount, short data);

    /**
     * Check if is a player head
     */
    public boolean isPlayerHead(String material, int data) {
        return material.equalsIgnoreCase("PLAYER_HEAD") || (material.equalsIgnoreCase("SKULL_ITEM") && data == 3);
    }

    /**
     * Get fireball material
     */
    public abstract Material materialFireball();

    /**
     * Player head material
     */
    public abstract Material materialPlayerHead();

    /**
     * Get snowball material
     */
    public abstract Material materialSnowball();

    /**
     * Get gold  helmet material
     */
    public abstract Material materialGoldenHelmet();

    /**
     * Get gold chest plate
     */
    public abstract Material materialGoldenChestPlate();

    /**
     * Get gold leggings
     */
    public abstract Material materialGoldenLeggings();

    /**
     * Get netherite  helmet material
     */
    public abstract Material materialNetheriteHelmet();

    /**
     * Get netherite chest plate
     */
    public abstract Material materialNetheriteChestPlate();

    /**
     * Get netherite leggings
     */
    public abstract Material materialNetheriteLeggings();

    /**
     * Get elytra - supports: 1.12.2+
     */
    public abstract Material materialElytra();

    /**
     * Cake material
     */
    public abstract Material materialCake();

    /**
     * Crafting table material
     */
    public abstract Material materialCraftingTable();

    /**
     * Enchanting table material
     */
    public abstract Material materialEnchantingTable();

    public abstract Material materialEndStone();

    /**
     * Check if bed
     */
    public boolean isBed(Material material) {
        return material.toString().contains("_BED");
    }

    /**
     * Item Data compare
     * This will always return true on versions major or equal 1.13
     */
    public boolean itemStackDataCompare(ItemStack i, short data) {
        return true;
    }

    /**
     * Set block data
     * For versions before 1.13
     */
    public void setJoinSignBackgroundBlockData(BlockState b, byte data) {

    }

    /**
     * Change the block behind the join sign.
     */
    public abstract void setJoinSignBackground(BlockState b, Material material);

    /**
     * Wool material
     */
    public abstract Material woolMaterial();

    /**
     * Red glass pane item stack
     *
     * @param amount the amount of the stack
     * @return the itemStack
     */
    public abstract ItemStack redGlassPane(int amount);

    /**
     * Green glass pane item stack
     *
     * @param amount the amount of the stack
     * @return the itemStack
     */
    public abstract ItemStack greenGlassPane(int amount);

    /**
     * Get an ItemStack identifier
     * will return null text if it does not have an identifier
     */
    public abstract String getShopUpgradeIdentifier(ItemStack itemStack);

    /**
     * Set an upgrade identifier
     */
    public abstract ItemStack setShopUpgradeIdentifier(ItemStack itemStack, String identifier);

    /**
     * Get player head with skin.
     *
     * @param copyTagFrom will copy nbt tag from this item.
     */
    public abstract ItemStack getPlayerHead(Player player, @Nullable ItemStack copyTagFrom);

    /**
     * This will send the player spawn packet after a player re-spawn.
     * <p>
     * Show the target player to players and spectators in the arena.
     */
    public abstract void sendPlayerSpawnPackets(Player player, IArena arena);

    /**
     * Get inventory name.
     */
    public abstract String getInventoryName(InventoryEvent e);

    /**
     * Make item unbreakable.
     */
    public abstract void setUnbreakable(ItemMeta itemMeta);

    /**
     * Get list of entities that are going to despawn based on a timer.
     */
    public ConcurrentHashMap<UUID, Despawnable> getDespawnablesList() {
        return despawnables;
    }

    public abstract int getVersion();

    public Plugin getPlugin() {
        return plugin;
    }

    public abstract void registerVersionListeners();

    /**
     * Get main level name.
     */
    public abstract String getMainLevel();

    public byte getCompressedAngle(float value) {
        return (byte) ((value * 256.0F) / 360.0F);
    }

    public void spigotShowPlayer(Player victim, Player receiver) {
        receiver.showPlayer(victim);
    }

    public void spigotHidePlayer(Player victim, Player receiver) {
        receiver.hidePlayer(victim);
    }

    /**
     * Make fireball go straight.
     *
     * @param fireball fireball instance;
     * @param vector   fireball direction to normalize.
     * @return modified fireball.
     */
    public abstract Fireball setFireballDirection(Fireball fireball, Vector vector);

    public abstract void playRedStoneDot(Player player);

    public abstract void clearArrowsFromPlayerBody(Player player);

    public abstract Block placeTowerBlocks(Block b, IArena a, TeamColor color, int x, int y, int z);

    public abstract Block placeLadder(Block b, int x, int y, int z, IArena a, int ladderdata);

    public abstract void playVillagerEffect(Player player, Location location);

    public abstract IHologram createHologram(Player p, Location location, String... lines);

    public abstract IHologram createHologram(Player p, Location location, IHoloLine... lines);

    public IHologram createHologram(List<Player> players, Location location, String... lines) {
        return createHologram((Iterable<Player>) players, location, lines);
    }

    /**
     * Legacy hologram hook kept for old version-support modules.
     */
    @Deprecated
    public IHologram createHologram(Iterable<Player> players, Location location, String... lines) {
        throw new UnsupportedOperationException("Legacy createHologram hook must be implemented by the version support module.");
    }

    public IHologram createHologram(List<Player> players, Location location, IHoloLine... lines) {
        return createHologram((Iterable<Player>) players, location, lines);
    }

    /**
     * Legacy hologram hook kept for old version-support modules.
     */
    @Deprecated
    public IHologram createHologram(Iterable<Player> players, Location location, IHoloLine... lines) {
        throw new UnsupportedOperationException("Legacy createHologram hook must be implemented by the version support module.");
    }

    public abstract IHoloLine lineFromText(String text, @Nonnull IHologram hologram);

    public abstract IGeneratorAnimation createDefaultGeneratorAnimation(ArmorStand armorStand);

    public void destroyPacketArmorStand(GeneratorHolder generatorHolder, List<Player> players) {
        destroyPacketArmorStand(generatorHolder, (Iterable<Player>) players);
    }

    /**
     * Legacy packet armor stand hook kept for old version-support modules.
     */
    @Deprecated
    public void destroyPacketArmorStand(GeneratorHolder generatorHolder, Iterable<Player> players) {
        throw new UnsupportedOperationException("Legacy destroyPacketArmorStand hook must be implemented by the version support module.");
    }

    public ArmorStand createPacketArmorStand(@Nonnull Location loc, List<Player> players) {
        return createPacketArmorStand(loc, (Iterable<Player>) players);
    }

    /**
     * Legacy packet armor stand hook kept for old version-support modules.
     */
    @Deprecated
    public ArmorStand createPacketArmorStand(@Nonnull Location loc, Iterable<Player> players) {
        throw new UnsupportedOperationException("Legacy createPacketArmorStand hook must be implemented by the version support module.");
    }

    public void updatePacketArmorStand(GeneratorHolder generatorHolder, List<Player> players) {
        updatePacketArmorStand(generatorHolder, (Iterable<Player>) players);
    }

    /**
     * Legacy packet armor stand hook kept for old version-support modules.
     */
    @Deprecated
    public void updatePacketArmorStand(GeneratorHolder generatorHolder, Iterable<Player> players) {
        throw new UnsupportedOperationException("Legacy updatePacketArmorStand hook must be implemented by the version support module.");
    }

    /**
     * Legacy hook kept for old version-support modules.
     */
    public void callPlayerDeathEvent(Player player, List<org.bukkit.inventory.ItemStack> drops, int droppedExp, int newLevel, String deathMessage) {
    }

    public abstract void updatePacketArmorStandEquipment(GeneratorHolder generatorHolder);
}
