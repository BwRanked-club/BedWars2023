package com.tomkeuper.bedwars.stats;

import java.util.Arrays;
import java.util.Locale;

public enum StatsMode {
    OVERALL("overall"),
    SOLO("solo"),
    DOUBLES("doubles"),
    TRIPLES("triples"),
    QUADS("quads"),
    ONE_VS_ONE("one_vs_one"),
    TWO_VS_TWO("two_vs_two"),
    THREE_VS_THREE("three_vs_three"),
    FOUR_VS_FOUR("four_vs_four");

    private final String id;

    StatsMode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static StatsMode fromId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String normalized = normalize(raw);
        return Arrays.stream(values())
                .filter(mode -> mode.id.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
    }

    public static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }
}
