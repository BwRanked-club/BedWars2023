package com.tomkeuper.bedwars.stats;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class StatsModeResolver {

    private StatsModeResolver() {
    }

    public static StatsMode resolve(IArena arena) {
        if (arena == null) return StatsMode.OVERALL;
        StatsMode aliased = resolveModeKey(arena.getGroup());
        return aliased == null ? StatsMode.OVERALL : aliased;
    }

    public static StatsMode resolveModeKey(String group) {
        String normalized = normalize(group);
        if (normalized.isEmpty()) return null;

        for (StatsMode mode : StatsMode.values()) {
            if (mode == StatsMode.OVERALL) continue;
            if (normalize(mode.getId()).equals(normalized)) {
                return mode;
            }
            if (normalize(getDisplayToken(mode)).equals(normalized)) {
                return mode;
            }
            List<String> aliases = BedWars.config.getYml().getStringList(
                    ConfigPath.GENERAL_CONFIGURATION_STATS_MODES_ALIAS_PATH.replace("%mode%", mode.getId())
            );
            for (String alias : aliases) {
                if (normalize(alias).equals(normalized)) {
                    return mode;
                }
            }
        }

        return switch (normalized) {
            case "solo", "solos" -> StatsMode.SOLO;
            case "duo", "duos", "double", "doubles", "duplas" -> StatsMode.DOUBLES;
            case "trio", "trios", "triple", "triples" -> StatsMode.TRIPLES;
            case "quad", "quads", "squad", "squads", "quartetos" -> StatsMode.QUADS;
            case "1v1" -> StatsMode.ONE_VS_ONE;
            case "2v2" -> StatsMode.TWO_VS_TWO;
            case "3v3" -> StatsMode.THREE_VS_THREE;
            case "4v4" -> StatsMode.FOUR_VS_FOUR;
            default -> null;
        };
    }

    public static List<String> getPlaceholderTokens(StatsMode mode) {
        Set<String> tokens = new LinkedHashSet<>();
        if (mode == null || mode == StatsMode.OVERALL) {
            return List.of();
        }

        tokens.add(getDisplayToken(mode));
        tokens.add(mode.getId());
        List<String> aliases = BedWars.config.getYml().getStringList(
                ConfigPath.GENERAL_CONFIGURATION_STATS_MODES_ALIAS_PATH.replace("%mode%", mode.getId())
        );
        tokens.addAll(aliases);
        return new ArrayList<>(tokens);
    }

    public static String getDisplayToken(StatsMode mode) {
        return switch (mode) {
            case SOLO -> "Solo";
            case DOUBLES -> "Duplas";
            case TRIPLES -> "Trios";
            case QUADS -> "Quartetos";
            case ONE_VS_ONE -> "1v1";
            case TWO_VS_TWO -> "2v2";
            case THREE_VS_THREE -> "3v3";
            case FOUR_VS_FOUR -> "4v4";
            default -> "Overall";
        };
    }

    private static String normalize(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
