package com.tomkeuper.bedwars.shop.quickbuy;

import com.tomkeuper.bedwars.api.arena.shop.ICategoryContent;
import com.tomkeuper.bedwars.api.shop.IQuickBuyElement;
import com.tomkeuper.bedwars.shop.ShopManager;
import com.tomkeuper.bedwars.shop.main.ShopCategory;

public class QuickBuyElement implements IQuickBuyElement {

    private int slot;
    private ICategoryContent categoryContent;
    private boolean loaded = false;


    public QuickBuyElement(String path, int slot){
        this.categoryContent = ShopCategory.getInstance().getCategoryContent(path, ShopManager.shop);
        if (this.categoryContent != null) this.loaded = true;
        this.slot = slot;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public ICategoryContent getCategoryContent() {
        return categoryContent;
    }

    /**
     * Rebind this quick-buy element to a different content instance.
     * Used to align with arena-resolved content without recreating the element.
     */
    public void setCategoryContent(ICategoryContent categoryContent) {
        this.categoryContent = categoryContent;
        this.loaded = categoryContent != null;
    }
}
