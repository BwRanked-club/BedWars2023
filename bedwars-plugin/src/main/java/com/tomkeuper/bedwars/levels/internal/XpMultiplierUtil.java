package com.tomkeuper.bedwars.levels.internal;

import com.tomkeuper.bedwars.BedWars;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;

final class XpMultiplierUtil {

    private XpMultiplierUtil() {
    }

    static int applyMultiplier(Player player, int baseXp) {
        if (baseXp <= 0) return 0;

        MultiplierData data = resolveMultiplier(player);
        if (!data.hasMultiplier) return baseXp;

        double multiplied = baseXp * data.multiplier;
        if (!Double.isFinite(multiplied)) return baseXp;

        long rounded = Math.round(multiplied);
        if (rounded < 0L) return 0;
        if (rounded > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) rounded;
    }

    static String getMultiplierSuffix(Player player) {
        MultiplierData data = resolveMultiplier(player);
        if (!data.hasMultiplier) return "";
        return " (x" + data.displayValue + ")";
    }

    private static MultiplierData resolveMultiplier(Player player) {
        if (player == null) return MultiplierData.none();

        Set<PermissionAttachmentInfo> permissions = player.getEffectivePermissions();
        if (permissions == null || permissions.isEmpty()) return MultiplierData.none();

        String prefix = getPermissionPrefix();
        boolean found = false;
        double highest = 1.0D;
        String displayValue = "1";

        for (PermissionAttachmentInfo info : permissions) {
            if (info == null || !info.getValue()) continue;
            String permission = info.getPermission();
            if (permission == null) continue;

            String lowerPermission = permission.toLowerCase(Locale.ROOT);
            if (!lowerPermission.startsWith(prefix)) continue;

            String multiplierRaw = permission.substring(prefix.length()).trim();
            if (multiplierRaw.isEmpty()) continue;

            Double multiplier = parsePositiveDouble(multiplierRaw);
            if (multiplier == null) continue;

            if (!found || multiplier > highest) {
                found = true;
                highest = multiplier;
                displayValue = formatMultiplier(multiplier);
            }
        }

        if (!found) return MultiplierData.none();
        return new MultiplierData(highest, displayValue, true);
    }

    private static String getPermissionPrefix() {
        String mainCmd = BedWars.mainCmd == null ? "bw" : BedWars.mainCmd;
        return mainCmd.toLowerCase(Locale.ROOT) + ".multiplier.xp-";
    }

    private static Double parsePositiveDouble(String rawValue) {
        try {
            double parsed = Double.parseDouble(rawValue);
            if (!Double.isFinite(parsed) || parsed <= 0D) return null;
            return parsed;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String formatMultiplier(double multiplier) {
        return BigDecimal.valueOf(multiplier).stripTrailingZeros().toPlainString();
    }

    private static final class MultiplierData {
        private final double multiplier;
        private final String displayValue;
        private final boolean hasMultiplier;

        private MultiplierData(double multiplier, String displayValue, boolean hasMultiplier) {
            this.multiplier = multiplier;
            this.displayValue = displayValue;
            this.hasMultiplier = hasMultiplier;
        }

        private static MultiplierData none() {
            return new MultiplierData(1.0D, "1", false);
        }
    }
}
