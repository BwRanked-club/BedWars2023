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
import com.tomkeuper.bedwars.api.stats.IPlayerStats;
import com.tomkeuper.bedwars.configuration.Sounds;
import com.tomkeuper.bedwars.support.papi.SupportPAPI;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.tomkeuper.bedwars.BedWars.*;
import static com.tomkeuper.bedwars.api.language.Language.getList;
import static com.tomkeuper.bedwars.api.language.Language.getMsg;

public final class Misc {

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

    public static void addDefaultStatsItem(@NotNull YamlConfiguration yml, int slot, @NotNull Material item, int data, @NotNull String path) {
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_MATERIAL.replace("%path%", path), item.toString());
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_DATA.replace("%path%", path), data);
        yml.addDefault(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_SLOT.replace("%path%", path), slot);
    }

    public static void openStatsGUI(@NotNull Player p) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!p.isOnline()) return;

            Inventory inv = Bukkit.createInventory(
                    null,
                    config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE),
                    replaceStatsPlaceholders(p, getMsg(p, Messages.PLAYER_STATS_GUI_INV_NAME), true)
            );

            for (String s : config.getYml().getConfigurationSection(ConfigPath.GENERAL_CONFIGURATION_STATS_PATH).getKeys(false)) {
                if (ConfigPath.GENERAL_CONFIGURATION_STATS_GUI_SIZE.contains(s)) continue;

                ItemStack i = nms.createItemStack(
                        config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_MATERIAL.replace("%path%", s)).toUpperCase(),
                        1,
                        (short) config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_DATA.replace("%path%", s))
                );

                ItemMeta im = i.getItemMeta();
                if (im != null) {
                    im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    im.setDisplayName(replaceStatsPlaceholders(p, getMsg(p, Messages.PLAYER_STATS_GUI_PATH + "-" + s + "-name"), true));
                    List<String> lore = new ArrayList<>();
                    for (String line : getList(p, Messages.PLAYER_STATS_GUI_PATH + "-" + s + "-lore")) {
                        lore.add(replaceStatsPlaceholders(p, line, true));
                    }
                    im.setLore(lore);
                    i.setItemMeta(im);
                }

                inv.setItem(config.getInt(ConfigPath.GENERAL_CONFIGURATION_STATS_ITEMS_SLOT.replace("%path%", s)), i);
            }

            p.openInventory(inv);
            Sounds.playSound("stats-gui-open", p);
        });
    }

    public static String replaceStatsPlaceholders(@NotNull Player player, @NotNull String s, boolean papiReplacements) {
        IPlayerStats stats = BedWars.getStatsManager().get(player.getUniqueId());

        s = s.replace("%bw_player%", player.getDisplayName())
                .replace("%bw_playername%", player.getName())
                .replace("%bw_prefix%", BedWars.getChatSupport().getPrefix(player))
                .replace("%bw_kills%", String.valueOf(stats.getKills()))
                .replace("%bw_deaths%", String.valueOf(stats.getDeaths()))
                .replace("%bw_losses%", String.valueOf(stats.getLosses()))
                .replace("%bw_wins%", String.valueOf(stats.getWins()))
                .replace("%bw_final_kills%", String.valueOf(stats.getFinalKills()))
                .replace("%bw_final_deaths%", String.valueOf(stats.getFinalDeaths()))
                .replace("%bw_beds%", String.valueOf(stats.getBedsDestroyed()))
                .replace("%bw_games_played%", String.valueOf(stats.getGamesPlayed()));

        final String dateFmt = getMsg(player, Messages.FORMATTING_STATS_DATE_FORMAT);
        Instant first = stats.getFirstPlay() != null ? stats.getFirstPlay() : Instant.now();
        Instant last = stats.getLastPlay() != null ? stats.getLastPlay() : Instant.now();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFmt);

        s = s.replace("%bw_play_first%", sdf.format(Timestamp.from(first)))
                .replace("%bw_play_last%", sdf.format(Timestamp.from(last)));

        return papiReplacements ? SupportPAPI.getSupportPAPI().replace(player, s) : s;
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
