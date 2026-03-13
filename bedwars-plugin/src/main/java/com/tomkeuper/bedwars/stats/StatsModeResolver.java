package com.tomkeuper.bedwars.stats;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;

import java.util.List;
import java.util.Locale;

public final class StatsModeResolver {

    private StatsModeResolver() {
    }

    public static StatsMode resolve(IArena arena) {
        if (arena == null) return StatsMode.OVERALL;

        StatsMode aliased = resolveFromAliases(arena.getGroup());
        if (aliased != null) {
            return aliased;
        }

        int teamSize = Math.max(1, arena.getMaxInTeam());
        int maxPlayers = Math.max(teamSize, arena.getMaxPlayers());
        if (maxPlayers == teamSize * 2) {
            return switch (teamSize) {
                case 1 -> StatsMode.ONE_VS_ONE;
                case 2 -> StatsMode.TWO_VS_TWO;
                case 3 -> StatsMode.THREE_VS_THREE;
                case 4 -> StatsMode.FOUR_VS_FOUR;
                default -> StatsMode.OVERALL;
            };
        }

        return switch (teamSize) {
            case 1 -> StatsMode.SOLO;
            case 2 -> StatsMode.DOUBLES;
            case 3 -> StatsMode.TRIPLES;
            case 4 -> StatsMode.QUADS;
            default -> StatsMode.OVERALL;
        };
    }

    private static StatsMode resolveFromAliases(String group) {
        String normalized = normalize(group);
        if (normalized.isEmpty()) return null;

        for (StatsMode mode : StatsMode.values()) {
            if (mode == StatsMode.OVERALL) continue;
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

    private static String normalize(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
