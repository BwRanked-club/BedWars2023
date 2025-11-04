package com.tomkeuper.bedwars.arena.mapreset.slime;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntTag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.flowpowered.nbt.stream.NBTOutputStream;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.server.ISetupSession;
import com.tomkeuper.bedwars.api.server.RestoreAdapter;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.tomkeuper.bedwars.api.util.FileUtil;
import com.tomkeuper.bedwars.api.util.ZipFileUtil;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@SuppressWarnings({"unused"})
public class SlimeAdapter extends RestoreAdapter {

    private final SlimePlugin slime;
    private final BedWars api;
    private SlimeLoader fileLoader;

    private static final int MAX_LOAD_RETRIES = 3;

    public SlimeAdapter(Plugin plugin) {
        super(plugin);

        var p = Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        if (!(p instanceof SlimePlugin)) {
            throw new IllegalStateException("SlimeWorldManager is required but not found or invalid.");
        }
        slime = (SlimePlugin) p;

        var reg = Bukkit.getServer().getServicesManager().getRegistration(BedWars.class);
        if (reg == null || reg.getProvider() == null) {
            throw new IllegalStateException("BedWars service provider not found in Bukkit ServicesManager.");
        }
        api = reg.getProvider();

        try {
            fileLoader = slime.getLoader("file");
            if (fileLoader == null) {
                throw new IllegalStateException(
                        "SWM returned a null 'file' loader. Check SWM config/loaders and ensure the 'file' loader is enabled.");
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Could not obtain SWM file loader.", e);
        }
    }


    @Override
    public void onEnable(@NotNull IArena a) {
        if (api.getVersionSupport().getMainLevel().equalsIgnoreCase(a.getWorldName())) {
            if (!(api.getServerType() == ServerType.BUNGEE && api.getArenaUtil().getGamesBeforeRestart() == 1)) {
                FileUtil.setMainLevel("ignore_main_level", api.getVersionSupport());
                getOwner().getLogger().log(Level.SEVERE, "Cannot use level-name as arenas. Creating a new void map for level-name and restarting.");
                Bukkit.getServer().spigot().restart();
                return;
            }
        }

        if (Bukkit.getWorld(a.getWorldName()) != null) {
            Bukkit.getScheduler().runTask(getOwner(), () -> a.init(Objects.requireNonNull(Bukkit.getWorld(a.getWorldName()))));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), () -> loadArenaAsync(a, 0));
    }

    private void loadArenaAsync(@NonNull IArena a, int attempt) {
        final String[] spawn = safeSpawn(a.getConfig().getString("waiting.Loc"));
        final SlimePropertyMap props = buildPropertyMap(spawn);

        try {
            SlimeWorld world = slime.loadWorld(fileLoader, a.getArenaName(), true, props);

            if (api.isAutoScale())
                world = world.clone(a.getWorldName());

            final SlimeWorld finalWorld = world;

            if (Bukkit.getWorld(finalWorld.getName()) != null) {
                getOwner().getLogger().log(Level.INFO,
                        "[AutoScale] World " + finalWorld.getName() + " já carregado, aguardando listener.");
                return;
            }

            Bukkit.getScheduler().runTask(getOwner(), () -> {
                if (Bukkit.getWorld(finalWorld.getName()) == null)
                    slime.generateWorld(finalWorld);

                World bukkitWorld = Bukkit.getWorld(finalWorld.getName());
                if (bukkitWorld != null) {

                    // ✅ Proteção contra init() duplicado (ex: listener + SWM)
                    if (!a.isInitialized()) {
                        a.init(bukkitWorld);
                        getOwner().getLogger().log(Level.INFO,
                                "[AutoScale] Initialized arena manually: " + a.getWorldName());
                    } else {
                        getOwner().getLogger().log(Level.INFO,
                                "[AutoScale] Arena " + a.getWorldName() + " já estava inicializada, ignorando duplicação.");
                    }

                } else {
                    getOwner().getLogger().log(Level.WARNING,
                            "[AutoScale] Failed to init arena " + a.getWorldName() + " (world null)");
                }
            });
        } catch (UnknownWorldException | IOException | CorruptedWorldException |
                 NewerFormatException | WorldInUseException ex) {
            api.getArenaUtil().removeFromEnableQueue(a);
            getOwner().getLogger().log(Level.SEVERE, "Failed to load arena '" + a.getArenaName() + "'.", ex);

        } catch (ConcurrentModificationException swmRace) {
            api.getArenaUtil().removeFromEnableQueue(a);
            getOwner().getLogger().severe("SlimeWorldManager race condition while loading '" + a.getArenaName() + "'.");

            if (attempt + 1 < MAX_LOAD_RETRIES) {
                int delay = 10 + ThreadLocalRandom.current().nextInt(10) + (attempt * 20);
                getOwner().getLogger().warning("Retrying to load arena '" + a.getArenaName() + "' in "
                        + delay + " ticks (attempt " + (attempt + 1) + "/" + MAX_LOAD_RETRIES + ").");
                Bukkit.getScheduler().runTaskLaterAsynchronously(getOwner(),
                        () -> loadArenaAsync(a, attempt + 1), delay);
            } else {
                getOwner().getLogger().severe("Exceeded max retries for '" + a.getArenaName() + "'. Aborting load.");
            }
        }
    }

    private @NotNull SlimePropertyMap buildPropertyMap(String @NotNull [] spawn) {
        SlimePropertyMap spm = new SlimePropertyMap();
        spm.setString(SlimeProperties.WORLD_TYPE, "flat");

        int sx = parseInt(spawn, 0, 0);
        int sy = parseInt(spawn, 1, 50);
        int sz = parseInt(spawn, 2, 0);

        spm.setInt(SlimeProperties.SPAWN_X, sx);
        spm.setInt(SlimeProperties.SPAWN_Y, sy);
        spm.setInt(SlimeProperties.SPAWN_Z, sz);

        spm.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
        spm.setBoolean(SlimeProperties.ALLOW_MONSTERS, false);
        spm.setString(SlimeProperties.DIFFICULTY, "easy");
        spm.setBoolean(SlimeProperties.PVP, true);
        // Opcional: manter ENVIRONMENT/WORLD_TYPE conforme desejado
        spm.setString(SlimeProperties.ENVIRONMENT, "normal");

        return spm;
    }

    private static int parseInt(String[] arr, int idx, int def) {
        if (idx < 0 || idx >= arr.length) return def;
        try {
            return (int) Double.parseDouble(arr[idx]);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String[] safeSpawn(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) return new String[]{"0", "50", "0"};
        String[] s = raw.split(",");
        if (s.length < 3) return new String[]{"0", "50", "0"};
        return s;
    }

    @Override
    public void onRestart(IArena a) {
        if (api.getServerType() == ServerType.BUNGEE) {
            if (api.getArenaUtil().getGamesBeforeRestart() == 0) {
                if (api.getArenaUtil().getArenas().size() == 1 &&
                        api.getArenaUtil().getArenas().get(0).getStatus() == GameState.restarting) {
                    var cmd = api.getConfigs().getMainConfig().getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_RESTART_CMD);
                    getOwner().getLogger().info("Dispatching command: " + cmd);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            } else {
                if (api.getArenaUtil().getGamesBeforeRestart() != -1) {
                    api.getArenaUtil().setGamesBeforeRestart(api.getArenaUtil().getGamesBeforeRestart() - 1);
                }
                Bukkit.getScheduler().runTask(getOwner(), () -> {
                    Bukkit.unloadWorld(a.getWorldName(), false);
                    if (api.getArenaUtil().canAutoScale(a.getArenaName())) {
                        Bukkit.getScheduler().runTaskLater(getOwner(), () ->
                                api.getArenaUtil().loadArena(a.getArenaName(), null), 80L);
                    }
                });
            }
        } else {
            Bukkit.getScheduler().runTask(getOwner(), () -> {
                Bukkit.unloadWorld(a.getWorldName(), false);
                Bukkit.getScheduler().runTaskLater(getOwner(), () ->
                        api.getArenaUtil().loadArena(a.getArenaName(), null), 80L);
            });
        }
    }

    @Override
    public void onDisable(IArena a) {
        if (api.isShuttingDown()) {
            Bukkit.unloadWorld(a.getWorldName(), false);
            return;
        }
        Bukkit.getScheduler().runTask(getOwner(), () -> Bukkit.unloadWorld(a.getWorldName(), false));
    }

    @Override
    public void onSetupSessionStart(ISetupSession s) {
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), () -> {
            String[] spawn = new String[]{"0", "50", "0"};
            if (s.getConfig().getYml().getString("waiting.Loc") != null) {
                spawn = safeSpawn(s.getConfig().getString("waiting.Loc"));
            }
            SlimePropertyMap spm = this.buildPropertyMap(spawn);

            try {
                if (Bukkit.getWorld(s.getWorldName()) != null) {
                    Bukkit.getScheduler().runTask(getOwner(), () ->
                            Bukkit.unloadWorld(s.getWorldName(), false));
                }

                SlimeWorld world;
                if (fileLoader.worldExists(s.getWorldName())) {
                    world = slime.loadWorld(fileLoader, s.getWorldName(), false, spm);
                    Bukkit.getScheduler().runTask(getOwner(), () ->
                            s.getPlayer().sendMessage(ChatColor.GREEN + "Loading world from SlimeWorldManager container."));
                } else {
                    File vanilla = new File(Bukkit.getWorldContainer(), s.getWorldName());
                    if (new File(vanilla, "level.dat").exists()) {
                        Bukkit.getScheduler().runTask(getOwner(), () ->
                                s.getPlayer().sendMessage(ChatColor.GREEN + "Importing world to the SlimeWorldManager container."));
                        slime.importWorld(vanilla, s.getWorldName().toLowerCase(), fileLoader);
                        world = slime.loadWorld(fileLoader, s.getWorldName(), false, spm);
                    } else {
                        Bukkit.getScheduler().runTask(getOwner(), () ->
                                s.getPlayer().sendMessage(ChatColor.GREEN + "Creating a new void map."));
                        world = slime.createEmptyWorld(fileLoader, s.getWorldName(), false, spm);
                    }
                }

                SlimeWorld sw = world;
                Bukkit.getScheduler().runTask(getOwner(), () -> {
                    slime.generateWorld(sw);
                    s.teleportPlayer();
                });
            } catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException |
                     WorldInUseException | WorldAlreadyExistsException | InvalidWorldException |
                     WorldTooBigException | WorldLoadedException ex) {
                Bukkit.getScheduler().runTask(getOwner(), () -> s.getPlayer().sendMessage(
                        ChatColor.RED + "An error occurred! Please check console."));
                getOwner().getLogger().log(Level.SEVERE, "Setup session failed for world '" + s.getWorldName() + "'.", ex);
                s.close();
            }
        });
    }

    @Override
    public void onSetupSessionClose(@NotNull ISetupSession session) {
        World world = Bukkit.getWorld(session.getWorldName());
        if (world == null) return;

        world.save();
        Bukkit.getScheduler().runTask(getOwner(), () ->
                Bukkit.unloadWorld(session.getWorldName(), true));
    }

    @Override
    public boolean isWorld(String name) {
        try {
            return fileLoader.worldExists(name);
        } catch (IOException e) {
            getOwner().getLogger().log(Level.WARNING, "Failed checking world existence for '" + name + "'.", e);
            return false;
        }
    }

    @Override
    public void deleteWorld(String name) {
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), () -> {
            try {
                fileLoader.deleteWorld(name);
            } catch (UnknownWorldException | IOException e) {
                getOwner().getLogger().log(Level.WARNING, "Failed to delete world '" + name + "'.", e);
            }
        });
    }

    @Override
    public void cloneArena(String name1, String name2) {
        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), () -> {
            SlimePropertyMap spm = this.buildPropertyMap(new String[]{"0", "118", "0"});
            try {
                SlimeWorld world = slime.loadWorld(fileLoader, name1, true, spm);
                world.clone(name2, fileLoader);
            } catch (UnknownWorldException | IOException | CorruptedWorldException |
                     NewerFormatException | WorldInUseException | WorldAlreadyExistsException ex) {
                getOwner().getLogger().log(Level.WARNING,
                        "Failed to clone arena '" + name1 + "' to '" + name2 + "'.", ex);
            }
        });
    }

    @Override
    public List<String> getWorldsList() {
        try {
            return fileLoader.listWorlds();
        } catch (IOException e) {
            getOwner().getLogger().log(Level.WARNING, "Failed listing SWM worlds.", e);
            return Collections.emptyList();
        }
    }

    public void convertWorlds() {
        File dir = new File(getOwner().getDataFolder(), "/Arenas");
        File ff;

        if (dir.exists()) {
            File[] fls = dir.listFiles();
            if (fls != null) {
                for (File fl : fls) {
                    if (!fl.isFile() || !fl.getName().endsWith(".yml")) continue;

                    final String arenaName = fl.getName().replace(".yml", "").toLowerCase();
                    ff = new File(Bukkit.getWorldContainer(), fl.getName().replace(".yml", ""));
                    try {
                        if (!fileLoader.worldExists(arenaName)) {
                            if (!fl.getName().equals(arenaName + ".yml")) {
                                if (!fl.renameTo(new File(dir, arenaName + ".yml"))) {
                                    getOwner().getLogger().log(Level.WARNING,
                                            "Could not rename " + fl.getName() + " to " + arenaName + ".yml");
                                }
                            }
                            File bc = new File(getOwner().getDataFolder() + "/Cache", ff.getName() + ".zip");
                            if (ff.exists() && bc.exists()) {
                                FileUtil.delete(ff);
                                ZipFileUtil.unzipFileIntoDirectory(bc, new File(Bukkit.getWorldContainer(), arenaName));
                            }
                            deleteWorldTrash(arenaName);
                            handleLevelDat(arenaName);
                            convertWorld(arenaName, null);
                        }
                    } catch (IOException e) {
                        getOwner().getLogger().log(Level.WARNING,
                                "Error while preparing vanilla -> slime conversion for '" + arenaName + "'.", e);
                    }
                }
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(getOwner(), () -> {
            File[] files = Bukkit.getWorldContainer().listFiles();
            if (files == null) return;
            for (File f : files) {
                if (f != null && f.isDirectory() && f.getName().contains("bw_temp_")) {
                    try {
                        FileUtils.deleteDirectory(f);
                    } catch (IOException e) {
                        getOwner().getLogger().log(Level.WARNING,
                                "Could not delete temp directory: " + f.getAbsolutePath(), e);
                    }
                }
            }
        });
    }

    @Override
    public String getDisplayName() {
        return "Slime World Manager by Grinderwolf";
    }

    private void convertWorld(String name, @Nullable Player player) {
        File worldFolder = new File(Bukkit.getWorldContainer(), name);
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            getOwner().getLogger().severe("Tried converting arena " + name + " to Slime format, but no bukkit world folder found.");
            return;
        }

        try {
            getOwner().getLogger().log(Level.INFO, "Converting " + name + " to the Slime format.");
            slime.importWorld(worldFolder, name, fileLoader);
        } catch (WorldAlreadyExistsException | InvalidWorldException | WorldLoadedException |
                 WorldTooBigException | IOException e) {
            if (player != null) {
                player.sendMessage(ChatColor.RED + "Could not convert " + name + " to the Slime format.");
                player.sendMessage(ChatColor.RED + "Check the console for details.");
                ISetupSession s = api.getSetupSession(player.getUniqueId());
                if (s != null) s.close();
            }
            getOwner().getLogger().log(Level.WARNING,
                    "Could not convert " + name + " to the Slime format.", e);
        }
    }

    private void deleteWorldTrash(String world) {
        File[] trash = new File[]{
                new File(Bukkit.getWorldContainer(), world + "/level.dat_mcr"),
                new File(Bukkit.getWorldContainer(), world + "/level.dat_old"),
                new File(Bukkit.getWorldContainer(), world + "/session.lock"),
                new File(Bukkit.getWorldContainer(), world + "/uid.dat")
        };
        for (File f : trash) {
            if (!f.exists()) continue;
            if (!f.delete()) {
                getOwner().getLogger().warning("Could not delete: " + f.getPath());
                getOwner().getLogger().warning("This may cause issues!");
            }
        }
    }

    private void handleLevelDat(String world) throws IOException {
        File worldFolder = new File(Bukkit.getWorldContainer(), world);
        if (!worldFolder.exists() || !worldFolder.isDirectory()) return;

        File levelFile = new File(worldFolder, "level.dat");
        if (levelFile.exists()) return;

        File regionFolder = new File(worldFolder, "region");
        if (!regionFolder.exists() || !regionFolder.isDirectory()) {
            getOwner().getLogger().severe("Tried detecting world version, but it has no regions! (" + world + ")");
            return;
        }

        File[] regionFiles = regionFolder.listFiles();
        if (regionFiles == null || regionFiles.length == 0) {
            getOwner().getLogger().severe("No region files found for '" + world + "'.");
            return;
        }

        Optional<File> firstRegion = Arrays.stream(regionFiles)
                .filter(f -> f.isFile() && f.getName().endsWith(".mca"))
                .findFirst();

        AtomicReference<Optional<Integer>> dataVersion = new AtomicReference<>(Optional.empty());

        if (firstRegion.isPresent()) {
            try (NBTInputStream in = new NBTInputStream(new FileInputStream(firstRegion.get()))) {
                var root = in.readTag();
                if (root != null) {
                    Optional<CompoundTag> asCompound = root.getAsCompoundTag();
                    if (asCompound.isPresent()) {
                        Optional<CompoundTag> chunk = asCompound.get().getAsCompoundTag("Chunk");
                        Optional<Integer> version = chunk.flatMap(c -> c.getIntValue("DataVersion"));
                        version.ifPresent(v -> {
                            dataVersion.set(Optional.of(v));
                            Bukkit.getLogger().info("Detected world DataVersion " + v + " for " + world);
                        });
                    }
                }
            } catch (Exception ignored) {
            }
        }

        String errorMessage = "Cannot create level.dat in " + worldFolder;

        if (!levelFile.createNewFile()) {
            getOwner().getLogger().severe(errorMessage);
            return;
        }

        try (NBTOutputStream outputStream = new NBTOutputStream(new FileOutputStream(levelFile))) {
            CompoundMap cm = new CompoundMap();
            cm.put(new IntTag("SpawnX", 0));
            cm.put(new IntTag("SpawnY", 255));
            cm.put(new IntTag("SpawnZ", 0));
            dataVersion.get().ifPresent(ver -> cm.put(new IntTag("DataVersion", ver)));

            CompoundTag dataTag = new CompoundTag("Data", cm);
            CompoundMap rootTag = new CompoundMap();
            rootTag.put(dataTag);

            outputStream.writeTag(new CompoundTag("", rootTag));
            outputStream.flush();
        } catch (Exception ex) {
            try {
                levelFile.delete();
            } catch (Exception ignored) {
            }
            getOwner().getLogger().log(Level.SEVERE, errorMessage, ex);
        }
    }

    private @Nullable SlimeLoader getFileLoaderSafe() {
        if (fileLoader == null) {
            try {
                fileLoader = slime.getLoader("file");
            } catch (Exception e) {
                getOwner().getLogger().warning("SWM file loader not ready yet.");
            }
        }
        return fileLoader;
    }
}
