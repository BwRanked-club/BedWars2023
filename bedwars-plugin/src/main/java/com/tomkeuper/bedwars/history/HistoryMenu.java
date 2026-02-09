package com.tomkeuper.bedwars.history;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.configuration.Sounds;
import com.tomkeuper.bedwars.support.papi.SupportPAPI;
import com.tomkeuper.bedwars.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static com.tomkeuper.bedwars.api.language.Language.getList;
import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public final class HistoryMenu {

    public static final String NBT_HISTORY_NEXT = "HISTORY_NEXT";
    public static final String NBT_HISTORY_PREV = "HISTORY_PREV";
    public static final String NBT_HISTORY_ENTRY_PREFIX = "HISTORY_MATCH_";

    private HistoryMenu() {
    }

    public static void open(Player viewer, UUID targetId, int page) {
        if (viewer == null || targetId == null) return;
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ENABLED)) {
            viewer.sendMessage(getMsg(viewer, Messages.PLAYER_HISTORY_NO_DATA));
            return;
        }

        int size = normalizeSize(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_GUI_SIZE));
        List<Integer> entrySlots = parseSlots(size);
        if (entrySlots.isEmpty()) return;

        String targetName = resolveTargetName(targetId);

        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> {
            int total = MatchHistoryService.countHistory(targetId);
            int totalPages = Math.max(1, (int) Math.ceil((double) total / entrySlots.size()));
            int safePage = Math.min(Math.max(1, page), totalPages);
            int offset = (safePage - 1) * entrySlots.size();
            List<MatchHistoryRecord> records = MatchHistoryService.fetchHistory(targetId, offset, entrySlots.size());

            Bukkit.getScheduler().runTask(BedWars.plugin, () -> buildAndOpen(viewer, targetId, targetName, size, entrySlots, safePage, totalPages, total, records));
        });
    }

    private static void buildAndOpen(Player viewer,
                                     UUID targetId,
                                     String targetName,
                                     int size,
                                     List<Integer> entrySlots,
                                     int page,
                                     int totalPages,
                                     int total,
                                     List<MatchHistoryRecord> records) {
        if (!viewer.isOnline()) return;

        String title = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_GUI_INV_NAME), targetName, null, 0, page, totalPages);
        HistoryMenuHolder holder = new HistoryMenuHolder(targetId, page);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        applyFiller(viewer, inv, size, targetName, page, totalPages);

        if (records == null || records.isEmpty()) {
            int emptySlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EMPTY_SLOT), size);
            if (emptySlot >= 0) {
                inv.setItem(emptySlot, buildEmptyItem(viewer, targetName, page, totalPages));
            }
        } else {
            for (int i = 0; i < records.size() && i < entrySlots.size(); i++) {
                int slot = entrySlots.get(i);
                MatchHistoryRecord record = records.get(i);
                int index = ((page - 1) * entrySlots.size()) + i + 1;
                inv.setItem(slot, buildEntryItem(viewer, targetName, record, index, page, totalPages));
            }
        }

        applyNavigation(viewer, inv, targetName, page, totalPages, total);

        viewer.openInventory(inv);
        Sounds.playSound("stats-gui-open", viewer);
    }

    private static void applyFiller(Player viewer, Inventory inv, int size, String targetName, int page, int totalPages) {
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_FILLER_ENABLED)) return;
        Material defaultFiller = parseMaterial(BedWars.getForCurrentVersion("STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE"), Material.PAPER);
        Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_FILLER_MATERIAL), defaultFiller);
        int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_FILLER_DATA);
        boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_FILLER_ENCHANTED);

        String name = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_GUI_FILLER_NAME), targetName, null, 0, page, totalPages);
        List<String> lore = new ArrayList<>();
        for (String line : getList(viewer, Messages.PLAYER_HISTORY_GUI_FILLER_LORE)) {
            lore.add(applyPlaceholders(viewer, line, targetName, null, 0, page, totalPages));
        }

        ItemStack filler = buildItem(material, data, enchanted, name, lore, "");
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    private static void applyNavigation(Player viewer, Inventory inv, String targetName, int page, int totalPages, int total) {
        int prevSlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_PREV_SLOT), inv.getSize());
        int nextSlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_NEXT_SLOT), inv.getSize());
        int pageSlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_PAGE_SLOT), inv.getSize());

        if (prevSlot >= 0 && page > 1) {
            inv.setItem(prevSlot, buildNavItem(viewer, targetName, Messages.PLAYER_HISTORY_GUI_PREV_NAME, Messages.PLAYER_HISTORY_GUI_PREV_LORE, ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_PREV_MATERIAL, ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_PREV_DATA, ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_PREV_ENCHANTED, NBT_HISTORY_PREV, page, totalPages));
        }

        if (nextSlot >= 0 && page < totalPages) {
            inv.setItem(nextSlot, buildNavItem(viewer, targetName, Messages.PLAYER_HISTORY_GUI_NEXT_NAME, Messages.PLAYER_HISTORY_GUI_NEXT_LORE, ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_NEXT_MATERIAL, ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_NEXT_DATA, ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_NEXT_ENCHANTED, NBT_HISTORY_NEXT, page, totalPages));
        }

        if (pageSlot >= 0) {
            String name = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_GUI_PAGE_NAME), targetName, null, 0, page, totalPages)
                    .replace("%bw_history_total%", String.valueOf(total));
            List<String> lore = new ArrayList<>();
            for (String line : getList(viewer, Messages.PLAYER_HISTORY_GUI_PAGE_LORE)) {
                lore.add(applyPlaceholders(viewer, line, targetName, null, 0, page, totalPages)
                        .replace("%bw_history_total%", String.valueOf(total)));
            }
            Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_PAGE_MATERIAL), Material.BOOK);
            int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_PAGE_DATA);
            boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_NAV_PAGE_ENCHANTED);
            inv.setItem(pageSlot, buildItem(material, data, enchanted, name, lore, ""));
        }
    }

    private static ItemStack buildEmptyItem(Player viewer, String targetName, int page, int totalPages) {
        Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EMPTY_MATERIAL), Material.BARRIER);
        int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EMPTY_DATA);
        boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EMPTY_ENCHANTED);

        String name = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_GUI_EMPTY_NAME), targetName, null, 0, page, totalPages);
        List<String> lore = new ArrayList<>();
        for (String line : getList(viewer, Messages.PLAYER_HISTORY_GUI_EMPTY_LORE)) {
            lore.add(applyPlaceholders(viewer, line, targetName, null, 0, page, totalPages));
        }

        return buildItem(material, data, enchanted, name, lore, "");
    }

    private static ItemStack buildEntryItem(Player viewer, String targetName, MatchHistoryRecord record, int index, int page, int totalPages) {
        Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_ENTRY_MATERIAL), Material.PAPER);
        int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_ENTRY_DATA);
        boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_ENTRY_ENCHANTED);

        String name = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_GUI_ENTRY_NAME), targetName, record, index, page, totalPages);
        List<String> lore = new ArrayList<>();
        for (String line : getList(viewer, Messages.PLAYER_HISTORY_GUI_ENTRY_LORE)) {
            lore.add(applyPlaceholders(viewer, line, targetName, record, index, page, totalPages));
        }

        String nbt = record.getMatchId() != null ? NBT_HISTORY_ENTRY_PREFIX + record.getMatchId().toString() : "";
        return buildItem(material, data, enchanted, name, lore, nbt);
    }

    private static ItemStack buildNavItem(Player viewer,
                                          String targetName,
                                          String namePath,
                                          String lorePath,
                                          String materialPath,
                                          String dataPath,
                                          String enchantedPath,
                                          String nbt,
                                          int page,
                                          int totalPages) {
        Material material = parseMaterial(BedWars.config.getString(materialPath), Material.ARROW);
        int data = BedWars.config.getInt(dataPath);
        boolean enchanted = BedWars.config.getBoolean(enchantedPath);

        String name = applyPlaceholders(viewer, getMsg(viewer, namePath), targetName, null, 0, page, totalPages);
        List<String> lore = new ArrayList<>();
        for (String line : getList(viewer, lorePath)) {
            lore.add(applyPlaceholders(viewer, line, targetName, null, 0, page, totalPages));
        }

        return buildItem(material, data, enchanted, name, lore, nbt);
    }

    private static ItemStack buildItem(Material material, int data, boolean enchanted, String name, List<String> lore, String nbt) {
        ItemBuilder builder = new ItemBuilder(material).setDurability(data).setName(name);
        if (enchanted) {
            builder.setGlow(true);
        }
        if (lore != null && !lore.isEmpty()) {
            builder.setLore(lore);
        }
        ItemStack item = builder.build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        if (nbt != null && !nbt.isEmpty()) {
            item = BedWars.nms.addCustomData(item, nbt);
        }
        return item;
    }

    private static String applyPlaceholders(Player viewer, String input, String targetName, MatchHistoryRecord record, int index, int page, int totalPages) {
        if (input == null) return "";
        String result = input
                .replace("%bw_target%", targetName)
                .replace("%bw_history_index%", String.valueOf(index))
                .replace("%bw_history_page%", String.valueOf(page))
                .replace("%bw_history_pages%", String.valueOf(totalPages));

        if (record != null) {
            String resultText = record.isWin() ? getMsg(viewer, Messages.HISTORY_RESULT_WIN) : getMsg(viewer, Messages.HISTORY_RESULT_LOSE);
            String dateTime = formatDateTime(viewer, record.getEndedAt());
            String duration = formatDuration(record.getDurationSeconds());
            result = result
                    .replace("%bw_history_match_id%", record.getMatchId() != null ? record.getMatchId().toString() : "")
                    .replace("%bw_history_player%", safe(record.getPlayerName()))
                    .replace("%bw_history_map%", safe(record.getArenaDisplay()))
                    .replace("%bw_history_map_name%", safe(record.getArenaName()))
                    .replace("%bw_history_group%", safe(record.getArenaGroup()))
                    .replace("%bw_history_mode%", safe(record.getMode()))
                    .replace("%bw_history_team%", safe(record.getTeamName()))
                    .replace("%bw_history_team_color%", safe(record.getTeamColor()))
                    .replace("%bw_history_placement%", String.valueOf(record.getPlacement()))
                    .replace("%bw_history_result%", resultText)
                    .replace("%bw_history_kills%", String.valueOf(record.getKills()))
                    .replace("%bw_history_final_kills%", String.valueOf(record.getFinalKills()))
                    .replace("%bw_history_total_kills%", String.valueOf(record.getTotalKills()))
                    .replace("%bw_history_deaths%", String.valueOf(record.getDeaths()))
                    .replace("%bw_history_final_deaths%", String.valueOf(record.getFinalDeaths()))
                    .replace("%bw_history_beds_destroyed%", String.valueOf(record.getBedsDestroyed()))
                    .replace("%bw_history_win%", String.valueOf(record.isWin()))
                    .replace("%bw_history_datetime%", dateTime)
                    .replace("%bw_history_duration%", duration)
                    .replace("%bw_history_server%", safe(record.getServerId()));
        }

        return SupportPAPI.getSupportPAPI().replace(viewer, result);
    }

    private static String formatDateTime(Player viewer, long epochMillis) {
        String fmt = getMsg(viewer, Messages.FORMATTING_HISTORY_DATE_FORMAT);
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        String tzId = BedWars.config.getString("timeZone");
        if (tzId != null && !tzId.isBlank()) {
            sdf.setTimeZone(TimeZone.getTimeZone(tzId));
        }
        return sdf.format(new Timestamp(epochMillis));
    }

    private static String formatDuration(int totalSeconds) {
        if (totalSeconds <= 0) return "0:00";
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) {
            return hours + ":" + (minutes < 10 ? "0" + minutes : String.valueOf(minutes)) + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
        }
        return minutes + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
    }

    private static String resolveTargetName(UUID targetId) {
        Player online = Bukkit.getPlayer(targetId);
        if (online != null) return online.getName();
        try {
            return Bukkit.getOfflinePlayer(targetId).getName();
        } catch (Exception ignored) {
        }
        return targetId.toString();
    }

    private static Material parseMaterial(String raw, Material fallback) {
        if (raw == null) return fallback;
        try {
            return Material.valueOf(raw.toUpperCase());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int normalizeSize(int size) {
        if (size % 9 != 0) size = 54;
        if (size < 9) size = 9;
        if (size > 54) size = 54;
        return size;
    }

    private static List<Integer> parseSlots(int invSize) {
        List<Integer> slots = new ArrayList<>();
        String raw = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_GUI_SLOTS);
        if (raw == null) raw = "";
        if (raw != null && !raw.isBlank()) {
            for (String part : raw.split(",")) {
                try {
                    int slot = Integer.parseInt(part.trim());
                    if (slot >= 0 && slot < invSize) {
                        slots.add(slot);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (!slots.isEmpty()) return slots;

        for (int row = 1; row <= 4; row++) {
            int base = row * 9;
            for (int col = 1; col <= 7; col++) {
                int slot = base + col;
                if (slot < invSize) slots.add(slot);
            }
        }
        return slots;
    }

    private static int clampSlot(int slot, int size) {
        if (slot < 0 || slot >= size) return -1;
        return slot;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}

