package com.tomkeuper.bedwars.shop.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.shop.ICategoryContent;
import com.tomkeuper.bedwars.api.shop.*;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.shop.ShopCache;
import com.tomkeuper.bedwars.shop.ShopManager;
import com.tomkeuper.bedwars.shop.main.ShopCategory;
import com.tomkeuper.bedwars.shop.main.ShopIndex;
import com.tomkeuper.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import com.tomkeuper.bedwars.shop.quickbuy.QuickBuyAdd;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP;
import static org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY;

public class InventoryListener implements Listener {

    /**
     * Check can move item outside inventory.
     * Block despawnable, permanent and start items dropping and inventory change.
     */
    public static boolean shouldCancelMovement(ItemStack i, ShopCache sc) {
        if (i == null) return false;
        if (i.getType() == Material.AIR) return false;
        if (sc == null) return false;

        if (BedWars.nms.isCustomBedWarsItem(i)) {
            if (BedWars.nms.getCustomData(i).equalsIgnoreCase("DEFAULT_ITEM")) {
                return true;
            }
        }

        String identifier = BedWars.nms.getShopUpgradeIdentifier(i);
        if (identifier == null) return false;
        if (identifier.equals("null")) return false;
        ICachedItem cachedItem = sc.getCachedItem(identifier);
        return cachedItem != null;
        // the commented line below  was blocking movement only if tiers amount > 1
        // return sc.getCachedItem(identifier).getCc().getContentTiers().size() > 1;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;

        IArena a = Arena.getArenaByPlayer(player);
        if (a == null) return;
        if (a.isSpectator(player)) return;

        ShopCache shopCache = ShopCache.getInstance().getShopCache(player.getUniqueId());
        IPlayerQuickBuyCache cache = PlayerQuickBuyCache.getInstance().getQuickBuyCache(player.getUniqueId());

        if (cache == null) return;
        if (shopCache == null) return;

        if (ShopIndex.getIndexViewers().contains(player.getUniqueId()) || ShopCategory.getInstance().getCategoryViewers().contains(player.getUniqueId())) {
            if (e.getClickedInventory() != null && e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                e.setCancelled(true);
                return;
            }
        }

        if (ShopIndex.getIndexViewers().contains(player.getUniqueId())) {
            e.setCancelled(true);

            // Use the arena-linked shop and its pre-resolved categories to handle clicks
            IShopIndex idx = a.getLinkedShop() != null ? a.getLinkedShop() : ShopManager.shop;
            if (idx instanceof ShopIndex) {
                java.util.Map<Integer, IShopCategory> chosen = ((ShopIndex) idx).getResolvedBySlot(a);
                IShopCategory clickedCategory = chosen.get(e.getSlot());
                if (clickedCategory != null) {
                    if (clickedCategory instanceof ShopCategory) {
                        ((ShopCategory) clickedCategory).open(player, shopCache);
                    } else {
                        clickedCategory.open(player, idx, shopCache);
                    }
                    return;
                }
            } else {
                // Fallback: check default categories only
                for (IShopCategory sc : idx.getCategoryList()) {
                    String n = sc.getName() == null ? "" : sc.getName().toLowerCase();
                    if (n.startsWith("default-") && e.getSlot() == sc.getSlot()) {
                        sc.open(player, idx, shopCache);
                        return;
                    }
                }
            }

            // Quick Buy area clicks
            for (IQuickBuyElement element : cache.getElements()) {
                if (element.getSlot() == e.getSlot()) {
                    if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        cache.setElement(element.getSlot(), (ICategoryContent) null);
                        cache.pushChangesToDB();
                        Bukkit.getScheduler().runTask(BedWars.plugin, () ->
                                ShopManager.shop.open(player, cache, false)
                        );
                        return;
                    }
                    if (element.getCategoryContent().execute(player, shopCache, element.getSlot())) {
                        // Reload the shop page. Needed to recalculate item purchasable
                        (a.getLinkedShop() != null ? a.getLinkedShop() : ShopManager.shop).open(player, cache, false);
                    }
                    return;
                }
            }
        } else if (ShopCategory.getInstance().getCategoryViewers().contains(player.getUniqueId())) {
            e.setCancelled(true);

            // Check if item is null or air (don't process clicks on air)
            if (e.getCurrentItem() == null) return;
            if (e.getCurrentItem().getType() == Material.AIR) return;

            // Check overrides
            IShopIndex idx = a.getLinkedShop() != null ? a.getLinkedShop() : ShopManager.shop;

            // Quick Buy button at top bar → back to index
            if (e.getSlot() == idx.getQuickBuyButton().getSlot()) {
                idx.open(player, cache, false);
                return;
            }

            // Category tabs click in the top bar
            if (idx instanceof ShopIndex) {
                Map<Integer, IShopCategory> chosen = ((ShopIndex) idx).getResolvedBySlot(a);
                IShopCategory clickedCategory = chosen.get(e.getSlot());
                if (clickedCategory != null) {
                    if (clickedCategory instanceof ShopCategory) {
                        ((ShopCategory) clickedCategory).open(player, shopCache);
                    } else {
                        clickedCategory.open(player, idx, shopCache);
                    }
                    return;
                }
            }

            // Content clicks: only check the selected category’s contents
            int selectedSlot = shopCache.getSelectedCategory();
            IShopCategory selectedCategory = null;
            if (idx instanceof ShopIndex) {
                selectedCategory = ((ShopIndex) idx).getResolvedBySlot(a).get(selectedSlot);
            }
            if (selectedCategory == null) {
                // Fallback: try default categories
                for (IShopCategory sc : idx.getCategoryList()) {
                    String n = sc.getName() == null ? "" : sc.getName().toLowerCase();
                    if (n.startsWith("default-") && sc.getSlot() == selectedSlot) {
                        selectedCategory = sc;
                        break;
                    }
                }
            }
            if (selectedCategory != null) {
                for (ICategoryContent cc : selectedCategory.getCategoryContentList()) {
                    if (cc.getSlot() == e.getSlot()) {
                        if (e.isShiftClick()) {
                            if (!cache.hasCategoryContent(cc))
                                new com.tomkeuper.bedwars.shop.quickbuy.QuickBuyAdd(player, cc);
                            return;
                        }
                        if (cc.execute(player, shopCache, cc.getSlot())) {
                            if (selectedCategory instanceof ShopCategory) {
                                ((ShopCategory) selectedCategory).open(player, shopCache); // reload page after purchase
                            } else {
                                selectedCategory.open(player, idx, shopCache);
                            }
                        }
                        return;
                    }
                }
            }

        } else if (QuickBuyAdd.getQuickBuyAdds().containsKey(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
            boolean add = false;
            for (int i : PlayerQuickBuyCache.quickSlots) {
                if (i == e.getSlot()) {
                    add = true;
                    break;
                }
            }
            if (!add) return;
            ICategoryContent cc = QuickBuyAdd.getQuickBuyAdds().get(e.getWhoClicked().getUniqueId());
            if (cc != null) {
                cache.setElement(e.getSlot(), cc);
                cache.pushChangesToDB();
                QuickBuyAdd.quickBuyAdds.remove(player.getUniqueId());
                Bukkit.getScheduler().runTask(BedWars.plugin, () ->
                        ShopManager.shop.open(player, cache, false)
                );
            }
        }
    }

    private boolean checkSlot(InventoryClickEvent e, Player p, ShopCache shopCache, IPlayerQuickBuyCache cache, ICategoryContent cc, IShopCategory sc) {
        if (cc.getSlot() == e.getSlot()) {
            if (e.isShiftClick()) {
                if (cache.hasCategoryContent(cc)) return true;
                new QuickBuyAdd(p, cc);
                return true;
            }
            if (cc.execute(p, shopCache, cc.getSlot())) {
                sc.open(p, ShopManager.shop, shopCache); // Reload the shop page. Needed to recalculate item purchasable
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onUpgradableMove(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ShopCache sc = ShopCache.getInstance().getShopCache(p.getUniqueId());
        if (sc == null) return;

        //block moving from hotbar
        if (e.getAction() == HOTBAR_SWAP && e.getClick() == ClickType.NUMBER_KEY) {
            if (e.getHotbarButton() > -1) {
                ItemStack i = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
                if (i != null) {
                    if (e.getClickedInventory() != e.getWhoClicked().getInventory()) {
                        if (shouldCancelMovement(i, sc)) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }

        //block moving cursor item
        if (e.getCursor() != null) {
            if (e.getCursor().getType() != Material.AIR) {
                if (e.getClickedInventory() == null) {
                    if (shouldCancelMovement(e.getCursor(), sc)) {
                        e.getWhoClicked().closeInventory();
                        e.setCancelled(true);
                    }
                } else if (e.getClickedInventory().getType() != e.getWhoClicked().getInventory().getType()) {
                    if (shouldCancelMovement(e.getCursor(), sc)) {
                        e.getWhoClicked().closeInventory();
                        e.setCancelled(true);
                    }
                }
            }
        }

        //block moving current item
        if (e.getCurrentItem() != null) {
            if (e.getCurrentItem().getType() != Material.AIR) {
                if (e.getClickedInventory() == null) {
                    if (shouldCancelMovement(e.getCursor(), sc)) {
                        e.getWhoClicked().closeInventory();
                        e.setCancelled(true);
                    }
                } else if (e.getClickedInventory().getType() != e.getWhoClicked().getInventory().getType()) {
                    if (shouldCancelMovement(e.getCurrentItem(), sc)) {
                        e.getWhoClicked().closeInventory();
                        e.setCancelled(true);
                    }
                }
            }
        }

        //block moving with shift
        if (e.getAction() == MOVE_TO_OTHER_INVENTORY) {
            if (shouldCancelMovement(e.getCurrentItem(), sc)) {
                if (e.getView().getTopInventory().getHolder() != null && e.getInventory().getHolder() == e.getWhoClicked())
                    return;
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onShopClose(InventoryCloseEvent e) {
        ShopIndex.indexViewers.remove(e.getPlayer().getUniqueId());
        ShopCategory.categoryViewers.remove(e.getPlayer().getUniqueId());
        QuickBuyAdd.quickBuyAdds.remove(e.getPlayer().getUniqueId());
    }
}
