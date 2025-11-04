package com.tomkeuper.bedwars.listeners.dropshandler;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.language.Messages;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.tomkeuper.bedwars.api.language.Language.getMsg;

@Slf4j
@UtilityClass
public class PlayerDrops {

    private static final EnumSet<Material> CURRENCY =
            EnumSet.of(Material.DIAMOND, Material.EMERALD, Material.IRON_INGOT, Material.GOLD_INGOT);

    public static boolean handlePlayerDrops(
            final IArena arena,
            final Player victim,
            final Player killer,
            final ITeam victimsTeam,
            final ITeam killersTeam,
            final PlayerKillEvent.PlayerKillCause cause,
            final List<ItemStack> inventory
    ) {
        if (arena.getConfig().getBoolean(ConfigPath.ARENA_NORMAL_DEATH_DROPS)) return false;
        if (victim == null) return true;
        final World vWorld = victim.getWorld();
        if (vWorld == null) return true;

        if (victimsTeam != null && victimsTeam.equals(killersTeam)) {
            dropCurrenciesAt(victim.getLocation(), inventory);
            return true;
        }

        if (cause == PlayerKillEvent.PlayerKillCause.PLAYER_PUSH
                || cause == PlayerKillEvent.PlayerKillCause.PLAYER_PUSH_FINAL
                || cause.isPvpLogOut()) {
            dropCurrenciesAt(victim.getLocation(), inventory);
            return true;
        }

        if (cause.isDespawnable()) {
            final Player receiver = pickReceiver(arena, killersTeam);
            if (receiver != null && arena.isPlayer(receiver) && !arena.isReSpawning(receiver)) {
                final Map<Material, Integer> materialDrops = new EnumMap<>(Material.class);
                giveCurrenciesTo(receiver, inventory, materialDrops);
                sendGainMessages(receiver, materialDrops);
                return true;
            }
            dropCurrenciesAt(victim.getLocation(), inventory);
            return true;
        }

        if (killer == null) {
            dropCurrenciesAt(victim.getLocation(), inventory);
            return true;
        }

        final Map<Material, Integer> materialDrops = new EnumMap<>(Material.class);
        giveCurrenciesTo(killer, inventory, materialDrops);
        sendGainMessages(killer, materialDrops);
        return true;
    }

    private static boolean isDroppableCurrency(final ItemStack i) {
        return i != null && i.getType() != Material.AIR && CURRENCY.contains(i.getType())
                && !BedWars.nms.isArmor(i) && !BedWars.nms.isBow(i)
                && !BedWars.nms.isSword(i) && !BedWars.nms.isTool(i)
                && BedWars.nms.getShopUpgradeIdentifier(i).trim().isEmpty();
    }

    private static void dropCurrenciesAt(final Location loc, final List<ItemStack> inventory) {
        if (loc == null) return;
        final World world = loc.getWorld();
        if (world == null) return;
        for (final ItemStack i : inventory) {
            if (i == null || i.getType() == Material.AIR) continue;
            if (!CURRENCY.contains(i.getType())) continue;
            world.dropItemNaturally(loc, i);
        }
    }

    private static void addToInventoryOrDrop(final Player receiver, final ItemStack stack) {
        if (receiver == null || stack == null || stack.getType() == Material.AIR) return;
        final Map<Integer, ItemStack> leftovers = receiver.getInventory().addItem(stack);
        if (!leftovers.isEmpty()) {
            final Location at = receiver.getLocation();
            final World w = at.getWorld();
            if (w == null) return;
            leftovers.values().forEach(item -> {
                if (item != null && item.getType() != Material.AIR) {
                    w.dropItemNaturally(at, item);
                }
            });
        }
    }

    private static void giveCurrenciesTo(final Player receiver, final List<ItemStack> inventory, final Map<Material, Integer> tally) {
        for (final ItemStack i : inventory) {
            if (!isDroppableCurrency(i)) continue;
            tally.merge(i.getType(), i.getAmount(), Integer::sum);
            addToInventoryOrDrop(receiver, i);
        }
    }

    private static void sendGainMessages(final Player killer, final Map<Material, Integer> materialDrops) {
        if (killer == null) return;
        for (final Map.Entry<Material, Integer> entry : materialDrops.entrySet()) {
            final int amount = entry.getValue();
            final Material type = entry.getKey();
            final String msgTemplate = switch (type) {
                case DIAMOND -> getMsg(killer, Messages.PLAYER_DIE_REWARD_DIAMOND)
                        .replace("%bw_meaning%", amount == 1 ?
                                getMsg(killer, Messages.MEANING_DIAMOND_SINGULAR)
                                : getMsg(killer, Messages.MEANING_DIAMOND_PLURAL));
                case EMERALD -> getMsg(killer, Messages.PLAYER_DIE_REWARD_EMERALD)
                        .replace("%bw_meaning%", amount == 1 ?
                                getMsg(killer, Messages.MEANING_EMERALD_SINGULAR)
                                : getMsg(killer, Messages.MEANING_EMERALD_PLURAL));
                case IRON_INGOT -> getMsg(killer, Messages.PLAYER_DIE_REWARD_IRON)
                        .replace("%bw_meaning%", amount == 1 ?
                                getMsg(killer, Messages.MEANING_IRON_SINGULAR)
                                : getMsg(killer, Messages.MEANING_IRON_PLURAL));
                case GOLD_INGOT -> getMsg(killer, Messages.PLAYER_DIE_REWARD_GOLD)
                        .replace("%bw_meaning%", amount == 1 ?
                                getMsg(killer, Messages.MEANING_GOLD_SINGULAR)
                                : getMsg(killer, Messages.MEANING_GOLD_PLURAL));
                default -> "";
            };
            if (!msgTemplate.isEmpty()) {
                killer.sendMessage(msgTemplate.replace("%bw_amount%", String.valueOf(amount)));
            }
        }
    }

    private static Player pickReceiver(final IArena arena, final ITeam team) {
        if (team == null) return null;
        for (final Player p : team.getMembers()) {
            if (p != null && arena.isPlayer(p) && !arena.isReSpawning(p)) return p;
        }
        return null;
    }
}
