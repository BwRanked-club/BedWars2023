package com.tomkeuper.bedwars.arena.feature;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.player.PlayerItemDepositEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.team.BedWarsTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResourceChestFeature implements Listener {

    private static final String ITEM_JSON_FILENAME = "item-names.json";
    private static final Map<String, String> ITEM_NAME_CACHE = new HashMap<>();
    private static final String[] PT_BR_WOOL_COLORS = {
            "lã branca", "lã laranja", "lã magenta", "lã azul-clara", "lã amarela", "lã verde-limão", "lã rosa", "lã cinza",
            "lã cinza-clara", "lã ciano", "lã roxa", "lã azul", "lã marrom", "lã verde", "lã vermelha", "lã preta"
    };
    private static ResourceChestFeature instance;
    private static long ITEM_JSON_MTIME = -1L;
    private static File ITEM_JSON_FILE;
    private final Set<Material> blocked;

    private ResourceChestFeature() {
        this.blocked = BedWars.config.getYml()
                .getStringList(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_BLOCKED)
                .stream()
                .map(String::toUpperCase)
                .map(Material::valueOf)
                .collect(Collectors.toSet());

        ITEM_JSON_FILE = new File(BedWars.plugin.getDataFolder(), ITEM_JSON_FILENAME);

        ensureItemJsonLoaded();

        Bukkit.getPluginManager().registerEvents(this, BedWars.plugin);
    }

    public static void init() {
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_ENABLED) && instance == null) {
            instance = new ResourceChestFeature();
        }
    }

    private static Inventory getSharedEnderChest(ITeam team, Player player) {
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENDER_CHEST_TEAM_SHARED) && team instanceof BedWarsTeam) {
            return ((BedWarsTeam) team).getSharedEnderChest();
        }
        return player.getEnderChest();
    }

    private static String getIso(Player p) {
        String iso = null;
        try {
            iso = Language.getPlayerLanguage(p).getIso();
        } catch (Throwable ignored) {
        }
        return normalizeIso(iso);
    }

    private static String normalizeIso(String iso) {
        if (iso == null || iso.isEmpty()) return "pt_BR";
        iso = iso.replace('-', '_');

        if (iso.equalsIgnoreCase("pt")) return "pt_BR";
        if (iso.equalsIgnoreCase("en")) return "en_US";
        return iso;
    }

    private static String resolveItemName(ItemStack item, String iso) {
        String mat = item.getType().name();
        int data = item.getDurability();

        String k1 = "item." + mat + "-" + data + "." + iso;
        String k2 = "item." + mat + "-" + data + ".default";
        String k3 = "item." + mat + "." + iso;
        String k4 = "item." + mat + ".default";

        if (ITEM_NAME_CACHE.containsKey(k1)) {
            logHit(mat, data, iso, k1);
            return ITEM_NAME_CACHE.get(k1);
        }
        if (ITEM_NAME_CACHE.containsKey(k2)) {
            logHit(mat, data, iso, k2);
            return ITEM_NAME_CACHE.get(k2);
        }
        if (ITEM_NAME_CACHE.containsKey(k3)) {
            logHit(mat, data, iso, k3);
            return ITEM_NAME_CACHE.get(k3);
        }
        if (ITEM_NAME_CACHE.containsKey(k4)) {
            logHit(mat, data, iso, k4);
            return ITEM_NAME_CACHE.get(k4);
        }

        if (iso.equalsIgnoreCase("pt_BR")) {
            if (mat.equals("WOOL") && data >= 0 && data < PT_BR_WOOL_COLORS.length)
                return PT_BR_WOOL_COLORS[data];
        }

        return prettyFromMaterial(mat, iso);
    }

    private static void logHit(String mat, int data, String iso, String key) {
        BedWars.plugin.getLogger().info("[ResourceChest] JSON hit: mat=" + mat + " data=" + data + " iso=" + iso + " key=" + key);
    }

    private static String prettyFromMaterial(String materialName, String iso) {
        String base = materialName.replace('_', ' ').toLowerCase(Locale.ROOT);
        if (iso.equalsIgnoreCase("pt_BR")) {
            switch (materialName) {
                case "WOOL":
                    return "lã";
                case "STAINED_GLASS":
                    return "vidro colorido";
                case "STAINED_GLASS_PANE":
                    return "vidraça colorida";
                case "STAINED_CLAY":
                    return "argila tingida";
                case "CARPET":
                    return "tapete";
            }
        }
        return base;
    }

    private static String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static synchronized void ensureItemJsonLoaded() {
        try {
            if (ITEM_JSON_FILE == null) {
                BedWars.plugin.getLogger().severe("[ResourceChest] ITEM_JSON_FILE == null");
                return;
            }
            if (!ITEM_JSON_FILE.exists()) {
                BedWars.plugin.getLogger().severe("[ResourceChest] item-names.json not founded into: " + ITEM_JSON_FILE.getAbsolutePath());
                return;
            }
            long mt = ITEM_JSON_FILE.lastModified();
            if (mt == ITEM_JSON_MTIME && !ITEM_NAME_CACHE.isEmpty()) {
                return;
            }

            String json = Files.readString(ITEM_JSON_FILE.toPath());
            Map<String, String> parsed = parseFlatJson(json);
            ITEM_NAME_CACHE.clear();
            ITEM_NAME_CACHE.putAll(parsed);
            ITEM_JSON_MTIME = mt;

        } catch (Exception ex) {
            BedWars.plugin.getLogger().severe("[ResourceChest] Error on load item-names.json: " + ex.getMessage());
        }
    }

    private static Map<String, String> parseFlatJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        int count = 0;
        while (m.find()) {
            String key = m.group(1).trim();
            String val = m.group(2);
            map.put(key, ChatColor.translateAlternateColorCodes('&', val));
            count++;
        }
        return map;
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeftClickChest(PlayerInteractEvent e) {
        IArena arena = Arena.getArenaByPlayer(e.getPlayer());
        if (arena == null) return;
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        boolean isChest = block.getType() == Material.CHEST;
        boolean isEnderChest = block.getType() == Material.ENDER_CHEST;
        if (!isChest && !isEnderChest) return;

        ITeam team = arena.getTeam(e.getPlayer());
        if (team == null) {
            if (!isEnderChest) return;
            if (arena.isSpectator(e.getPlayer()) || arena.getRespawnSessions().containsKey(e.getPlayer())) return;
        }

        Player player = e.getPlayer();
        ItemStack hand = e.getItem();

        if (hand == null || hand.getType() == Material.AIR) return;

        if (blocked.contains(hand.getType())
                || BedWars.nms.isTool(hand)
                || BedWars.nms.getCustomData(hand).equalsIgnoreCase("DEFAULT_ITEM")) {
            return;
        }

        Inventory inventory = isChest
                ? ((Chest) block.getState()).getBlockInventory()
                : getSharedEnderChest(team, player);

        PlayerItemDepositEvent event = new PlayerItemDepositEvent(player, arena, hand, inventory, block.getType());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);

        int inserted = safeDeposit(event.getPlayer(), event.getItem(), event.getTargetInventory());
        if (inserted > 0) {
            sendDepositMessage(event.getPlayer(), event.getItem(), inserted);
            ITeam team2 = arena.getTeam(event.getPlayer());
            if (team2 != null && event.getItem().getType().name().contains("SWORD")) {
                team2.defaultSword(player, true);
            }
        }
    }

    private int safeDeposit(Player player, ItemStack hand, Inventory inventory) {
        ItemStack toStore = hand.clone();
        Map<Integer, ItemStack> leftovers = inventory.addItem(toStore);

        int attempted = toStore.getAmount();
        int notInserted = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
        int inserted = attempted - notInserted;

        if (inserted <= 0) {
            player.sendMessage(Language.getMsg(player, Messages.INTERACT_FULL_CHEST));
            return 0;
        }

        if (notInserted <= 0) {
            player.getInventory().setItemInHand(new ItemStack(Material.AIR));
        } else {
            ItemStack newHand = hand.clone();
            newHand.setAmount(notInserted);
            player.getInventory().setItemInHand(newHand);
        }

        player.updateInventory();
        return inserted;
    }

    private void sendDepositMessage(Player player, ItemStack original, int inserted) {
        ensureItemJsonLoaded();
        String iso = getIso(player);
        String formattedName = resolveItemName(original, iso);
        String msg = Language.getMsg(player, Messages.ITEM_DEPOSITED_SUCCESSFULLY)
                .replace("{AMOUNT}", String.valueOf(inserted))
                .replace("{ITEM}", formattedName);
        BedWars.plugin.getLogger().info("[ResourceChest] MSG -> " + msg + " (iso=" + iso + ")");
        player.sendMessage(msg);
    }
}
