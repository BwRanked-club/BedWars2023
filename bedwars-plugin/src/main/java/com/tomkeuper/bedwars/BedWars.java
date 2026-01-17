/*
 * BedWars2023 - A bed wars mini-game.
 * Copyright (C) 2024 Tomas Keuper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: contact@fyreblox.com
 */

package com.tomkeuper.bedwars;

import com.tomkeuper.bedwars.addon.AddonManager;
import com.tomkeuper.bedwars.api.addon.Addon;
import com.tomkeuper.bedwars.api.addon.IAddonManager;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.chat.IChat;
import com.tomkeuper.bedwars.api.configuration.ConfigManager;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.database.IDatabase;
import com.tomkeuper.bedwars.api.economy.IEconomy;
import com.tomkeuper.bedwars.api.hologram.IHologramManager;
import com.tomkeuper.bedwars.api.items.handlers.IPermanentItem;
import com.tomkeuper.bedwars.api.items.handlers.IPermanentItemHandler;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.levels.Level;
import com.tomkeuper.bedwars.api.party.Party;
import com.tomkeuper.bedwars.api.server.RestoreAdapter;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.tomkeuper.bedwars.api.server.VersionSupport;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.ArenaManager;
import com.tomkeuper.bedwars.arena.VoidChunkGenerator;
import com.tomkeuper.bedwars.arena.despawnables.TargetListener;
import com.tomkeuper.bedwars.arena.feature.AntiDropFeature;
import com.tomkeuper.bedwars.arena.feature.GenSplitFeature;
import com.tomkeuper.bedwars.arena.feature.ResourceChestFeature;
import com.tomkeuper.bedwars.arena.feature.SpoilPlayerTNTFeature;
import com.tomkeuper.bedwars.arena.spectator.SpectatorListeners;
import com.tomkeuper.bedwars.arena.tasks.HologramTask;
import com.tomkeuper.bedwars.arena.tasks.OneTick;
import com.tomkeuper.bedwars.arena.tasks.Refresh;
import com.tomkeuper.bedwars.arena.upgrades.BaseListener;
import com.tomkeuper.bedwars.arena.upgrades.HealPoolListener;
import com.tomkeuper.bedwars.commands.bedwars.MainCommand;
import com.tomkeuper.bedwars.commands.leave.LeaveCommand;
import com.tomkeuper.bedwars.commands.party.PartyCommand;
import com.tomkeuper.bedwars.commands.rejoin.RejoinCommand;
import com.tomkeuper.bedwars.commands.shout.ShoutCommand;
import com.tomkeuper.bedwars.configuration.*;
import com.tomkeuper.bedwars.connectionmanager.redis.RedisConnection;
import com.tomkeuper.bedwars.database.H2;
import com.tomkeuper.bedwars.database.MySQL;
import com.tomkeuper.bedwars.database.SQLite;
import com.tomkeuper.bedwars.halloween.HalloweenSpecial;
import com.tomkeuper.bedwars.handlers.items.LobbyItem;
import com.tomkeuper.bedwars.handlers.items.PreGameItem;
import com.tomkeuper.bedwars.handlers.items.SpectatorItem;
import com.tomkeuper.bedwars.handlers.main.CommandItemHandler;
import com.tomkeuper.bedwars.handlers.main.LeaveItemHandler;
import com.tomkeuper.bedwars.handlers.main.StatsItemHandler;
import com.tomkeuper.bedwars.hologram.HologramManager;
import com.tomkeuper.bedwars.language.English;
import com.tomkeuper.bedwars.language.LangListener;
import com.tomkeuper.bedwars.language.Portuguese;
import com.tomkeuper.bedwars.levels.internal.InternalLevel;
import com.tomkeuper.bedwars.levels.internal.LevelListeners;
import com.tomkeuper.bedwars.listeners.*;
import com.tomkeuper.bedwars.listeners.arenaselector.ArenaSelectorListener;
import com.tomkeuper.bedwars.listeners.blockstatus.BlockStatusListener;
import com.tomkeuper.bedwars.listeners.chat.ChatAFK;
import com.tomkeuper.bedwars.listeners.chat.ChatFormatting;
import com.tomkeuper.bedwars.listeners.joinhandler.JoinHandlerCommon;
import com.tomkeuper.bedwars.listeners.joinhandler.JoinListenerMultiArena;
import com.tomkeuper.bedwars.maprestore.internal.InternalAdapter;
import com.tomkeuper.bedwars.money.internal.MoneyListeners;
import com.tomkeuper.bedwars.shop.ShopCache;
import com.tomkeuper.bedwars.shop.ShopDataMigrator;
import com.tomkeuper.bedwars.shop.ShopManager;
import com.tomkeuper.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import com.tomkeuper.bedwars.sidebar.BoardManager;
import com.tomkeuper.bedwars.stats.StatsManager;
import com.tomkeuper.bedwars.support.papi.PAPISupport;
import com.tomkeuper.bedwars.support.papi.SupportPAPI;
import com.tomkeuper.bedwars.support.party.*;
import com.tomkeuper.bedwars.support.vault.NoChat;
import com.tomkeuper.bedwars.support.vault.NoEconomy;
import com.tomkeuper.bedwars.support.vault.WithChat;
import com.tomkeuper.bedwars.support.vault.WithEconomy;
import com.tomkeuper.bedwars.upgrades.UpgradesManager;
import com.tomkeuper.bedwars.utils.ItemBuilder;
import com.tomkeuper.bedwars.utils.SlimLogger;
import de.dytanic.cloudnet.wrapper.Wrapper;
import io.github.slimjar.app.builder.ApplicationBuilder;
import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.api.TabAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class BedWars extends JavaPlugin {

    private static final String minecraftVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
    @Getter
    private static final Collection<IPermanentItem> lobbyItems = new ArrayList<>();
    @Getter
    private static final Collection<IPermanentItem> spectatorItems = new ArrayList<>();
    @Getter
    private static final Collection<IPermanentItem> preGameItems = new ArrayList<>();
    @Getter
    private static final Map<String, IPermanentItemHandler> itemHandlers = new ConcurrentHashMap<>();
    private static final Map<String, String> NMS_BY_MC;
    @Setter
    public static boolean debug = true;
    public static boolean autoscale = true;
    public static boolean isPaper = false;
    public static int hologramUpdateDistance = 50;
    public static String mainCmd = "bw", link = "https://polymart.org/resource/bedwars2023.5702";
    public static ConfigManager signs, generators;
    public static MainConfig config;
    public static ShopManager shop;
    public static PlayerQuickBuyCache playerQuickBuyCache;
    public static ShopCache shopCache;
    @Getter
    public static StatsManager statsManager;
    public static BedWars plugin;
    public static VersionSupport nms;
    public static ArenaManager arenaManager = new ArenaManager();
    public static IAddonManager addonManager = new AddonManager();
    public static IHologramManager hologramManager = new HologramManager();
    protected static Level level;
    @Getter
    private static ServerType serverType = ServerType.MULTIARENA;
    private static UpgradesManager upgradesManager;
    @Getter
    private static Party partyManager = new NoParty();
    private static IChat chat = new NoChat();
    @Getter
    private static IEconomy economy;
    private static String nmsVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    @Getter
    private static String lobbyWorld = "";
    @Getter
    private static boolean shuttingDown = false;
    @Setter
    @Getter
    private static IDatabase remoteDatabase;
    @Getter
    private static RedisConnection redisConnection;
    private static com.tomkeuper.bedwars.api.BedWars api;

    static {
        Map<String, String> m = new HashMap<>();
        m.put("1.20.4", "v1_20_R3");
        m.put("1.20.5", "v1_20_R4");
        m.put("1.20.6", "v1_20_6");
        m.put("1.21", "v1_21_R1");
        m.put("1.21.1", "v1_21_R1");
        m.put("1.21.2", "v1_21_R2");
        m.put("1.21.3", "v1_21_R2");
        m.put("1.21.4", "v1_21_R3");
        m.put("1.21.6", "v1_21_R5");
        m.put("1.21.7", "v1_21_R5");
        m.put("1.21.8", "v1_21_R5");
        NMS_BY_MC = Collections.unmodifiableMap(m);
    }

    private BukkitAudiences adventure;
    private boolean serverSoftwareSupport = true, papiSupportLoaded = false, vaultEconomyLoaded = false, vaultChatLoaded = false;

    public static void setLevelAdapter(Level levelsManager) {
        if (levelsManager instanceof InternalLevel) {
            if (LevelListeners.instance == null) {
                Bukkit.getPluginManager().registerEvents(new LevelListeners(), BedWars.plugin);
            }
        } else {
            if (LevelListeners.instance != null) {
                PlayerJoinEvent.getHandlerList().unregister(LevelListeners.instance);
                PlayerQuitEvent.getHandlerList().unregister(LevelListeners.instance);
                LevelListeners.instance = null;
            }
        }
        level = levelsManager;
    }

    public static void setServerType(ServerType serverType) {
        BedWars.serverType = serverType;
        if (serverType == ServerType.BUNGEE) autoscale = true;
    }

    public static void setAutoscale(boolean autoscale) {
        BedWars.autoscale = autoscale;
    }

    // SETTERS

    public static void setLobbyWorld(String lobbyWorld) {
        BedWars.lobbyWorld = lobbyWorld;
    }

    public static void setPartyManager(Party partyManager) {
        BedWars.partyManager = partyManager;
    }

    public static void setEconomy(IEconomy economy) {
        BedWars.economy = economy;
    }

    public static String getForCurrentVersion(String v18, String v12, String v13) {
        return switch (getServerVersion()) {
            case "v1_8_R3" -> v18;
            case "v1_12_R1" -> v12;
            default -> v13;
        };
    }

    public static IChat getChatSupport() {
        return chat;
    }

    public static Level getLevelSupport() {
        return level;
    }

    // GETTERS

    public static ConfigManager getGeneratorsCfg() {
        return generators;
    }

    public static String getServerVersion() {
        return nmsVersion;
    }

    public static UpgradesManager getUpgradeManager() {
        return upgradesManager;
    }

    public static com.tomkeuper.bedwars.api.BedWars getAPI() {
        return api;
    }

    public static void registerEvents(Listener... listeners) {
        Arrays.stream(listeners).forEach(l -> plugin.getServer().getPluginManager().registerEvents(l, plugin));
    }

    public static void debug(String message) {
        if (debug) {
            plugin.getLogger().info("DEBUG: " + message);
        }
    }

    public static boolean registerItemHandler(IPermanentItemHandler handler) {
        return itemHandlers.putIfAbsent(handler.getId(), handler) == null;
    }

    @Override
    public void onLoad() {
        if (!ensureSpigotCompatible()) return;

        downloadLibraries();

        isPaper = classPresent("com.destroystokyo.paper.PaperConfig");
        plugin = this;

        nmsVersion = resolveNmsVersion(minecraftVersion);
        if (nmsVersion == null) {
            serverSoftwareSupport = false;
            getLogger().severe("Cannot resolve NMS for " + minecraftVersion);
            return;
        }
        if (!loadVersionSupport(nmsVersion)) return;

        if (nmsVersion == null || !loadVersionSupport(nmsVersion)) return;

        getLogger().info("Loading support for paper/spigot: " + minecraftVersion);

        new English();
        new Portuguese();

        config = new MainConfig(this, "config");
        hologramUpdateDistance = config.getInt(ConfigPath.GENERAL_CONFIGURATION_HOLOGRAM_UPDATE_DISTANCE);
        generators = new GeneratorsConfig(this, "generators", getDataFolder().getPath());

        if (getServerType() != ServerType.BUNGEE) {
            signs = new SignsConfig(this, "signs", getDataFolder().getPath());
        }
    }

    @Override
    public void onEnable() {
        if (!serverSoftwareSupport || nms == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.adventure = BukkitAudiences.create(this);
        nms.registerVersionListeners();

        if (!this.handleWorldAdapter()) api.setRestoreAdapter(new InternalAdapter(this));

        nms.registerCommand(mainCmd, new MainCommand(mainCmd));
        if (nms.getVersion() >= 9) {
            this.registerDelayedCommands();
        } else {
            Bukkit.getScheduler().runTaskLater(this, this::registerDelayedCommands, 20L);
        }

        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_CLOUDNET_SUPPORT) && serverType == ServerType.BUNGEE) {
            plugin.getLogger().log(java.util.logging.Level.INFO, "CloudNet Service ID = " + Wrapper.getInstance().getServiceId().getName());
            config.set(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID, Wrapper.getInstance().getServiceId().getName());
        }

        if (getServerType() == ServerType.MULTIARENA)
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (!config.getLobbyWorldName().isEmpty()) {
                    if (Bukkit.getWorld(config.getLobbyWorldName()) == null && new File(Bukkit.getWorldContainer(), config.getLobbyWorldName() + "/level.dat").exists()) {
                        if (!config.getLobbyWorldName().equalsIgnoreCase(Bukkit.getServer().getWorlds().get(0).getName())) {
                            Bukkit.getScheduler().runTaskLater(this, () -> {
                                Bukkit.createWorld(new WorldCreator(config.getLobbyWorldName()));
                                if (Bukkit.getWorld(config.getLobbyWorldName()) != null) {
                                    Bukkit.getScheduler().runTaskLater(plugin, () -> Objects.requireNonNull(Bukkit.getWorld(config.getLobbyWorldName())).getEntities().stream().filter(e -> e instanceof Monster).forEach(Entity::remove), 20L);
                                }
                            }, 100L);
                        }
                    }
                    Location l = config.getConfigLoc("lobbyLoc");
                    if (l != null) {
                        World w = Bukkit.getWorld(config.getLobbyWorldName());
                        if (w != null) {
                            w.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                        }
                    }
                }
            }, 1L);

        registerEvents(
                new QuitAndTeleportListener(),
                new PlayerBlockListeners(),
                new PlayerListeners(),
                new PlayerInventoryListeners(),
                new PlayerInteractListeners(),
                new RefreshGUI(),
                new ServerListeners(),
                new FireballListener(),
                new EggBridgeListener(),
                new SpectatorListeners(),
                new BaseListener(),
                new TargetListener(),
                new LangListener(),
                new Warnings(this),
                new ChatAFK(),
                new GameEndListener()
        );

        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HEAL_POOL_ENABLE))
            registerEvents(new HealPoolListener());

        registerEvents(
                new ArenaSelectorListener(),
                new BlockStatusListener(),
                new AutoscaleListener()
        );
        registerEvents(new JoinListenerMultiArena());
        registerEvents(new JoinHandlerCommon());

        registerEvents(new WorldLoadListener());

        registerEvents(new ChunkListener());
        registerEvents(new InvisibilityPotionListener());
        statsManager = new StatsManager();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_ALLOW_PARTIES)) {
                if (getServer().getPluginManager().isPluginEnabled("Parties")) {
                    getLogger().info("Hook into Parties (by AlessioDP) support!");
                    partyManager = new PartiesAdapter();
                } else if (Bukkit.getServer().getPluginManager().isPluginEnabled("PartyAndFriends")) {
                    getLogger().info("Hook into Party and Friends for Spigot (by Simonsator) support!");
                    partyManager = new PAF();
                } else if (Bukkit.getServer().getPluginManager().isPluginEnabled("Spigot-Party-API-PAF")) {
                    getLogger().info("Hook into Spigot Party API for Party and Friends Extended (by Simonsator) support!");
                    partyManager = new PAFBungeecordRedisApi();
                }
                if (partyManager instanceof NoParty) {
                    partyManager = new Internal();
                    getLogger().info("Loading internal Party system. /party");
                }
            } else {
                partyManager = new NoParty();
            }
        }, 10L);
        setLevelAdapter(new InternalLevel());
        Bukkit.getScheduler().runTaskTimer(this, new Refresh(), 20L, 20L);
        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_ROTATE_GEN))
            Bukkit.getScheduler().runTaskTimer(this, new OneTick(), 120, 1);

        Bukkit.getScheduler().runTaskLater(this, new HologramTask(), config.getInt(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_HOLOGRAM_UPDATE_RATE));

        nms.registerEntities();
        if (config.getString(ConfigPath.GENERAL_CONFIGURATION_DATABASE_TYPE).equalsIgnoreCase("mysql")) {
            MySQL mySQL = new MySQL();
            long time = System.currentTimeMillis();
            if (!mySQL.connect()) {
                this.getLogger().severe("Could not connect to database! Please verify your credentials and make sure that the server IP is whitelisted in MySQL.");
                remoteDatabase = new SQLite();
            } else {
                remoteDatabase = mySQL;
            }
            if (System.currentTimeMillis() - time >= 5000) {
                this.getLogger().severe("It took " + ((System.currentTimeMillis() - time) / 1000) + " ms to establish a database connection!\nUsing this remote connection is not recommended!");
            }
            remoteDatabase.init();
        } else if (config.getString(ConfigPath.GENERAL_CONFIGURATION_DATABASE_TYPE).equalsIgnoreCase("sqlite")) {
            remoteDatabase = new SQLite();
            remoteDatabase.init();
        } else if (config.getString(ConfigPath.GENERAL_CONFIGURATION_DATABASE_TYPE).equalsIgnoreCase("h2")) {
            remoteDatabase = new H2();
            remoteDatabase.init();
        }

        Language.setupCustomStatsMessages();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPISupport().register();
            SupportPAPI.setSupportPAPI(new SupportPAPI.withPAPI());
            papiSupportLoaded = true;
        }

        Bukkit.getScheduler().runTask(this, () -> {
            if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
                try {
                    RegisteredServiceProvider<?> rsp = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
                    if (rsp != null) {
                        WithChat.setChat((net.milkbowl.vault.chat.Chat) rsp.getProvider());
                        vaultChatLoaded = true;
                        chat = new WithChat();
                    } else {
                        plugin.getLogger().info("Vault found, but no chat provider!");
                        chat = new NoChat();
                    }
                } catch (Exception ignored) {
                    chat = new NoChat();
                }
                try {
                    registerEvents(new MoneyListeners());
                    RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                    if (rsp != null) {
                        WithEconomy.setEconomy(rsp.getProvider());
                        vaultEconomyLoaded = true;
                        economy = new WithEconomy();
                    } else {
                        plugin.getLogger().info("Vault found, but no economy provider!");
                        economy = new NoEconomy();
                    }
                } catch (Exception ignored) {
                    economy = new NoEconomy();
                }
            } else {
                chat = new NoChat();
                economy = new NoEconomy();
            }
        });

        if (config.getBoolean(ConfigPath.GENERAL_CHAT_FORMATTING)) registerEvents(new ChatFormatting());

        nms.registerTntWhitelist(
                (float) config.getDouble(ConfigPath.GENERAL_TNT_PROTECTION_END_STONE_BLAST),
                (float) config.getDouble(ConfigPath.GENERAL_TNT_PROTECTION_GLASS_BLAST)
        );

        Sounds.init();
        shop = new ShopManager();
        shop.loadShop();
        shop.loadOverrides();

        ShopDataMigrator.runIfNeeded();

        registerItemHandlers(new StatsItemHandler("stats", this, api), new CommandItemHandler("command", this, api), new LeaveItemHandler("leave", this, api));
        shopCache = new ShopCache();
        playerQuickBuyCache = new PlayerQuickBuyCache();
        for (Language l : Language.getLanguages()) {
            l.setupUnSetCategories();
            Language.addDefaultMessagesCommandItems(l);
        }
        LevelsConfig.init();
        MoneyConfig.init();
        Metrics metrics = new Metrics(this, 18317);
        metrics.addCustomChart(new SimplePie("server_type", () -> getServerType().toString()));
        metrics.addCustomChart(new SimplePie("default_language", () -> Language.getDefaultLanguage().getIso()));
        metrics.addCustomChart(new SimplePie("auto_scale", () -> String.valueOf(autoscale)));
        metrics.addCustomChart(new SimplePie("party_adapter", () -> partyManager.getClass().getSimpleName()));
        metrics.addCustomChart(new SimplePie("chat_adapter", () -> chat.getClass().getSimpleName()));
        metrics.addCustomChart(new SimplePie("level_adapter", () -> getLevelSupport().getClass().getSimpleName()));
        metrics.addCustomChart(new SimplePie("db_adapter", () -> getRemoteDatabase().getClass().getSimpleName()));
        metrics.addCustomChart(new SimplePie("map_adapter", () -> String.valueOf(getAPI().getRestoreAdapter().getOwner().getName())));

        upgradesManager = new UpgradesManager();
        upgradesManager.init();

        Bukkit.getScheduler().runTask(this, () -> {
            if (Bukkit.getPluginManager().getPlugin("TAB") != null) {
                getLogger().info("Hooking into TAB support!");

                if (BoardManager.init()) {
                    getLogger().info("TAB support has been loaded");
                    if (autoscale) {
                        Arena.ensureAllArenasLoaded();
                    } else {
                        loadArenasAndSigns();
                    }
                } else {
                    this.getLogger().severe("Tab scoreboard is not enabled! Applying tab configuration automatically...");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bw applyTabConfig");
                    getLogger().info("TAB configuration command has been executed.");
                    this.getLogger().warning("\n\nRestarting the server to apply the changes...\n\n");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                }
            } else {
                this.getLogger().severe("TAB by NEZNAMY could not be hooked!");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        });

        Bukkit.getScheduler().runTask(this, () -> {
            loadLobbyItems();
            loadSpectatorItems();
            loadPreGameItems();
        });

        setupFeatures();

        Bukkit.getScheduler().runTaskLater(this, () -> addonManager.loadAddons(), 60L);

        if (redisConnection != null) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (redisConnection.checkSettings("default_rankup_cost", String.valueOf(LevelsConfig.getNextCost(1)))) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Redis settings match the default values.");
                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The network settings do not match the set values! Please check the configuration!");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Want to set value as default? Use '" + ChatColor.WHITE + "/bw redisUpdate default_rankup_cost" + ChatColor.RED + "' from the console!");
                }
            }, 70L);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            this.getLogger().info("BedWars2023 v" + plugin.getDescription().getVersion() + " has been enabled!");
            this.getLogger().info("");
            this.getLogger().info("Server Type: " + getServerType().toString() + (getServerType() == ServerType.BUNGEE ? " (ServerID: " + config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID) + ")" : ""));
            this.getLogger().info("Auto Scale: " + autoscale);
            this.getLogger().info("Datasource: " + remoteDatabase.getClass().getSimpleName());
            this.getLogger().info("Restore Adapter: " + api.getRestoreAdapter().getDisplayName());
            this.getLogger().info("NMS version: " + nms.getClass().getSimpleName());
            this.getLogger().info("");
            StringJoiner arenaString = new StringJoiner(", ");
            arenaString.setEmptyValue("None");
            for (IArena arena : api.getArenaUtil().getArenas()) {
                arenaString.add(arena.getArenaName());
            }
            this.getLogger().info("Arena" + (api.getArenaUtil().getArenas().isEmpty() || api.getArenaUtil().getArenas().size() > 1 ? "s" : "") + " (" + api.getArenaUtil().getArenas().size() + "): " + arenaString);
            StringJoiner addonString = new StringJoiner(", ");
            addonString.setEmptyValue("None");
            for (Addon addon : api.getAddonsUtil().getAddons()) {
                addonString.add(addon.getName());
            }
            this.getLogger().info("Addon" + (addonManager.getAddons().isEmpty() || addonManager.getAddons().size() > 1 ? "s" : "") + " (" + addonManager.getAddons().size() + "): " + addonString);
            this.getLogger().info("");
            this.getLogger().info("PAPI Support: " + papiSupportLoaded);
            this.getLogger().info("Vault Chat Hook: " + vaultChatLoaded);
            this.getLogger().info("Vault Economy Hook: " + vaultEconomyLoaded);
            this.getLogger().info("");
            this.getLogger().info("TAB Version: " + Bukkit.getPluginManager().getPlugin("TAB").getDescription().getVersion());
            this.getLogger().info("TAB Features: ");
            this.getLogger().info("  - Scoreboard: " + (TabAPI.getInstance().getScoreboardManager() == null ? "false" : "true"));
            this.getLogger().info("  - BossBar: " + ((TabAPI.getInstance().getBossBarManager() == null) ? "false" : "true"));
            this.getLogger().info("  - TablistNameFormatting: " + ((TabAPI.getInstance().getTabListFormatManager() == null) ? "false" : "true"));
            this.getLogger().info("  - HeaderFooterFormatting: " + ((TabAPI.getInstance().getHeaderFooterManager() == null) ? "false" : "true"));
            this.getLogger().info("");
            this.getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }, 80L);
    }

    // HELPERS

    @Override
    public void onDisable() {
        shuttingDown = true;
        addonManager.unloadAddons();
        if (!serverSoftwareSupport) return;
        if (getServerType() == ServerType.BUNGEE && redisConnection != null) redisConnection.close();
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        if (api != null) {
            for (IArena a : new LinkedList<>(Arena.getArenas())) {
                try {
                    a.disable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public BukkitAudiences adventure() {
        return this.adventure;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new VoidChunkGenerator();
    }

    private boolean ensureSpigotCompatible() {
        if (classPresent("org.spigotmc.SpigotConfig")) return true;
        getLogger().severe("I can't run on your server software. Please check:");
        getLogger().severe("https://wiki.tomkeuper.com/docs/BedWars2023/compatibility");
        serverSoftwareSupport = false;
        return false;
    }

    private boolean downloadLibraries() {
        try {
            Path libs = Paths.get(getDataFolder().getPath(), "libs");
            ApplicationBuilder.appending("BedWars2023")
                    .logger(new SlimLogger(this))
                    .downloadDirectoryPath(libs)
                    .mirrorSelector((a, b) -> a)
                    .build();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (IOException | ReflectiveOperationException | URISyntaxException | NoSuchAlgorithmException e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Failed to prepare shaded libraries.", e);
            return false;
        }
    }

    private boolean classPresent(String fqcn) {
        try {
            Class.forName(fqcn);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private String resolveNmsVersion(String mcVersion) {
        String mapped = NMS_BY_MC.get(mcVersion);
        if (mapped != null) return mapped;
        return Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    private boolean loadVersionSupport(String nmsVer) {
        try {
            String fqcn = "com.tomkeuper.bedwars.support.version." + nmsVer + "." + nmsVer;
            Class<?> supp = Class.forName(fqcn);

            api = new API();
            Bukkit.getServicesManager().register(com.tomkeuper.bedwars.api.BedWars.class, api, this, ServicePriority.Highest);

            nms = (VersionSupport) supp
                    .getConstructor(Class.forName("org.bukkit.plugin.Plugin"), String.class)
                    .newInstance(this, nmsVer);
            return true;
        } catch (ClassNotFoundException e) {
            serverSoftwareSupport = false;
            getLogger().severe("I can't run on your version: " + minecraftVersion);
            return false;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            getLogger().log(java.util.logging.Level.SEVERE, "Could not load support for server version: " + minecraftVersion, e);
            serverSoftwareSupport = false;
            return false;
        }
    }

    private void setupFeatures() {
        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_HALLOWEEN)) HalloweenSpecial.init();
        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_ENABLED)) ResourceChestFeature.init();

        SpoilPlayerTNTFeature.init();
        GenSplitFeature.init();
        AntiDropFeature.init();
    }

    private void loadArenasAndSigns() {
        api.getRestoreAdapter().convertWorlds();

        File dir = new File(plugin.getDataFolder(), "/Arenas");
        if (!dir.exists()) return;

        File[] fls = dir.listFiles();
        if (fls == null) return;

        List<File> files = new ArrayList<>();
        for (File fl : fls) {
            if (fl.isFile() && fl.getName().endsWith(".yml")) {
                files.add(fl);
            }
        }

        if (autoscale) return;

        if (serverType == ServerType.BUNGEE) {
            if (files.isEmpty()) {
                this.getLogger().log(java.util.logging.Level.WARNING, "Could not find any arena!");
                return;
            }
            String name = files.get(new Random().nextInt(files.size())).getName().replace(".yml", "");
            new Arena(name, null);
        } else {
            for (File file : files) {
                new Arena(file.getName().replace(".yml", ""), null);
            }
        }
    }

    private String cfg(String template, String key) {
        return template.replace("%path%", key);
    }

    private <T extends IPermanentItem> void loadItems(
            String sectionPath,
            String materialPath,
            String dataPath,
            String slotPath,
            String enchantedPath,
            String commandPath,
            String nmsTag,
            Collection<T> target,
            ItemFactory<T> factory
    ) {
        if (config.getYml().get(sectionPath) == null) return;
        for (String key : config.getYml().getConfigurationSection(sectionPath).getKeys(false)) {
            if (!checkConfigEntries(key, materialPath, dataPath, slotPath, enchantedPath)) continue;

            Material material = Material.valueOf(config.getYml().getString(cfg(materialPath, key)));
            int data = config.getInt(cfg(dataPath, key));
            int slot = config.getInt(cfg(slotPath, key));
            boolean enchanted = config.getBoolean(cfg(enchantedPath, key));

            ItemStack base = new ItemBuilder(material).setDurability((short) data).build();
            if (enchanted) base = new ItemBuilder(base.getType()).setGlow(true).build();

            ItemStack tagged = nms.addCustomData(base, nmsTag);
            IPermanentItemHandler handler = itemHandlers.getOrDefault(key,
                    config.getYml().getString(cfg(commandPath, key)) != null ? itemHandlers.get("command") : null);

            if (handler == null) {
                getLogger().severe("No handler or command found for " + nmsTag + ": " + key);
                continue;
            }

            T item = factory.create(handler, tagged, slot, key);
            debug("Loaded " + nmsTag + ": " + key);
            target.add(item);
        }
    }

    private void loadPreGameItems() {
        loadItems(
                ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_PATH,
                ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_MATERIAL,
                ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_DATA,
                ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_SLOT,
                ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_ENCHANTED,
                ConfigPath.GENERAL_CONFIGURATION_PRE_GAME_ITEMS_COMMAND,
                "preGameItem",
                preGameItems,
                PreGameItem::new
        );
    }

    private void loadSpectatorItems() {
        loadItems(
                ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_PATH,
                ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_MATERIAL,
                ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_DATA,
                ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_SLOT,
                ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_ENCHANTED,
                ConfigPath.GENERAL_CONFIGURATION_SPECTATOR_ITEMS_COMMAND,
                "spectatorItem",
                spectatorItems,
                SpectatorItem::new
        );
    }

    private void loadLobbyItems() {
        loadItems(
                ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_PATH,
                ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_MATERIAL,
                ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_DATA,
                ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_SLOT,
                ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_ENCHANTED,
                ConfigPath.GENERAL_CONFIGURATION_LOBBY_ITEMS_COMMAND,
                "lobbyItem",
                lobbyItems,
                LobbyItem::new
        );
    }

    private boolean checkConfigEntries(String item, String... paths) {
        boolean valid = true;
        for (String path : paths) {
            if (config.getYml().get(path.replace("%path%", item)) == null) {
                BedWars.plugin.getLogger().severe(path.replace("%path%", item) + " is not set!");
                valid = false;
            }
        }
        return valid;
    }

    private boolean handleWorldAdapter() {
        String adapterPath;
        if (nms.getVersion() <= 12) {
            Plugin swmPlugin = Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
            if (swmPlugin == null) {
                return false;
            }
            PluginDescriptionFile pluginDescription = swmPlugin.getDescription();
            if (pluginDescription == null) {
                return false;
            }
            String[] versionString = pluginDescription.getVersion().split("\\.");
            int major = Integer.parseInt(versionString[0]);
            int minor = Integer.parseInt(versionString[1]);
            int release = versionString.length >= 3 ? Integer.parseInt(versionString[2]) : 0;
            if (((major == 2 && minor == 2 && release == 1) || swmPlugin.getDescription().getVersion().equals("2.3.0-SNAPSHOT")) && (nms.getVersion() == 0 || nms.getVersion() == 5)) {
                adapterPath = "com.tomkeuper.bedwars.arena.mapreset.slime.SlimeAdapter";
            } else if ((major == 2 && (minor >= 8 && minor <= 10) && (release >= 0 && release <= 9)) && (nms.getVersion() == 8)) {
                adapterPath = "com.tomkeuper.bedwars.arena.mapreset.slime.AdvancedSlimeAdapter";
            } else if ((major > 2 || major == 2 && minor >= 10) && (nms.getVersion() >= 9 && nms.getVersion() <= 12)) {
                adapterPath = "com.tomkeuper.bedwars.arena.mapreset.slime.SlimePaperAdapter";
            } else {
                this.getLogger().warning("Could not find adapter path for SWM version, is it unsupported?");
                return false;
            }
        } else {
            if (Bukkit.getServer().getName().equalsIgnoreCase("AdvancedSlimePaper")) {
                adapterPath = "com.tomkeuper.bedwars.arena.mapreset.slime.AdvancedSlimePaperAdapter";
            } else {
                this.getLogger().warning("Could not find adapter path for ASP version, is it unsupported?");
                return false;
            }
        }
        try {
            Constructor<?> constructor = Class.forName(adapterPath).getConstructor(Plugin.class);
            getLogger().info("Loading restore adapter: " + adapterPath + " ...");
            RestoreAdapter candidate = (RestoreAdapter) constructor.newInstance(this);
            api.setRestoreAdapter(candidate);
            getLogger().info("Hook into " + candidate.getDisplayName() + " as restore adapter.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().warning("Something went wrong! Using internal reset adapter...");
        }
        return false;
    }

    private void registerItemHandlers(IPermanentItemHandler... handlers) {
        for (IPermanentItemHandler handler : handlers) {
            if (registerItemHandler(handler)) {
                getLogger().info("Registered item handler: " + handler.getId());
            } else {
                getLogger().warning("Could not register item handler: " + handler.getId());
            }
        }
    }

    private void registerDelayedCommands() {
        nms.registerCommand("g", new ShoutCommand("g"));
        nms.registerCommand("reconectar", new RejoinCommand("reconectar"));
        nms.registerCommand("l", new LeaveCommand("l"));
        nms.registerCommand("party", new PartyCommand("party"));
    }

    @FunctionalInterface
    private interface ItemFactory<T extends IPermanentItem> {
        T create(IPermanentItemHandler handler, ItemStack stack, int slot, String id);
    }
}
