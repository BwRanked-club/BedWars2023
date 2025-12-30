package com.tomkeuper.bedwars.shop.main;

import com.tomkeuper.bedwars.api.language.Language;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Base inventory layout with common UI behavior for shops/menus.
 * Encapsulates name paths, separators and helper methods to keep UI consistent.
 */
public abstract class AbstractInventoryLayout {

    @Getter
    private final int invSize;
    @Getter
    private final String namePath;
    private final String separatorNamePath;
    private final String separatorLorePath;
    protected ItemStack separatorSelected;
    protected ItemStack separatorStandard;

    protected AbstractInventoryLayout(int invSize,
                                      String namePath,
                                      String separatorNamePath,
                                      String separatorLorePath,
                                      ItemStack separatorSelected,
                                      ItemStack separatorStandard) {
        this.invSize = invSize;
        this.namePath = namePath;
        this.separatorNamePath = separatorNamePath;
        this.separatorLorePath = separatorLorePath;
        this.separatorSelected = separatorSelected;
        this.separatorStandard = separatorStandard;
    }

    /**
     * Add shop separator between categories and items.
     */
    public void addSeparator(Player player, Inventory inv) {
        ItemStack i = separatorStandard.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, separatorNamePath));
            im.setLore(Language.getList(player, separatorLorePath));
            i.setItemMeta(im);
        }

        for (int x = 9; x < 18; x++) {
            inv.setItem(x, i);
        }
    }

    /**
     * This is the item that indicates the selected category.
     */
    public ItemStack getSelectedItem(Player player) {
        ItemStack i = separatorSelected.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, separatorNamePath));
            im.setLore(Language.getList(player, separatorLorePath));
            i.setItemMeta(im);
        }
        return i;
    }
}