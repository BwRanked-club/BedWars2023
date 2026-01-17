package com.tomkeuper.bedwars.arena;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.NextEvent;
import com.tomkeuper.bedwars.api.arena.generator.GeneratorType;
import com.tomkeuper.bedwars.api.arena.generator.IGenerator;
import com.tomkeuper.bedwars.api.arena.shop.ShopHolo;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.arena.team.ITeamAssigner;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.entity.Despawnable;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.api.events.gameplay.NextEventChangeEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerReJoinEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaDisableEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaEnableEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaRestartEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaSpectateEvent;
import com.tomkeuper.bedwars.api.items.handlers.IPermanentItem;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.region.Region;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.tomkeuper.bedwars.api.shop.IShopIndex;
import com.tomkeuper.bedwars.api.tasks.AnnouncementTask;
import com.tomkeuper.bedwars.api.tasks.PlayingTask;
import com.tomkeuper.bedwars.api.tasks.RestartingTask;
import com.tomkeuper.bedwars.api.tasks.StartingTask;
import com.tomkeuper.bedwars.api.upgrades.UpgradesIndex;
import com.tomkeuper.bedwars.arena.tasks.*;
import com.tomkeuper.bedwars.arena.team.BedWarsTeam;
import com.tomkeuper.bedwars.arena.team.TeamAssigner;
import com.tomkeuper.bedwars.configuration.ArenaConfig;
import com.tomkeuper.bedwars.configuration.Sounds;
import com.tomkeuper.bedwars.levels.internal.InternalLevel;
import com.tomkeuper.bedwars.levels.internal.PerMinuteTask;
import com.tomkeuper.bedwars.listeners.blockstatus.BlockStatusListener;
import com.tomkeuper.bedwars.listeners.dropshandler.PlayerDrops;
import com.tomkeuper.bedwars.listeners.offline.OfflineGraceService;
import com.tomkeuper.bedwars.money.internal.MoneyPerMinuteTask;
import com.tomkeuper.bedwars.shop.ShopCache;
import com.tomkeuper.bedwars.shop.ShopManager;
import com.tomkeuper.bedwars.shop.main.ShopIndex;
import com.tomkeuper.bedwars.sidebar.BoardManager;
import com.tomkeuper.bedwars.support.citizens.JoinNPC;
import com.tomkeuper.bedwars.support.paper.PaperSupport;
import com.tomkeuper.bedwars.support.papi.SupportPAPI;
import com.tomkeuper.bedwars.support.vault.WithEconomy;
import com.tomkeuper.bedwars.utils.ItemBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

import static com.tomkeuper.bedwars.BedWars.*;
import static com.tomkeuper.bedwars.api.language.Language.*;
import static com.tomkeuper.bedwars.arena.upgrades.BaseListener.isOnABase;

@SuppressWarnings("WeakerAccess")
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Arena implements IArena {

    public static final Map<UUID, Integer> afkCheck = new HashMap<>();
    public static final Map<UUID, Integer> magicMilk = new HashMap<>();
    private static final Map<String, IArena> arenaByName = new HashMap<>();
    @Getter
    private static final Map<Player, IArena> arenaByPlayer = new HashMap<>();
    private static final Map<String, IArena> arenaByIdentifier = new HashMap<>();
    @Getter
    private static final LinkedList<IArena> arenas = new LinkedList<>();
    @Getter
    private static final LinkedList<IArena> enableQueue = new LinkedList<>();
    private static final Map<Player, Location> playerLocation = new HashMap<>();
    @Getter
    private static int gamesBeforeRestart = config.getInt(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_GAMES_BEFORE_RESTART);
    private final List<ServerPlaceholder> serverPlaceholders = new ArrayList<>();
    private final List<Player> leaving = new ArrayList<>();
    @Getter
    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();
    private final EnumSet<NextEvent> disabledNextEvents = EnumSet.noneOf(NextEvent.class);
    @Getter
    public int upgradeDiamondsCount = 0;
    @Getter
    public int upgradeEmeraldsCount = 0;
    @Setter
    @Getter
    public boolean allowSpectate = true;
    @Getter
    public boolean allowMapBreak = false;
    public boolean enderDragonDestory = false;
    private List<Player> players = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    @Getter
    private List<Block> signs = new ArrayList<>();
    private GameState status = GameState.restarting;
    private YamlConfiguration yml;
    private ArenaConfig cm;
    private int minPlayers = 2;
    private int maxPlayers = 10;
    private int maxInTeam = 1;
    @Getter
    private int islandRadius = 10;
    @Getter
    private World world;
    @EqualsAndHashCode.Include
    private String group = "Default";
    @EqualsAndHashCode.Include
    private String arenaName;
    @Getter
    private String worldName;
    private List<ITeam> teams = new ArrayList<>();
    @Getter
    private LinkedList<Vector> placed = new LinkedList<>();
    private List<String> nextEvents = new ArrayList<>();
    @Getter
    private List<Region> regionsList = new ArrayList<>();
    private List<BossBar> dragonBossbars = new ArrayList<>();
    private List<Scoreboard> scoreboards = new ArrayList<>();
    private int renderDistance, magicMilkTime = 30;
    @Getter
    private NextEvent nextEvent = NextEvent.DIAMOND_GENERATOR_TIER_II;
    private int diamondTier = 1, emeraldTier = 1;
    private ConcurrentHashMap<Player, Integer> respawnSessions = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<Player, Integer> showTime = new ConcurrentHashMap<>();
    private IShopIndex linkedShop;
    private UpgradesIndex linkedUpgrades;
    private Map<String, Integer> playerKills = new HashMap<>();
    private Map<String, Integer> playerTotalKills = new HashMap<>();
    private Map<Player, Integer> playerBedsDestroyed = new HashMap<>();
    private Map<Player, Integer> playerFinalKills = new HashMap<>();
    private Map<Player, Integer> playerDeaths = new HashMap<>();
    private Map<Player, Integer> playerFinalKillDeaths = new HashMap<>();
    @Getter
    private StartingTask startingTask;
    @Getter
    private PlayingTask playingTask;
    @Getter
    private RestartingTask restartingTask;
    private AnnouncementTask announcementTask;
    @Getter
    private List<IGenerator> oreGenerators = new ArrayList<>();
    private final HashMap<String, List<ShopHolo>> shopHolosIso = new HashMap<>();
    private PerMinuteTask perMinuteTask;
    private MoneyPerMinuteTask moneyperMinuteTask;
    private Location respawnLocation, spectatorLocation, waitingLocation;
    private int yKillHeight;
    private Instant startTime;
    private ITeamAssigner teamAssigner = new TeamAssigner();
    @Getter
    @Setter
    private volatile boolean initialized = false;
    private NextEvent forcedNextEvent = null;

    public Arena(String name, @Nullable CommandSender p) {
        if (autoscale) {
            boolean inQueue = enableQueue.stream().anyMatch(a -> a.getArenaName().equalsIgnoreCase(name));
            boolean standbyExists = hasStandbyForTemplate(name);

            if (autoscale && name.startsWith("bw_temp_")) {
                inQueue = false;
                standbyExists = false;
            }

            if (inQueue || standbyExists) {
                BedWars.debug("[AutoScale] Template " + name + " is already active or in the queue. Ignoring creation.");
                if (p != null)
                    p.sendMessage(ChatColor.RED + "Template " + name + " is already active or in the queue.");
                return;
            }
        } else {
            for (var mm : enableQueue) {
                if (mm.getArenaName().equalsIgnoreCase(name)) {
                    plugin.getLogger().severe("Tried to load arena " + name + " but it is already in the enable queue.");
                    if (p != null)
                        p.sendMessage(ChatColor.RED + "Tried to load arena " + name + " but it is already in the enable queue.");
                    return;
                }
            }
            if (getArenaByName(name) != null) {
                plugin.getLogger().severe("Tried to load arena " + name + " but it is already enabled.");
                if (p != null)
                    p.sendMessage(ChatColor.RED + "Tried to load arena " + name + " but it is already enabled.");
                return;
            }
        }

        this.arenaName = name;
        this.worldName = autoscale ? BedWars.arenaManager.generateGameID() : arenaName;

        cm = new ArenaConfig(BedWars.plugin, name, plugin.getDataFolder().getPath() + "/Arenas");
        yml = cm.getYml();

        if (yml.get("Team") == null) {
            if (p != null) p.sendMessage("You didn't set any team for arena: " + name);
            plugin.getLogger().severe("You didn't set any team for arena: " + name);
            return;
        }
        if (yml.getConfigurationSection("Team").getKeys(false).size() < 2) {
            if (p != null) p.sendMessage("§cYou must set at least 2 teams on: " + name);
            plugin.getLogger().severe("You must set at least 2 teams on: " + name);
            return;
        }

        maxInTeam = yml.getInt("maxInTeam");
        maxPlayers = yml.getConfigurationSection("Team").getKeys(false).size() * maxInTeam;
        minPlayers = yml.getInt("minPlayers");
        allowSpectate = yml.getBoolean("allowSpectate");
        enderDragonDestory = yml.getBoolean(ConfigPath.ARENA_ALLOW_DRAGON_DESTROY_WHEN_PROTECTED);
        allowMapBreak = yml.getBoolean(ConfigPath.ARENA_ALLOW_MAP_BREAK);
        magicMilkTime = yml.getInt(ConfigPath.ARENA_MAGIC_MILK_TIME);
        islandRadius = yml.getInt(ConfigPath.ARENA_ISLAND_RADIUS);

        if (config.getYml().get("arenaGroups") != null) {
            if (config.getYml().getStringList("arenaGroups").contains(yml.getString("group"))) {
                group = yml.getString("group");
            }
        }

        if (!BedWars.getAPI().getRestoreAdapter().isWorld(name)) {
            if (p != null) p.sendMessage(ChatColor.RED + "There isn't any map called " + name);
            plugin.getLogger().log(Level.WARNING, "There isn't any map called " + name);
            return;
        }

        boolean error = false;
        for (String team : yml.getConfigurationSection("Team").getKeys(false)) {
            String colorS = Optional.ofNullable(yml.getString("Team." + team + ".Color"))
                    .map(String::toUpperCase).orElse(null);
            if (colorS == null) continue;
            try {
                TeamColor.valueOf(colorS);
            } catch (Exception e) {
                if (p != null) p.sendMessage("§cInvalid color at team: " + team + " in arena: " + name);
                plugin.getLogger().severe("Invalid color at team: " + team + " in arena: " + name);
                error = true;
            }
            for (String stuff : Arrays.asList("Color", "Spawn", "Bed", "Shop", "Upgrade", "Iron", "Gold")) {
                if (yml.get("Team." + team + "." + stuff) == null) {
                    if (p != null) p.sendMessage("§c" + stuff + " not set for " + team + " team on: " + name);
                    plugin.getLogger().severe(stuff + " not set for " + team + " team on: " + name);
                    error = true;
                }
            }
        }

        if (yml.get("generator.Diamond") == null)
            plugin.getLogger().warning("There aren't any Diamond generators set on: " + name);
        if (yml.get("generator.Emerald") == null)
            plugin.getLogger().warning("There aren't any Emerald generators set on: " + name);
        if (yml.get("waiting.Loc") == null) {
            if (p != null) p.sendMessage("§cWaiting spawn not set on: " + name);
            plugin.getLogger().severe("Waiting spawn not set on: " + name);
            return;
        }
        if (error) return;

        yKillHeight = yml.getInt(ConfigPath.ARENA_Y_LEVEL_KILL);
        addToEnableQueue(this);

        Language.saveIfNotExists(Messages.ARENA_DISPLAY_GROUP_PATH + getGroup().toLowerCase(),
                String.valueOf(getGroup().charAt(0)).toUpperCase() + group.substring(1).toLowerCase());
        Language.getLanguages().forEach(language -> {
            if (!language.exists(Messages.NPC_NAME_TEAM_UPGRADES.replace("%group%", group))) {
                language.generateNPCMessages(language.getYml(), group);
            }
        });
    }

    public static void ensureAllArenasLoaded() {
        if (!BedWars.autoscale) return;

        File arenasDir = new File(BedWars.plugin.getDataFolder(), "Arenas");
        if (!arenasDir.exists() || !arenasDir.isDirectory()) {
            BedWars.debug("[AutoScale] No 'Arenas' directory found.");
            return;
        }

        File[] arenaFiles = arenasDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (arenaFiles == null || arenaFiles.length == 0) {
            BedWars.debug("[AutoScale] No arena templates found to auto-load.");
            return;
        }

        int loaded = 0;
        for (File arenaFile : arenaFiles) {
            String name = arenaFile.getName().replace(".yml", "");
            if (Arena.getArenaByName(name) != null) continue;
            new Arena(name, null);
            loaded++;
        }

        BedWars.plugin.getLogger().info("[AutoScale] Bootstrapped " + loaded + " arena templates (" + arenaFiles.length + " found).");
    }

    public static IArena getArenaByName(String arenaName) {
        return arenaByName.get(arenaName);
    }

    public static IArena getArenaByIdentifier(String worldName) {
        return arenaByIdentifier.get(worldName);
    }

    public static @Nullable IArena getArenaByPlayer(Player p) {
        return arenaByPlayer.get(p);
    }

    public static void setArenaByPlayer(Player p, IArena arena) {
        arenaByPlayer.put(p, arena);
        arena.refreshSigns();
        JoinNPC.updateNPCs(arena.getGroup());
    }

    public static void setArenaByName(IArena arena) {
        arenaByName.put(arena.getArenaName(), arena);
    }

    public static void removeArenaByName(@NotNull String arena) {
        arenaByName.remove(arena.replace("_clone", ""));
    }

    public static void removeArenaByPlayer(Player p, @NotNull IArena arena) {
        arenaByPlayer.remove(p);
        arena.refreshSigns();
        JoinNPC.updateNPCs(arena.getGroup());
    }

    public static boolean hasStandbyForTemplate(String templateName) {
        for (IArena a : getEnableQueue()) {
            if (a.getArenaName().equalsIgnoreCase(templateName)) return true;
        }

        for (IArena a : getArenas()) {
            if (!a.getArenaName().equalsIgnoreCase(templateName)) continue;
            GameState s = a.getStatus();
            if (s == GameState.waiting || s == GameState.starting) return true;
        }
        return false;
    }

    public static int countStandbyForTemplate(String templateName) {
        int c = 0;
        for (IArena a : getEnableQueue()) {
            if (a.getArenaName().equalsIgnoreCase(templateName)) c++;
        }
        for (IArena a : getArenas()) {
            if (!a.getArenaName().equalsIgnoreCase(templateName)) continue;
            GameState s = a.getStatus();
            if (s == GameState.waiting || s == GameState.starting) c++;
        }
        return c;
    }

    public static boolean isVip(Player p) {
        return p.hasPermission(mainCmd + ".*") || p.hasPermission(mainCmd + ".vip");
    }

    public static void sendLobbyCommandItems(Player p) {
        if (!BedWars.config.getLobbyWorldName().equalsIgnoreCase(p.getWorld().getName())) return;
        p.getInventory().clear();

        for (IPermanentItem lobbyItem : BedWars.getAPI().getItemUtil().getLobbyItems()) {
            ItemStack item = lobbyItem.getItem();

            if (BedWars.nms.isPlayerHead(item.getType().name(), item.getDurability())) {
                item = buildPlayerHead(p, item, lobbyItem.getIdentifier());
            } else {
                item = BedWars.nms.addCustomData(item, lobbyItem.getIdentifier());
                item = BedWars.nms.setTag(item, "ACTION", lobbyItem.getIdentifier());
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String name = SupportPAPI.getSupportPAPI().replace(p, getMsg(p, Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_NAME.replace("%path%", lobbyItem.getIdentifier())));
                List<String> lore = SupportPAPI.getSupportPAPI().replace(p, getList(p, Messages.GENERAL_CONFIGURATION_LOBBY_ITEMS_LORE.replace("%path%", lobbyItem.getIdentifier())));
                meta.setDisplayName(name);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            if (lobbyItem.getHandler().isVisible(p, null)) {
                p.getInventory().setItem(lobbyItem.getSlot(), item);
            }
        }
    }

    public static boolean isInArena(Player p) {
        return arenaByPlayer.containsKey(p);
    }

    public static int getPlayers(@NotNull String group) {
        int i = 0;
        String[] groups = group.split("\\+");
        for (String g : groups)
            for (IArena a : getArenas()) if (a.getGroup().equalsIgnoreCase(g)) i += a.getPlayers().size();
        return i;
    }

    public static boolean joinRandomArena(Player p) {
        List<IArena> arenas = getSorted(getArenas());

        int amount = getPartyManager().hasParty(p) ? (int) getPartyManager().getMembers(p).stream().filter(member -> {
            IArena arena = Arena.getArenaByPlayer(member);
            return arena == null || arena.isSpectator(member);
        }).count() : 1;

        for (IArena a : arenas) {
            if (a.getPlayers().size() == a.getMaxPlayers()) continue;
            if (a.getMaxPlayers() - a.getPlayers().size() >= amount) {
                if (a.addPlayer(p, false)) break;
            }
        }
        return true;
    }

    public static List<IArena> getSorted(List<IArena> arenas) {
        List<IArena> sorted = new ArrayList<>(arenas);
        Collections.shuffle(sorted);
        sorted.sort((o1, o2) -> {
            if (o1.getStatus() == GameState.starting && o2.getStatus() == GameState.starting) {
                return Integer.compare(o2.getPlayers().size(), o1.getPlayers().size());
            } else if (o1.getStatus() == GameState.starting) {
                return -1;
            } else if (o2.getStatus() == GameState.starting) {
                return 1;
            } else if (o1.getStatus() == GameState.waiting && o2.getStatus() == GameState.waiting) {
                return Integer.compare(o2.getPlayers().size(), o1.getPlayers().size());
            } else if (o1.getStatus() == GameState.waiting) {
                return -1;
            } else if (o2.getStatus() == GameState.waiting) {
                return 1;
            } else if (o1.getStatus() == GameState.playing && o2.getStatus() == GameState.playing) {
                return 0;
            } else if (o1.getStatus() == GameState.playing) {
                return -1;
            } else return 1;
        });
        return sorted;
    }

    public static boolean joinRandomFromGroup(Player p, @NotNull String group) {
        List<IArena> arenas = getSorted(getArenas());

        int amount = getPartyManager().hasParty(p) ? (int) getPartyManager().getMembers(p).stream().filter(member -> {
            IArena arena = Arena.getArenaByPlayer(member);
            return arena == null || arena.isSpectator(member);
        }).count() : 1;

        String[] groups = group.split("\\+");
        for (IArena a : arenas) {
            if (a.getPlayers().size() == a.getMaxPlayers()) continue;
            for (String g : groups) {
                if (a.getGroup().equalsIgnoreCase(g)) {
                    if (a.getMaxPlayers() - a.getPlayers().size() >= amount) {
                        if (a.addPlayer(p, false)) return true;
                    }
                }
            }
        }
        return false;
    }

    public static void setGamesBeforeRestart(int gamesBeforeRestart) {
        Arena.gamesBeforeRestart = gamesBeforeRestart;
    }

    public static void removeFromEnableQueue(IArena a) {
        enableQueue.remove(a);
        if (!enableQueue.isEmpty()) {
            BedWars.getAPI().getRestoreAdapter().onEnable(enableQueue.get(0));
            plugin.getLogger().info("Loading arena: " + enableQueue.get(0).getWorldName());
        }
    }

    public static void addToEnableQueue(IArena a) {
        enableQueue.add(a);
        plugin.getLogger().info("Arena " + a.getWorldName() + " was added to the enable queue.");
        if (enableQueue.size() == 1) {
            BedWars.getAPI().getRestoreAdapter().onEnable(a);
            plugin.getLogger().info("Loading arena: " + a.getWorldName());
        }
    }

    public static boolean canAutoScale(String arenaName) {
        if (!autoscale) return false;
        if (Arena.getArenas().isEmpty()) return true;

        for (IArena ar : Arena.getEnableQueue()) if (ar.getArenaName().equalsIgnoreCase(arenaName)) return false;
        if (Arena.getGamesBeforeRestart() != -1 && Arena.getArenas().size() >= Arena.getGamesBeforeRestart())
            return false;

        int activeClones = 0;
        for (IArena ar : Arena.getArenas()) {
            if (ar.getArenaName().equalsIgnoreCase(arenaName)) {
                if (ar.getStatus() == GameState.waiting || ar.getStatus() == GameState.starting) return false;
            }
            if (ar.getArenaName().equals(arenaName)) activeClones++;
        }
        return config.getInt(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_AUTO_SCALE_LIMIT) > activeClones;
    }

    private static String formatElapsed(Instant start, Instant end) {
        if (start == null || end == null) return "0:00";
        long secs = java.time.Duration.between(start, end).getSeconds();
        if (secs < 0) secs = 0;
        long m = secs / 60;
        long s = secs % 60;
        return m + ":" + (s < 10 ? "0" + s : String.valueOf(s));
    }

    private static String centerText(String text, int chatWidth) {
        if (text == null) return "";
        text = ChatColor.translateAlternateColorCodes('&', text);

        int width = 0;
        boolean bold = false;
        for (char c : ChatColor.stripColor(text).toCharArray()) {
            switch (c) {
                case 'i':
                case 'l':
                case 't':
                case ' ':
                    width += 4;
                    break;
                case 'f':
                case 'k':
                    width += 5;
                    break;
                case 'm':
                case 'w':
                    width += 9;
                    break;
                default:
                    width += 6;
                    break;
            }
        }

        int spaces = (chatWidth - width) / 8;
        return " ".repeat(Math.max(0, spaces)) +
                text;
    }

    private static ItemStack buildPlayerHead(Player p, ItemStack template, String identifier) {
        ItemBuilder ib = new ItemBuilder(Material.SKULL_ITEM).setDurability(3).setSkull(p.getName());

        ItemMeta orig = template.getItemMeta();
        ItemStack head = ib.build();
        ItemMeta skullMeta = head.getItemMeta();
        if (orig != null && skullMeta != null) {
            skullMeta.setDisplayName(orig.getDisplayName());
            skullMeta.setLore(orig.getLore());
            for (ItemFlag f : orig.getItemFlags()) skullMeta.addItemFlags(f);
            for (java.util.Map.Entry<org.bukkit.enchantments.Enchantment, Integer> e : orig.getEnchants().entrySet()) {
                skullMeta.addEnchant(e.getKey(), e.getValue(), true);
            }
            head.setItemMeta(skullMeta);
        }

        head = BedWars.nms.addCustomData(head, identifier);
        head = BedWars.nms.setTag(head, "ACTION", identifier);
        return head;
    }

    @Override
    public void init(World world) {
        if (!autoscale && getArenaByName(arenaName) != null) return;

        if (initialized) {
            BedWars.debug("Arena " + arenaName + " was already initialized, ignoring duplicate init.");
            return;
        }
        setInitialized(true);

        removeFromEnableQueue(this);
        debug("Initialized arena " + getArenaName() + " with map " + world.getName());
        this.world = world;
        this.worldName = world.getName();
        getConfig().setName(worldName);

        try {
            this.linkedShop = ShopManager.shop;
            if (this.linkedShop != null) {
                ((ShopIndex) this.linkedShop).preResolveForArena(this);
            }
        } catch (Throwable ignored) {
        }
        try {
            this.linkedUpgrades = BedWars.getUpgradeManager().getMenuForArena(this);
        } catch (Throwable ignored) {
        }

        world.getEntities().stream().filter(e -> e.getType() != EntityType.PLAYER && e.getType() != EntityType.PAINTING && e.getType() != EntityType.ITEM_FRAME).forEach(Entity::remove);

        for (String s : getConfig().getList(ConfigPath.ARENA_GAME_RULES)) {
            String[] rule = s.split(":");
            if (rule.length == 2) world.setGameRuleValue(rule[0], rule[1]);
        }
        world.setAutoSave(false);

        for (Entity e : world.getEntities()) {
            if (e.getType() == EntityType.ARMOR_STAND && !((ArmorStand) e).isVisible()) e.remove();
        }

        for (String team : yml.getConfigurationSection("Team").getKeys(false)) {
            if (getTeam(team) != null) {
                BedWars.plugin.getLogger().severe("A team with name: " + team + " was already loaded for arena: " + getArenaName());
                continue;
            }
            var bwt = new BedWarsTeam(team, TeamColor.valueOf(yml.getString("Team." + team + ".Color").toUpperCase()), cm.getArenaLoc("Team." + team + ".Spawn"), cm.getArenaLoc("Team." + team + ".Bed"), cm.getArenaLoc("Team." + team + ".Shop"), cm.getArenaLoc("Team." + team + ".Upgrade"), this);
            teams.add(bwt);
            bwt.spawnGenerators();
        }

        Location location;
        for (String type : Arrays.asList("Diamond", "Emerald")) {
            if (yml.get("generator." + type) != null) {
                for (String s : yml.getStringList("generator." + type)) {
                    location = cm.convertStringToArenaLocation(s);
                    if (location == null) {
                        plugin.getLogger().severe("Invalid location for " + type + " generator: " + s);
                        continue;
                    }
                    oreGenerators.add(new OreGenerator(location, this, GeneratorType.valueOf(type.toUpperCase()), null, true));
                }
            }
        }

        arenas.add(this);
        arenaByName.put(getArenaName(), this);
        arenaByIdentifier.put(worldName, this);

        world.getWorldBorder().setCenter(cm.getArenaLoc("waiting.Loc"));
        world.getWorldBorder().setSize(yml.getInt("worldBorder"));

        if (!getConfig().getYml().isSet(ConfigPath.ARENA_WAITING_POS1) && getConfig().getYml().isSet(ConfigPath.ARENA_WAITING_POS2)) {
            plugin.getLogger().severe("Lobby Pos1 isn't set! The arena's lobby won't be removed!");
        }
        if (getConfig().getYml().isSet(ConfigPath.ARENA_WAITING_POS1) && !getConfig().getYml().isSet(ConfigPath.ARENA_WAITING_POS2)) {
            plugin.getLogger().severe("Lobby Pos2 isn't set! The arena's lobby won't be removed!");
        }

        registerSigns();
        Bukkit.getPluginManager().callEvent(new ArenaEnableEvent(this));

        respawnLocation = Optional.ofNullable(cm.getArenaLoc(ConfigPath.ARENA_SPEC_LOC)).orElse(Optional.ofNullable(cm.getArenaLoc("waiting.Loc")).orElse(world.getSpawnLocation()));

        spectatorLocation = Optional.ofNullable(cm.getArenaLoc(ConfigPath.ARENA_SPEC_LOC)).orElse(Optional.ofNullable(cm.getArenaLoc("waiting.Loc")).orElse(world.getSpawnLocation()));

        waitingLocation = Optional.ofNullable(cm.getArenaLoc("waiting.Loc")).orElse(world.getSpawnLocation());

        changeStatus(GameState.waiting);

        for (NextEvent ne : NextEvent.values()) nextEvents.add(ne.toString());

        upgradeDiamondsCount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_START) == null ? "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_II_START : getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_START);

        upgradeEmeraldsCount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_START) == null ? "Default." + ConfigPath.GENERATOR_EMERALD_TIER_II_START : getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_START);

        plugin.getLogger().info("Load done: " + getArenaName());

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File("spigot.yml"));
        renderDistance = yaml.get("world-settings." + getWorldName() + ".entity-tracking-range.players") == null ? yaml.getInt("world-settings.default.entity-tracking-range.players") : yaml.getInt("world-settings." + getWorldName() + ".entity-tracking-range.players");

        this.initialized = true;

        registerScoreboards();
    }

    public boolean addPlayer(Player p, boolean skipOwnerCheck) {
        if (p == null) return false;

        IArena current = getArenaByPlayer(p);
        if (current != null) {
            if (current.isSpectator(p)) current.removeSpectator(p, false);
            else current.removePlayer(p, false);
        }
        debug("Player added: " + p.getName() + " arena: " + getArenaName());

        boolean isStatusChange = false;
        isOnABase.remove(p);

        if (getArenaByPlayer(p) != null) return false;

        if (getPartyManager().hasParty(p) && !skipOwnerCheck) {
            if (!getPartyManager().isOwner(p)) {
                p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_DENIED_NOT_PARTY_LEADER));
                return false;
            }
            int partySize = (int) getPartyManager().getMembers(p).stream().filter(member -> {
                IArena arena = Arena.getArenaByPlayer(member);
                return arena == null || arena.isSpectator(member);
            }).count();

            if (partySize > maxInTeam * getTeams().size() - getPlayers().size()) {
                p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_DENIED_PARTY_TOO_BIG));
                return false;
            }
            for (Player mem : new ArrayList<>(getPartyManager().getMembers(p))) {
                if (mem == p) continue;
                IArena a = Arena.getArenaByPlayer(mem);
                if (a != null && a.isSpectator(mem)) a.removeSpectator(mem, false);
                addPlayer(mem, true);
            }
        }

        leaving.remove(p);

        if (status == GameState.waiting || (status == GameState.starting && startingTask != null && startingTask.getCountdown() > 1)) {
            if (players.size() >= maxPlayers && !isVip(p)) {
                TextComponent text = new TextComponent(getMsg(p, Messages.COMMAND_JOIN_DENIED_IS_FULL));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, config.getYml().getString("storeLink")));
                p.spigot().sendMessage(text);
                return false;
            } else if (players.size() >= maxPlayers) {
                boolean canJoin = false;
                for (Player on : new ArrayList<>(players)) {
                    if (!isVip(on)) {
                        canJoin = true;
                        removePlayer(on, false);
                        TextComponent vipKick = new TextComponent(getMsg(p, Messages.ARENA_JOIN_VIP_KICK));
                        vipKick.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, config.getYml().getString("storeLink")));
                        p.spigot().sendMessage(vipKick);
                        break;
                    }
                }
                if (!canJoin) {
                    p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_DENIED_IS_FULL_OF_VIPS));
                    return false;
                }
            }

            PlayerJoinArenaEvent ev = new PlayerJoinArenaEvent(this, p, false);
            Bukkit.getPluginManager().callEvent(ev);
            if (ev.isCancelled()) return false;

            ReJoin rejoin = ReJoin.getPlayer(p);
            if (rejoin != null) rejoin.destroy(true);

            p.closeInventory();
            players.add(p);
            p.setFlying(false);
            p.setAllowFlight(false);
            p.setHealth(p.getMaxHealth());

            broadcastJoin(p, ev.getMessage());

            setArenaByPlayer(p, this);

            if (status == GameState.waiting) {
                int teams = 0, teammates = 0, partyMembers = 0;

                for (Player on : getPlayers()) {
                    if (getPartyManager().isOwner(on)) teams++;
                    if (getPartyManager().hasParty(on)) {
                        teammates++;
                        partyMembers += getPartyManager().getMembers(on).size();
                    }
                }

                if (partyMembers >= maxPlayers) {
                    Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> changeStatus(GameState.starting), 10L);
                    isStatusChange = true;
                } else if (minPlayers <= players.size() && teams > 0 && players.size() != teammates / teams) {
                    Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> changeStatus(GameState.starting), 10L);
                    isStatusChange = true;
                } else if (players.size() >= minPlayers && teams == 0) {
                    Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> changeStatus(GameState.starting), 10L);
                    isStatusChange = true;
                }
            }

            if (players.size() >= getMaxPlayers() / 2 && players.size() > minPlayers && startingTask != null && Bukkit.getScheduler().isCurrentlyRunning(startingTask.getTask())) {
                if (startingTask.getCountdown() > getConfig().getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_HALF)) {
                    startingTask.setCountdown(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_HALF));
                }
            }

            if (getServerType() != ServerType.BUNGEE) {
                PlayerGoods.createIfNeeded(p, true);
                playerLocation.put(p, p.getLocation());
            }
            PaperSupport.teleportC(p, getWaitingLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

            sendPreGameCommandItems(p);
            for (PotionEffect pf : p.getActivePotionEffects()) p.removePotionEffect(pf.getType());

        } else if (status == GameState.playing || status == GameState.starting && startingTask != null && startingTask.getCountdown() <= 1) {
            addSpectator(p, false, null);
            return false;
        }

        p.getInventory().setArmorContents(null);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (getServerType() == ServerType.BUNGEE) BedWars.nms.sendPlayerSpawnPackets(p, this);
            for (Player on : Bukkit.getOnlinePlayers()) {
                if (on == null || on.equals(p)) continue;
                if (isPlayer(on)) {
                    BedWars.nms.spigotShowPlayer(p, on);
                    BedWars.nms.spigotShowPlayer(on, p);
                } else {
                    BedWars.nms.spigotHidePlayer(p, on);
                    BedWars.nms.spigotHidePlayer(on, p);
                }
            }
            if (getServerType() == ServerType.BUNGEE) BedWars.nms.sendPlayerSpawnPackets(p, this);
        }, 17L);

        if (getServerType() == ServerType.BUNGEE) p.getEnderChest().clear();

        if (getPlayers().size() >= getMaxPlayers() && startingTask != null && Bukkit.getScheduler().isCurrentlyRunning(startingTask.getTask())) {
            if (startingTask.getCountdown() > BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_SHORTENED)) {
                startingTask.setCountdown(BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_START_COUNTDOWN_SHORTENED));
            }
        }

        if (!isStatusChange && (BedWars.getServerType() == ServerType.MULTIARENA || BedWars.getServerType() == ServerType.SHARED)) {
            BoardManager.getInstance().giveTabFeatures(p, this, false);
        }

        refreshSigns();
        JoinNPC.updateNPCs(getGroup());
        return true;
    }

    private void broadcastJoin(Player p, String customMessage) {
        for (Player on : players) {
            Language language = getPlayerLanguage(on);
            if (customMessage == null || customMessage.isEmpty()) {
                on.sendMessage(getMsg(language, p, Messages.COMMAND_JOIN_PLAYER_JOIN_MSG).replace("%bw_v_prefix%", getChatSupport().getPrefix(p)).replace("%bw_v_suffix%", getChatSupport().getSuffix(p)).replace("%bw_playername%", p.getName()).replace("%bw_player%", p.getDisplayName()).replace("%bw_on%", String.valueOf(getPlayers().size())).replace("%bw_max%", String.valueOf(getMaxPlayers())));
            } else {
                on.sendMessage(customMessage);
            }
        }
    }

    public boolean addSpectator(@NotNull Player p, boolean playerBefore, Location staffTeleport) {
        if (!(allowSpectate || playerBefore || staffTeleport != null)) {
            p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_SPECTATOR_DENIED_MSG));
            return false;
        }

        var spectateEvent = new ArenaSpectateEvent(p, this);
        Bukkit.getPluginManager().callEvent(spectateEvent);
        if (spectateEvent.isCancelled()) return false;

        debug("Spectator added: " + p.getName() + " arena: " + getArenaName());

        if (!playerBefore) {
            var ev = new PlayerJoinArenaEvent(this, p, true);
            Bukkit.getPluginManager().callEvent(ev);
            if (ev.isCancelled()) return false;
        }

        ReJoin reJoin = ReJoin.getPlayer(p);
        if (reJoin != null) reJoin.destroy(true);

        p.closeInventory();
        spectators.add(p);
        players.remove(p);

        if (!playerBefore) {
            if (getServerType() != ServerType.BUNGEE) {
                PlayerGoods.createIfNeeded(p, true);
                playerLocation.put(p, p.getLocation());
            }
            setArenaByPlayer(p, this);
        }

        BoardManager.getInstance().giveTabFeatures(p, this, false);
        nms.setCollide(p, this, false);

        if (!playerBefore) {
            if (staffTeleport == null)
                PaperSupport.teleportC(p, getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            else PaperSupport.teleportC(p, staffTeleport, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        p.setGameMode(GameMode.ADVENTURE);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (leaving.contains(p)) return;
            p.setAllowFlight(true);
            p.setFlying(true);
        }, 5L);

        if (p.getPassenger() != null && p.getPassenger().getType() == EntityType.ARMOR_STAND) p.getPassenger().remove();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (leaving.contains(p)) return;
            for (Player on : Bukkit.getOnlinePlayers()) {
                if (on == p) continue;
                if (getSpectators().contains(on)) {
                    BedWars.nms.spigotShowPlayer(p, on);
                    BedWars.nms.spigotShowPlayer(on, p);
                } else if (getPlayers().contains(on)) {
                    BedWars.nms.spigotHidePlayer(p, on);
                    BedWars.nms.spigotShowPlayer(on, p);
                } else {
                    BedWars.nms.spigotHidePlayer(p, on);
                    BedWars.nms.spigotHidePlayer(on, p);
                }
            }

            if (!playerBefore) {
                if (staffTeleport == null)
                    PaperSupport.teleportC(p, getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                else PaperSupport.teleport(p, staffTeleport);
            } else {
                PaperSupport.teleport(p, getSpectatorLocation());
            }

            p.setAllowFlight(true);
            p.setFlying(true);

            sendSpectatorCommandItems(p);
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
            p.getInventory().setArmorContents(null);
        });

        leaving.remove(p);
        p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_SPECTATOR_MSG).replace("%bw_arena%", this.getDisplayName()));

        for (IGenerator o : getOreGenerators()) o.updateHolograms(p);
        for (ITeam t : getTeams()) {
            if (!t.isShopSpawned()) continue;
            nms.spawnShopHologram(getConfig().getArenaLoc("Team." + t.getName() + ".Upgrade"), (getMaxInTeam() > 1 ? Messages.NPC_NAME_TEAM_UPGRADES.replace("%group%", group) : Messages.NPC_NAME_SOLO_UPGRADES.replace("%group%", group)), Collections.singletonList(p), this, t);
            nms.spawnShopHologram(getConfig().getArenaLoc("Team." + t.getName() + ".Shop"), (getMaxInTeam() > 1 ? Messages.NPC_NAME_TEAM_SHOP.replace("%group%", group) : Messages.NPC_NAME_SOLO_SHOP.replace("%group%", group)), Collections.singletonList(p), this, t);
            for (IGenerator o : t.getGenerators()) o.updateHolograms(p);
        }

        showTime.remove(p);
        refreshSigns();
        JoinNPC.updateNPCs(getGroup());
        return true;
    }

    public void removePlayer(@NotNull Player p, boolean disconnect) {
        removePlayer(p, disconnect, false);
    }

    public void removePlayer(@NotNull Player p, boolean disconnect, boolean skipPartyCheck) {
        if (leaving.contains(p)) return;
        leaving.add(p);

        debug("Player removed: " + p.getName() + " arena: " + getArenaName());
        respawnSessions.remove(p);

        ITeam team = null;

        afkCheck.remove(p.getUniqueId());
        BedWars.getAPI().getAFKUtil().setPlayerAFK(p, false);

        if (status == GameState.playing) {
            for (ITeam t : getTeams()) {
                if (t.isMember(p)) {
                    team = t;
                    OfflineGraceService.startGrace(this, team, p);
                    t.getMembers().remove(p);
                    t.destroyBedHolo(p);
                }
            }
        }

        List<ShopCache.CachedItem> cacheList = new ArrayList<>();
        if (ShopCache.getInstance().getShopCache(p.getUniqueId()) != null) {
            cacheList = ShopCache.getInstance().getShopCache(p.getUniqueId()).getCachedPermanents();
        }

        LastHit lastHit = LastHit.getLastHit(p);
        Player lastDamager = null;
        if (lastHit != null && lastHit.getTime() >= System.currentTimeMillis() - 13_000) {
            if (lastHit.getDamager() instanceof Player) lastDamager = (Player) lastHit.getDamager();
        }
        Bukkit.getPluginManager().callEvent(new PlayerLeaveArenaEvent(p, this, lastDamager));

        players.remove(p);
        removeArenaByPlayer(p, this);

        for (PotionEffect pf : p.getActivePotionEffects()) p.removePotionEffect(pf.getType());
        if (p.getPassenger() != null && p.getPassenger().getType() == EntityType.ARMOR_STAND) p.getPassenger().remove();

        boolean teamuri = getPlayers().stream().anyMatch(on -> getPartyManager().hasParty(on));

        if (status == GameState.starting && ((maxInTeam > players.size() && teamuri) || (players.size() < minPlayers && !teamuri))) {
            changeStatus(GameState.waiting);
            for (Player on : players)
                on.sendMessage(getMsg(on, Messages.ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT));
        } else if (status == GameState.playing) {
            int alive_teams = 0;
            for (ITeam t : getTeams()) {
                if (t != null && (!t.getMembers().isEmpty()
                        || OfflineGraceService.hasActiveForTeam(this, t))) {
                    alive_teams++;
                }
            }
            if (alive_teams == 1 && !BedWars.isShuttingDown()) {
                checkWinner();
                Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> changeStatus(GameState.restarting), 10L);
                if (team != null && !team.isBedDestroyed()) {
                    for (Player p2 : this.getPlayers()) {
                        p2.sendMessage(getMsg(p2, Messages.TEAM_ELIMINATED_CHAT).replace("%bw_team_color%", team.getColor().chat().toString()).replace("%bw_team_name%", team.getDisplayName(getPlayerLanguage(p2))));
                    }
                    for (Player p2 : this.getSpectators()) {
                        p2.sendMessage(getMsg(p2, Messages.TEAM_ELIMINATED_CHAT).replace("%bw_team_color%", team.getColor().chat().toString()).replace("%bw_team_name%", team.getDisplayName(getPlayerLanguage(p2))));
                    }
                }
            } else if (alive_teams == 0 && !BedWars.isShuttingDown()) {
                Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> changeStatus(GameState.restarting), 10L);
            } else if (!BedWars.isShuttingDown()) {
                new ReJoin(p, this, team, cacheList);
            }

            if (team != null) {
                ITeam killerTeam = getTeam(lastDamager);
                if (lastDamager != null && isPlayer(lastDamager) && killerTeam != null) {
                    String message;
                    PlayerKillEvent.PlayerKillCause cause;
                    if (team.isBedDestroyed()) {
                        cause = PlayerKillEvent.PlayerKillCause.PLAYER_DISCONNECT_FINAL;
                        message = Messages.PLAYER_DIE_PVP_LOG_OUT_FINAL;
                    } else {
                        message = Messages.PLAYER_DIE_PVP_LOG_OUT_REGULAR;
                        cause = PlayerKillEvent.PlayerKillCause.PLAYER_DISCONNECT;
                    }
                    PlayerKillEvent event = new PlayerKillEvent(this, p, lastDamager, player -> Language.getMsg(player, message), cause);
                    for (Player inGame : getPlayers()) {
                        Language lang = getPlayerLanguage(inGame);
                        inGame.sendMessage(event.getMessage().apply(inGame).replace("%bw_team_name%", team.getDisplayName(lang)).replace("%bw_player_color%", team.getColor().chat().toString()).replace("%bw_player%", p.getDisplayName()).replace("%bw_playername%", p.getName()).replace("%bw_killer_color%", killerTeam.getColor().chat().toString()).replace("%bw_killer_name%", lastDamager.getDisplayName()).replace("%bw_killer_team_name%", killerTeam.getDisplayName(lang)));
                    }
                    for (Player inGame : getSpectators()) {
                        Language lang = getPlayerLanguage(inGame);
                        inGame.sendMessage(event.getMessage().apply(inGame).replace("%bw_team_name%", team.getDisplayName(lang)).replace("%bw_player_color%", team.getColor().chat().toString()).replace("%bw_player%", p.getDisplayName()).replace("%bw_playername%", p.getName()).replace("%bw_killer_color%", killerTeam.getColor().chat().toString()).replace("%bw_killer_name%", lastDamager.getDisplayName()).replace("%bw_killer_team_name%", killerTeam.getDisplayName(lang)));
                    }
                    PlayerDrops.handlePlayerDrops(this, p, lastDamager, team, killerTeam, cause, new ArrayList<>(Arrays.asList(p.getInventory().getContents())));
                }
            }
        }

        for (Player on : getPlayers()) {
            Language language = getPlayerLanguage(on);
            on.sendMessage(getMsg(language, p, Messages.COMMAND_LEAVE_MSG).replace("%bw_v_prefix%", getChatSupport().getPrefix(p)).replace("%bw_v_suffix%", getChatSupport().getSuffix(p)).replace("%bw_playername%", p.getName()).replace("%bw_player%", p.getDisplayName()).replace("%bw_on%", String.valueOf(getPlayers().size())).replace("%bw_max%", String.valueOf(getMaxPlayers())));
        }
        for (Player on : getSpectators()) {
            Language language = getPlayerLanguage(on);
            on.sendMessage(getMsg(language, p, Messages.COMMAND_LEAVE_MSG).replace("%bw_v_prefix%", getChatSupport().getPrefix(p)).replace("%bw_v_suffix%", getChatSupport().getSuffix(p)).replace("%bw_playername%", p.getName()).replace("%bw_player%", p.getDisplayName()).replace("%bw_on%", String.valueOf(getPlayers().size())).replace("%bw_max%", String.valueOf(getMaxPlayers())));
        }

        if (getServerType() == ServerType.SHARED) {
            BoardManager.getInstance().remove(p);
            this.sendToMainLobby(p);
        } else if (getServerType() == ServerType.BUNGEE) {
            Misc.moveToLobbyOrKick(p, this, true);
            return;
        } else {
            this.sendToMainLobby(p);
        }

        String iso = Language.getPlayerLanguage(p).getIso();
        List<ShopHolo> holos = shopHolosIso.getOrDefault(iso, Collections.emptyList());
        for (ShopHolo holo : holos) holo.clearForPlayer(p);

        PlayerGoods pg = PlayerGoods.getPlayerGoods(p);
        if (pg == null) {
            if (BedWars.getServerType() == ServerType.MULTIARENA) sendLobbyCommandItems(p);
        } else {
            pg.restore();
        }
        playerLocation.remove(p);
        for (PotionEffect pf : p.getActivePotionEffects()) p.removePotionEffect(pf.getType());

        if (!BedWars.isShuttingDown()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for (Player on : Bukkit.getOnlinePlayers()) {
                    if (on.equals(p)) continue;
                    if (getArenaByPlayer(on) == null) {
                        BedWars.nms.spigotShowPlayer(p, on);
                        BedWars.nms.spigotShowPlayer(on, p);
                    } else {
                        BedWars.nms.spigotHidePlayer(p, on);
                        BedWars.nms.spigotHidePlayer(on, p);
                    }
                }
                if (!disconnect) BoardManager.getInstance().giveTabFeatures(p, null, false);
            }, 5L);
        }

        if (!skipPartyCheck && getPartyManager().hasParty(p)) {
            if (getPartyManager().isOwner(p)) {
                if (status != GameState.restarting) {
                    teamuri = getPlayers().stream().anyMatch(on -> getPartyManager().hasParty(on));
                    if (status == GameState.starting && ((maxInTeam > players.size() && teamuri) || (players.size() < minPlayers && !teamuri))) {
                        changeStatus(GameState.waiting);
                        for (Player on : players)
                            on.sendMessage(getMsg(on, Messages.ARENA_START_COUNTDOWN_STOPPED_INSUFF_PLAYERS_CHAT));
                    }
                }
            } else {
                getPartyManager().removeFromParty(p);
            }
        }

        p.setFlying(false);
        p.setAllowFlight(false);

        if (status == GameState.restarting && ReJoin.exists(p)) {
            if (Objects.requireNonNull(ReJoin.getPlayer(p)).getArena() == this)
                Objects.requireNonNull(ReJoin.getPlayer(p)).destroy(false);
        }

        Integer taskId = magicMilk.remove(p.getUniqueId());
        if (taskId != null && taskId > 0) Bukkit.getScheduler().cancelTask(taskId);

        showTime.remove(p);
        refreshSigns();
        JoinNPC.updateNPCs(getGroup());
        if (lastHit != null) lastHit.remove();
    }

    public void removeSpectator(@NotNull Player p, boolean disconnect) {
        removeSpectator(p, disconnect, false);
    }

    public void removeSpectator(@NotNull Player p, boolean disconnect, boolean skipPartyCheck) {
        if (leaving.contains(p)) return;
        leaving.add(p);

        debug("Spectator removed: " + p.getName() + " arena: " + getArenaName());

        Bukkit.getPluginManager().callEvent(new PlayerLeaveArenaEvent(p, this, null));
        spectators.remove(p);
        removeArenaByPlayer(p, this);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        nms.setCollide(p, this, true);

        afkCheck.remove(p.getUniqueId());
        BedWars.getAPI().getAFKUtil().setPlayerAFK(p, false);

        if (getServerType() == ServerType.SHARED) {
            BoardManager.getInstance().remove(p);
            this.sendToMainLobby(p);
        } else if (getServerType() == ServerType.MULTIARENA) {
            this.sendToMainLobby(p);
        }

        String iso = Language.getPlayerLanguage(p).getIso();
        List<ShopHolo> holos = shopHolosIso.getOrDefault(iso, Collections.emptyList());
        for (ShopHolo holo : holos) holo.clearForPlayer(p);

        for (PotionEffect pf : p.getActivePotionEffects()) p.removePotionEffect(pf.getType());

        PlayerGoods pg = PlayerGoods.getPlayerGoods(p);
        if (pg == null) {
            if (BedWars.getServerType() == ServerType.MULTIARENA) sendLobbyCommandItems(p);
        } else {
            pg.restore();
        }
        if (getServerType() == ServerType.BUNGEE) {
            Misc.moveToLobbyOrKick(p, this, true);
            return;
        }
        playerLocation.remove(p);

        if (!BedWars.isShuttingDown()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player on : Bukkit.getOnlinePlayers()) {
                    if (on.equals(p)) continue;
                    if (getArenaByPlayer(on) == null) {
                        BedWars.nms.spigotShowPlayer(p, on);
                        BedWars.nms.spigotShowPlayer(on, p);
                    } else {
                        BedWars.nms.spigotHidePlayer(p, on);
                        BedWars.nms.spigotHidePlayer(on, p);
                    }
                }
                if (!disconnect) BoardManager.getInstance().giveTabFeatures(p, null, false);
            });
        }

        if (!skipPartyCheck && getPartyManager().hasParty(p) && !getPartyManager().isOwner(p)) {
            getPartyManager().removeFromParty(p);
        }

        p.setFlying(false);
        p.setAllowFlight(false);

        if (ReJoin.exists(p) && ReJoin.getPlayer(p).getArena() == this) ReJoin.getPlayer(p).destroy(false);

        if (magicMilk.containsKey(p.getUniqueId())) {
            int taskId = magicMilk.get(p.getUniqueId());
            if (taskId > 0) Bukkit.getScheduler().cancelTask(taskId);
        }

        refreshSigns();
        JoinNPC.updateNPCs(getGroup());
    }

    public boolean switchPlayerTeam(Player player, ITeam targetTeam, boolean respawnNow) {
        if (player == null || targetTeam == null) return false;
        if (getStatus() != GameState.playing) return false;

        ITeam current = getTeam(player);
        if (current != null && current.equals(targetTeam)) return false;

        if (current != null) current.getMembers().remove(player);

        // add to new team
        targetTeam.addPlayers(player);

        // optional: immediate reposition + inventory refresh
        if (respawnNow) {
            targetTeam.respawnMember(player);
        } else {
            // at least teleport to new spawn
            player.teleport(targetTeam.getSpawn());
        }

        return true;
    }

    public boolean reJoin(Player p) {
        ReJoin reJoin = ReJoin.getPlayer(p);
        if (reJoin == null || reJoin.getArena() != this || !reJoin.canReJoin()) return false;

        if (reJoin.getTask() != null) reJoin.getTask().destroy();

        PlayerReJoinEvent ev = new PlayerReJoinEvent(p, this, BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_REJOIN_RE_SPAWN_COUNTDOWN));
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return false;

        for (Player on : Bukkit.getOnlinePlayers()) {
            if (on.equals(p)) continue;
            if (!isInArena(on)) {
                BedWars.nms.spigotHidePlayer(on, p);
                BedWars.nms.spigotHidePlayer(p, on);
            }
        }

        p.closeInventory();
        players.add(p);
        leaving.remove(p);

        for (Player on : players) {
            on.sendMessage(getMsg(on, Messages.COMMAND_REJOIN_PLAYER_RECONNECTED).replace("%bw_playername%", p.getName()).replace("%bw_player%", p.getDisplayName()).replace("%bw_on%", String.valueOf(getPlayers().size())).replace("%bw_max%", String.valueOf(getMaxPlayers())));
        }
        for (Player on : spectators) {
            on.sendMessage(getMsg(on, Messages.COMMAND_REJOIN_PLAYER_RECONNECTED).replace("%bw_playername%", p.getName()).replace("%bw_player%", p.getDisplayName()).replace("%bw_on%", String.valueOf(getPlayers().size())).replace("%bw_max%", String.valueOf(getMaxPlayers())));
        }
        setArenaByPlayer(p, this);

        if (BedWars.getServerType() != ServerType.BUNGEE) playerLocation.put(p, p.getLocation());
        PaperSupport.teleportC(p, getSpectatorLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        p.getInventory().clear();

        ShopCache sc = ShopCache.getInstance().getShopCache(p.getUniqueId());
        if (sc != null) sc.destroy();
        sc = new ShopCache(p.getUniqueId());
        for (ShopCache.CachedItem ci : reJoin.getPermanentsAndNonDowngradables()) sc.getCachedItems().add(ci);

        OfflineGraceService.cancelGrace(p.getUniqueId());

        reJoin.getBedWarsTeam().reJoin(p, ev.getRespawnTime());
        reJoin.destroy(false);

        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> BoardManager.getInstance().giveTabFeatures(p, this, true), 10L);
        return true;
    }

    public void disable() {
        for (Player p : new ArrayList<>(players)) removePlayer(p, false);
        for (Player p : new ArrayList<>(spectators)) removeSpectator(p, false);

        cancelTasks();
        plugin.getLogger().log(Level.WARNING, "Disabling arena: " + getArenaName());
        for (Player inWorld : getWorld().getPlayers()) inWorld.kickPlayer("You're not supposed to be here.");
        BedWars.getAPI().getRestoreAdapter().onDisable(this);
        Bukkit.getPluginManager().callEvent(new ArenaDisableEvent(getArenaName(), getWorldName()));
        destroyData();
    }

    public void restart() {
        this.initialized = false;
        cancelTasks();
        plugin.getLogger().log(Level.FINE, "Restarting arena: " + getArenaName());
        Bukkit.getPluginManager().callEvent(new ArenaRestartEvent(getArenaName(), getWorldName()));
        for (Player inWorld : getWorld().getPlayers()) inWorld.kickPlayer("You're not supposed to be here.");
        BedWars.getAPI().getRestoreAdapter().onRestart(this);
        destroyData();
    }

    private void cancelTasks() {
        if (getRestartingTask() != null) getRestartingTask().cancel();
        if (getStartingTask() != null) getStartingTask().cancel();
        if (getPlayingTask() != null) getPlayingTask().cancel();
        if (getAnnouncementTask() != null) getAnnouncementTask().cancel();
        if (moneyperMinuteTask != null) moneyperMinuteTask.cancel();
        if (perMinuteTask != null) perMinuteTask.cancel();
    }

    @Override
    public int getMaxInTeam() {
        return maxInTeam;
    }

    public String getDisplayStatus(Language lang) {
        String s = switch (status) {
            case waiting -> lang.m(Messages.ARENA_STATUS_WAITING_NAME);
            case starting -> lang.m(Messages.ARENA_STATUS_STARTING_NAME);
            case restarting -> lang.m(Messages.ARENA_STATUS_RESTARTING_NAME);
            case playing -> lang.m(Messages.ARENA_STATUS_PLAYING_NAME);
        };
        return s.replace("%bw_full%", this.getPlayers().size() == this.getMaxPlayers() ? lang.m(Messages.MEANING_FULL) : "");
    }

    @Override
    public String getDisplayGroup(Player player) {
        return getPlayerLanguage(player).m(Messages.ARENA_DISPLAY_GROUP_PATH + getGroup().toLowerCase());
    }

    @Override
    public String getDisplayGroup(@NotNull Language language) {
        return language.m(Messages.ARENA_DISPLAY_GROUP_PATH + getGroup().toLowerCase());
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public String getDisplayName() {
        String def = (Character.toUpperCase(arenaName.charAt(0)) + arenaName.substring(1)).replace("_", " ").replace("-", " ");
        return getConfig().getYml().getString(ConfigPath.ARENA_DISPLAY_NAME, def).trim().isEmpty() ? def : getConfig().getString(ConfigPath.ARENA_DISPLAY_NAME);
    }

    @Override
    public void setWorldName(String name) {
        this.worldName = name;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public void setGroup(String group) {
        this.group = group;
        scoreboards.forEach(Scoreboard::unregister);
        registerScoreboards();
    }

    @Override
    public String getArenaName() {
        return arenaName;
    }

    @Override
    public List<ITeam> getTeams() {
        return teams;
    }

    @Override
    public ArenaConfig getConfig() {
        return cm;
    }

    @Override
    public void addPlacedBlock(Block block) {
        if (block == null) return;
        placed.add(new Vector(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public void removePlacedBlock(Block block) {
        if (block == null) return;
        if (!isBlockPlaced(block)) return;
        placed.remove(new Vector(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public boolean isBlockPlaced(Block block) {
        for (Vector v : getPlaced()) {
            if (v.getX() == block.getX() && v.getY() == block.getY() && v.getZ() == block.getZ()) return true;
        }
        return false;
    }

    @Override
    public int getPlayerKills(Player p, boolean finalKills) {
        return finalKills ? playerFinalKills.getOrDefault(p, 0) : playerKills.getOrDefault(p.getName(), 0);
    }

    @Override
    public int getPlayerTotalKills(Player p) {
        return playerTotalKills.getOrDefault(p.getName(), 0);
    }

    public int getPlayerBedsDestroyed(Player p) {
        return playerBedsDestroyed.getOrDefault(p, 0);
    }

    @Override
    public void registerScoreboards() {
        scoreboards = BoardManager.getInstance().registerArenaScoreboards(this);
    }

    public void changeStatus(GameState newStatus) {
        if (newStatus == GameState.restarting && this.status == GameState.restarting) return;

        GameState old = this.status;
        if (old != GameState.playing && newStatus == GameState.playing) startTime = Instant.now();
        this.status = newStatus;
        Bukkit.getPluginManager().callEvent(new GameStateChangeEvent(this, old, newStatus));
        refreshSigns();

        if (newStatus == GameState.playing) {
            for (Player p : players) {
                afkCheck.remove(p.getUniqueId());
                BedWars.getAPI().getAFKUtil().setPlayerAFK(p, false);
            }
            for (Player p : spectators) {
                afkCheck.remove(p.getUniqueId());
                BedWars.getAPI().getAFKUtil().setPlayerAFK(p, false);
            }
        }

        BukkitScheduler bs = Bukkit.getScheduler();
        if (startingTask != null && (bs.isCurrentlyRunning(startingTask.getTask()) || bs.isQueued(startingTask.getTask())))
            startingTask.cancel();
        startingTask = null;

        if (playingTask != null && (bs.isCurrentlyRunning(playingTask.getTask()) || bs.isQueued(playingTask.getTask())))
            playingTask.cancel();
        playingTask = null;

        if (restartingTask != null && (bs.isCurrentlyRunning(restartingTask.getTask()) || bs.isQueued(restartingTask.getTask())))
            restartingTask.cancel();
        restartingTask = null;

        if (moneyperMinuteTask != null) moneyperMinuteTask.cancel();
        if (perMinuteTask != null) perMinuteTask.cancel();

        if (newStatus == GameState.starting) {
            startingTask = new GameStartingTask(this);
        } else if (newStatus == GameState.playing) {
            if (BedWars.getLevelSupport() instanceof InternalLevel) perMinuteTask = new PerMinuteTask(this);
            if (BedWars.getEconomy() instanceof WithEconomy) moneyperMinuteTask = new MoneyPerMinuteTask(this);
            playingTask = new GamePlayingTask(this);
            if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_IN_GAME_ANNOUNCEMENT_ENABLE)) {
                announcementTask = new GameAnnouncementTask(this);
            }
        } else if (newStatus == GameState.restarting) {
            restartingTask = new GameRestartingTask(this);
        }

        PlayerPlaceholder prefixPlaceholderTab = (PlayerPlaceholder) TabAPI.getInstance().getPlaceholderManager().getPlaceholder("%bw_prefix_tab%");
        PlayerPlaceholder suffixPlaceholderTab = (PlayerPlaceholder) TabAPI.getInstance().getPlaceholderManager().getPlaceholder("%bw_suffix_tab%");
        PlayerPlaceholder prefixPlaceholderHead = (PlayerPlaceholder) TabAPI.getInstance().getPlaceholderManager().getPlaceholder("%bw_prefix_head%");
        PlayerPlaceholder suffixPlaceholderHead = (PlayerPlaceholder) TabAPI.getInstance().getPlaceholderManager().getPlaceholder("%bw_suffix_head%");

        players.forEach(c -> updateTab(c, prefixPlaceholderTab, suffixPlaceholderTab, prefixPlaceholderHead, suffixPlaceholderHead));
        spectators.forEach(c -> updateTab(c, prefixPlaceholderTab, suffixPlaceholderTab, prefixPlaceholderHead, suffixPlaceholderHead));
    }

    private void updateTab(Player c, PlayerPlaceholder pt, PlayerPlaceholder st, PlayerPlaceholder ph, PlayerPlaceholder sh) {
        BoardManager.getInstance().giveTabFeatures(c, this, false);
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(c.getUniqueId());
        if (tabPlayer == null) return;
        pt.updateValue(tabPlayer, BoardManager.getInstance().getPrefixTab(tabPlayer));
        st.updateValue(tabPlayer, BoardManager.getInstance().getSuffixTab(tabPlayer));
        ph.updateValue(tabPlayer, BoardManager.getInstance().getPrefixHead(tabPlayer));
        sh.updateValue(tabPlayer, BoardManager.getInstance().getSuffixHead(tabPlayer));
    }

    @Override
    public boolean isPlayer(Player p) {
        return players != null && players.contains(p);
    }

    @Override
    public boolean isSpectator(Player p) {
        return spectators.contains(p);
    }

    @Override
    public boolean isSpectator(UUID player) {
        for (Player p : getSpectators()) if (p.getUniqueId().equals(player)) return true;
        return false;
    }

    @Override
    public boolean isReSpawning(UUID player) {
        if (player == null) return false;
        for (Player reSpawnSession : respawnSessions.keySet())
            if (reSpawnSession.getUniqueId().equals(player)) return true;
        return false;
    }

    public void addSign(Location loc) {
        if (loc == null) return;
        var type = loc.getBlock().getType().toString();
        if (type.endsWith("_SIGN") || type.endsWith("_WALL_SIGN")) {
            signs.add(loc.getBlock());
            refreshSigns();
            BlockStatusListener.updateBlock(this);
        }
    }

    @Override
    public GameState getStatus() {
        return status;
    }

    public void setStatus(GameState status) {
        if (this.status != GameState.playing && status == GameState.playing) startTime = Instant.now();
        if (this.status == GameState.starting && status == GameState.waiting) {
            for (Player player : getPlayers()) {
                Language playerLang = getPlayerLanguage(player);
                nms.sendTitle(player, playerLang.m(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_TITLE), playerLang.m(Messages.ARENA_STATUS_START_COUNTDOWN_CANCELLED_SUB_TITLE), 0, 40, 10);
            }
        }
        this.status = status;
    }

    public synchronized void refreshSigns() {
        for (Block b : getSigns()) {
            if (b == null) continue;
            var t = b.getType().toString();
            if (!(t.endsWith("_SIGN") || t.endsWith("_WALL_SIGN"))) continue;
            if (!(b.getState() instanceof Sign s)) continue;
            int line = 0;
            for (String string : BedWars.signs.getList("format")) {
                if (string == null) continue;
                if (getPlayers() == null) continue;
                s.setLine(line, string.replace("[on]", String.valueOf(getPlayers().size())).replace("[max]", String.valueOf(getMaxPlayers())).replace("[arena]", getDisplayName()).replace("[status]", getDisplayStatus(Language.getDefaultLanguage())).replace("[type]", String.valueOf(getMaxInTeam())));
                line++;
            }
            try {
                s.update(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public List<Player> getSpectators() {
        return spectators;
    }

    public void addPlayerKill(Player p, boolean finalKill, Player victim) {
        if (p == null) return;
        playerTotalKills.merge(p.getName(), 1, Integer::sum);
        if (finalKill) {
            playerFinalKills.merge(p, 1, Integer::sum);
            playerFinalKillDeaths.put(victim, 1);
        } else {
            playerKills.merge(p.getName(), 1, Integer::sum);
        }
    }

    public void addPlayerBedDestroyed(Player p) {
        playerBedsDestroyed.merge(p, 1, Integer::sum);
    }

    public void sendPreGameCommandItems(Player p) {
        p.getInventory().clear();

        for (IPermanentItem preGameItem : BedWars.getAPI().getItemUtil().getPreGameItems()) {
            ItemStack item = preGameItem.getItem();

            if (BedWars.nms.isPlayerHead(item.getType().name(), item.getDurability())) {
                item = buildPlayerHead(p, item, preGameItem.getIdentifier());
            } else {
                item = BedWars.nms.addCustomData(item, preGameItem.getIdentifier());
                item = BedWars.nms.setTag(item, "ACTION", preGameItem.getIdentifier());
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String name = SupportPAPI.getSupportPAPI().replace(p, getMsg(p, Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_NAME.replace("%path%", preGameItem.getIdentifier())));
                List<String> lore = SupportPAPI.getSupportPAPI().replace(p, getList(p, Messages.GENERAL_CONFIGURATION_WAITING_ITEMS_LORE.replace("%path%", preGameItem.getIdentifier())));
                meta.setDisplayName(name);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            if (preGameItem.getHandler().isVisible(p, this)) {
                p.getInventory().setItem(preGameItem.getSlot(), item);
            }
        }
    }

    public void sendSpectatorCommandItems(Player p) {
        p.getInventory().clear();

        for (IPermanentItem spectatorItem : BedWars.getAPI().getItemUtil().getSpectatorItems()) {
            ItemStack item = spectatorItem.getItem();

            if (BedWars.nms.isPlayerHead(item.getType().name(), item.getDurability())) {
                item = buildPlayerHead(p, item, spectatorItem.getIdentifier());
            } else {
                item = BedWars.nms.addCustomData(item, spectatorItem.getIdentifier());
                item = BedWars.nms.setTag(item, "ACTION", spectatorItem.getIdentifier());
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String name = SupportPAPI.getSupportPAPI().replace(p, getMsg(p, Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_NAME.replace("%path%", spectatorItem.getIdentifier())));
                List<String> lore = SupportPAPI.getSupportPAPI().replace(p, getList(p, Messages.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_LORE.replace("%path%", spectatorItem.getIdentifier())));
                meta.setDisplayName(name);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            if (spectatorItem.getHandler().isVisible(p, this)) {
                p.getInventory().setItem(spectatorItem.getSlot(), item);
            }
        }
    }

    @Override
    public ITeam getTeam(Player p) {
        for (ITeam t : getTeams()) if (t.isMember(p)) return t;
        return null;
    }

    @Override
    public ITeam getExTeam(UUID p) {
        for (ITeam t : getTeams()) if (t.wasMember(p)) return t;
        return null;
    }

    public void checkWinner() {
        if (status == GameState.restarting) return;

        int aliveTeams = 0;
        ITeam winnerCandidate = null;

        for (ITeam t : getTeams()) {
            boolean teamAlive = !t.getMembers().isEmpty()
                    || OfflineGraceService.hasActiveForTeam(this, t);
            if (teamAlive) {
                aliveTeams++;
                if (!t.getMembers().isEmpty()) {
                    winnerCandidate = t;
                }
            }
        }

        if (aliveTeams != 1) {
            if (players.isEmpty() && status != GameState.restarting) changeStatus(GameState.restarting);
            return;
        }

        if (winnerCandidate == null) return;

        for (Player p : winnerCandidate.getMembers()) if (p.isOnline()) p.getInventory().clear();

        String formattedTime = formatElapsed(getStartTime(), Instant.now());

        Player topBedBreaker = null;
        int topBeds = 0;

        Player firstPlayer = null, secondPlayer = null, thirdPlayer = null;
        StringBuilder winners = new StringBuilder();

        for (int i = 0; i < winnerCandidate.getMembers().size(); i++) {
            Player p = winnerCandidate.getMembers().get(i);
            if (p.getWorld().equals(getWorld()))
                nms.sendTitle(p, getMsg(p, Messages.GAME_END_VICTORY_PLAYER_TITLE), null, 0, 70, 20);
            if (!winners.toString().contains(p.getDisplayName())) {
                if (winnerCandidate.getSize() > 1 && i + 1 != winnerCandidate.getMembers().size()) {
                    winners.append(getMsg(p, Messages.FORMATTING_EACH_WINNER)
                                    .replace("%bw_v_prefix%", getChatSupport().getPrefix(p))
                                    .replace("%bw_v_suffix%", getChatSupport().getSuffix(p))
                                    .replace("%bw_playername%", p.getName())
                                    .replace("%bw_player%", p.getDisplayName()))
                            .append("§7, ");
                } else {
                    winners.append(getMsg(p, Messages.FORMATTING_EACH_WINNER)
                            .replace("%bw_v_prefix%", getChatSupport().getPrefix(p))
                            .replace("%bw_v_suffix%", getChatSupport().getSuffix(p))
                            .replace("%bw_playername%", p.getName())
                            .replace("%bw_player%", p.getDisplayName()));
                }
            }
        }

        int first = 0, second = 0, third = 0;

        if (!playerBedsDestroyed.isEmpty()) {
            for (Map.Entry<Player, Integer> e : playerBedsDestroyed.entrySet()) {
                if (e.getKey() == null) continue;
                int beds = e.getValue() == null ? 0 : e.getValue();
                if (beds > topBeds) {
                    topBeds = beds;
                    topBedBreaker = e.getKey();
                }
            }
        }

        if (!playerTotalKills.isEmpty()) {
            LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();
            playerTotalKills.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

            int entry = 0;
            for (Map.Entry<String, Integer> e : reverseSortedMap.entrySet()) {
                if (entry == 0) {
                    firstPlayer = Bukkit.getPlayerExact(e.getKey());
                    first = e.getValue();
                } else if (entry == 1) {
                    secondPlayer = Bukkit.getPlayerExact(e.getKey());
                    second = e.getValue();
                } else if (entry == 2) {
                    thirdPlayer = Bukkit.getPlayerExact(e.getKey());
                    third = e.getValue();
                    break;
                }
                entry++;
            }
        }

        for (Player player : world.getPlayers()) {
            if (!winnerCandidate.getMembers().contains(player))
                nms.sendTitle(player, getMsg(player, Messages.GAME_END_GAME_OVER_PLAYER_TITLE), null, 0, 70, 20);

            for (String s : getList(player, Messages.GAME_END_TOP_PLAYER_CHAT)) {
                String message = s
                        .replace("%bw_first_format%", firstPlayer == null ? getMsg(player, Messages.MEANING_NOBODY)
                                : getMsg(firstPlayer, Messages.GAME_END_FIRST_KILLER)
                                .replace("%bw_v_prefix%", getChatSupport().getPrefix(firstPlayer))
                                .replace("%bw_v_suffix%", getChatSupport().getSuffix(firstPlayer))
                                .replace("%bw_playername%", firstPlayer.getName())
                                .replace("%bw_player%", firstPlayer.getDisplayName()))
                        .replace("%bw_first_kills%", String.valueOf(first))
                        .replace("%bw_second_format%", secondPlayer == null ? getMsg(player, Messages.MEANING_NOBODY)
                                : getMsg(secondPlayer, Messages.GAME_END_SECOND_KILLER)
                                .replace("%bw_v_prefix%", getChatSupport().getPrefix(secondPlayer))
                                .replace("%bw_v_suffix%", getChatSupport().getSuffix(secondPlayer))
                                .replace("%bw_playername%", secondPlayer.getName())
                                .replace("%bw_player%", secondPlayer.getDisplayName()))
                        .replace("%bw_second_kills%", String.valueOf(second))
                        .replace("%bw_third_format%", thirdPlayer == null ? getMsg(player, Messages.MEANING_NOBODY)
                                : getMsg(thirdPlayer, Messages.GAME_END_THIRD_KILLER)
                                .replace("%bw_v_prefix%", getChatSupport().getPrefix(thirdPlayer))
                                .replace("%bw_v_suffix%", getChatSupport().getSuffix(thirdPlayer))
                                .replace("%bw_playername%", thirdPlayer.getName())
                                .replace("%bw_player%", thirdPlayer.getDisplayName()))
                        .replace("%bw_third_kills%", String.valueOf(third))
                        .replace("%bw_game_time%", formattedTime)
                        .replace("%bw_bedbreaker_format%", topBedBreaker == null ? getMsg(player, Messages.MEANING_NOBODY)
                                : getMsg(topBedBreaker, Messages.FORMATTING_EACH_WINNER)
                                .replace("%bw_v_prefix%", getChatSupport().getPrefix(topBedBreaker))
                                .replace("%bw_v_suffix%", getChatSupport().getSuffix(topBedBreaker))
                                .replace("%bw_playername%", topBedBreaker.getName())
                                .replace("%bw_player%", topBedBreaker.getDisplayName()))
                        .replace("%bw_beds%", String.valueOf(topBeds))
                        .replace("%bw_winner_format%", getMaxInTeam() > 1
                                ? getMsg(player, Messages.FORMATTING_TEAM_WINNER_FORMAT).replace("%bw_winner_members%", winners.toString())
                                : getMsg(player, Messages.FORMATTING_SOLO_WINNER_FORMAT).replace("%bw_winner_members%", winners.toString()))
                        .replace("%bw_team_color%", winnerCandidate.getColor().chat().toString())
                        .replace("%bw_team_name%", winnerCandidate.getDisplayName(getPlayerLanguage(player)));

                String stripped = ChatColor.stripColor(message);
                if (!stripped.startsWith("---") && !stripped.endsWith("---")) {
                    message = centerText(message, 154);
                }

                player.sendMessage(SupportPAPI.getSupportPAPI().replace(player, message));
            }

            sendMapRatingPrompt(player);
        }

        changeStatus(GameState.restarting);

        List<UUID> winnersList = new ArrayList<>(), losers = new ArrayList<>(), aliveWinners = new ArrayList<>();
        for (Player p : getPlayers()) aliveWinners.add(p.getUniqueId());
        ITeam winnerTeam = winnerCandidate;

        for (Player p : winnerTeam.getMembersCache()) winnersList.add(p.getUniqueId());
        for (ITeam bwt : getTeams()) {
            if (bwt == winnerTeam) continue;
            for (Player p : bwt.getMembersCache()) losers.add(p.getUniqueId());
        }
        Bukkit.getPluginManager().callEvent(new GameEndEvent(this, winnersList, losers, winnerTeam, aliveWinners));
    }

    private void sendMapRatingPrompt(Player player) {
        if (player == null) return;

        String question = getMsg(player, Messages.GAME_END_MAP_RATING_QUESTION)
                .replace("%bw_map%", getDisplayName())
                .replace("%bw_arena%", getArenaName())
                .replace("%bw_group%", getGroup());
        if (!question.isEmpty()) {
            player.sendMessage(question);
        }

        String starText = getMsg(player, Messages.GAME_END_MAP_RATING_STAR);
        String hoverTemplate = getMsg(player, Messages.GAME_END_MAP_RATING_HOVER);
        TextComponent stars = new TextComponent("");

        for (int i = 1; i <= 5; i++) {
            TextComponent star = new TextComponent(starText);
            String command = "/" + BedWars.mainCmd + " rateMap " + getArenaName() + " " + i;
            star.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
            if (!hoverTemplate.isEmpty()) {
                String hoverText = hoverTemplate
                        .replace("%bw_rating%", String.valueOf(i))
                        .replace("%bw_map%", getDisplayName())
                        .replace("%bw_arena%", getArenaName())
                        .replace("%bw_group%", getGroup());
                star.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
            }
            stars.addExtra(star);
            if (i < 5) {
                stars.addExtra(" ");
            }
        }

        player.spigot().sendMessage(stars);
    }

    public void addPlayerDeath(Player p) {
        if (playerDeaths == null) return;
        playerDeaths.merge(p, 1, Integer::sum);
    }

    @Override
    public void updateNextEvent() {
        debug("---");
        debug("updateNextEvent called");

        NextEvent current = getNextEvent();

        if (current == NextEvent.EMERALD_GENERATOR_TIER_II && upgradeEmeraldsCount == 0) {
            int next = getGeneratorsCfg().getInt(
                    getGeneratorsCfg().getYml().get(getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_START) == null
                            ? "Default." + ConfigPath.GENERATOR_EMERALD_TIER_III_START
                            : getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_START
            );

            if (upgradeDiamondsCount < next && diamondTier == 1)
                setNextEventControlled(NextEvent.DIAMOND_GENERATOR_TIER_II);
            else if (upgradeDiamondsCount < next && diamondTier == 2)
                setNextEventControlled(NextEvent.DIAMOND_GENERATOR_TIER_III);
            else setNextEventControlled(NextEvent.EMERALD_GENERATOR_TIER_III);

            upgradeEmeraldsCount = next;
            emeraldTier = 2;

            sendEmeraldsUpgradeMessages();
            for (IGenerator o : getOreGenerators()) {
                if (o.getType() == GeneratorType.EMERALD && o.getBedWarsTeam() == null) o.upgrade();
            }

        } else if (current == NextEvent.DIAMOND_GENERATOR_TIER_II && upgradeDiamondsCount == 0) {
            int next = getGeneratorsCfg().getInt(
                    getGeneratorsCfg().getYml().get(getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_START) == null
                            ? "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_III_START
                            : getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_START
            );

            if (upgradeEmeraldsCount < next && emeraldTier == 1)
                setNextEventControlled(NextEvent.EMERALD_GENERATOR_TIER_II);
            else if (upgradeEmeraldsCount < next && emeraldTier == 2)
                setNextEventControlled(NextEvent.EMERALD_GENERATOR_TIER_III);
            else setNextEventControlled(NextEvent.DIAMOND_GENERATOR_TIER_III);

            upgradeDiamondsCount = next;
            diamondTier = 2;

            sendDiamondsUpgradeMessages();
            for (IGenerator o : getOreGenerators()) {
                if (o.getType() == GeneratorType.DIAMOND && o.getBedWarsTeam() == null) o.upgrade();
            }

        } else if (current == NextEvent.EMERALD_GENERATOR_TIER_III && upgradeEmeraldsCount == 0) {
            emeraldTier = 3;
            sendEmeraldsUpgradeMessages();

            if (diamondTier == 1 && upgradeDiamondsCount > 0)
                setNextEventControlled(NextEvent.DIAMOND_GENERATOR_TIER_II);
            else if (diamondTier == 2 && upgradeDiamondsCount > 0)
                setNextEventControlled(NextEvent.DIAMOND_GENERATOR_TIER_III);
            else setNextEventControlled(NextEvent.BEDS_DESTROY);

            for (IGenerator o : getOreGenerators()) {
                if (o.getType() == GeneratorType.EMERALD && o.getBedWarsTeam() == null) o.upgrade();
            }

        } else if (current == NextEvent.DIAMOND_GENERATOR_TIER_III && upgradeDiamondsCount == 0) {
            diamondTier = 3;
            sendDiamondsUpgradeMessages();

            if (emeraldTier == 1 && upgradeEmeraldsCount > 0)
                setNextEventControlled(NextEvent.EMERALD_GENERATOR_TIER_II);
            else if (emeraldTier == 2 && upgradeEmeraldsCount > 0)
                setNextEventControlled(NextEvent.EMERALD_GENERATOR_TIER_III);
            else setNextEventControlled(NextEvent.BEDS_DESTROY);

            for (IGenerator o : getOreGenerators()) {
                if (o.getType() == GeneratorType.DIAMOND && o.getBedWarsTeam() == null) o.upgrade();
            }

        } else if (current == NextEvent.BEDS_DESTROY && getPlayingTask().getBedsDestroyCountdown() == 0) {
            setNextEventControlled(NextEvent.ENDER_DRAGON);

        } else if (current == NextEvent.ENDER_DRAGON && getPlayingTask().getDragonSpawnCountdown() == 0) {
            setNextEventControlled(NextEvent.GAME_END);

            Stream.concat(getPlayers().stream(), getSpectators().stream())
                    .forEach(p -> nms.sendTitle(
                            p,
                            getMsg(p, Messages.DRAW_TITLE),
                            getMsg(p, Messages.DRAW_SUBTITLE),
                            0, 20, 10
                    ));
        }

        debug("---");
        debug(getNextEvent().toString());
    }

    private void setNextEventControlled(NextEvent event) {
        forcedNextEvent = null;
        nextEvent = skipDisabled(event);
    }

    private NextEvent nextByOrder(NextEvent event) {
        NextEvent[] values = NextEvent.values();
        int idx = event.ordinal();
        if (idx >= values.length - 1) return NextEvent.GAME_END;
        return values[idx + 1];
    }

    private NextEvent skipDisabled(NextEvent event) {
        NextEvent cursor = event;
        int guard = 0;

        while (cursor != null && isNextEventDisabled(cursor) && guard++ < 20) {
            cursor = nextByOrder(cursor);
        }

        return cursor == null ? NextEvent.GAME_END : cursor;
    }

    public boolean setCurrentEventDurationSeconds(int seconds) {
        seconds = Math.max(0, seconds);

        NextEvent current = getNextEvent();
        switch (current) {
            case DIAMOND_GENERATOR_TIER_II:
            case DIAMOND_GENERATOR_TIER_III:
                upgradeDiamondsCount = seconds;
                return true;

            case EMERALD_GENERATOR_TIER_II:
            case EMERALD_GENERATOR_TIER_III:
                upgradeEmeraldsCount = seconds;
                return true;

            case BEDS_DESTROY:
                getPlayingTask().setBedsDestroyCountdown(seconds);
                return true;

            case ENDER_DRAGON:
                getPlayingTask().setDragonSpawnCountdown(seconds);
                return true;

            case GAME_END:
                getPlayingTask().setGameEndCountdown(seconds);
                return true;

            default:
                return false;
        }
    }

    public void disableNextEvent(NextEvent event) {
        if (event == null) return;
        disabledNextEvents.add(event);
    }

    public void enableNextEvent(NextEvent event) {
        if (event == null) return;
        disabledNextEvents.remove(event);
    }

    public boolean isNextEventDisabled(NextEvent event) {
        return event != null && disabledNextEvents.contains(event);
    }

    public void forceNextEvent(NextEvent event) {
        this.forcedNextEvent = event;
    }

    public void clearForcedNextEvent() {
        this.forcedNextEvent = null;
    }

    public Set<NextEvent> getDisabledNextEvents() {
        return EnumSet.copyOf(disabledNextEvents);
    }

    private void registerSigns() {
        if (getServerType() == ServerType.BUNGEE) return;
        if (BedWars.signs.getYml().get("locations") == null) return;
        for (String st : BedWars.signs.getYml().getStringList("locations")) {
            String[] data = st.split(",");
            if (!data[0].equals(getArenaName())) continue;
            Location l;
            try {
                l = new Location(Bukkit.getWorld(data[6]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3]));
            } catch (Exception e) {
                plugin.getLogger().severe("Could not load sign at: " + Arrays.toString(data));
                continue;
            }
            addSign(l);
        }
    }

    public ITeam getTeam(String name) {
        for (ITeam bwt : getTeams()) if (bwt.getName().equals(name)) return bwt;
        return null;
    }

    @Override
    public ConcurrentHashMap<Player, Integer> getRespawnSessions() {
        return respawnSessions;
    }

    @Override
    public AnnouncementTask getAnnouncementTask() {
        return announcementTask;
    }

    public NextEvent getNextEvent() {
        return forcedNextEvent != null ? forcedNextEvent : nextEvent;
    }

    public void setNextEvent(NextEvent nextEvent) {
        if (this.nextEvent != null) {
            Sounds.playSound(this.nextEvent.getSoundPath(), getPlayers());
            Sounds.playSound(this.nextEvent.getSoundPath(), getSpectators());
        }
        Bukkit.getPluginManager().callEvent(new NextEventChangeEvent(this, nextEvent, this.nextEvent));
        this.nextEvent = nextEvent;
    }

    public List<String> getNextEvents() {
        return new ArrayList<>(nextEvents);
    }

    public int getPlayerDeaths(Player p, boolean finalDeaths) {
        return finalDeaths ? playerFinalKillDeaths.getOrDefault(p, 0) : playerDeaths.getOrDefault(p, 0);
    }

    public void sendDiamondsUpgradeMessages() {
        Stream.concat(getPlayers().stream(), getSpectators().stream())
                .forEach(p -> p
                        .sendMessage(getMsg(p, Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT)
                                .replace("%bw_generator_type%", getMsg(p, Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND))
                                .replace("%bw_tier%", getMsg(p, (diamondTier == 2 ? Messages.FORMATTING_GENERATOR_TIER2 : Messages.FORMATTING_GENERATOR_TIER3)))
                        ));
    }

    public void sendEmeraldsUpgradeMessages() {
        Stream.concat(getPlayers().stream(), getSpectators().stream())
                .forEach(p -> p
                        .sendMessage(getMsg(p, Messages.GENERATOR_UPGRADE_CHAT_ANNOUNCEMENT)
                                .replace("%bw_generator_type%", getMsg(p, Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD))
                                .replace("%bw_tier%", getMsg(p, (emeraldTier == 2 ? Messages.FORMATTING_GENERATOR_TIER2 : Messages.FORMATTING_GENERATOR_TIER3)))
                        ));
    }

    public void destroyData() {
        destroyReJoins();
        if (worldName != null) arenaByIdentifier.remove(worldName);
        arenas.remove(this);
        for (ReJoinTask rjt : ReJoinTask.getReJoinTasks()) if (rjt.getArena() == this) rjt.destroy();
        for (Despawnable despawnable : new ArrayList<>(BedWars.nms.getDespawnablesList().values())) {
            if (despawnable.getTeam().getArena() == this) despawnable.destroy();
        }
        for (ServerPlaceholder placeholder : serverPlaceholders) {
            TabAPI.getInstance().getPlaceholderManager().unregisterPlaceholder(placeholder);
        }
        if (TabAPI.getInstance().getBossBarManager() != null) {
            for (BossBar bossBar : dragonBossbars) bossBar.getPlayers().forEach(bossBar::removePlayer);
            dragonBossbars = null;
        }
        scoreboards.forEach(Scoreboard::unregister);
        scoreboards = null;
        arenaByName.remove(arenaName);
        arenaByPlayer.entrySet().removeIf(entry -> entry.getValue() == this);
        players = null;
        spectators = null;
        signs = null;
        yml = null;
        cm = null;
        world = null;
        for (IGenerator og : oreGenerators) og.destroyData();
        isOnABase.entrySet().removeIf(entry -> entry.getValue().getArena().equals(this));
        for (ITeam bwt : teams) bwt.destroyData();
        playerLocation.entrySet().removeIf(e -> Objects.requireNonNull(e.getValue().getWorld()).getName().equalsIgnoreCase(worldName));
        teams = null;
        placed = null;
        nextEvents = null;
        regionsList = null;
        respawnSessions = null;
        showTime = null;
        playerKills = null;
        playerTotalKills = null;
        playerBedsDestroyed = null;
        playerFinalKills = null;
        playerDeaths = null;
        playerFinalKillDeaths = null;
        startingTask = null;
        playingTask = null;
        restartingTask = null;
        oreGenerators = null;
        perMinuteTask = null;
        moneyperMinuteTask = null;
        fireballCooldowns.clear();

        if (BedWars.getRedisConnection() != null) BedWars.getRedisConnection().cleanupRedisEntry(this);
    }

    @Override
    public int getRenderDistance() {
        return renderDistance;
    }

    @Override
    public Location getReSpawnLocation() {
        return respawnLocation;
    }

    @Override
    public Location getSpectatorLocation() {
        return spectatorLocation;
    }

    @Override
    public void setAllowMapBreak(boolean allowMapBreak) {
        this.allowMapBreak = allowMapBreak;
    }

    @Override
    public boolean isTeamBed(Location location) {
        return null != getBedsTeam(location);
    }

    @Override
    public @Nullable ITeam getBedsTeam(@NotNull Location location) {
        if (!location.getWorld().getName().equals(this.worldName)) {
            throw new RuntimeException("Given location is not on this game world.");
        }
        if (!nms.isBed(location.getBlock().getType())) return null;
        for (ITeam team : this.teams) if (team.isBed(location)) return team;
        return null;
    }

    @Override
    public @Nullable IShopIndex getLinkedShop() {
        return linkedShop;
    }

    @Override
    public void setLinkedShop(@Nullable IShopIndex shop) {
        this.linkedShop = shop;
    }

    @Override
    public @Nullable UpgradesIndex getLinkedUpgrades() {
        return linkedUpgrades;
    }

    @Override
    public void setLinkedUpgrades(@Nullable UpgradesIndex upgrades) {
        this.linkedUpgrades = upgrades;
    }

    @Override
    public boolean isAllowEnderDragonDestroy() {
        return enderDragonDestory;
    }

    @Override
    public void setAllowEnderDragonDestroy(boolean allowDestory) {
        this.enderDragonDestory = allowDestory;
    }

    @Override
    public int getMagicMilkTime() {
        return magicMilkTime;
    }

    @Override
    public boolean isMapBreakable() {
        return allowMapBreak;
    }

    @Override
    public Location getWaitingLocation() {
        return waitingLocation;
    }

    @Override
    public boolean startReSpawnSession(Player player, int seconds) {
        if (respawnSessions.get(player) != null) return false;
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isPlayer(player)) return false;

        player.getInventory().clear();
        if (seconds > 1) {
            for (Player playing : arena.getPlayers()) {
                if (playing.equals(player)) continue;
                BedWars.nms.spigotHidePlayer(player, playing);
            }
            PaperSupport.teleportC(player, getReSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setAllowFlight(true);
            player.setFlying(true);

            respawnSessions.put(player, seconds);
            Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setFireTicks(0);

                nms.setCollide(player, this, false);
                for (Player invisible : getShowTime().keySet()) BedWars.nms.hideArmor(invisible, player);
            }, 5L);
        } else {
            ITeam team = getTeam(player);
            team.respawnMember(player);
        }
        return true;
    }

    @Override
    public boolean isReSpawning(Player player) {
        return respawnSessions.containsKey(player);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IArena other)) return false;
        return Objects.equals(other.getWorldName(), this.getWorldName());
    }

    private void destroyReJoins() {
        List<ReJoin> reJoins = new ArrayList<>(ReJoin.getReJoinList());
        for (ReJoin reJoin : reJoins) if (reJoin.getArena() == this) reJoin.destroy(true);
    }

    @Override
    public boolean isProtected(Location location) {
        return Misc.isBuildProtected(location, this);
    }

    @Override
    public void abandonGame(Player player) {
        if (player == null) return;

        this.playerBedsDestroyed.remove(player);
        this.playerFinalKills.remove(player);
        this.playerDeaths.remove(player);
        this.playerFinalKillDeaths.remove(player);

        ITeam team = getTeams().stream().filter(team1 -> team1.wasMember(player.getUniqueId())).findFirst().orElse(null);
        if (team != null) {
            team.getMembersCache().removeIf(cachedPlayer -> cachedPlayer.getUniqueId().equals(player.getUniqueId()));
            ReJoin rejoin = ReJoin.getPlayer(player);
            if (rejoin != null) rejoin.destroy(team.getMembers().isEmpty());
        }
    }

    @Override
    public int getYKillHeight() {
        return yKillHeight;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public ITeamAssigner getTeamAssigner() {
        return teamAssigner;
    }

    @Override
    public void setTeamAssigner(ITeamAssigner teamAssigner) {
        if (teamAssigner == null) {
            this.teamAssigner = new TeamAssigner();
            plugin.getLogger().info("Using Default team assigner on arena: " + this.getArenaName());
        } else {
            this.teamAssigner = teamAssigner;
            plugin.getLogger().warning("Using " + teamAssigner.getClass().getSimpleName() + " team assigner on arena: " + this.getArenaName());
        }
    }

    @Override
    public List<ShopHolo> getShopHolograms(String iso) {
        return shopHolosIso.get(iso);
    }

    @Override
    public List<BossBar> getDragonBossbars() {
        return dragonBossbars;
    }

    private void sendToMainLobby(Player player) {
        if (BedWars.getServerType() == ServerType.SHARED) {
            Location loc = playerLocation.get(player);
            if (loc == null) {
                PaperSupport.teleportC(player, Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                plugin.getLogger().log(Level.SEVERE, player.getName() + " was teleported to the main world because lobby location is not set!");
            } else {
                player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        } else if (BedWars.getServerType() == ServerType.MULTIARENA) {
            if (BedWars.getLobbyWorld().isEmpty()) {
                PaperSupport.teleportC(player, Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                plugin.getLogger().log(Level.SEVERE, player.getName() + " was teleported to the main world because lobby location is not set!");
            } else {
                PaperSupport.teleportC(player, config.getConfigLoc("lobbyLoc"), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }
    }

    public void set1_8BossBarName(ITeam team, EnderDragon dragon) {
        for (Player player : team.getArena().getPlayers()) {
            String name = Language.getMsg(player, Messages.FORMATTING_BOSSBAR_DRAGON).replace("%bw_team%", team.getColor().chat() + team.getName()).replace("%bw_team_color%", String.valueOf(team.getColor().chat())).replace("%bw_team_name%", team.getDisplayName(getPlayerLanguage(player))).replace("%bw_team_letter%", String.valueOf(!team.getName().isEmpty() ? team.getName().charAt(0) : ""));
            dragon.setCustomName(name);
        }
    }

    public void createTABTeamDragonBossBar(ITeam team, int dragonNumber) {
        if (TabAPI.getInstance().getBossBarManager() == null) {
            BedWars.plugin.getLogger().warning("BossBar is disabled in TAB config! Please enable it there.\n Make sure to remove the ServerInfo default config if you want to use dragon bossbars");
            return;
        }
        String dragonPlaceholderName = "%bw_" + team.getArena().getWorldName() + "_" + team.getName() + "+" + dragonNumber + "%";
        ServerPlaceholder dragonPlaceholder = TabAPI.getInstance().getPlaceholderManager().registerServerPlaceholder(dragonPlaceholderName, 500, () -> String.valueOf(team.getDragons().get(dragonNumber).getHealth() / team.getDragons().get(dragonNumber).getMaxHealth() * 100));
        serverPlaceholders.add(dragonPlaceholder);
        for (Player player : team.getArena().getPlayers()) {
            String name = Language.getMsg(player, Messages.FORMATTING_BOSSBAR_DRAGON).replace("%bw_team%", team.getColor().chat() + team.getName()).replace("%bw_team_color%", String.valueOf(team.getColor().chat())).replace("%bw_team_name%", team.getDisplayName(getPlayerLanguage(player))).replace("%bw_team_letter%", String.valueOf(!team.getName().isEmpty() ? team.getName().charAt(0) : ""));
            BossBar bb = TabAPI.getInstance().getBossBarManager().createBossBar(name, dragonPlaceholderName, String.valueOf(team.getColor()), "PROGRESS");
            bb.addPlayer(Objects.requireNonNull(TabAPI.getInstance().getPlayer(player.getUniqueId())));
            dragonBossbars.add(bb);
        }
    }

    public void setAllGeneratorsEnabled(boolean enabled) {
        for (IGenerator gen : getOreGenerators()) {
            if (enabled) gen.enable();
            else gen.disable();
        }
        for (ITeam team : getTeams()) {
            for (IGenerator gen : team.getGenerators()) {
                if (enabled) gen.enable();
                else gen.disable();
            }
        }
    }

    public void setGeneratorsEnabledByType(GeneratorType type, boolean enabled) {
        for (IGenerator gen : getOreGenerators()) {
            if (gen.getType() == type) {
                if (enabled) gen.enable();
                else gen.disable();
            }
        }
        for (ITeam team : getTeams()) {
            for (IGenerator gen : team.getGenerators()) {
                if (gen.getType() == type) {
                    if (enabled) gen.enable();
                    else gen.disable();
                }
            }
        }
    }

    public void setGeneratorsSpeedByType(GeneratorType type, double multiplier) {
        for (IGenerator gen : getOreGenerators()) {
            if (gen.getType() == type && gen instanceof OreGenerator) {
                ((OreGenerator) gen).applySpeedMultiplier(multiplier, true);
            }
        }
        for (ITeam team : getTeams()) {
            for (IGenerator gen : team.getGenerators()) {
                if (gen.getType() == type && gen instanceof OreGenerator) {
                    ((OreGenerator) gen).applySpeedMultiplier(multiplier, true);
                }
            }
        }
    }
}
