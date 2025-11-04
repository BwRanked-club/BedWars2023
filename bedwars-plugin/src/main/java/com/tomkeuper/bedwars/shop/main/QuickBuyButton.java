package com.tomkeuper.bedwars.shop.main;

import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.shop.IQuickBuyButton;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QuickBuyButton implements IQuickBuyButton {

    private int slot;
    private ItemStack itemStack;
    private String namePath, lorePath;

    /**
     * Create a new quick buy button
     *
     * @param namePath  Language name path
     * @param lorePath  Language lore path.
     * @param slot      Item slot in inventory
     * @param itemStack Button ItemStack preview
     */
    public QuickBuyButton(int slot, ItemStack itemStack, String namePath, String lorePath) {
        this.slot = slot;
        this.itemStack = itemStack;
        this.namePath = namePath;
        this.lorePath = lorePath;
    }

    /**
     * Get the quick buy button in the player's language
     */
    @Override
    public ItemStack getItemStack(Player player) {
        ItemStack i = itemStack.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, namePath));
            im.setLore(Language.getList(player, lorePath));
            i.setItemMeta(im);
        }
        return i;
    }

    /**
     * Get quick buy item slot
     */
    @Override
    public int getSlot() {
        return slot;
    }
}
