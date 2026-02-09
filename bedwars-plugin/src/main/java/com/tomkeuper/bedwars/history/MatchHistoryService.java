package com.tomkeuper.bedwars.history;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.database.IDatabase;
import com.tomkeuper.bedwars.database.H2;
import com.tomkeuper.bedwars.database.MySQL;
import com.tomkeuper.bedwars.database.SQLite;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class MatchHistoryService {

    private MatchHistoryService() {
    }

    public static void saveHistory(List<MatchHistoryRecord> records) {
        if (records == null || records.isEmpty()) return;
        java.util.Set<UUID> players = new java.util.HashSet<>();
        for (MatchHistoryRecord record : records) {
            players.add(record.getPlayerId());
        }
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            mysql.saveMatchHistory(records);
        } else if (database instanceof SQLite sqlite) {
            sqlite.saveMatchHistory(records);
        } else if (database instanceof H2 h2) {
            h2.saveMatchHistory(records);
        }

        for (UUID playerId : players) {
            applyRetention(playerId);
        }
        purgeOldHistory();
    }

    public static MatchHistoryPage fetchPage(UUID playerId, int page, int pageSize) {
        if (playerId == null || pageSize <= 0) return new MatchHistoryPage(Collections.emptyList(), 0);
        int total = countHistory(playerId);
        int safePage = Math.max(1, page);
        int offset = (safePage - 1) * pageSize;
        List<MatchHistoryRecord> records = fetchHistory(playerId, offset, pageSize);
        return new MatchHistoryPage(records, total);
    }

    public static int countHistory(UUID playerId) {
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            return mysql.countMatchHistory(playerId);
        } else if (database instanceof SQLite sqlite) {
            return sqlite.countMatchHistory(playerId);
        } else if (database instanceof H2 h2) {
            return h2.countMatchHistory(playerId);
        }
        return 0;
    }

    public static List<MatchHistoryRecord> fetchHistory(UUID playerId, int offset, int limit) {
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            return mysql.getMatchHistory(playerId, offset, limit);
        } else if (database instanceof SQLite sqlite) {
            return sqlite.getMatchHistory(playerId, offset, limit);
        } else if (database instanceof H2 h2) {
            return h2.getMatchHistory(playerId, offset, limit);
        }
        return Collections.emptyList();
    }

    public static String resolveModeLabel(int teamSize) {
        String base = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_MODE_LABELS + "." + teamSize);
        if (base == null || base.trim().isEmpty()) {
            base = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_MODE_LABELS + ".default");
            if (base == null || base.trim().isEmpty()) {
                base = "%bw_team_size%v%bw_team_size%";
            }
        }
        return base.replace("%bw_team_size%", String.valueOf(teamSize));
    }

    private static void applyRetention(UUID playerId) {
        int maxRecords = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_MAX_RECORDS);
        if (maxRecords <= 0) return;

        IDatabase database = BedWars.getRemoteDatabase();
        if (maxRecords > 0) {
            if (database instanceof MySQL mysql) {
                mysql.trimMatchHistory(playerId, maxRecords);
            } else if (database instanceof SQLite sqlite) {
                sqlite.trimMatchHistory(playerId, maxRecords);
            } else if (database instanceof H2 h2) {
                h2.trimMatchHistory(playerId, maxRecords);
            }
        }

    }

    private static void purgeOldHistory() {
        int purgeDays = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_PURGE_DAYS);
        if (purgeDays <= 0) return;
        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            mysql.purgeMatchHistoryOlderThan(purgeDays);
        } else if (database instanceof SQLite sqlite) {
            sqlite.purgeMatchHistoryOlderThan(purgeDays);
        } else if (database instanceof H2 h2) {
            h2.purgeMatchHistoryOlderThan(purgeDays);
        }
    }
}

