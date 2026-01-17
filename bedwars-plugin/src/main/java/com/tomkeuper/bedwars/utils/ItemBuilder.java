package com.tomkeuper.bedwars.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final ItemStack item;
    private ItemMeta itemMeta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.itemMeta = item.getItemMeta();
    }

    @Deprecated
    public ItemBuilder(int id) {
        this.item = new ItemStack(id, 1);
        this.itemMeta = this.item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        if (itemMeta != null) itemMeta.setDisplayName(color(name));
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (glow && itemMeta != null) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        if (itemMeta != null) itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        if (itemMeta != null) {
            itemMeta.setLore(Arrays.stream(lore).map(this::color).collect(Collectors.toList()));
        }
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (itemMeta != null) {
            itemMeta.setLore(lore.stream().map(this::color).collect(Collectors.toList()));
        }
        return this;
    }

    public ItemBuilder setMaterial(Material material) {
        item.setType(material);
        this.itemMeta = item.getItemMeta();
        return this;
    }

    public ItemBuilder setDurability(int durability) {
        item.setDurability((short) durability);
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        if (itemMeta != null) itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder setLeatherColor(Color color) {
        if (itemMeta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        }
        return this;
    }

    public ItemBuilder setSkull(String owner) {
        ensureSkull();
        if (!(itemMeta instanceof SkullMeta meta)) return this;

        OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
        meta.setOwner(player.getName());
        return this;
    }

    public ItemBuilder setSkullSkin(String textureUrl) {
        ensureSkull();
        if (!(itemMeta instanceof SkullMeta)) return this;

        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            String value = Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}").getBytes());
            profile.getProperties().put("textures", new Property("textures", value));

            Field profileField = itemMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(itemMeta, profile);
        } catch (Exception ignored) {
        }

        return this;
    }

    public ItemStack build() {
        if (itemMeta != null) item.setItemMeta(itemMeta);
        return item;
    }

    private String color(String s) {
        return s == null ? null : s.replace("&", "§");
    }

    private void ensureSkull() {
        if (item.getType() != Material.SKULL_ITEM) {
            item.setType(Material.SKULL_ITEM);
            item.setDurability((short) 3);
            this.itemMeta = item.getItemMeta();
        }
    }
}
