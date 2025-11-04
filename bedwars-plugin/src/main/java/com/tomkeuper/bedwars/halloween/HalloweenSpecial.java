package com.tomkeuper.bedwars.halloween;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.shop.IShopCategory;
import com.tomkeuper.bedwars.halloween.shop.PumpkinContent;
import com.tomkeuper.bedwars.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static com.tomkeuper.bedwars.BedWars.config;

public class HalloweenSpecial {

    private static HalloweenSpecial INSTANCE;

    private HalloweenSpecial() {
        BedWars.plugin.getLogger().info(ChatColor.AQUA + "Loaded Halloween Special <3");
        // pumpkin hats
        Bukkit.getPluginManager().registerEvents(new HalloweenListener(), BedWars.plugin);

        // pumpkin in shop
        IShopCategory blockCategory = ShopManager.shop.getCategoryList().stream().filter(category -> category.getName().equals("blocks-category")).findFirst().orElse(null);
        if (blockCategory != null) {
            PumpkinContent content = new PumpkinContent(blockCategory);
            content.setCategoryIdentifier("default-" + content.getCategoryIdentifier());
            if (content.isLoaded()) {
                blockCategory.getCategoryContentList().add(content);
            }
        }
    }

    /**
     * Initialize Halloween Special.
     */
    public static void init() {
        if (INSTANCE == null) {
            if (!checkAvailabilityDate()) return;
            INSTANCE = new HalloweenSpecial();
        }
    }

    protected static boolean checkAvailabilityDate() {
        // check date
        ZoneId zone = ZoneId.of(config.getString("timeZone"));
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(zone).toLocalDate();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        // allowed between October 21 and November 1
        return (month == 10 && day > 21 || month == 11 && day < 2);
    }

    public static HalloweenSpecial getINSTANCE() {
        return INSTANCE;
    }
}
