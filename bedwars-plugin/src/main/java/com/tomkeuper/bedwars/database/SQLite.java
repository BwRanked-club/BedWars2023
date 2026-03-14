package com.tomkeuper.bedwars.database;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.database.IDatabase;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.shop.IQuickBuyElement;
import com.tomkeuper.bedwars.api.stats.IPlayerStats;
import com.tomkeuper.bedwars.stats.PlayerStats;
import com.tomkeuper.bedwars.stats.ModeStats;
import com.tomkeuper.bedwars.stats.StatsMode;
import com.tomkeuper.bedwars.history.MatchHistoryEventRecord;
import com.tomkeuper.bedwars.history.MatchHistoryRecord;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLite implements IDatabase {

    private String url;

    private Connection connection;

    public SQLite() {
        File folder = new File(BedWars.plugin.getDataFolder() + "/Cache");
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                BedWars.plugin.getLogger().severe("Could not create /Cache folder!");
            }
        }
        File dataFolder = new File(folder.getPath() + "/player_data.db");

        //TODO Remove check in V2.0
        if (dataFolder.getPath().equals(folder.getPath() + "/shop.db")) {
            dataFolder.renameTo(new File(folder.getPath() + "/player_data.db"));
        }
        if (!dataFolder.exists()) {
            try {
                if (!dataFolder.createNewFile()) {
                    BedWars.plugin.getLogger().severe("Could not create /Cache/player_data.db file!");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        this.url = "jdbc:sqlite:" + dataFolder;
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            if (e instanceof ClassNotFoundException) {
                BedWars.plugin.getLogger().severe("Could not find SQLite Driver on your system!");
            }
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        String sql;
        try {
            checkConnection();

            sql = "CREATE TABLE IF NOT EXISTS global_stats (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name VARCHAR(200), uuid VARCHAR(36), first_play TIMESTAMP NULL DEFAULT NULL, " +
                    "last_play TIMESTAMP DEFAULT NULL, wins INTEGER(10), kills INTEGER(10), " +
                    "final_kills INTEGER(10), looses INTEGER(10), deaths INTEGER(10), final_deaths INTEGER(10), beds_destroyed INTEGER(10), " +
                    "beds_lost INTEGER(10), assists INTEGER(10), final_assists INTEGER(10), games_played INTEGER(10));";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
            try (Statement statement = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS player_stats_modes (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "mode VARCHAR(64) NOT NULL, " +
                        "first_play TIMESTAMP NULL DEFAULT NULL, " +
                        "last_play TIMESTAMP DEFAULT NULL, " +
                        "wins INTEGER(10), kills INTEGER(10), final_kills INTEGER(10), looses INTEGER(10), " +
                        "deaths INTEGER(10), final_deaths INTEGER(10), beds_destroyed INTEGER(10), beds_lost INTEGER(10), " +
                        "assists INTEGER(10), final_assists INTEGER(10), games_played INTEGER(10), " +
                        "PRIMARY KEY (uuid, mode)" +
                        ");";
                statement.executeUpdate(sql);
            }
            ensureStatsSchema();
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS quick_buy (uuid VARCHAR(36) PRIMARY KEY, " +
                        "slot_19 VARCHAR(200), slot_20 VARCHAR(200), slot_21 VARCHAR(200), slot_22 VARCHAR(200), slot_23 VARCHAR(200), slot_24 VARCHAR(200), slot_25 VARCHAR(200)," +
                        "slot_28 VARCHAR(200), slot_29 VARCHAR(200), slot_30 VARCHAR(200), slot_31 VARCHAR(200), slot_32 VARCHAR(200), slot_33 VARCHAR(200), slot_34 VARCHAR(200)," +
                        "slot_37 VARCHAR(200), slot_38 VARCHAR(200), slot_39 VARCHAR(200), slot_40 VARCHAR(200), slot_41 VARCHAR(200), slot_42 VARCHAR(200), slot_43 VARCHAR(200));";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS player_levels (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(200), " +
                        "level INTEGER, xp INTEGER, name VARCHAR(200), next_cost INTEGER);";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS  player_language (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(200), " +
                        "iso VARCHAR(200));";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS quick_buy_share (" +
                        "code VARCHAR(12) PRIMARY KEY, " +
                        "owner VARCHAR(36) UNIQUE, " +
                        "compact TEXT, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ");";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS map_ratings (" +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "arena_name VARCHAR(200) NOT NULL, " +
                        "arena_display VARCHAR(200), " +
                        "arena_group VARCHAR(64), " +
                        "rating INTEGER NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (uuid, arena_name)" +
                        ");";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS match_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "uuid VARCHAR(36) NOT NULL, " +
                        "match_id VARCHAR(36), " +
                        "name VARCHAR(200), " +
                        "arena_name VARCHAR(200), " +
                        "arena_display VARCHAR(200), " +
                        "arena_group VARCHAR(64), " +
                        "team_size INTEGER, " +
                        "mode VARCHAR(64), " +
                        "team_name VARCHAR(64), " +
                        "team_color VARCHAR(32), " +
                        "placement INTEGER, " +
                        "win INTEGER, " +
                        "kills INTEGER, " +
                        "final_kills INTEGER, " +
                        "total_kills INTEGER, " +
                        "deaths INTEGER, " +
                        "final_deaths INTEGER, " +
                        "beds_destroyed INTEGER, " +
                        "started_at INTEGER, " +
                        "ended_at INTEGER, " +
                        "duration_seconds INTEGER, " +
                        "server_id VARCHAR(64)" +
                        ");";
                st.executeUpdate(sql);
                st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_match_history_uuid ON match_history(uuid);");
            }
            ensureMatchHistorySchema();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasStats(UUID uuid) {
        String sql = "SELECT uuid FROM global_stats WHERE uuid = ?;";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveStats(IPlayerStats stats) {
        PlayerStats playerStats = (PlayerStats) stats;
        String sql;
        try {
            checkConnection();

            if (hasStats(stats.getUuid())) {
                sql = "UPDATE global_stats SET last_play=?, wins=?, kills=?, final_kills=?, looses=?, deaths=?, final_deaths=?, beds_destroyed=?, beds_lost=?, assists=?, final_assists=?, games_played=?, name=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setTimestamp(1, toTimestamp(stats.getLastPlay()));
                    statement.setInt(2, stats.getWins());
                    statement.setInt(3, stats.getKills());
                    statement.setInt(4, stats.getFinalKills());
                    statement.setInt(5, stats.getLosses());
                    statement.setInt(6, stats.getDeaths());
                    statement.setInt(7, stats.getFinalDeaths());
                    statement.setInt(8, stats.getBedsDestroyed());
                    statement.setInt(9, stats.getBedsLost());
                    statement.setInt(10, stats.getAssists());
                    statement.setInt(11, stats.getFinalAssists());
                    statement.setInt(12, stats.getGamesPlayed());
                    statement.setString(13, stats.getName());
                    statement.setString(14, stats.getUuid().toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO global_stats (name, uuid, first_play, last_play, wins, kills, final_kills, looses, deaths, final_deaths, beds_destroyed, beds_lost, assists, final_assists, games_played) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, stats.getName());
                    statement.setString(2, stats.getUuid().toString());
                    statement.setTimestamp(3, toTimestamp(stats.getFirstPlay()));
                    statement.setTimestamp(4, toTimestamp(stats.getLastPlay()));
                    statement.setInt(5, stats.getWins());
                    statement.setInt(6, stats.getKills());
                    statement.setInt(7, stats.getFinalKills());
                    statement.setInt(8, stats.getLosses());
                    statement.setInt(9, stats.getDeaths());
                    statement.setInt(10, stats.getFinalDeaths());
                    statement.setInt(11, stats.getBedsDestroyed());
                    statement.setInt(12, stats.getBedsLost());
                    statement.setInt(13, stats.getAssists());
                    statement.setInt(14, stats.getFinalAssists());
                    statement.setInt(15, stats.getGamesPlayed());
                    statement.executeUpdate();
                }
            }
            saveModeStats(playerStats);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IPlayerStats fetchStats(UUID uuid) {
        PlayerStats stats = new PlayerStats(uuid);
        String sql = "SELECT * FROM global_stats WHERE uuid = ?;";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        stats.setName(result.getString("name"));
                        stats.setFirstPlay(toInstant(result.getTimestamp("first_play")));
                        stats.setLastPlay(toInstant(result.getTimestamp("last_play")));
                        stats.setWins(result.getInt("wins"));
                        stats.setKills(result.getInt("kills"));
                        stats.setFinalKills(result.getInt("final_kills"));
                        stats.setLosses(result.getInt("looses"));
                        stats.setDeaths(result.getInt("deaths"));
                        stats.setFinalDeaths(result.getInt("final_deaths"));
                        stats.setBedsDestroyed(result.getInt("beds_destroyed"));
                        stats.setBedsLost(result.getInt("beds_lost"));
                        stats.setAssists(result.getInt("assists"));
                        stats.setFinalAssists(result.getInt("final_assists"));
                        stats.setGamesPlayed(result.getInt("games_played"));
                    }
                }
            }
            loadModeStats(stats);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private void saveModeStats(PlayerStats stats) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM player_stats_modes WHERE uuid = ?;")) {
            delete.setString(1, stats.getUuid().toString());
            delete.executeUpdate();
        }

        String sql = "INSERT INTO player_stats_modes (uuid, mode, first_play, last_play, wins, kills, final_kills, looses, deaths, final_deaths, beds_destroyed, beds_lost, assists, final_assists, games_played) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<StatsMode, ModeStats> entry : stats.getTrackedModeStats().entrySet()) {
                if (!entry.getValue().hasActivity()) continue;
                statement.setString(1, stats.getUuid().toString());
                statement.setString(2, entry.getKey().getId());
                statement.setTimestamp(3, toTimestamp(entry.getValue().getFirstPlay()));
                statement.setTimestamp(4, toTimestamp(entry.getValue().getLastPlay()));
                statement.setInt(5, entry.getValue().getWins());
                statement.setInt(6, entry.getValue().getKills());
                statement.setInt(7, entry.getValue().getFinalKills());
                statement.setInt(8, entry.getValue().getLosses());
                statement.setInt(9, entry.getValue().getDeaths());
                statement.setInt(10, entry.getValue().getFinalDeaths());
                statement.setInt(11, entry.getValue().getBedsDestroyed());
                statement.setInt(12, entry.getValue().getBedsLost());
                statement.setInt(13, entry.getValue().getAssists());
                statement.setInt(14, entry.getValue().getFinalAssists());
                statement.setInt(15, entry.getValue().getGamesPlayed());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void loadModeStats(PlayerStats stats) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_stats_modes WHERE uuid = ?;")) {
            statement.setString(1, stats.getUuid().toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    StatsMode mode = StatsMode.fromId(result.getString("mode"));
                    if (mode == null || mode == StatsMode.OVERALL) continue;
                    ModeStats modeStats = new ModeStats();
                    modeStats.setFirstPlay(toInstant(result.getTimestamp("first_play")));
                    modeStats.setLastPlay(toInstant(result.getTimestamp("last_play")));
                    modeStats.setWins(result.getInt("wins"));
                    modeStats.setKills(result.getInt("kills"));
                    modeStats.setFinalKills(result.getInt("final_kills"));
                    modeStats.setLosses(result.getInt("looses"));
                    modeStats.setDeaths(result.getInt("deaths"));
                    modeStats.setFinalDeaths(result.getInt("final_deaths"));
                    modeStats.setBedsDestroyed(result.getInt("beds_destroyed"));
                    modeStats.setBedsLost(result.getInt("beds_lost"));
                    modeStats.setAssists(result.getInt("assists"));
                    modeStats.setFinalAssists(result.getInt("final_assists"));
                    modeStats.setGamesPlayed(result.getInt("games_played"));
                    stats.setModeStats(mode, modeStats);
                }
            }
        }
    }

    private void ensureStatsSchema() throws SQLException {
        ensureColumn("global_stats", "beds_lost", "INTEGER(10) DEFAULT 0");
        ensureColumn("global_stats", "assists", "INTEGER(10) DEFAULT 0");
        ensureColumn("global_stats", "final_assists", "INTEGER(10) DEFAULT 0");
        ensureColumn("player_stats_modes", "beds_lost", "INTEGER(10) DEFAULT 0");
        ensureColumn("player_stats_modes", "assists", "INTEGER(10) DEFAULT 0");
        ensureColumn("player_stats_modes", "final_assists", "INTEGER(10) DEFAULT 0");
    }

    private void ensureColumn(String tableName, String columnName, String definition) throws SQLException {
        if (columnExists(tableName, columnName)) return;
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition + ";");
        }
    }

    private Timestamp toTimestamp(java.time.Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private java.time.Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    @Override
    public void saveCustomStat(String columnName, UUID player, Object value, String dataType) {
        String sql;
        checkCustomColumnExists(columnName, dataType);
        try {
            checkConnection();

            if (hasStats(player)) {
                sql = "UPDATE global_stats SET " + columnName + "=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setObject(1, value);
                    statement.setString(2, player.toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO global_stats (uuid, " + columnName + ") VALUES (?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, player.toString());
                    statement.setObject(2, value);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkCustomColumnExists(String columnName, String dataType) {
        String sql = "PRAGMA table_info(global_stats)";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                boolean columnExists = false;
                while (resultSet.next()) {
                    String existingColumnName = resultSet.getString("name");
                    if (existingColumnName.equalsIgnoreCase(columnName)) {
                        columnExists = true;
                        break;
                    }
                }
                if (!columnExists) {
                    sql = "ALTER TABLE global_stats ADD COLUMN " + columnName + " " + dataType;
                    try (PreparedStatement statement1 = connection.prepareStatement(sql)) {
                        statement1.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean resetStatsTable() {
        try {
            checkConnection();
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM global_stats;");
                statement.executeUpdate("DELETE FROM player_stats_modes;");
                try {
                    statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='global_stats';");
                } catch (SQLException ignored) {
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Object getCustomStat(String columnName, UUID player) {
        String sql = "SELECT " + columnName + " FROM global_stats WHERE uuid = ?;";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getObject(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UUID> listQuickBuyUUIDs() {
        List<UUID> list = new ArrayList<>();
        try {
            checkConnection();
            try (PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM quick_buy;")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String s = rs.getString("uuid");
                        try {
                            list.add(UUID.fromString(s));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public String getQuickBuySlots(UUID p, int slot) {
        String result = "";
        try {
            checkConnection();

            try (PreparedStatement ps = connection.prepareStatement("SELECT slot_" + slot + " FROM quick_buy WHERE uuid = ?;")) {
                ps.setString(1, p.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result = rs.getString("slot_" + slot);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean hasQuickBuy(UUID uuid) {
        try {
            checkConnection();

            try (Statement statement = connection.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT uuid FROM quick_buy WHERE uuid = '" + uuid.toString() + "';")) {
                    if (rs.next()) {
                        rs.close();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public int getColumn(UUID player, String column) {
        String sql = "SELECT ? FROM global_stats WHERE uuid = ?;";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, column);
                statement.setString(2, player.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(column);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        return 0;
    }

    @Override
    public Object[] getLevelData(UUID player) {
        Object[] r = new Object[]{1, 0, "", 0};
        try {
            checkConnection();

            try (PreparedStatement ps = connection.prepareStatement("SELECT level, xp, name, next_cost FROM player_levels WHERE uuid = ?;")) {
                ps.setString(1, player.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        r[0] = rs.getInt("level");
                        r[1] = rs.getInt("xp");
                        r[2] = rs.getString("name");
                        r[3] = rs.getInt("next_cost");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return r;
    }

    @Override
    public void setLevelData(UUID player, int level, int xp, String displayName, int nextCost) {
        try {
            checkConnection();

            try (PreparedStatement pss = connection.prepareStatement("SELECT uuid from player_levels WHERE uuid = ?;")) {
                pss.setString(1, player.toString());
                try (ResultSet rs = pss.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO player_levels (uuid, level, xp, name, next_cost) VALUES (?, ?, ?, ?, ?);")) {
                            //ps.setInt(1, 0);
                            ps.setString(1, player.toString());
                            ps.setInt(2, level);
                            ps.setInt(3, xp);
                            ps.setString(4, displayName);
                            ps.setInt(5, nextCost);
                            ps.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ps = displayName == null ? connection.prepareStatement("UPDATE player_levels SET level=?, xp=? WHERE uuid = '" + player + "';") : connection.prepareStatement("UPDATE player_levels SET level=?, xp=?, name=?, next_cost=? WHERE uuid = '" + player + "';")) {
                            ps.setInt(1, level);
                            ps.setInt(2, xp);
                            if (displayName != null) {
                                ps.setString(3, displayName);
                                ps.setInt(4, nextCost);
                            }
                            ps.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setLanguage(UUID player, String iso) {
        try {
            checkConnection();

            try (Statement statement = connection.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT iso FROM player_language WHERE uuid = '" + player.toString() + "';")) {
                    if (rs.next()) {
                        try (Statement st = connection.createStatement()) {
                            st.executeUpdate("UPDATE player_language SET iso='" + iso + "' WHERE uuid = '" + player + "';");
                        }
                    } else {
                        try (PreparedStatement st = connection.prepareStatement("INSERT INTO player_language (uuid, iso) VALUES (?, ?);")) {
                            st.setString(1, player.toString());
                            st.setString(2, iso);
                            st.execute();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLanguage(UUID player) {
        String iso = Language.getDefaultLanguage().getIso();
        try {
            checkConnection();

            try (PreparedStatement ps = connection.prepareStatement("SELECT iso FROM player_language WHERE uuid = ?;")) {
                ps.setString(1, player.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        iso = rs.getString("iso");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return iso;
    }

    @Override
    public void pushQuickBuyChanges(HashMap<Integer, String> updateSlots, UUID uuid, List<IQuickBuyElement> elements) {
        if (updateSlots.isEmpty()) return;
        boolean hasQuick;
        if (!(hasQuick = hasQuickBuy(uuid))) {
            for (IQuickBuyElement element : elements) {
                if (!updateSlots.containsKey(element.getSlot())) {
                    updateSlots.put(element.getSlot(), element.getCategoryContent().getIdentifier());
                }
            }
        }
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        int i = 0;
        if (hasQuick) {
            for (Map.Entry<Integer, String> entry : updateSlots.entrySet()) {
                i++;
                columns.append("slot_").append(entry.getKey()).append("=?");
                if (i != updateSlots.size()) {
                    columns.append(", ");
                }
            }
        } else {
            for (Map.Entry<Integer, String> entry : updateSlots.entrySet()) {
                i++;
                columns.append("slot_").append(entry.getKey());
                values.append("?");
                if (i != updateSlots.size()) {
                    columns.append(", ");
                    values.append(", ");
                }
            }
        }
        String sql = hasQuick ? "UPDATE quick_buy SET " + columns + " WHERE uuid=?;" : "INSERT INTO quick_buy (uuid," + columns + ") VALUES (?," + values + ");";
        try {
            checkConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int index = hasQuick ? 0 : 1;
                for (int key : updateSlots.keySet()) {
                    index++;
                    String identifier = updateSlots.get(key);
                    ps.setString(index, identifier.trim().isEmpty() ? null : identifier);
                }
                ps.setString(hasQuick ? updateSlots.size() + 1 : 1, uuid.toString());
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveMapRating(UUID player, String arenaName, String arenaDisplay, String arenaGroup, int rating) {
        try {
            checkConnection();
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM map_ratings WHERE uuid = ? AND arena_name = ?;")) {
                delete.setString(1, player.toString());
                delete.setString(2, arenaName);
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO map_ratings (uuid, arena_name, arena_display, arena_group, rating, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);")) {
                insert.setString(1, player.toString());
                insert.setString(2, arenaName);
                insert.setString(3, arenaDisplay);
                insert.setString(4, arenaGroup);
                insert.setInt(5, rating);
                insert.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getAverageRating(String arenaName) {
        String sql = "SELECT AVG(rating) FROM map_ratings WHERE arena_name = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, arenaName);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        double avg = result.getDouble(1);
                        return result.wasNull() ? 0D : avg;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0D;
    }

    public void saveMatchHistory(List<MatchHistoryRecord> records) {
        if (records == null || records.isEmpty()) return;
        String sql = "INSERT INTO match_history (uuid, match_id, name, arena_name, arena_display, arena_group, team_size, mode, team_name, team_color, placement, win, kills, final_kills, total_kills, deaths, final_deaths, beds_destroyed, started_at, ended_at, duration_seconds, server_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (MatchHistoryRecord record : records) {
                    statement.setString(1, record.getPlayerId().toString());
                    statement.setString(2, record.getMatchId() != null ? record.getMatchId().toString() : null);
                    statement.setString(3, record.getPlayerName());
                    statement.setString(4, record.getArenaName());
                    statement.setString(5, record.getArenaDisplay());
                    statement.setString(6, record.getArenaGroup());
                    statement.setInt(7, record.getTeamSize());
                    statement.setString(8, record.getMode());
                    statement.setString(9, record.getTeamName());
                    statement.setString(10, record.getTeamColor());
                    statement.setInt(11, record.getPlacement());
                    statement.setInt(12, record.isWin() ? 1 : 0);
                    statement.setInt(13, record.getKills());
                    statement.setInt(14, record.getFinalKills());
                    statement.setInt(15, record.getTotalKills());
                    statement.setInt(16, record.getDeaths());
                    statement.setInt(17, record.getFinalDeaths());
                    statement.setInt(18, record.getBedsDestroyed());
                    statement.setLong(19, record.getStartedAt());
                    statement.setLong(20, record.getEndedAt());
                    statement.setInt(21, record.getDurationSeconds());
                    statement.setString(22, record.getServerId());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<MatchHistoryRecord> getMatchHistory(UUID playerId, int offset, int limit) {
        List<MatchHistoryRecord> records = new ArrayList<>();
        String sql = "SELECT id, uuid, match_id, name, arena_name, arena_display, arena_group, team_size, mode, team_name, team_color, placement, win, kills, final_kills, total_kills, deaths, final_deaths, beds_destroyed, started_at, ended_at, duration_seconds, server_id " +
                "FROM match_history WHERE uuid = ? ORDER BY ended_at ASC LIMIT ? OFFSET ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setInt(2, limit);
                statement.setInt(3, offset);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        records.add(new MatchHistoryRecord(
                                result.getLong("id"),
                                safeUuid(result.getString("match_id")),
                                UUID.fromString(result.getString("uuid")),
                                result.getString("name"),
                                result.getString("arena_name"),
                                result.getString("arena_display"),
                                result.getString("arena_group"),
                                result.getInt("team_size"),
                                result.getString("mode"),
                                result.getString("team_name"),
                                result.getString("team_color"),
                                result.getInt("placement"),
                                result.getInt("win") == 1,
                                result.getInt("kills"),
                                result.getInt("final_kills"),
                                result.getInt("total_kills"),
                                result.getInt("deaths"),
                                result.getInt("final_deaths"),
                                result.getInt("beds_destroyed"),
                                result.getLong("started_at"),
                                result.getLong("ended_at"),
                                result.getInt("duration_seconds"),
                                result.getString("server_id")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public int countMatchHistory(UUID playerId) {
        String sql = "SELECT COUNT(*) FROM match_history WHERE uuid = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void trimMatchHistory(UUID playerId, int maxRecords) {
        if (maxRecords <= 0) return;
        String sql = "DELETE FROM match_history WHERE uuid = ? AND id NOT IN (" +
                "SELECT id FROM match_history WHERE uuid = ? ORDER BY ended_at DESC LIMIT ?" +
                ");";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, playerId.toString());
                statement.setInt(3, maxRecords);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void purgeMatchHistoryOlderThan(int days) {
        if (days <= 0) return;
        long threshold = System.currentTimeMillis() - (days * 86400000L);
        String sql = "DELETE FROM match_history WHERE ended_at < ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, threshold);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveMatchHistoryEvents(List<MatchHistoryEventRecord> records) {
        if (records == null || records.isEmpty()) return;
        String sql = "INSERT INTO match_history_events (match_id, event_time, event_type, actor_uuid, actor_name, target_uuid, target_name, team_name, team_color, meta) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (MatchHistoryEventRecord record : records) {
                    statement.setString(1, record.getMatchId() != null ? record.getMatchId().toString() : null);
                    statement.setLong(2, record.getEventTime());
                    statement.setString(3, record.getEventType());
                    statement.setString(4, record.getActorId() != null ? record.getActorId().toString() : null);
                    statement.setString(5, record.getActorName());
                    statement.setString(6, record.getTargetId() != null ? record.getTargetId().toString() : null);
                    statement.setString(7, record.getTargetName());
                    statement.setString(8, record.getTeamName());
                    statement.setString(9, record.getTeamColor());
                    statement.setString(10, record.getMeta());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<MatchHistoryEventRecord> getMatchHistoryEvents(UUID matchId, int offset, int limit) {
        List<MatchHistoryEventRecord> records = new ArrayList<>();
        String sql = "SELECT id, match_id, event_time, event_type, actor_uuid, actor_name, target_uuid, target_name, team_name, team_color, meta " +
                "FROM match_history_events WHERE match_id = ? ORDER BY event_time ASC LIMIT ? OFFSET ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, matchId != null ? matchId.toString() : "");
                statement.setInt(2, limit);
                statement.setInt(3, offset);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        records.add(new MatchHistoryEventRecord(
                                result.getLong("id"),
                                safeUuid(result.getString("match_id")),
                                result.getLong("event_time"),
                                result.getString("event_type"),
                                safeUuid(result.getString("actor_uuid")),
                                result.getString("actor_name"),
                                safeUuid(result.getString("target_uuid")),
                                result.getString("target_name"),
                                result.getString("team_name"),
                                result.getString("team_color"),
                                result.getString("meta")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public int countMatchHistoryEvents(UUID matchId) {
        String sql = "SELECT COUNT(*) FROM match_history_events WHERE match_id = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, matchId != null ? matchId.toString() : "");
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void trimMatchHistoryEvents(UUID matchId, int maxRecords) {
        if (maxRecords <= 0 || matchId == null) return;
        String sql = "DELETE FROM match_history_events WHERE match_id = ? AND id NOT IN (" +
                "SELECT id FROM match_history_events WHERE match_id = ? ORDER BY event_time DESC LIMIT ?" +
                ");";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, matchId.toString());
                statement.setString(2, matchId.toString());
                statement.setInt(3, maxRecords);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMatchHistoryEvents(UUID matchId) {
        if (matchId == null) return;
        String sql = "DELETE FROM match_history_events WHERE match_id = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, matchId.toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashMap<Integer, String> getQuickBuySlots(UUID uuid, int[] slot) {
        HashMap<Integer, String> results = new HashMap<>();
        if (slot.length == 0) {
            return results;
        }
        try {
            checkConnection();

            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM quick_buy WHERE uuid = ?;")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        for (int i : slot) {
                            String id = rs.getString("slot_" + i);
                            if (null != id && !id.isEmpty()) {
                                results.put(i, id);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private void checkConnection() throws SQLException {
        boolean renew = false;

        if (this.connection == null)
            renew = true;
        else if (this.connection.isClosed())
            renew = true;

        if (renew)
            this.connection = DriverManager.getConnection(url);
    }

    private void ensureMatchHistorySchema() throws SQLException {
        if (!columnExists("match_history", "match_id")) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("ALTER TABLE match_history ADD COLUMN match_id VARCHAR(36);");
            }
        }
        try (Statement st = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS match_history_events (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "match_id VARCHAR(36) NOT NULL, " +
                    "event_time INTEGER, " +
                    "event_type VARCHAR(64), " +
                    "actor_uuid VARCHAR(36), " +
                    "actor_name VARCHAR(200), " +
                    "target_uuid VARCHAR(36), " +
                    "target_name VARCHAR(200), " +
                    "team_name VARCHAR(64), " +
                    "team_color VARCHAR(32), " +
                    "meta TEXT" +
                    ");";
            st.executeUpdate(sql);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_match_history_events_match ON match_history_events(match_id);");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_match_history_events_time ON match_history_events(event_time);");
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("PRAGMA table_info(" + tableName + ");")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    if (name != null && name.equalsIgnoreCase(columnName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private UUID safeUuid(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return UUID.fromString(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

}
