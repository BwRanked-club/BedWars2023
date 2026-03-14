package com.tomkeuper.bedwars.arena;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.exceptions.InvalidMaterialException;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.region.Region;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.tomkeuper.bedwars.configuration.Sounds;
import com.tomkeuper.bedwars.levels.internal.PlayerLevel;
import com.tomkeuper.bedwars.stats.ModeStats;
import com.tomkeuper.bedwars.stats.PlayerStats;
import com.tomkeuper.bedwars.stats.StatsMode;
import com.tomkeuper.bedwars.stats.StatsModeResolver;
import com.tomkeuper.bedwars.stats.StatsMenuHolder;
import com.tomkeuper.bedwars.support.papi.SupportPAPI;
import com.tomkeuper.bedwars.utils.ItemBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import static com.tomkeuper.bedwars.BedWars.*;
import static com.tomkeuper.bedwars.api.language.Language.getList;
import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public final class Misc {
    private static final String FALLBACK_PLAYER_NAME = "%bw_playername%";
    private static final Pattern SINGLE_PLACEHOLDER_PATTERN = Pattern.compile("^%[a-zA-Z0-9_]+%$");

    private Misc() {
    }

    public static void moveToLobbyOrKick(@NotNull Player p, @Nullable IArena arena, boolean notAbandon) {
        if (getServerType() == ServerType.BUNGEE) {
            forceKick(p, arena, notAbandon);
            return;
        }

        final String lobbyWorldName = config.getLobbyWorldName();
        final boolean alreadyInLobbyWorld = p.getWorld().getName().equalsIgnoreCase(lobbyWorldName);

        Location lobbyLoc = config.getConfigLoc("lobbyLoc");
        if (!alreadyInLobbyWorld && lobbyLoc != null) {
            try {
                p.teleport(lobbyLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            } catch (Exception ex) {
                Bukkit.getLogger().severe("Could not teleport player to lobby! Set it again with /bw setLobby");
                forceKick(p, arena, notAbandon);
                return;
            }
            removeFromArenaIfNeeded(p, arena, notAbandon);
            return;
        }

        forceKick(p, arena, notAbandon);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void forceKick(@NotNull Player p, @Nullable IArena arena, boolean notAbandon) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_LOBBY_SERVER));
        p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

        markAbandonIfNeeded(p, arena, notAbandon);

        if (getServerType() == ServerType.BUNGEE) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline()) {
                    p.kickPlayer(getMsg(p, Messages.ARENA_RESTART_PLAYER_KICK));
                    markAbandonIfNeeded(p, arena, notAbandon);
                }
            }, 30L);
        }
    }

    private static void removeFromArenaIfNeeded(@NotNull Player p, @Nullable IArena arena, boolean notAbandon) {
        if (arena == null) return;

        if (arena.isSpectator(p)) {
            arena.removeSpectator(p, false, true);
            return;
        }

        arena.removePlayer(p, false, true);

        if (getPartyManager().isInternal() && getPartyManager().hasParty(p) && getPartyManager().isOwner(p)) {
            for (Player member : new ArrayList<>(getPartyManager().getMembers(p))) {
                if (arena.isPlayer(member)) {
                    arena.removePlayer(member, false, true);
                } else if (arena.isSpectator(member)) {
                    arena.removeSpectator(member, false, true);
                } else {
                    BedWars.debug("Cannot remove " + member.getName() + " from " + arena.getDisplayName() + " (not player nor spectator).");
                }
            }
        }

        markAbandonIfNeeded(p, arena, notAbandon);
    }

    private static void markAbandonIfNeeded(@NotNull Player p, @Nullable IArena arena, boolean notAbandon) {
        if (arena == null) return;
        if (notAbandon) return;
        if (arena.getStatus() != GameState.playing) return;
        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MARK_LEAVE_AS_ABANDON)) {
            arena.abandonGame(p);
        }
    }

    @SuppressWarnings("unused")
    public static void launchFirework(@NotNull Player p) {
        spawnFirework(p.getWorld(), p.getEyeLocation(), true);
    }

    @SuppressWarnings("unused")
    public static void launchFirework(@NotNull Location l) {
        spawnFirework(l.getWorld(), l, false);
    }

    private static void spawnFirework(@NotNull World world, @NotNull Location loc, boolean setVelocityFromLook) {
        final Color[] colors = {
                Color.WHITE, Color.AQUA, Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME, Color.RED,
                Color.YELLOW, Color.BLACK, Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE
        };
        ThreadLocalRandom r = ThreadLocalRandom.current();

        Firework fw = world.spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(1);
        meta.addEffect(FireworkEffect.builder()
                .withFade(colors[r.nextInt(colors.length)])
                .withTrail()
                .withColor(colors[r.nextInt(colors.length)])
                .with(FireworkEffect.Type.BALL_LARGE)
                .build());
        fw.setFireworkMeta(meta);

        if (setVelocityFromLook) {
            fw.setVelocity(loc.getDirection());
        }
    }

    public static String replaceFirst(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

    public static ItemStack createItem(@NotNull Material material,
                                       byte data,
                                       boolean enchanted,
                                       @NotNull String name,
                                       @NotNull List<String> lore,
                                       @Nullable Player owner,
                                       @NotNull String metaKey,
                                       @NotNull String metaData) {
        ItemStack i = new ItemStack(material, 1, data);
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            im.setLore(lore);
            if (enchanted) {
                im.addEnchant(Enchantment.LUCK, 1, true);
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            i.setItemMeta(im);
        }

        if (!metaKey.isEmpty() && !metaData.isEmpty()) {
            i = nms.addCustomData(i, metaKey + "_" + metaData);
        }

        if (owner != null && nms.isPlayerHead(material.toString(), data)) {
            i = nms.getPlayerHead(owner, i);
        }
        return i;
    }

    @SuppressWarnings("unused")
    public static ItemStack createItemStack(@NotNull String material,
                                            int data,
                                            @NotNull String name,
                                            @NotNull List<String> lore,
                                            boolean enchanted,
                                            @NotNull String customData) throws InvalidMaterialException {
        final Material m;
        try {
            m = Material.valueOf(material);
        } catch (Exception e) {
            throw new InvalidMaterialException(material);
        }

        ItemStack i = new ItemStack(m, 1, (short) data);
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            im.setLore(lore);
            if (enchanted) {
                im.addEnchant(Enchantment.LUCK, 1, true);
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            i.setItemMeta(im);
        }

        if (!customData.isEmpty()) {
            i = nms.addCustomData(i, customData);
        }
        return i;
    }

    @SuppressWarnings("unused")
    public static BlockFace getDirection(@NotNull Location loc) {
        int rotation = Math.round(loc.getYaw()) % 360;
        if (rotation < 0) rotation += 360;

        if (rotation < 22) return BlockFace.SOUTH;
        if (rotation < 67) return BlockFace.SOUTH;
        if (rotation < 112) return BlockFace.WEST;
        if (rotation < 157) return BlockFace.NORTH;
        if (rotation < 202) return BlockFace.NORTH;
        if (rotation < 247) return BlockFace.NORTH;
        if (rotation < 292) return BlockFace.EAST;
        if (rotation < 337) return BlockFace.SOUTH;
        return BlockFace.SOUTH;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isProjectile(@NotNull Material m) {
        return m == Material.EGG
                || m == nms.materialFireball()
                || m == nms.materialSnowball()
                || m == Material.ARROW;
    }

    public static TextComponent msgHoverClick(@NotNull String msg, @NotNull String hover, @NotNull String click, @NotNull ClickEvent.Action clickAction) {
        TextComponent tc = new TextComponent(msg);
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        tc.setClickEvent(new ClickEvent(clickAction, click));
        return tc;
    }

    public static void addDefaultStatsItem(@NotNull YamlConfiguration yml,
                                           int slot,
                                           @NotNull Material item,
                                           int data,
                                           int amount,
                                           boolean enchanted,
                                           @NotNull String path,
                                           @NotNull String mode) {
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_MATERIAL.replace("%path%", path), item.toString());
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_DATA.replace("%path%", path), data);
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_SLOT.replace("%path%", path), slot);
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_AMOUNT.replace("%path%", path), amount);
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_ENCHANTED.replace("%path%", path), enchanted);
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_MODE.replace("%path%", path), mode);
    }

    public static void openStatsGUI(@NotNull Player player) {
        PlayerStats stats = BedWars.getStatsManager().getMutable(player.getUniqueId());
        if (stats == null) {
            return;
        }
        openStatsGUI(player, stats, player.getUniqueId(), player.getName());
    }

    public static void openStatsGUI(@NotNull Player viewer,
                                    @NotNull PlayerStats targetStats,
                                    @NotNull java.util.UUID targetId,
                                    @Nullable String fallbackName) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!viewer.isOnline()) {
                return;
            }

            Player targetPlayer = Bukkit.getPlayer(targetId);
            String targetName = resolveStatsTargetName(targetStats, targetPlayer, fallbackName);
            StatsMenuHolder holder = new StatsMenuHolder(targetId);
            Inventory inv = Bukkit.createInventory(
                    holder,
                    config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE),
                    replaceStatsPlaceholders(viewer, targetStats, targetPlayer, targetName, getMsg(viewer, Messages.PLAYER_STATS_GUI_INV_NAME), StatsMode.OVERALL, true)
            );
            holder.setInventory(inv);

            applyStatsFiller(inv);

            if (config.getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_STATS_PATH) == null) {
                return;
            }
            for (String key : config.getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_STATS_PATH).getKeys(false)) {
                if (ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE.contains(key) || "filler".equalsIgnoreCase(key)) {
                    continue;
                }

                String modeId = config.getString(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_MODE.replace("%path%", key));
                StatsMode mode = StatsMode.fromId(modeId);
                if (mode == null) {
                    continue;
                }

                ItemStack item = nms.createItemStack(
                        config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_MATERIAL.replace("%path%", key)).toUpperCase(Locale.ROOT),
                        Math.max(1, config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_AMOUNT.replace("%path%", key))),
                        (short) config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_DATA.replace("%path%", key))
                );

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_ENCHANTED.replace("%path%", key))) {
                        meta.addEnchant(Enchantment.LUCK, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    meta.setDisplayName(replaceStatsPlaceholders(viewer, targetStats, targetPlayer, targetName, getMsg(viewer, Messages.PLAYER_STATS_GUI_PATH + "-" + key + "-name"), mode, true));
                    List<String> lore = new ArrayList<>();
                    for (String line : getList(viewer, Messages.PLAYER_STATS_GUI_PATH + "-" + key + "-lore")) {
                        lore.add(replaceStatsPlaceholders(viewer, targetStats, targetPlayer, targetName, line, mode, true));
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                inv.setItem(config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_SLOT.replace("%path%", key)), item);
            }

            viewer.openInventory(inv);
            Sounds.playSound("stats-gui-open", viewer);
        });
    }

    public static String replaceStatsPlaceholders(@NotNull Player player, @NotNull String s, boolean papiReplacements) {
        return replaceStatsPlaceholders(player, s, StatsMode.OVERALL, papiReplacements);
    }

    public static String replaceStatsPlaceholders(@NotNull Player player,
                                                  @NotNull String s,
                                                  @NotNull StatsMode mode,
                                                  boolean papiReplacements) {
        PlayerStats stats = BedWars.getStatsManager().getMutable(player.getUniqueId());
        if (stats == null) {
            return s;
        }
        return replaceStatsPlaceholders(player, stats, player, player.getName(), s, mode, papiReplacements);
    }

    public static String replaceStatsPlaceholders(@NotNull Player viewer,
                                                  @NotNull PlayerStats targetStats,
                                                  @Nullable Player targetPlayer,
                                                  @Nullable String fallbackName,
                                                  @NotNull String s,
                                                  @NotNull StatsMode mode,
                                                  boolean papiReplacements) {
        ModeStats selected = targetStats.getModeStatsOrEmpty(mode);
        PlayerLevel targetLevel = resolveTargetLevel(targetStats, targetPlayer);
        String targetName = resolveStatsTargetName(targetStats, targetPlayer, fallbackName);
        String statsDisplayName = targetPlayer != null ? getPlayerName(targetPlayer) : targetName;
        String prefix = targetPlayer != null ? BedWars.getChatSupport().getPrefix(targetPlayer) : "";

        s = s.replace("%bw_player%", statsDisplayName)
                .replace("%bw_playername%", targetName)
                .replace("%bw_prefix%", prefix)
                .replace("%bw_mode%", mode.getId())
                .replace("%bw_kills%", String.valueOf(selected.getKills()))
                .replace("%bw_total_kills%", String.valueOf(selected.getTotalKills()))
                .replace("%bw_deaths%", String.valueOf(selected.getDeaths()))
                .replace("%bw_losses%", String.valueOf(selected.getLosses()))
                .replace("%bw_wins%", String.valueOf(selected.getWins()))
                .replace("%bw_final_kills%", String.valueOf(selected.getFinalKills()))
                .replace("%bw_final_deaths%", String.valueOf(selected.getFinalDeaths()))
                .replace("%bw_beds%", String.valueOf(selected.getBedsDestroyed()))
                .replace("%bw_beds_broken%", String.valueOf(selected.getBedsDestroyed()))
                .replace("%bw_beds_lost%", String.valueOf(selected.getBedsLost()))
                .replace("%bw_assists%", String.valueOf(selected.getAssists()))
                .replace("%bw_final_assists%", String.valueOf(selected.getFinalAssists()))
                .replace("%bw_games_played%", String.valueOf(selected.getGamesPlayed()))
                .replace("%bw_level%", targetLevel == null ? "0" : targetLevel.getLevelName())
                .replace("%bw_level_unformatted%", targetLevel == null ? "0" : String.valueOf(targetLevel.getLevel()))
                .replace("%bw_current_xp%", targetLevel == null ? "0" : targetLevel.getFormattedCurrentXp())
                .replace("%bw_required_xp%", targetLevel == null ? "0" : targetLevel.getFormattedRequiredXp())
                .replace("%bw_xp%", targetLevel == null ? "0" : targetLevel.getFormattedCurrentXp())
                .replace("%bw_progress%", targetLevel == null ? "" : targetLevel.getProgress())
                .replace("%bw_kdr%", formatRatio(selected.getKills(), selected.getDeaths()))
                .replace("%bw_fkdr%", formatRatio(selected.getFinalKills(), selected.getFinalDeaths()))
                .replace("%bw_wlr%", formatRatio(selected.getWins(), selected.getLosses()));

        final String dateFmt = getMsg(viewer, Messages.FORMATTING_STATS_DATE_FORMAT);
        SimpleDateFormat sdf = new SimpleDateFormat(dateFmt);
        String never = getMsg(viewer, Messages.MEANING_NEVER);
        String first = selected.getFirstPlay() == null ? never : sdf.format(Timestamp.from(selected.getFirstPlay()));
        String last = selected.getLastPlay() == null ? never : sdf.format(Timestamp.from(selected.getLastPlay()));

        s = s.replace("%bw_play_first%", first)
                .replace("%bw_play_last%", last);
        s = replaceStatsAliasPlaceholders(s, selected, targetLevel);

        for (StatsMode scopedMode : StatsMode.values()) {
            if (scopedMode == StatsMode.OVERALL) continue;
            s = replaceExplicitModePlaceholders(s, targetStats, targetLevel, scopedMode);
        }

        return papiReplacements && targetPlayer != null ? SupportPAPI.getSupportPAPI().replace(targetPlayer, s) : s;
    }

    private static String replaceExplicitModePlaceholders(String input,
                                                          PlayerStats targetStats,
                                                          @Nullable PlayerLevel targetLevel,
                                                          StatsMode scopedMode) {
        ModeStats scopedStats = targetStats.getModeStatsOrEmpty(scopedMode);

        String value = input;
        for (String token : StatsModeResolver.getPlaceholderTokens(scopedMode)) {
            String[] prefixes = {
                    "%bw_" + token + "_",
                    "%bw_mode_" + token + "_",
                    "%bw_stats_"
            };
            for (String prefix : prefixes) {
                if ("%bw_stats_".equals(prefix)) {
                    value = value.replace(prefix + "kills_" + token + "%", String.valueOf(scopedStats.getKills()))
                            .replace(prefix + "total_kills_" + token + "%", String.valueOf(scopedStats.getTotalKills()))
                            .replace(prefix + "deaths_" + token + "%", String.valueOf(scopedStats.getDeaths()))
                            .replace(prefix + "losses_" + token + "%", String.valueOf(scopedStats.getLosses()))
                            .replace(prefix + "wins_" + token + "%", String.valueOf(scopedStats.getWins()))
                            .replace(prefix + "final_kills_" + token + "%", String.valueOf(scopedStats.getFinalKills()))
                            .replace(prefix + "final_deaths_" + token + "%", String.valueOf(scopedStats.getFinalDeaths()))
                            .replace(prefix + "beds_" + token + "%", String.valueOf(scopedStats.getBedsDestroyed()))
                            .replace(prefix + "beds_broken_" + token + "%", String.valueOf(scopedStats.getBedsDestroyed()))
                            .replace(prefix + "beds_lost_" + token + "%", String.valueOf(scopedStats.getBedsLost()))
                            .replace(prefix + "assists_" + token + "%", String.valueOf(scopedStats.getAssists()))
                            .replace(prefix + "final_assists_" + token + "%", String.valueOf(scopedStats.getFinalAssists()))
                            .replace(prefix + "games_played_" + token + "%", String.valueOf(scopedStats.getGamesPlayed()))
                            .replace(prefix + "level_" + token + "%", targetLevel == null ? "0" : targetLevel.getLevelName())
                            .replace(prefix + "level_unformatted_" + token + "%", targetLevel == null ? "0" : String.valueOf(targetLevel.getLevel()))
                            .replace(prefix + "xp_" + token + "%", targetLevel == null ? "0" : targetLevel.getFormattedCurrentXp())
                            .replace(prefix + "current_xp_" + token + "%", targetLevel == null ? "0" : targetLevel.getFormattedCurrentXp())
                            .replace(prefix + "required_xp_" + token + "%", targetLevel == null ? "0" : targetLevel.getFormattedRequiredXp())
                            .replace(prefix + "progress_" + token + "%", targetLevel == null ? "" : targetLevel.getProgress());
                    continue;
                }
                value = value.replace(prefix + "kills%", String.valueOf(scopedStats.getKills()))
                        .replace(prefix + "total_kills%", String.valueOf(scopedStats.getTotalKills()))
                        .replace(prefix + "deaths%", String.valueOf(scopedStats.getDeaths()))
                        .replace(prefix + "losses%", String.valueOf(scopedStats.getLosses()))
                        .replace(prefix + "wins%", String.valueOf(scopedStats.getWins()))
                        .replace(prefix + "final_kills%", String.valueOf(scopedStats.getFinalKills()))
                        .replace(prefix + "final_deaths%", String.valueOf(scopedStats.getFinalDeaths()))
                        .replace(prefix + "beds%", String.valueOf(scopedStats.getBedsDestroyed()))
                        .replace(prefix + "beds_broken%", String.valueOf(scopedStats.getBedsDestroyed()))
                        .replace(prefix + "beds_lost%", String.valueOf(scopedStats.getBedsLost()))
                        .replace(prefix + "assists%", String.valueOf(scopedStats.getAssists()))
                        .replace(prefix + "final_assists%", String.valueOf(scopedStats.getFinalAssists()))
                        .replace(prefix + "games_played%", String.valueOf(scopedStats.getGamesPlayed()))
                        .replace(prefix + "level%", targetLevel == null ? "0" : targetLevel.getLevelName())
                        .replace(prefix + "level_unformatted%", targetLevel == null ? "0" : String.valueOf(targetLevel.getLevel()))
                        .replace(prefix + "xp%", targetLevel == null ? "0" : targetLevel.getFormattedCurrentXp())
                        .replace(prefix + "current_xp%", targetLevel == null ? "0" : targetLevel.getFormattedCurrentXp())
                        .replace(prefix + "required_xp%", targetLevel == null ? "0" : targetLevel.getFormattedRequiredXp())
                        .replace(prefix + "progress%", targetLevel == null ? "" : targetLevel.getProgress());
            }
        }
        return value;
    }

    private static void applyStatsFiller(@NotNull Inventory inv) {
        if (!config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_STATS_FILLER_ENABLED)) {
            return;
        }

        ItemStack filler = nms.createItemStack(
                config.getString(ConfigPath.GENERAL_CONFIGURATION_STATS_FILLER_MATERIAL).toUpperCase(Locale.ROOT),
                1,
                (short) config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_FILLER_DATA)
        );
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        if (config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_STATS_FILLER_ENCHANTED)) {
            filler = new ItemBuilder(filler.getType()).setDurability(filler.getDurability()).setGlow(true).build();
        }
        for (int slot = 0; slot < inv.getSize(); slot++) {
            inv.setItem(slot, filler.clone());
        }
    }

    private static String resolveStatsTargetName(@NotNull PlayerStats stats, @Nullable Player targetPlayer, @Nullable String fallbackName) {
        if (targetPlayer != null) {
            return targetPlayer.getName();
        }
        if (stats.getName() != null && !stats.getName().isBlank()) {
            return stats.getName();
        }
        return fallbackName == null || fallbackName.isBlank() ? "Unknown" : fallbackName;
    }

    private static @Nullable PlayerLevel resolveTargetLevel(@NotNull PlayerStats targetStats, @Nullable Player targetPlayer) {
        if (targetPlayer != null) {
            return PlayerLevel.getLevelByPlayer(targetPlayer.getUniqueId());
        }
        return PlayerLevel.getCachedLevelByPlayer(targetStats.getUuid());
    }

    private static String replaceStatsAliasPlaceholders(String input, ModeStats stats, @Nullable PlayerLevel targetLevel) {
        return input.replace("%bw_stats_kills%", String.valueOf(stats.getKills()))
                .replace("%bw_stats_total_kills%", String.valueOf(stats.getTotalKills()))
                .replace("%bw_stats_deaths%", String.valueOf(stats.getDeaths()))
                .replace("%bw_stats_losses%", String.valueOf(stats.getLosses()))
                .replace("%bw_stats_wins%", String.valueOf(stats.getWins()))
                .replace("%bw_stats_final_kills%", String.valueOf(stats.getFinalKills()))
                .replace("%bw_stats_final_deaths%", String.valueOf(stats.getFinalDeaths()))
                .replace("%bw_stats_beds%", String.valueOf(stats.getBedsDestroyed()))
                .replace("%bw_stats_beds_broken%", String.valueOf(stats.getBedsDestroyed()))
                .replace("%bw_stats_beds_lost%", String.valueOf(stats.getBedsLost()))
                .replace("%bw_stats_assists%", String.valueOf(stats.getAssists()))
                .replace("%bw_stats_final_assists%", String.valueOf(stats.getFinalAssists()))
                .replace("%bw_stats_games_played%", String.valueOf(stats.getGamesPlayed()))
                .replace("%bw_stats_level%", targetLevel == null ? "0" : targetLevel.getLevelName())
                .replace("%bw_stats_level_unformatted%", targetLevel == null ? "0" : String.valueOf(targetLevel.getLevel()))
                .replace("%bw_stats_xp%", targetLevel == null ? "0" : targetLevel.getFormattedCurrentXp())
                .replace("%bw_stats_current_xp%", targetLevel == null ? "0" : targetLevel.getFormattedCurrentXp())
                .replace("%bw_stats_required_xp%", targetLevel == null ? "0" : targetLevel.getFormattedRequiredXp())
                .replace("%bw_stats_progress%", targetLevel == null ? "" : targetLevel.getProgress());
    }

    private static String formatRatio(int numerator, int denominator) {
        if (denominator <= 0) {
            return String.format(Locale.US, "%.2f", (double) numerator);
        }
        return String.format(Locale.US, "%.2f", (double) numerator / (double) denominator);
    }

    public static String getPlayerName(@NotNull Player player) {
        String configured = BedWars.config.getString(ConfigPath.SB_CONFIG_SIDEBAR_PLAYER_NAME);
        if (configured == null) {
            return player.getDisplayName();
        }

        String trimmed = configured.trim();
        if (trimmed.isEmpty()) {
            return player.getDisplayName();
        }

        String stripped = stripColors(trimmed);
        if ("%bw_player%".equalsIgnoreCase(stripped)) {
            return player.getDisplayName();
        }
        if (FALLBACK_PLAYER_NAME.equalsIgnoreCase(stripped)) {
            return player.getName();
        }

        String resolved = SupportPAPI.getSupportPAPI().replace(player, trimmed);
        if (resolved == null || resolved.trim().isEmpty()) {
            return player.getDisplayName();
        }

        if (resolved.equals(trimmed)) {
            return player.getDisplayName();
        }

        String resolvedStripped = stripColors(resolved);
        if (isSinglePlaceholder(resolvedStripped)) {
            return player.getDisplayName();
        }

        return resolved;
    }

    private static boolean isSinglePlaceholder(String value) {
        if (value == null) return false;
        return SINGLE_PLACEHOLDER_PATTERN.matcher(value.trim()).matches();
    }

    private static String stripColors(String value) {
        if (value == null) return "";
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', value));
    }

    public static boolean isNumber(@NotNull String s) {
        try {
            new BigDecimal(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isOutsideOfBorder(@NotNull Location l) {
        WorldBorder border = l.getWorld().getWorldBorder();
        double radius = (border.getSize() / 2) + border.getWarningDistance();
        Location center = border.getCenter();
        return center.distance(l) >= radius;
    }

    public static boolean isBuildProtected(@NotNull Location l, @NotNull IArena a) {
        for (Region region : a.getRegionsList()) {
            if (region.isInRegion(l)) return true;
        }
        return isOutsideOfBorder(l);
    }

    public static Location minLoc(@NotNull Location loc1, @NotNull Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) {
            throw new IllegalStateException("Locations are not in the same world!");
        }
        return new Location(
                loc1.getWorld(),
                Math.min(loc1.getX(), loc2.getX()),
                Math.min(loc1.getY(), loc2.getY()),
                Math.min(loc1.getZ(), loc2.getZ())
        );
    }

    public static Location maxLoc(@NotNull Location loc1, @NotNull Location loc2) {
        if (loc1.getWorld() != loc2.getWorld()) {
            throw new IllegalStateException("Locations are not in the same world!");
        }
        return new Location(
                loc1.getWorld(),
                Math.max(loc1.getX(), loc2.getX()),
                Math.max(loc1.getY(), loc2.getY()),
                Math.max(loc1.getZ(), loc2.getZ())
        );
    }
}
