package com.tomkeuper.bedwars.shop;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.shop.ICategoryContent;
import com.tomkeuper.bedwars.api.shop.ICachedItem;
import com.tomkeuper.bedwars.api.shop.IShopCache;
import com.tomkeuper.bedwars.api.shop.IShopCategory;
import com.tomkeuper.bedwars.arena.Arena;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ShopCache implements IShopCache {
    private static final List<ShopCache> shopCaches = new ArrayList<>();
    private static ShopCache instance;
    @Getter
    private UUID player;
    private List<CachedItem> cachedItems = new LinkedList<>();
    private int selectedCategory;
    private HashMap<IShopCategory, Byte> categoryWeight = new HashMap<>();
    public ShopCache() {
        instance = this;
    }

    public ShopCache(UUID player) {
        this.player = player;
        this.selectedCategory = ShopManager.shop.getQuickBuyButton().getSlot();
        shopCaches.add(this);
    }

    public static ShopCache getInstance() {
        return instance;
    }

    @Override
    public int getSelectedCategory() {
        return selectedCategory;
    }

    @Override
    public void setSelectedCategory(int slot) {
        this.selectedCategory = slot;
    }

    @Override
    public int getContentTier(String identifier) {
        ICachedItem ci = getCachedItem(identifier);
        return ci == null ? 1 : ci.getTier();
    }

    @Override
    public ShopCache getShopCache(UUID player) {
        for (ShopCache sc : new ArrayList<>(shopCaches)) {
            if (sc.player.equals(player)) return sc;
        }
        return null;
    }

    /**
     * Destroy data
     */
    public void destroy() {
        shopCaches.remove(this);
        cachedItems.clear();
        cachedItems = null;
        categoryWeight = null;
    }

    /**
     * Used to give items on player respawn
     */
    public void managePermanentsAndDowngradables(Arena arena) {
        BedWars.debug("Restore permanents on death for: " + player);
        for (CachedItem ci : cachedItems) {
            ci.manageDeath(arena);
        }
    }

    /**
     * Get a player's cached item
     */
    @Override
    public ICachedItem getCachedItem(String identifier) {
        for (CachedItem ci : cachedItems) {
            if (ci.getCc().getIdentifier().equals(identifier)) return ci;
        }
        return null;
    }

    /**
     * Check if the player has a cached item
     */
    @Override
    public boolean hasCachedItem(ICategoryContent cc) {
        for (CachedItem ci : cachedItems) {
            if (ci.getCc() == cc) return true;
        }
        return false;
    }

    @Override
    public ICachedItem getCachedItem(ICategoryContent cc) {
        for (CachedItem ci : cachedItems) {
            if (ci.getCc() == cc) return ci;
        }
        return null;
    }

    /**
     * Upgrade cached item
     * Add it if not found
     */
    @Override
    public void upgradeCachedItem(ICategoryContent cc, int slot) {
        ICachedItem ci = getCachedItem(cc.getIdentifier());
        if (ci == null) {
            ci = new CachedItem(cc);
            ci.updateItem(slot, Bukkit.getPlayer(player));
        } else {
            if (cc.getContentTiers().size() > ci.getTier()) {
                BedWars.debug("Cached item upgrade for " + cc.getIdentifier() + " player " + player);
                ci.upgrade(slot);
            }
        }
    }

    /**
     * Used for categories where you can't buy lower items
     * Ex. if you have bought diamond iron from it, you can't buy stone iron
     */
    @Override
    public void setCategoryWeight(IShopCategory sc, byte weight) {
        if (categoryWeight.containsKey(sc)) {
            categoryWeight.replace(sc, weight);
        } else {
            categoryWeight.put(sc, weight);
        }
    }

    public byte getCategoryWeight(IShopCategory sc) {
        return categoryWeight.getOrDefault(sc, (byte) 0);
    }

    /**
     * Get permanent and non downgradable shop items.
     */
    public List<CachedItem> getCachedPermanents() {
        List<CachedItem> ci = new ArrayList<>();
        for (CachedItem c : cachedItems) {
            if (c.getCc().isPermanent()) {
                ci.add(c);
            }
        }
        return ci;
    }

    public List<CachedItem> getCachedItems() {
        return cachedItems;
    }

    /**
     * Keep trace of shop items and player's tiers
     */
    @SuppressWarnings("WeakerAccess")
    public class CachedItem implements ICachedItem {
        @Getter
        private final ICategoryContent cc;
        private int tier = 1;

        public CachedItem(ICategoryContent cc) {
            this.cc = cc;
            cachedItems.add(this);
            BedWars.debug("New Cached item " + cc.getIdentifier() + " for player " + player);
        }

        @Override
        public int getTier() {
            return tier;
        }

        /**
         * Give permanents on death
         * and downgrade if necessary
         */
        public void manageDeath(Arena arena) {
            if (!cc.isPermanent()) return;
            if (cc.isDowngradable() && tier > 1) tier--;
            BedWars.debug("ShopCache Item Restore: " + cc.getIdentifier() + " for " + player);
            //noinspection ConstantConditions
            cc.giveItems(Bukkit.getPlayer(player), getShopCache(player), arena);
        }

        @Override
        public void upgrade(int slot) {
            tier++;
            Player p = Bukkit.getPlayer(player);
            for (ItemStack i : p.getInventory().getContents()) {
                if (i == null) continue;
                if (i.getType() == Material.AIR) continue;
                if (BedWars.nms.getShopUpgradeIdentifier(i).equals(cc.getIdentifier())) {
                    p.getInventory().remove(i);
                }
            }
            updateItem(slot, p);
            p.updateInventory();
        }

        @Override
        public void updateItem(int slot, Player p) {
            if (p.getOpenInventory() != null) {
                if (p.getOpenInventory().getTopInventory() != null) {
                    p.getOpenInventory().getTopInventory().setItem(slot, cc.getItemStack(Bukkit.getPlayer(player), getShopCache(player)));
                }
            }
        }
    }
}
