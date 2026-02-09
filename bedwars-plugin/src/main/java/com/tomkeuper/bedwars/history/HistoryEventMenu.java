package com.tomkeuper.bedwars.history;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.configuration.Permissions;
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
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import static com.tomkeuper.bedwars.api.language.Language.getList;
import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public final class HistoryEventMenu {

    public static final String NBT_HISTORY_EVENT_NEXT = "HISTORY_EVENT_NEXT";
    public static final String NBT_HISTORY_EVENT_PREV = "HISTORY_EVENT_PREV";
    public static final String NBT_HISTORY_EVENT_BACK = "HISTORY_EVENT_BACK";

    private HistoryEventMenu() {
    }

    public static void open(Player viewer, UUID targetId, UUID matchId, int page, int historyPage) {
        if (viewer == null || targetId == null || matchId == null) {
            if (viewer != null) {
                viewer.sendMessage(getMsg(viewer, Messages.PLAYER_HISTORY_EVENTS_NO_DATA));
            }
            return;
        }
        if (!viewer.hasPermission(Permissions.PERMISSION_ADMIN)) {
            viewer.sendMessage(getMsg(viewer, Messages.PLAYER_HISTORY_EVENTS_NO_PERMISSION));
            return;
        }
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ENABLED) ||
                !BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_EVENTS_ENABLED)) {
            viewer.sendMessage(getMsg(viewer, Messages.PLAYER_HISTORY_EVENTS_NO_DATA));
            return;
        }

        int size = normalizeSize(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_GUI_SIZE));
        List<Integer> entrySlots = parseSlots(size);
        if (entrySlots.isEmpty()) return;

        String targetName = resolveTargetName(targetId);

        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> {
            int total = MatchHistoryEventService.countEvents(matchId);
            int totalPages = Math.max(1, (int) Math.ceil((double) total / entrySlots.size()));
            int safePage = Math.min(Math.max(1, page), totalPages);
            int offset = (safePage - 1) * entrySlots.size();
            List<MatchHistoryEventRecord> records = MatchHistoryEventService.fetchEvents(matchId, offset, entrySlots.size());

            Bukkit.getScheduler().runTask(BedWars.plugin, () -> buildAndOpen(viewer, targetId, matchId, targetName, size, entrySlots, safePage, totalPages, total, historyPage, records));
        });
    }

    private static void buildAndOpen(Player viewer,
                                     UUID targetId,
                                     UUID matchId,
                                     String targetName,
                                     int size,
                                     List<Integer> entrySlots,
                                     int page,
                                     int totalPages,
                                     int total,
                                     int historyPage,
                                     List<MatchHistoryEventRecord> records) {
        if (!viewer.isOnline()) return;

        String title = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_INV_NAME), targetName, null, null, 0, page, totalPages)
                .replace("%bw_history_total%", String.valueOf(total));
        HistoryEventMenuHolder holder = new HistoryEventMenuHolder(targetId, matchId, page, historyPage);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        applyFiller(viewer, inv, size, targetName, page, totalPages);

        if (records == null || records.isEmpty()) {
            int emptySlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_EMPTY_SLOT), size);
            if (emptySlot >= 0) {
                inv.setItem(emptySlot, buildEmptyItem(viewer, targetName, page, totalPages));
            }
        } else {
            for (int i = 0; i < records.size() && i < entrySlots.size(); i++) {
                int slot = entrySlots.get(i);
                MatchHistoryEventRecord record = records.get(i);
                int index = ((page - 1) * entrySlots.size()) + i + 1;
                inv.setItem(slot, buildEntryItem(viewer, targetName, record, index, page, totalPages));
            }
        }

        applyNavigation(viewer, inv, targetName, page, totalPages, total, historyPage);

        viewer.openInventory(inv);
        Sounds.playSound("stats-gui-open", viewer);
    }

    private static void applyFiller(Player viewer, Inventory inv, int size, String targetName, int page, int totalPages) {
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_FILLER_ENABLED)) return;
        Material defaultFiller = parseMaterial(BedWars.getForCurrentVersion("STAINED_GLASS_PANE", "STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE"), Material.PAPER);
        Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_FILLER_MATERIAL), defaultFiller);
        int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_FILLER_DATA);
        boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_FILLER_ENCHANTED);

        String name = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_FILLER_NAME), targetName, null, null, 0, page, totalPages);
        List<String> lore = new ArrayList<>();
        for (String line : getList(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_FILLER_LORE)) {
            lore.add(applyPlaceholders(viewer, line, targetName, null, null, 0, page, totalPages));
        }

        ItemStack filler = buildItem(material, data, enchanted, name, lore, "");
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    private static void applyNavigation(Player viewer, Inventory inv, String targetName, int page, int totalPages, int total, int historyPage) {
        int prevSlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_PREV_SLOT), inv.getSize());
        int nextSlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_NEXT_SLOT), inv.getSize());
        int pageSlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_PAGE_SLOT), inv.getSize());
        int backSlot = clampSlot(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_BACK_SLOT), inv.getSize());

        if (prevSlot >= 0 && page > 1) {
            inv.setItem(prevSlot, buildNavItem(viewer, targetName, Messages.PLAYER_HISTORY_EVENTS_GUI_PREV_NAME, Messages.PLAYER_HISTORY_EVENTS_GUI_PREV_LORE, ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_PREV_MATERIAL, ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_PREV_DATA, ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_PREV_ENCHANTED, NBT_HISTORY_EVENT_PREV, page, totalPages));
        }

        if (nextSlot >= 0 && page < totalPages) {
            inv.setItem(nextSlot, buildNavItem(viewer, targetName, Messages.PLAYER_HISTORY_EVENTS_GUI_NEXT_NAME, Messages.PLAYER_HISTORY_EVENTS_GUI_NEXT_LORE, ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_NEXT_MATERIAL, ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_NEXT_DATA, ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_NEXT_ENCHANTED, NBT_HISTORY_EVENT_NEXT, page, totalPages));
        }

        if (pageSlot >= 0) {
            String name = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_PAGE_NAME), targetName, null, null, 0, page, totalPages)
                    .replace("%bw_history_total%", String.valueOf(total));
            List<String> lore = new ArrayList<>();
            for (String line : getList(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_PAGE_LORE)) {
                lore.add(applyPlaceholders(viewer, line, targetName, null, null, 0, page, totalPages)
                        .replace("%bw_history_total%", String.valueOf(total)));
            }
            Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_PAGE_MATERIAL), Material.BOOK);
            int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_PAGE_DATA);
            boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_PAGE_ENCHANTED);
            inv.setItem(pageSlot, buildItem(material, data, enchanted, name, lore, ""));
        }

        if (backSlot >= 0) {
            String nbt = NBT_HISTORY_EVENT_BACK;
            Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_BACK_MATERIAL), Material.ARROW);
            int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_BACK_DATA);
            boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_NAV_BACK_ENCHANTED);

            String name = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_BACK_NAME), targetName, null, null, 0, page, totalPages);
            List<String> lore = new ArrayList<>();
            for (String line : getList(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_BACK_LORE)) {
                lore.add(applyPlaceholders(viewer, line, targetName, null, null, 0, page, totalPages)
                        .replace("%bw_history_page%", String.valueOf(historyPage)));
            }
            inv.setItem(backSlot, buildItem(material, data, enchanted, name, lore, nbt));
        }
    }

    private static ItemStack buildEmptyItem(Player viewer, String targetName, int page, int totalPages) {
        Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_EMPTY_MATERIAL), Material.BARRIER);
        int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_EMPTY_DATA);
        boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_EMPTY_ENCHANTED);

        String name = applyPlaceholders(viewer, getMsg(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_EMPTY_NAME), targetName, null, null, 0, page, totalPages);
        List<String> lore = new ArrayList<>();
        for (String line : getList(viewer, Messages.PLAYER_HISTORY_EVENTS_GUI_EMPTY_LORE)) {
            lore.add(applyPlaceholders(viewer, line, targetName, null, null, 0, page, totalPages));
        }

        return buildItem(material, data, enchanted, name, lore, "");
    }

    private static ItemStack buildEntryItem(Player viewer, String targetName, MatchHistoryEventRecord record, int index, int page, int totalPages) {
        Material material = parseMaterial(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_ENTRY_MATERIAL), Material.PAPER);
        int data = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_ENTRY_DATA);
        boolean enchanted = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_ENTRY_ENCHANTED);

        Map<String, String> meta = MatchHistoryEventService.deserializeMeta(record.getMeta());
        String name = resolveEventTemplate(viewer, record.getEventType(), true);
        List<String> lore = resolveEventTemplateLore(viewer, record.getEventType());
        String finalName = applyPlaceholders(viewer, name, targetName, record, meta, index, page, totalPages);
        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            finalLore.add(applyPlaceholders(viewer, line, targetName, record, meta, index, page, totalPages));
        }

        return buildItem(material, data, enchanted, finalName, finalLore, "");
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

        String name = applyPlaceholders(viewer, getMsg(viewer, namePath), targetName, null, null, 0, page, totalPages);
        List<String> lore = new ArrayList<>();
        for (String line : getList(viewer, lorePath)) {
            lore.add(applyPlaceholders(viewer, line, targetName, null, null, 0, page, totalPages));
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

    private static String applyPlaceholders(Player viewer,
                                            String input,
                                            String targetName,
                                            MatchHistoryEventRecord record,
                                            Map<String, String> meta,
                                            int index,
                                            int page,
                                            int totalPages) {
        if (input == null) return "";
        String result = input
                .replace("%bw_target%", targetName)
                .replace("%bw_history_event_index%", String.valueOf(index))
                .replace("%bw_history_page%", String.valueOf(page))
                .replace("%bw_history_pages%", String.valueOf(totalPages));

        if (record != null) {
            String dateTime = formatDateTime(viewer, record.getEventTime());
            String typeName = resolveEventTypeName(viewer, record.getEventType());
            result = result
                    .replace("%bw_event_match_id%", record.getMatchId() != null ? record.getMatchId().toString() : "")
                    .replace("%bw_event_type%", typeName)
                    .replace("%bw_event_type_key%", safe(record.getEventType()))
                    .replace("%bw_event_time%", dateTime)
                    .replace("%bw_event_epoch%", String.valueOf(record.getEventTime()))
                    .replace("%bw_event_actor%", safe(record.getActorName()))
                    .replace("%bw_event_actor_uuid%", record.getActorId() != null ? record.getActorId().toString() : "")
                    .replace("%bw_event_target%", safe(record.getTargetName()))
                    .replace("%bw_event_target_uuid%", record.getTargetId() != null ? record.getTargetId().toString() : "")
                    .replace("%bw_event_team%", safe(record.getTeamName()))
                    .replace("%bw_event_team_color%", safe(record.getTeamColor()))
                    .replace("%bw_event_meta%", safe(record.getMeta()));
        }

        if (meta != null && !meta.isEmpty()) {
            for (Map.Entry<String, String> entry : meta.entrySet()) {
                String key = entry.getKey();
                if (key == null) continue;
                String token = "%bw_event_meta_" + key + "%";
                result = result.replace(token, entry.getValue() == null ? "" : entry.getValue());
            }
        }

        return SupportPAPI.getSupportPAPI().replace(viewer, result);
    }

    private static String resolveEventTemplate(Player viewer, String type, boolean name) {
        String key = type == null ? "" : type.toLowerCase();
        String path = name ? Messages.PLAYER_HISTORY_EVENT_TEMPLATE_NAME.replace("%type%", key)
                : Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", key);
        Language lang = Language.getPlayerLanguage(viewer);
        if (lang.exists(path)) {
            return name ? getMsg(viewer, path) : "";
        }
        return name ? getMsg(viewer, Messages.PLAYER_HISTORY_EVENT_DEFAULT_NAME) : "";
    }

    private static List<String> resolveEventTemplateLore(Player viewer, String type) {
        String key = type == null ? "" : type.toLowerCase();
        String path = Messages.PLAYER_HISTORY_EVENT_TEMPLATE_LORE.replace("%type%", key);
        Language lang = Language.getPlayerLanguage(viewer);
        if (lang.exists(path)) {
            return getList(viewer, path);
        }
        return getList(viewer, Messages.PLAYER_HISTORY_EVENT_DEFAULT_LORE);
    }

    private static String resolveEventTypeName(Player viewer, String typeKey) {
        if (typeKey == null || typeKey.isBlank()) return "";
        String path = Messages.PLAYER_HISTORY_EVENT_TYPE_NAME.replace("%type%", typeKey.toLowerCase());
        Language lang = Language.getPlayerLanguage(viewer);
        if (lang.exists(path)) {
            return getMsg(viewer, path);
        }
        return typeKey;
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
        String raw = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_HISTORY_EVENTS_GUI_SLOTS);
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
