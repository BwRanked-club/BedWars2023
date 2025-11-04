package com.tomkeuper.bedwars.shop.quickbuy;

import com.tomkeuper.bedwars.api.arena.shop.ICategoryContent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.shop.IPlayerQuickBuyCache;
import com.tomkeuper.bedwars.shop.ShopCache;
import com.tomkeuper.bedwars.shop.ShopManager;
import com.tomkeuper.bedwars.shop.main.ShopCategory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class QuickBuyAdd {

    public static HashMap<UUID, ICategoryContent> quickBuyAdds = new HashMap<>();

    public QuickBuyAdd(Player player, ICategoryContent cc){
        ShopCategory.categoryViewers.remove(player.getUniqueId());
        open(player, cc);
    }

    public void open(Player player, ICategoryContent cc){
        Inventory inv = Bukkit.createInventory(null, ShopManager.shop.getInvSize(), Language.getMsg(player, Messages.SHOP_QUICK_ADD_NAME));
        IPlayerQuickBuyCache cache = PlayerQuickBuyCache.getInstance().getQuickBuyCache(player.getUniqueId());
        ShopCache sc = ShopCache.getInstance().getShopCache(player.getUniqueId());
        if (sc == null || cache == null){
            player.closeInventory();
        }
        inv.setItem(4, cc.getItemStack(player, Objects.requireNonNull(sc)));

        Objects.requireNonNull(cache).addInInventory(inv, sc);

        player.openInventory(inv);
        quickBuyAdds.put(player.getUniqueId(), cc);
    }

    public static HashMap<UUID, ICategoryContent> getQuickBuyAdds() {
        return new HashMap<>(quickBuyAdds);
    }
}
