package com.tomkeuper.bedwars.ratings;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.database.IDatabase;
import com.tomkeuper.bedwars.database.H2;
import com.tomkeuper.bedwars.database.MySQL;
import com.tomkeuper.bedwars.database.SQLite;

import java.util.UUID;

public final class MapRatingService {

    private MapRatingService() {
    }

    public static void saveRating(UUID playerId, IArena arena, int rating) {
        if (playerId == null || arena == null) return;

        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            mysql.saveMapRating(playerId, arena.getArenaName(), arena.getDisplayName(), arena.getGroup(), rating);
        } else if (database instanceof SQLite sqlite) {
            sqlite.saveMapRating(playerId, arena.getArenaName(), arena.getDisplayName(), arena.getGroup(), rating);
        } else if (database instanceof H2 h2) {
            h2.saveMapRating(playerId, arena.getArenaName(), arena.getDisplayName(), arena.getGroup(), rating);
        }
    }

    public static double getAverageRating(String arenaName) {
        if (arenaName == null || arenaName.isBlank()) return 0D;

        IDatabase database = BedWars.getRemoteDatabase();
        if (database instanceof MySQL mysql) {
            return mysql.getAverageRating(arenaName);
        } else if (database instanceof SQLite sqlite) {
            return sqlite.getAverageRating(arenaName);
        } else if (database instanceof H2 h2) {
            return h2.getAverageRating(arenaName);
        }
        return 0D;
    }
}
