package com.tomkeuper.bedwars.history;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.database.IDatabase;
import com.tomkeuper.bedwars.database.H2;
import com.tomkeuper.bedwars.database.MySQL;
import com.tomkeuper.bedwars.database.SQLite;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MatchHistoryEventService {

    private MatchHistoryEventService() {
    }

    public static void saveEventAsync(MatchHistoryEventRecord record) {
        if (record == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> saveEvents(Collections.singletonList(record)));
    }

    public static void saveEvents(List<MatchHistoryEventRecord> records) {
        if (records == null || records.isEmpty()) return;
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            mysql.saveMatchHistoryEvents(records);
        } else if (database instanceof SQLite sqlite) {
            sqlite.saveMatchHistoryEvents(records);
        } else if (database instanceof H2 h2) {
            h2.saveMatchHistoryEvents(records);
        }

        int maxRecords = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_EVENTS_MAX_PER_MATCH);
        if (maxRecords > 0) {
            for (MatchHistoryEventRecord record : records) {
                if (record.getMatchId() != null) {
                    trimEvents(record.getMatchId(), maxRecords);
                }
            }
        }
    }

    public static List<MatchHistoryEventRecord> fetchEvents(UUID matchId, int offset, int limit) {
        if (matchId == null) return Collections.emptyList();
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            return mysql.getMatchHistoryEvents(matchId, offset, limit);
        } else if (database instanceof SQLite sqlite) {
            return sqlite.getMatchHistoryEvents(matchId, offset, limit);
        } else if (database instanceof H2 h2) {
            return h2.getMatchHistoryEvents(matchId, offset, limit);
        }
        return Collections.emptyList();
    }

    public static int countEvents(UUID matchId) {
        if (matchId == null) return 0;
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            return mysql.countMatchHistoryEvents(matchId);
        } else if (database instanceof SQLite sqlite) {
            return sqlite.countMatchHistoryEvents(matchId);
        } else if (database instanceof H2 h2) {
            return h2.countMatchHistoryEvents(matchId);
        }
        return 0;
    }

    public static void trimEvents(UUID matchId, int maxRecords) {
        if (matchId == null || maxRecords <= 0) return;
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            mysql.trimMatchHistoryEvents(matchId, maxRecords);
        } else if (database instanceof SQLite sqlite) {
            sqlite.trimMatchHistoryEvents(matchId, maxRecords);
        } else if (database instanceof H2 h2) {
            h2.trimMatchHistoryEvents(matchId, maxRecords);
        }
    }

    public static void deleteEvents(UUID matchId) {
        if (matchId == null) return;
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            mysql.deleteMatchHistoryEvents(matchId);
        } else if (database instanceof SQLite sqlite) {
            sqlite.deleteMatchHistoryEvents(matchId);
        } else if (database instanceof H2 h2) {
            h2.deleteMatchHistoryEvents(matchId);
        }
    }

    public static String serializeMeta(Map<String, String> meta) {
        if (meta == null || meta.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : meta.entrySet()) {
            if (!first) out.append("|");
            first = false;
            out.append(escape(entry.getKey())).append("=").append(escape(entry.getValue()));
        }
        return out.toString();
    }

    public static Map<String, String> deserializeMeta(String raw) {
        Map<String, String> result = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return result;
        StringBuilder token = new StringBuilder();
        String key = null;
        boolean escape = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (escape) {
                token.append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '=' && key == null) {
                key = token.toString();
                token.setLength(0);
                continue;
            }
            if (c == '|') {
                if (key != null) {
                    result.put(key, token.toString());
                }
                key = null;
                token.setLength(0);
                continue;
            }
            token.append(c);
        }
        if (key != null) {
            result.put(key, token.toString());
        }
        return result;
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("|", "\\|").replace("=", "\\=");
    }
}
