package com.tomkeuper.bedwars.language;

import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class LanguageEncodingRepair {

    private static final Charset LEGACY_CHARSET = Charset.forName("windows-1252");

    private LanguageEncodingRepair() {
    }

    static boolean repair(YamlConfiguration yml) {
        boolean changed = false;
        for (Map.Entry<String, Object> entry : yml.getValues(true).entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String text) {
                String repaired = repairText(text);
                if (!text.equals(repaired)) {
                    yml.set(entry.getKey(), repaired);
                    changed = true;
                }
                continue;
            }
            if (!(value instanceof List<?> list)) {
                continue;
            }

            List<String> repairedList = new ArrayList<>();
            boolean listChanged = false;
            for (Object element : list) {
                if (!(element instanceof String text)) {
                    repairedList.clear();
                    listChanged = false;
                    break;
                }
                String repaired = repairText(text);
                repairedList.add(repaired);
                if (!text.equals(repaired)) {
                    listChanged = true;
                }
            }
            if (listChanged) {
                yml.set(entry.getKey(), repairedList);
                changed = true;
            }
        }
        return changed;
    }

    private static String repairText(String value) {
        if (value == null || !looksBroken(value)) {
            return value;
        }

        String current = value;
        for (int i = 0; i < 3; i++) {
            String candidate = new String(current.getBytes(LEGACY_CHARSET), StandardCharsets.UTF_8);
            if (candidate.equals(current) || mojibakeScore(candidate) >= mojibakeScore(current)) {
                break;
            }
            current = candidate;
        }
        return current;
    }

    private static boolean looksBroken(String value) {
        return mojibakeScore(value) > 0;
    }

    private static int mojibakeScore(String value) {
        int score = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == 'Ã' || c == 'Â' || c == 'â' || c == '\uFFFD') {
                score++;
            }
        }
        return score;
    }
}
