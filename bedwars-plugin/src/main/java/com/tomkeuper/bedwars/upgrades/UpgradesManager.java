package com.tomkeuper.bedwars.upgrades;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.upgrades.MenuContent;
import com.tomkeuper.bedwars.api.upgrades.UpgradesIndex;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.configuration.UpgradesConfig;
import com.tomkeuper.bedwars.upgrades.listeners.InventoryListener;
import com.tomkeuper.bedwars.upgrades.listeners.UpgradeOpenListener;
import com.tomkeuper.bedwars.upgrades.menu.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;

import static com.tomkeuper.bedwars.BedWars.nms;
import static com.tomkeuper.bedwars.BedWars.plugin;

public class UpgradesManager {

    private static final String SETTINGS_SUFFIX = "-upgrades-settings";
    private static final String KEY_MENU_CONTENT = ".menu-content";
    private static final String KEY_CATEGORY_CONTENT = ".category-content";

    private final Set<UUID> upgradeViewers = new HashSet<>();

    private final Map<String, MenuContent> menuContentByName = new HashMap<>();
    private final Map<String, UpgradesIndex> menuByName = new HashMap<>();

    private final Map<IArena, UpgradesIndex> customMenuForArena = new HashMap<>();

    private final UpgradesConfig upgrades;

    public UpgradesManager() {
        upgrades = new UpgradesConfig("upgrades", plugin.getDataFolder().getPath());
    }

    public void init() {
        for (String rootKey : upgrades.getYml().getConfigurationSection("").getKeys(false)) {
            final String name = rootKey;

            if (name.startsWith("upgrade-")) {
                ensureLoaded(name, this::loadUpgrade, "upgrade");
            } else if (name.startsWith("separator-")) {
                ensureLoaded(name, this::loadSeparator, "separator");
            } else if (name.startsWith("category-")) {
                ensureLoaded(name, this::loadCategory, "category");
            } else if (name.startsWith("base-trap-")) {
                ensureLoaded(name, this::loadBaseTrap, "base-trap");
            } else if (name.endsWith(SETTINGS_SUFFIX)) {
                String group = name.substring(0, name.length() - SETTINGS_SUFFIX.length());
                if (!group.isEmpty() && !loadMenu(group)) {
                    warn("Could not load menu: " + group);
                }
            }
        }

        BedWars.registerEvents(new InventoryListener(), new UpgradeOpenListener());
    }

    private void ensureLoaded(String name, Loader loader, String typeLog) {
        if (getMenuContent(name) == null && !loader.load(name)) {
            warn("Could not load " + typeLog + ": " + name);
        }
    }

    public boolean isWatchingUpgrades(UUID uuid) {
        return upgradeViewers.contains(uuid);
    }

    public void setWatchingUpgrades(UUID uuid) {
        upgradeViewers.add(uuid);
    }

    public void removeWatchingUpgrades(UUID uuid) {
        upgradeViewers.remove(uuid);
    }

    public boolean loadMenu(String groupName) {
        final String base = groupName + SETTINGS_SUFFIX;
        if (!upgrades.getYml().isSet(base + KEY_MENU_CONTENT)) return false;
        String normGroup = norm(groupName);
        if (menuByName.containsKey(normGroup)) return false;

        InternalMenu menu = new InternalMenu(groupName);
        for (String component : upgrades.getYml().getStringList(base + KEY_MENU_CONTENT)) {
            parseAndAddComponent(component, menu::addContent);
        }

        menuByName.put(normGroup, menu);
        debug("Registering upgrade menu: " + groupName);
        return true;
    }

    private boolean loadCategory(String name) {
        if (!validKey(name, "category-")) return false;
        if (!upgrades.getYml().isSet(name)) return false;
        if (getMenuContent(name) != null) return false;

        MenuCategory category = new MenuCategory(name, createDisplayItem(name));

        for (String component : upgrades.getYml().getStringList(name + KEY_CATEGORY_CONTENT)) {
            parseAndAddComponent(component, category::addContent);
        }

        menuContentByName.put(norm(name), category);
        debug("Registering upgrade: " + name);
        return true;
    }

    private boolean loadUpgrade(String name) {
        if (!validKey(name, "upgrade-")) return false;
        if (!upgrades.getYml().isSet(name)) return false;
        if (!upgrades.getYml().isSet(name + ".tier-1")) return false;
        if (getMenuContent(name) != null) return false;

        MenuUpgrade mu = new MenuUpgrade(name);

        for (String tier : upgrades.getYml().getConfigurationSection(name).getKeys(false)) {
            if (!tier.startsWith("tier-")) continue;

            String base = name + "." + tier;
            if (!requireKeys(base, "receive", "display-item", "cost", "currency")) {
                debug("Could not load Upgrade " + name + " tier: " + tier + ". Missing required fields.");
                continue;
            }

            int cost = upgrades.getYml().getInt(base + ".cost");
            Material currency = getCurrency(upgrades.getYml().getString(base + ".currency"));
            UpgradeTier ut = new UpgradeTier(name, tier, createDisplayItem(base), cost, currency);
            if (!mu.addTier(ut)) {
                warn("Could not load tier: " + tier + " at upgrade: " + name);
            }
        }

        menuContentByName.put(norm(name), mu);
        debug("Registering upgrade: " + name);
        return true;
    }

    private boolean loadSeparator(String name) {
        if (!validKey(name, "separator-")) return false;
        if (!upgrades.getYml().isSet(name)) return false;
        if (getMenuContent(name) != null) return false;

        MenuSeparator ms = new MenuSeparator(name, createDisplayItem(name));
        menuContentByName.put(norm(name), ms);
        debug("Registering upgrade: " + name);
        return true;
    }

    private boolean loadTrapSlot(String name) {
        if (!validKey(name, "trap-slot-")) return false;
        if (!upgrades.getYml().isSet(name)) return false;
        if (getMenuContent(name) != null) return false;

        MenuTrapSlot mts = new MenuTrapSlot(name, createDisplayItem(name));
        menuContentByName.put(norm(name), mts);
        debug("Registering upgrade: " + name);
        return true;
    }

    private boolean loadBaseTrap(String name) {
        if (!validKey(name, "base-trap-")) return false;
        if (!upgrades.getYml().isSet(name)) return false;
        if (!upgrades.getYml().isSet(name + ".receive")) {
            debug("Could not load BaseTrap. Receive not set.");
            return false;
        }
        if (!upgrades.getYml().isSet(name + ".display-item")) {
            debug("Could not load BaseTrap. Display item not set.");
            return false;
        }

        int cost = upgrades.getYml().getInt(name + ".cost");
        Material currency = getCurrency(upgrades.getYml().getString(name + ".currency"));
        MenuBaseTrap bt = new MenuBaseTrap(name, createDisplayItem(name), cost, currency);

        menuContentByName.put(norm(name), bt);
        debug("Registering upgrade: " + name);
        return true;
    }

    private interface PosConsumer {
        void add(MenuContent content, int slot);
    }

    private interface Loader {
        boolean load(String name);
    }

    private void parseAndAddComponent(String component, PosConsumer adder) {
        if (component == null || component.isEmpty()) return;

        String[] data = component.split(",");
        if (data.length <= 1) return;

        String ref = data[0];
        MenuContent mc = resolveOrLoad(ref);
        if (mc == null) return;

        for (int i = 1; i < data.length; i++) {
            String token = data[i];
            if (Misc.isNumber(token)) {
                adder.add(mc, Integer.parseInt(token));
            }
        }
    }

    private MenuContent resolveOrLoad(String ref) {
        MenuContent mc = getMenuContent(ref);
        if (mc != null) return mc;

        if (ref.startsWith("category-")) {
            return loadCategory(ref) ? getMenuContent(ref) : null;
        } else if (ref.startsWith("upgrade-")) {
            return loadUpgrade(ref) ? getMenuContent(ref) : null;
        } else if (ref.startsWith("trap-slot-")) {
            return loadTrapSlot(ref) ? getMenuContent(ref) : null;
        } else if (ref.startsWith("separator-")) {
            return loadSeparator(ref) ? getMenuContent(ref) : null;
        } else if (ref.startsWith("base-trap-")) {
            return loadBaseTrap(ref) ? getMenuContent(ref) : null;
        }
        return null;
    }

    private static boolean validKey(String name, String prefix) {
        return name != null && name.startsWith(prefix);
    }

    private boolean requireKeys(String base, String... keys) {
        for (String k : keys) {
            if (!upgrades.getYml().isSet(base + "." + k)) return false;
        }
        return true;
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private static void debug(String msg) {
        BedWars.debug(msg);
    }

    private static void warn(String msg) {
        Bukkit.getLogger().log(Level.WARNING, msg);
    }

    public int getMoney(Player player, Material currency) {
        if (currency == Material.AIR) {
            return (int) Math.floor(BedWars.getEconomy().getMoney(player));
        }
        return BedWars.getAPI().getShopUtil().calculateMoney(player, currency);
    }

    public Material getCurrency(String name) {
        if (name == null || name.isEmpty()) return null;
        return BedWars.getAPI().getShopUtil().getCurrency(name);
    }

    public MenuContent getMenuContent(ItemStack item) {
        if (item == null) return null;
        String identifier = nms.getCustomData(item);
        if (identifier == null || "null".equalsIgnoreCase(identifier)) return null;
        if (!identifier.startsWith("MCONT_")) return null;
        String key = identifier.substring("MCONT_".length());
        if (key.isEmpty()) return null;
        return menuContentByName.get(norm(key));
    }

    public MenuContent getMenuContent(String identifier) {
        if (identifier == null) return null;
        return menuContentByName.get(norm(identifier));
    }

    public void setCustomMenuForArena(IArena arena, UpgradesIndex menu) {
        Objects.requireNonNull(arena, "arena");
        Objects.requireNonNull(menu, "menu");
        UpgradesIndex old = customMenuForArena.put(arena, menu);
        if (old == null) {
            debug("Registering custom menu for arena: " + arena.getArenaName() + ". Using index: " + menu.getName());
        } else {
            debug("Overriding custom menu for arena: " + arena.getArenaName() + ". Using index: " + menu.getName() + " Old index: " + old.getName());
        }
    }

    public UpgradesIndex getMenuForArena(IArena arena) {
        UpgradesIndex custom = customMenuForArena.get(arena);
        if (custom != null) return custom;
        UpgradesIndex byGroup = menuByName.get(norm(arena.getGroup()));
        return byGroup != null ? byGroup : menuByName.get("default");
    }

    private ItemStack createDisplayItem(String path) {
        Material material = parseMaterial(upgrades.getYml().getString(path + ".display-item.material"), Material.BEDROCK);

        int amount = safeInt(upgrades.getYml().getString(path + ".display-item.amount"), 1);
        short data = (short) upgrades.getYml().getInt(path + ".display-item.data");

        ItemStack stack = new ItemStack(material, Math.max(1, amount), data);

        if (upgrades.getYml().getBoolean(path + ".display-item.enchanted")) {
            stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ItemMeta im = stack.getItemMeta();
            if (im != null) {
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                stack.setItemMeta(im);
            }
        }
        return stack;
    }

    private static Material parseMaterial(String name, Material def) {
        if (name == null) return def;
        try {
            return Material.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            warn(name + " is not a valid material. Falling back to " + def);
            return def;
        }
    }

    private static int safeInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public String getCurrencyMsg(Player p, UpgradeTier ut) {
        String key = switch (ut.getCurrency()) {
            case IRON_INGOT -> ut.getCost() == 1 ? Messages.MEANING_IRON_SINGULAR : Messages.MEANING_IRON_PLURAL;
            case GOLD_INGOT -> ut.getCost() == 1 ? Messages.MEANING_GOLD_SINGULAR : Messages.MEANING_GOLD_PLURAL;
            case EMERALD -> ut.getCost() == 1 ? Messages.MEANING_EMERALD_SINGULAR : Messages.MEANING_EMERALD_PLURAL;
            case DIAMOND -> ut.getCost() == 1 ? Messages.MEANING_DIAMOND_SINGULAR : Messages.MEANING_DIAMOND_PLURAL;
            case AIR -> ut.getCost() == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL;
            default -> null;
        };
        return key == null ? "" : Language.getMsg(p, key);
    }

    public String getCurrencyMsg(Player p, int money, String currency) {
        if (currency == null) {
            return Language.getMsg(p, money == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL);
        }
        String key = switch (currency.toLowerCase(Locale.ROOT)) {
            case "iron" -> money == 1 ? Messages.MEANING_IRON_SINGULAR : Messages.MEANING_IRON_PLURAL;
            case "gold" -> money == 1 ? Messages.MEANING_GOLD_SINGULAR : Messages.MEANING_GOLD_PLURAL;
            case "emerald" -> money == 1 ? Messages.MEANING_EMERALD_SINGULAR : Messages.MEANING_EMERALD_PLURAL;
            case "diamond" -> money == 1 ? Messages.MEANING_DIAMOND_SINGULAR : Messages.MEANING_DIAMOND_PLURAL;
            default -> money == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL;
        };
        return Language.getMsg(p, key);
    }

    public String getCurrencyMsg(Player p, int money, Material currency) {
        String key = switch (currency) {
            case IRON_INGOT -> money == 1 ? Messages.MEANING_IRON_SINGULAR : Messages.MEANING_IRON_PLURAL;
            case GOLD_INGOT -> money == 1 ? Messages.MEANING_GOLD_SINGULAR : Messages.MEANING_GOLD_PLURAL;
            case EMERALD -> money == 1 ? Messages.MEANING_EMERALD_SINGULAR : Messages.MEANING_EMERALD_PLURAL;
            case DIAMOND -> money == 1 ? Messages.MEANING_DIAMOND_SINGULAR : Messages.MEANING_DIAMOND_PLURAL;
            default -> money == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL;
        };
        return Language.getMsg(p, key);
    }

    public ChatColor getCurrencyColor(Material currency) {
        return switch (currency) {
            case DIAMOND -> ChatColor.AQUA;
            case GOLD_INGOT -> ChatColor.GOLD;
            case IRON_INGOT -> ChatColor.WHITE;
            case EMERALD -> ChatColor.GREEN;
            default -> ChatColor.DARK_GREEN;
        };
    }

    public UpgradesConfig getConfiguration() {
        return upgrades;
    }

    public int getMenuSize() {
        int size = upgrades.getYml().getInt("default-upgrades-settings.menu-size", 45);
        if (size <= 0 || size > 54 || size % 9 != 0) size = 45;
        return size;
    }

    public Map<String, MenuContent> menuContentByName() {
        return menuContentByName;
    }
}
