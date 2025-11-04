package com.tomkeuper.bedwars.arena;

import com.tomkeuper.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

import static com.tomkeuper.bedwars.BedWars.plugin;

/**
 * Stores a full player snapshot to restore after a game.
 */
class PlayerGoods {

    private static final Map<UUID, PlayerGoods> PLAYER_GOODS = new HashMap<>();

    private UUID uuid;
    private final int level;
    private final int foodLevel;
    private final double health;
    private final double healthScale;
    private final float exp;

    private ItemStack[] inventoryContents;
    private ItemStack[] armorContents;
    private ItemStack[] enderChestContents;
    private List<PotionEffect> potionEffects;

    private final GameMode gameMode;
    private final boolean allowFlight;
    private final boolean flying;
    private final String displayName;
    private final String playerListName;

    PlayerGoods(Player player, boolean prepare) {
        this(player, prepare, false);
    }

    PlayerGoods(Player player, boolean prepare, boolean rejoin) {

        // ✅ Caso já exista snapshot, não retorna: apenas inicializa com dados atuais
        if (hasGoods(player)) {
            plugin.getLogger().severe(player.getName() + " already has a PlayerGoods snapshot!");
        }

        // --- Snapshot always initialized (fix for "not initialized") ---
        this.uuid = player.getUniqueId();
        this.level = player.getLevel();
        this.exp = player.getExp();
        this.health = player.getHealth();
        this.healthScale = player.getHealthScale();
        this.foodLevel = player.getFoodLevel();

        this.inventoryContents = cloneArray(player.getInventory().getContents());
        this.armorContents = cloneArray(player.getInventory().getArmorContents());
        this.enderChestContents = rejoin ? null : cloneArray(player.getEnderChest().getContents());

        this.potionEffects = new ArrayList<>(player.getActivePotionEffects());

        this.gameMode = player.getGameMode();
        this.allowFlight = player.getAllowFlight();
        this.flying = player.isFlying();
        this.playerListName = player.getPlayerListName();
        this.displayName = player.getDisplayName();

        PLAYER_GOODS.put(this.uuid, this);

        if (prepare) {
            clearAllActivePotions(player);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            if (!rejoin) player.getEnderChest().clear();

            player.setExp(0f);
            player.setLevel(0);
            player.setHealthScale(20.0);
            player.setHealth(20.0);
            player.setFoodLevel(20);

            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    static boolean hasGoods(Player player) {
        return PLAYER_GOODS.containsKey(player.getUniqueId());
    }

    static PlayerGoods getPlayerGoods(Player player) {
        return PLAYER_GOODS.get(player.getUniqueId());
    }

    void restore() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        PLAYER_GOODS.remove(uuid);

        clearAllActivePotions(player);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.setLevel(level);
        player.setExp(exp);
        player.setHealthScale(healthScale);

        try {
            double max = player.getMaxHealth();
            player.setHealth(Math.min(health, max <= 0 ? 20.0 : max));
        } catch (Exception e) {
            BedWars.plugin.getLogger().severe("Failed restoring health: " + health + " -> using 20.0");
            player.setHealth(20.0);
        }

        player.setFoodLevel(foodLevel);

        if (inventoryContents != null) player.getInventory().setContents(cloneArray(inventoryContents));
        if (armorContents != null) player.getInventory().setArmorContents(cloneArray(armorContents));

        player.getEnderChest().clear();
        if (enderChestContents != null) player.getEnderChest().setContents(cloneArray(enderChestContents));

        if (potionEffects != null) {
            for (PotionEffect effect : potionEffects) {
                player.addPotionEffect(effect);
            }
        }

        player.setGameMode(gameMode);
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);

        player.setDisplayName(displayName);
        player.setPlayerListName(playerListName);

        this.uuid = null;
        this.inventoryContents = null;
        this.armorContents = null;
        this.enderChestContents = null;
        this.potionEffects = null;
    }

    private static void clearAllActivePotions(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private static ItemStack[] cloneArray(ItemStack[] source) {
        if (source == null) return null;
        ItemStack[] copy = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] == null ? null : source[i].clone();
        }
        return copy;
    }
}
