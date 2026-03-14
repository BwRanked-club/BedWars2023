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
import java.sql.*;
import java.util.*;

public class H2 implements IDatabase {

    private final String url;

    private Connection connection;

    public H2() {
        File folder = new File(BedWars.plugin.getDataFolder() + "/Cache");
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                BedWars.plugin.getLogger().severe("Could not create /Cache folder!");
            }
        }
        this.url = "jdbc:h2:" + BedWars.plugin.getDataFolder().getAbsolutePath() + File.separator + "Cache" + File.separator + "player_data.h2" + ";TRACE_LEVEL_FILE=0";
        try {
            Class.forName("com.tomkeuper.bedwars.libs.h2.Driver");
            DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            if (e instanceof ClassNotFoundException) {
                BedWars.plugin.getLogger().severe("Could Not Find H2 Driver on your system!");
            }
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        String sql;
        try {
            checkConnection();

            sql = "CREATE TABLE IF NOT EXISTS GLOBAL_STATS (ID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(200), UUID VARCHAR(36), FIRST_PLAY TIMESTAMP NULL DEFAULT NULL, " +
                    "LAST_PLAY TIMESTAMP DEFAULT NULL, WINS INTEGER, KILLS INTEGER, " +
                    "FINAL_KILLS INTEGER, LOSES INTEGER, DEATHS INTEGER, FINAL_DEATHS INTEGER, BEDS_DESTROYED INTEGER, " +
                    "BEDS_LOST INTEGER, ASSISTS INTEGER, FINAL_ASSISTS INTEGER, GAMES_PLAYED INTEGER);";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
            try (Statement statement = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS PLAYER_STATS_MODES (" +
                        "UUID VARCHAR(36) NOT NULL, " +
                        "MODE VARCHAR(64) NOT NULL, " +
                        "FIRST_PLAY TIMESTAMP NULL DEFAULT NULL, " +
                        "LAST_PLAY TIMESTAMP DEFAULT NULL, " +
                        "WINS INTEGER, KILLS INTEGER, FINAL_KILLS INTEGER, LOSES INTEGER, " +
                        "DEATHS INTEGER, FINAL_DEATHS INTEGER, BEDS_DESTROYED INTEGER, BEDS_LOST INTEGER, " +
                        "ASSISTS INTEGER, FINAL_ASSISTS INTEGER, GAMES_PLAYED INTEGER, " +
                        "PRIMARY KEY (UUID, MODE)" +
                        ");";
                statement.executeUpdate(sql);
            }
            ensureStatsSchema();
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS QUICK_BUY (UUID VARCHAR(36) PRIMARY KEY, " +
                        "SLOT_19 VARCHAR(200), SLOT_20 VARCHAR(200), SLOT_21 VARCHAR(200), SLOT_22 VARCHAR(200), SLOT_23 VARCHAR(200), SLOT_24 VARCHAR(200), SLOT_25 VARCHAR(200)," +
                        "SLOT_28 VARCHAR(200), SLOT_29 VARCHAR(200), SLOT_30 VARCHAR(200), SLOT_31 VARCHAR(200), SLOT_32 VARCHAR(200), SLOT_33 VARCHAR(200), SLOT_34 VARCHAR(200)," +
                        "SLOT_37 VARCHAR(200), SLOT_38 VARCHAR(200), SLOT_39 VARCHAR(200), SLOT_40 VARCHAR(200), SLOT_41 VARCHAR(200), SLOT_42 VARCHAR(200), SLOT_43 VARCHAR(200));";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS PLAYER_LEVELS (id INTEGER PRIMARY KEY AUTO_INCREMENT, UUID VARCHAR(200), " +
                        "LEVEL INTEGER, XP INTEGER, NAME VARCHAR(200), NEXT_COST INTEGER);";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS PLAYER_LANGUAGE (id INTEGER PRIMARY KEY AUTO_INCREMENT, UUID VARCHAR(200), " +
                        "iso VARCHAR(200));";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS QUICK_BUY_SHARE (" +
                        "CODE VARCHAR(12) PRIMARY KEY, " +
                        "OWNER VARCHAR(36) UNIQUE, " +
                        "COMPACT CLOB, " +
                        "CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ");";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS MAP_RATINGS (" +
                        "UUID VARCHAR(36) NOT NULL, " +
                        "ARENA_NAME VARCHAR(200) NOT NULL, " +
                        "ARENA_DISPLAY VARCHAR(200), " +
                        "ARENA_GROUP VARCHAR(64), " +
                        "RATING INTEGER NOT NULL, " +
                        "CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "PRIMARY KEY (UUID, ARENA_NAME)" +
                        ");";
                st.executeUpdate(sql);
            }
            try (Statement st = connection.createStatement()) {
                sql = "CREATE TABLE IF NOT EXISTS MATCH_HISTORY (" +
                        "ID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                        "UUID VARCHAR(36) NOT NULL, " +
                        "MATCH_ID VARCHAR(36), " +
                        "NAME VARCHAR(200), " +
                        "ARENA_NAME VARCHAR(200), " +
                        "ARENA_DISPLAY VARCHAR(200), " +
                        "ARENA_GROUP VARCHAR(64), " +
                        "TEAM_SIZE INTEGER, " +
                        "MODE VARCHAR(64), " +
                        "TEAM_NAME VARCHAR(64), " +
                        "TEAM_COLOR VARCHAR(32), " +
                        "PLACEMENT INTEGER, " +
                        "WIN INTEGER, " +
                        "KILLS INTEGER, " +
                        "FINAL_KILLS INTEGER, " +
                        "TOTAL_KILLS INTEGER, " +
                        "DEATHS INTEGER, " +
                        "FINAL_DEATHS INTEGER, " +
                        "BEDS_DESTROYED INTEGER, " +
                        "STARTED_AT BIGINT, " +
                        "ENDED_AT BIGINT, " +
                        "DURATION_SECONDS INTEGER, " +
                        "SERVER_ID VARCHAR(64)" +
                        ");";
                st.executeUpdate(sql);
                st.executeUpdate("CREATE INDEX IF NOT EXISTS IDX_MATCH_HISTORY_UUID ON MATCH_HISTORY(UUID);");
            }
            ensureMatchHistorySchema();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasStats(UUID uuid) {
        String sql = "SELECT UUID FROM GLOBAL_STATS WHERE UUID = ?;";
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
                sql = "UPDATE GLOBAL_STATS SET last_play=?, wins=?, kills=?, final_kills=?, loses=?, deaths=?, final_deaths=?, beds_destroyed=?, beds_lost=?, assists=?, final_assists=?, games_played=?, NAME=? WHERE UUID = ?;";
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
                sql = "INSERT INTO GLOBAL_STATS (Name, UUID, first_play, last_play, wins, kills, final_kills, loses, deaths, final_deaths, beds_destroyed, beds_lost, assists, final_assists, games_played) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
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
        String sql = "SELECT * FROM GLOBAL_STATS WHERE UUID = ?;";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        stats.setName(result.getString("NAME"));
                        stats.setFirstPlay(toInstant(result.getTimestamp("first_play")));
                        stats.setLastPlay(toInstant(result.getTimestamp("last_play")));
                        stats.setWins(result.getInt("wins"));
                        stats.setKills(result.getInt("kills"));
                        stats.setFinalKills(result.getInt("final_kills"));
                        stats.setLosses(result.getInt("loses"));
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
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM PLAYER_STATS_MODES WHERE UUID = ?;")) {
            delete.setString(1, stats.getUuid().toString());
            delete.executeUpdate();
        }

        String sql = "MERGE INTO PLAYER_STATS_MODES (UUID, MODE, FIRST_PLAY, LAST_PLAY, WINS, KILLS, FINAL_KILLS, LOSES, DEATHS, FINAL_DEATHS, BEDS_DESTROYED, BEDS_LOST, ASSISTS, FINAL_ASSISTS, GAMES_PLAYED) " +
                "KEY (UUID, MODE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
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
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM PLAYER_STATS_MODES WHERE UUID = ?;")) {
            statement.setString(1, stats.getUuid().toString());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    StatsMode mode = StatsMode.fromId(result.getString("MODE"));
                    if (mode == null || mode == StatsMode.OVERALL) continue;
                    ModeStats modeStats = new ModeStats();
                    modeStats.setFirstPlay(toInstant(result.getTimestamp("FIRST_PLAY")));
                    modeStats.setLastPlay(toInstant(result.getTimestamp("LAST_PLAY")));
                    modeStats.setWins(result.getInt("WINS"));
                    modeStats.setKills(result.getInt("KILLS"));
                    modeStats.setFinalKills(result.getInt("FINAL_KILLS"));
                    modeStats.setLosses(result.getInt("LOSES"));
                    modeStats.setDeaths(result.getInt("DEATHS"));
                    modeStats.setFinalDeaths(result.getInt("FINAL_DEATHS"));
                    modeStats.setBedsDestroyed(result.getInt("BEDS_DESTROYED"));
                    modeStats.setBedsLost(result.getInt("BEDS_LOST"));
                    modeStats.setAssists(result.getInt("ASSISTS"));
                    modeStats.setFinalAssists(result.getInt("FINAL_ASSISTS"));
                    modeStats.setGamesPlayed(result.getInt("GAMES_PLAYED"));
                    stats.setModeStats(mode, modeStats);
                }
            }
        }
    }

    private void ensureStatsSchema() throws SQLException {
        ensureColumn("GLOBAL_STATS", "BEDS_LOST", "INTEGER DEFAULT 0");
        ensureColumn("GLOBAL_STATS", "ASSISTS", "INTEGER DEFAULT 0");
        ensureColumn("GLOBAL_STATS", "FINAL_ASSISTS", "INTEGER DEFAULT 0");
        ensureColumn("PLAYER_STATS_MODES", "BEDS_LOST", "INTEGER DEFAULT 0");
        ensureColumn("PLAYER_STATS_MODES", "ASSISTS", "INTEGER DEFAULT 0");
        ensureColumn("PLAYER_STATS_MODES", "FINAL_ASSISTS", "INTEGER DEFAULT 0");
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
                sql = "UPDATE GLOBAL_STATS SET " + columnName + "=? WHERE UUID = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setObject(1, value);
                    statement.setString(2, player.toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO GLOBAL_STATS (UUID, " + columnName + ") VALUES (?, ?);";
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
        try {
            checkConnection();

            if (!columnExists("GLOBAL_STATS", columnName)) {
                String sql = "ALTER TABLE GLOBAL_STATS ADD COLUMN " + columnName + " " + dataType;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.executeUpdate();
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
                statement.executeUpdate("TRUNCATE TABLE GLOBAL_STATS;");
                statement.executeUpdate("TRUNCATE TABLE PLAYER_STATS_MODES;");
            }
            return true;
        } catch (SQLException ignored) {
            try {
                checkConnection();
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM GLOBAL_STATS;");
                    statement.executeUpdate("DELETE FROM PLAYER_STATS_MODES;");
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public Object getCustomStat(String columnName, UUID player) {
        String sql = "SELECT " + columnName + " FROM GLOBAL_STATS WHERE UUID = ?;";
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
            try (PreparedStatement ps = connection.prepareStatement("SELECT UUID FROM QUICK_BUY;")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String s = rs.getString("UUID");
                        try {
                            list.add(java.util.UUID.fromString(s));
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

            try (PreparedStatement ps = connection.prepareStatement("SELECT SLOT_" + slot + " FROM QUICK_BUY WHERE UUID = ?;")) {
                ps.setString(1, p.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result = rs.getString("SLOT_" + slot);
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
                try (ResultSet rs = statement.executeQuery("SELECT UUID FROM QUICK_BUY WHERE UUID = '" + uuid.toString() + "';")) {
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

    @Override
    public int getColumn(UUID player, String column) {
        String sql = "SELECT ? FROM GLOBAL_STATS WHERE UUID = ?;";
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

            try (PreparedStatement ps = connection.prepareStatement("SELECT LEVEL, XP, NAME, NEXT_COST FROM PLAYER_LEVELS WHERE UUID = ?;")) {
                ps.setString(1, player.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        r[0] = rs.getInt("LEVEL");
                        r[1] = rs.getInt("XP");
                        r[2] = rs.getString("NAME");
                        r[3] = rs.getInt("NEXT_COST");
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

            try (PreparedStatement pss = connection.prepareStatement("SELECT UUID from PLAYER_LEVELS WHERE UUID = ?;")) {
                pss.setString(1, player.toString());
                try (ResultSet rs = pss.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO PLAYER_LEVELS (UUID, LEVEL, XP, NAME, NEXT_COST) VALUES (?, ?, ?, ?, ?);")) {
                            //ps.setInt(1, 0);
                            ps.setString(1, player.toString());
                            ps.setInt(2, level);
                            ps.setInt(3, xp);
                            ps.setString(4, displayName);
                            ps.setInt(5, nextCost);
                            ps.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ps = displayName == null ? connection.prepareStatement("UPDATE PLAYER_LEVELS SET LEVEL=?, XP=? WHERE UUID = '" + player + "';") : connection.prepareStatement("UPDATE PLAYER_LEVELS SET LEVEL=?, XP=?, NAME=?, NEXT_COST=? WHERE UUID = '" + player + "';")) {
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
                try (ResultSet rs = statement.executeQuery("SELECT iso FROM PLAYER_LANGUAGE WHERE UUID = '" + player.toString() + "';")) {
                    if (rs.next()) {
                        try (Statement st = connection.createStatement()) {
                            st.executeUpdate("UPDATE PLAYER_LANGUAGE SET iso='" + iso + "' WHERE UUID = '" + player + "';");
                        }
                    } else {
                        try (PreparedStatement st = connection.prepareStatement("INSERT INTO PLAYER_LANGUAGE (UUID, iso) VALUES (?, ?);")) {
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

            try (PreparedStatement ps = connection.prepareStatement("SELECT iso FROM PLAYER_LANGUAGE WHERE UUID = ?;")) {
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
                columns.append("SLOT_").append(entry.getKey()).append("=?");
                if (i != updateSlots.size()) {
                    columns.append(", ");
                }
            }
        } else {
            for (Map.Entry<Integer, String> entry : updateSlots.entrySet()) {
                i++;
                columns.append("SLOT_").append(entry.getKey());
                values.append("?");
                if (i != updateSlots.size()) {
                    columns.append(", ");
                    values.append(", ");
                }
            }
        }
        String sql = hasQuick ? "UPDATE QUICK_BUY SET " + columns + " WHERE UUID=?;" : "INSERT INTO QUICK_BUY (UUID," + columns + ") VALUES (?," + values + ");";
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
                    "DELETE FROM MAP_RATINGS WHERE UUID = ? AND ARENA_NAME = ?;")) {
                delete.setString(1, player.toString());
                delete.setString(2, arenaName);
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO MAP_RATINGS (UUID, ARENA_NAME, ARENA_DISPLAY, ARENA_GROUP, RATING, CREATED_AT, UPDATED_AT) " +
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
        String sql = "SELECT AVG(RATING) FROM MAP_RATINGS WHERE ARENA_NAME = ?;";
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
        String sql = "INSERT INTO MATCH_HISTORY (UUID, MATCH_ID, NAME, ARENA_NAME, ARENA_DISPLAY, ARENA_GROUP, TEAM_SIZE, MODE, TEAM_NAME, TEAM_COLOR, PLACEMENT, WIN, KILLS, FINAL_KILLS, TOTAL_KILLS, DEATHS, FINAL_DEATHS, BEDS_DESTROYED, STARTED_AT, ENDED_AT, DURATION_SECONDS, SERVER_ID) " +
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
        String sql = "SELECT ID, UUID, MATCH_ID, NAME, ARENA_NAME, ARENA_DISPLAY, ARENA_GROUP, TEAM_SIZE, MODE, TEAM_NAME, TEAM_COLOR, PLACEMENT, WIN, KILLS, FINAL_KILLS, TOTAL_KILLS, DEATHS, FINAL_DEATHS, BEDS_DESTROYED, STARTED_AT, ENDED_AT, DURATION_SECONDS, SERVER_ID " +
                "FROM MATCH_HISTORY WHERE UUID = ? ORDER BY ENDED_AT ASC LIMIT ? OFFSET ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setInt(2, limit);
                statement.setInt(3, offset);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        records.add(new MatchHistoryRecord(
                                result.getLong("ID"),
                                safeUuid(result.getString("MATCH_ID")),
                                UUID.fromString(result.getString("UUID")),
                                result.getString("NAME"),
                                result.getString("ARENA_NAME"),
                                result.getString("ARENA_DISPLAY"),
                                result.getString("ARENA_GROUP"),
                                result.getInt("TEAM_SIZE"),
                                result.getString("MODE"),
                                result.getString("TEAM_NAME"),
                                result.getString("TEAM_COLOR"),
                                result.getInt("PLACEMENT"),
                                result.getInt("WIN") == 1,
                                result.getInt("KILLS"),
                                result.getInt("FINAL_KILLS"),
                                result.getInt("TOTAL_KILLS"),
                                result.getInt("DEATHS"),
                                result.getInt("FINAL_DEATHS"),
                                result.getInt("BEDS_DESTROYED"),
                                result.getLong("STARTED_AT"),
                                result.getLong("ENDED_AT"),
                                result.getInt("DURATION_SECONDS"),
                                result.getString("SERVER_ID")
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
        String sql = "SELECT COUNT(*) FROM MATCH_HISTORY WHERE UUID = ?;";
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
        String sql = "DELETE FROM MATCH_HISTORY WHERE UUID = ? AND ID NOT IN (" +
                "SELECT ID FROM MATCH_HISTORY WHERE UUID = ? ORDER BY ENDED_AT DESC LIMIT ?" +
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
        String sql = "DELETE FROM MATCH_HISTORY WHERE ENDED_AT < ?;";
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
        String sql = "INSERT INTO MATCH_HISTORY_EVENTS (MATCH_ID, EVENT_TIME, EVENT_TYPE, ACTOR_UUID, ACTOR_NAME, TARGET_UUID, TARGET_NAME, TEAM_NAME, TEAM_COLOR, META) " +
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
        String sql = "SELECT ID, MATCH_ID, EVENT_TIME, EVENT_TYPE, ACTOR_UUID, ACTOR_NAME, TARGET_UUID, TARGET_NAME, TEAM_NAME, TEAM_COLOR, META " +
                "FROM MATCH_HISTORY_EVENTS WHERE MATCH_ID = ? ORDER BY EVENT_TIME ASC LIMIT ? OFFSET ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, matchId != null ? matchId.toString() : "");
                statement.setInt(2, limit);
                statement.setInt(3, offset);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        records.add(new MatchHistoryEventRecord(
                                result.getLong("ID"),
                                safeUuid(result.getString("MATCH_ID")),
                                result.getLong("EVENT_TIME"),
                                result.getString("EVENT_TYPE"),
                                safeUuid(result.getString("ACTOR_UUID")),
                                result.getString("ACTOR_NAME"),
                                safeUuid(result.getString("TARGET_UUID")),
                                result.getString("TARGET_NAME"),
                                result.getString("TEAM_NAME"),
                                result.getString("TEAM_COLOR"),
                                result.getString("META")
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
        String sql = "SELECT COUNT(*) FROM MATCH_HISTORY_EVENTS WHERE MATCH_ID = ?;";
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
        String sql = "DELETE FROM MATCH_HISTORY_EVENTS WHERE MATCH_ID = ? AND ID NOT IN (" +
                "SELECT ID FROM MATCH_HISTORY_EVENTS WHERE MATCH_ID = ? ORDER BY EVENT_TIME DESC LIMIT ?" +
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
        String sql = "DELETE FROM MATCH_HISTORY_EVENTS WHERE MATCH_ID = ?;";
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

            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM QUICK_BUY WHERE UUID = ?;")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        for (int i : slot) {
                            String id = rs.getString("SLOT_" + i);
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
        if (!columnExists("MATCH_HISTORY", "MATCH_ID")) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("ALTER TABLE MATCH_HISTORY ADD COLUMN MATCH_ID VARCHAR(36);");
            }
        }
        try (Statement st = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS MATCH_HISTORY_EVENTS (" +
                    "ID INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "MATCH_ID VARCHAR(36) NOT NULL, " +
                    "EVENT_TIME BIGINT, " +
                    "EVENT_TYPE VARCHAR(64), " +
                    "ACTOR_UUID VARCHAR(36), " +
                    "ACTOR_NAME VARCHAR(200), " +
                    "TARGET_UUID VARCHAR(36), " +
                    "TARGET_NAME VARCHAR(200), " +
                    "TEAM_NAME VARCHAR(64), " +
                    "TEAM_COLOR VARCHAR(32), " +
                    "META TEXT" +
                    ");";
            st.executeUpdate(sql);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS IDX_MATCH_HISTORY_EVENTS_MATCH ON MATCH_HISTORY_EVENTS(MATCH_ID);");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS IDX_MATCH_HISTORY_EVENTS_TIME ON MATCH_HISTORY_EVENTS(EVENT_TIME);");
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?;")) {
            ps.setString(1, tableName.toUpperCase(Locale.ROOT));
            ps.setString(2, columnName.toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
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
