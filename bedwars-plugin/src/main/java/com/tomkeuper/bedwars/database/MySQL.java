package com.tomkeuper.bedwars.database;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.database.IDatabase;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.shop.IQuickBuyElement;
import com.tomkeuper.bedwars.api.stats.IPlayerStats;
import com.tomkeuper.bedwars.stats.PlayerStats;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tomkeuper.bedwars.BedWars.config;

@SuppressWarnings("WeakerAccess")
public class MySQL implements IDatabase {

    private HikariDataSource dataSource;
    private final String host;
    private final String database;
    private final String user;
    private final String pass;
    private final int port;
    private final boolean ssl;
    private final boolean certificateVerification;
    private final int poolSize;
    private final int maxLifetime;

    /**
     * Create new MySQL connection.
     */
    public MySQL() {
        this.host = config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_DATABASE_HOST);
        this.database = config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_DATABASE_DATABASE);
        this.user = config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_DATABASE_USER);
        this.pass = config.getYml().getString(ConfigPath.GENERAL_CONFIGURATION_DATABASE_PASS);
        this.port = config.getYml().getInt(ConfigPath.GENERAL_CONFIGURATION_DATABASE_PORT);
        this.ssl = config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_DATABASE_SSL);
        this.certificateVerification = config.getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_DATABASE_VERIFY_CERT);
        this.poolSize = config.getYml().getInt(ConfigPath.GENERAL_CONFIGURATION_DATABASE_POOL_SIZE);
        this.maxLifetime = config.getYml().getInt(ConfigPath.GENERAL_CONFIGURATION_DATABASE_MAX_LIFETIME);
    }

    /**
     * Creates the SQL connection pool and tries to connect.
     *
     * @return true if connected successfully.
     */
    public boolean connect() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setPoolName("BedWars2023MySQLPool");

        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMaxLifetime(maxLifetime * 1000L);

        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);

        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);

        hikariConfig.addDataSourceProperty("useSSL", String.valueOf(ssl));
        if (!certificateVerification) {
            hikariConfig.addDataSourceProperty("verifyServerCertificate", String.valueOf(false));
        }

        hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
        hikariConfig.addDataSourceProperty("encoding", "UTF-8");
        hikariConfig.addDataSourceProperty("useUnicode", "true");

        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("jdbcCompliantTruncation", "false");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "275");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Recover if connection gets interrupted
        hikariConfig.addDataSourceProperty("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));

        dataSource = new HikariDataSource(hikariConfig);

        try {
            dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean hasStats(UUID uuid) {
        String sql = "SELECT uuid FROM global_stats WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
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
    public void init() {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS global_stats (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(200), uuid VARCHAR(200), first_play TIMESTAMP NULL DEFAULT NULL, " +
                    "last_play TIMESTAMP NULL DEFAULT NULL, wins INT(200), kills INT(200), " +
                    "final_kills INT(200), looses INT(200), deaths INT(200), final_deaths INT(200), beds_destroyed INT(200), games_played INT(200));";

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }

            sql = "CREATE TABLE IF NOT EXISTS quick_buy (uuid VARCHAR(36) PRIMARY KEY, " +
                    "slot_19 VARCHAR(200), slot_20 VARCHAR(200), slot_21 VARCHAR(200), slot_22 VARCHAR(200), slot_23 VARCHAR(200), slot_24 VARCHAR(200), slot_25 VARCHAR(200)," +
                    "slot_28 VARCHAR(200), slot_29 VARCHAR(200), slot_30 VARCHAR(200), slot_31 VARCHAR(200), slot_32 VARCHAR(200), slot_33 VARCHAR(200), slot_34 VARCHAR(200)," +
                    "slot_37 VARCHAR(200), slot_38 VARCHAR(200), slot_39 VARCHAR(200), slot_40 VARCHAR(200), slot_41 VARCHAR(200), slot_42 VARCHAR(200), slot_43 VARCHAR(200));";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }

            sql = "CREATE TABLE IF NOT EXISTS player_levels (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(200), " +
                    "level INT(200), xp INT(200), name VARCHAR(200) CHARACTER SET utf8, next_cost INT(200));";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }

            sql = "CREATE TABLE IF NOT EXISTS player_language (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(200), iso VARCHAR(200));";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }

            sql = "CREATE TABLE IF NOT EXISTS quick_buy_share (" +
                    "code VARCHAR(12) PRIMARY KEY," +
                    "owner VARCHAR(36) UNIQUE," +
                    "compact MEDIUMTEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(sql);
            }

            sql = "CREATE TABLE IF NOT EXISTS map_ratings (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "arena_name VARCHAR(200) NOT NULL," +
                    "arena_display VARCHAR(200)," +
                    "arena_group VARCHAR(64)," +
                    "rating TINYINT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "UNIQUE KEY uniq_player_map (uuid, arena_name)" +
                    ");";
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Migrate quick_buy_2 table to quick_buy.
     * This will:
     * 1. Check if quick_buy_2 exists
     * 2. Drop old quick_buy if it exists
     * 3. Rename quick_buy_2 to quick_buy
     *
     * @return true if migration was performed or not needed, false if an error occurred
     */
    public boolean migrateQuickBuyTable() {
        try (Connection connection = dataSource.getConnection()) {
            // Check if quick_buy_2 exists
            boolean hasQuickBuy2;
            try (ResultSet rs = connection.getMetaData().getTables(null, null, "quick_buy_2", null)) {
                hasQuickBuy2 = rs.next();
            }

            if (!hasQuickBuy2) return true;

            try (Statement statement = connection.createStatement()) {
                // Drop old quick_buy table if it exists
                // Check if old quick_buy exists
                boolean hasOldQuickBuy = false;
                try (ResultSet rs = connection.getMetaData().getTables(null, null, "quick_buy", null)) {
                    hasOldQuickBuy = rs.next();
                }

                if (hasOldQuickBuy) {
                    BedWars.plugin.getLogger().info("Dropping old 'quick_buy' table...");
                    statement.executeUpdate("DROP TABLE IF EXISTS quick_buy;");
                }

                // Rename quick_buy_2 to quick_buy
                BedWars.plugin.getLogger().info("Renaming 'quick_buy_2' to 'quick_buy'...");

                // Rename quick_buy_2 to quick_buy
                statement.executeUpdate("RENAME TABLE quick_buy_2 TO quick_buy;");

                BedWars.plugin.getLogger().info("Successfully renamed 'quick_buy_2' to 'quick_buy'.");
                return true;
            }
        } catch (SQLException e) {
            BedWars.plugin.getLogger().severe("Failed to migrate Quick Buy table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void saveStats(IPlayerStats stats) {
        String sql;
        try (Connection connection = dataSource.getConnection()) {
            if (hasStats(stats.getUuid())) {
                sql = "UPDATE global_stats SET first_play=?, last_play=?, wins=?, kills=?, final_kills=?, looses=?, deaths=?, final_deaths=?, beds_destroyed=?, games_played=?, name=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setTimestamp(1, stats.getFirstPlay() != null ? Timestamp.from(stats.getFirstPlay()) : null);
                    statement.setTimestamp(2, stats.getLastPlay() != null ? Timestamp.from(stats.getLastPlay()) : null);
                    statement.setInt(3, stats.getWins());
                    statement.setInt(4, stats.getKills());
                    statement.setInt(5, stats.getFinalKills());
                    statement.setInt(6, stats.getLosses());
                    statement.setInt(7, stats.getDeaths());
                    statement.setInt(8, stats.getFinalDeaths());
                    statement.setInt(9, stats.getBedsDestroyed());
                    statement.setInt(10, stats.getGamesPlayed());
                    statement.setString(11, stats.getName());
                    statement.setString(12, stats.getUuid().toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO global_stats (name, uuid, first_play, last_play, wins, kills, final_kills, looses, deaths, final_deaths, beds_destroyed, games_played) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, stats.getName());
                    statement.setString(2, stats.getUuid().toString());
                    statement.setTimestamp(3, Timestamp.from(stats.getFirstPlay()));
                    statement.setTimestamp(4, Timestamp.from(stats.getLastPlay()));
                    statement.setInt(5, stats.getWins());
                    statement.setInt(6, stats.getKills());
                    statement.setInt(7, stats.getFinalKills());
                    statement.setInt(8, stats.getLosses());
                    statement.setInt(9, stats.getDeaths());
                    statement.setInt(10, stats.getFinalDeaths());
                    statement.setInt(11, stats.getBedsDestroyed());
                    statement.setInt(12, stats.getGamesPlayed());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IPlayerStats fetchStats(UUID uuid) {
        IPlayerStats stats = new PlayerStats(uuid);
        String sql = "SELECT first_play, last_play, wins, kills, final_kills, looses, deaths, final_deaths," +
                "beds_destroyed, games_played FROM global_stats WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        Timestamp firstPlay = result.getTimestamp(1);
                        Timestamp lastPlay = result.getTimestamp(2);
                        stats.setFirstPlay(firstPlay != null ? firstPlay.toInstant() : null);
                        stats.setLastPlay(lastPlay != null ? lastPlay.toInstant() : null);
                        stats.setWins(result.getInt(3));
                        stats.setKills(result.getInt(4));
                        stats.setFinalKills(result.getInt(5));
                        stats.setLosses(result.getInt(6));
                        stats.setDeaths(result.getInt(7));
                        stats.setFinalDeaths(result.getInt(8));
                        stats.setBedsDestroyed(result.getInt(9));
                        stats.setGamesPlayed(result.getInt(10));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    @Override
    public void saveCustomStat(String columnName, UUID player, Object value, String dataType) {
        String sql;
        checkCustomColumnExists(columnName, dataType);
        try (Connection connection = dataSource.getConnection()) {
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

    @Override
    public Object getCustomStat(String columnName, UUID player) {
        String sql = "SELECT " + columnName + " FROM global_stats WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
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

    public void checkCustomColumnExists(String columnName, String dataType) {
        String sql = "SHOW COLUMNS FROM global_stats LIKE ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, columnName);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        sql = "ALTER TABLE global_stats ADD COLUMN " + columnName + " " + dataType;
                        try (PreparedStatement statement1 = connection.prepareStatement(sql)) {
                            statement1.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<UUID> listQuickBuyUUIDs() {
        List<UUID> list = new java.util.ArrayList<>();
        String sql = "SELECT uuid FROM quick_buy;";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String s = rs.getString("uuid");
                try {
                    list.add(UUID.fromString(s));
                } catch (Exception ignored) {
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public String getQuickBuySlots(UUID uuid, int slot) {
        String sql = "SELECT slot_" + slot + " FROM quick_buy WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getString(1);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    @Override
    public HashMap<Integer, String> getQuickBuySlots(UUID uuid, int[] slot) {
        HashMap<Integer, String> results = new HashMap<>();
        if (slot.length == 0) {
            return results;
        }
        try (Connection connection = dataSource.getConnection()) {
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

    @Override
    public boolean hasQuickBuy(UUID uuid) {
        String sql = "SELECT uuid FROM quick_buy WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
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
    public int getColumn(UUID player, String column) {
        String sql = "SELECT ? FROM global_stats WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
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
    public Object[] getLevelData(UUID uuid) {
        String sql = "SELECT level, xp, name, next_cost FROM player_levels WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return new Object[]{
                                result.getInt(1),
                                result.getInt(2),
                                result.getString(3),
                                result.getInt(4)
                        };
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Object[]{1, 0, "", 0};
    }

    @Override
    public void setLevelData(UUID uuid, int level, int xp, String displayName, int nextCost) {
        String sql = "SELECT uuid from player_levels WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        sql = "INSERT INTO player_levels VALUES (?, ?, ?, ?, ?, ?);";
                        try (PreparedStatement statement2 = connection.prepareStatement(sql)) {
                            statement2.setInt(1, 0);
                            statement2.setString(2, uuid.toString());
                            statement2.setInt(3, level);
                            statement2.setInt(4, xp);
                            statement2.setString(5, displayName);
                            statement2.setInt(6, nextCost);
                            statement2.executeUpdate();
                        }
                    } else {
                        if (displayName == null) {
                            sql = "UPDATE player_levels SET level=?, xp=? WHERE uuid = ?;";
                        } else {
                            sql = "UPDATE player_levels SET level=?, xp=?, name=?, next_cost=? WHERE uuid = ?;";
                        }
                        try (PreparedStatement statement2 = connection.prepareStatement(sql)) {
                            statement2.setInt(1, level);
                            statement2.setInt(2, xp);
                            if (displayName != null) {
                                statement2.setString(3, displayName);
                                statement2.setInt(4, nextCost);
                                statement2.setString(5, uuid.toString());
                            } else {
                                statement2.setString(3, uuid.toString());
                            }
                            statement2.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setLanguage(UUID uuid, String iso) {
        String sql = "SELECT iso FROM player_language WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        sql = "UPDATE player_language SET iso = ? WHERE uuid = ?;";
                        try (PreparedStatement statement2 = connection.prepareStatement(sql)) {
                            statement2.setString(1, iso);
                            statement2.setString(2, uuid.toString());
                            statement2.executeUpdate();
                        }
                    } else {
                        sql = "INSERT INTO player_language VALUES (0, ?, ?);";
                        try (PreparedStatement statement2 = connection.prepareStatement(sql)) {
                            statement2.setString(1, uuid.toString());
                            statement2.setString(2, iso);
                            statement2.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLanguage(UUID uuid) {
        String sql = "SELECT iso FROM player_language WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getString(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Language.getDefaultLanguage().getIso();
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
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
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
        String sql = "INSERT INTO map_ratings (uuid, arena_name, arena_display, arena_group, rating) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE rating=VALUES(rating), arena_display=VALUES(arena_display), " +
                "arena_group=VALUES(arena_group), updated_at=CURRENT_TIMESTAMP;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.toString());
                statement.setString(2, arenaName);
                statement.setString(3, arenaDisplay);
                statement.setString(4, arenaGroup);
                statement.setInt(5, rating);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getAverageRating(String arenaName) {
        String sql = "SELECT AVG(rating) FROM map_ratings WHERE arena_name = ?;";
        try (Connection connection = dataSource.getConnection()) {
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

}
