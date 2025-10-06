package com.bobobo.plugins.b0nuscode.db;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private final ExecutorService executor;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.executor = Executors.newCachedThreadPool();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File databaseFile = new File(dataFolder, "playerdata.db");
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);
            createTables();

            plugin.getLogger().info("Database initialized successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createPromoTable = "CREATE TABLE IF NOT EXISTS promo_usage (" +
                "uuid TEXT PRIMARY KEY, " +
                "promo_codes TEXT" +
                ")";

        String createTimeRewardsTable = "CREATE TABLE IF NOT EXISTS time_rewards (" +
                "uuid TEXT, " +
                "reward_key TEXT, " +
                "claimed BOOLEAN DEFAULT FALSE, " +
                "PRIMARY KEY (uuid, reward_key)" +
                ")";

        String createPromoStatsTable = "CREATE TABLE IF NOT EXISTS promo_stats (" +
                "promo_code TEXT PRIMARY KEY, " +
                "uses INTEGER DEFAULT 0" +
                ")";

        String createPromoTimeBonusTable = "CREATE TABLE IF NOT EXISTS promo_time_bonus (" +
                "uuid TEXT, " +
                "promo_code TEXT, " +
                "activated_at INTEGER, " +
                "PRIMARY KEY (uuid, promo_code)" +
                ")";

        String createPromoTimeBonusStatsTable = "CREATE TABLE IF NOT EXISTS promo_time_bonus_stats (" +
                "uuid TEXT, " +
                "promo_code TEXT, " +
                "minutes INTEGER, " +
                "claimed_at INTEGER, " +
                "PRIMARY KEY (uuid, promo_code, minutes)" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPromoTable);
            stmt.execute(createTimeRewardsTable);
            stmt.execute(createPromoStatsTable);
            stmt.execute(createPromoTimeBonusTable);
            stmt.execute(createPromoTimeBonusStatsTable);
        }
    }

    public CompletableFuture<Boolean> hasUsedPromoAsync(UUID uuid, String promoCode) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT promo_codes FROM promo_usage WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String promoCodes = rs.getString("promo_codes");
                    return promoCodes != null && promoCodes.contains(promoCode.toLowerCase());
                }
                return false;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error checking promo usage: " + e.getMessage());
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> hasUsedAnyPromoAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT promo_codes FROM promo_usage WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String promoCodes = rs.getString("promo_codes");
                    return promoCodes != null && !promoCodes.trim().isEmpty();
                }
                return false;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error checking any promo usage: " + e.getMessage());
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Void> setPromoUsedAsync(UUID uuid, String promoCode) {
        return CompletableFuture.runAsync(() -> {
            String selectSql = "SELECT promo_codes FROM promo_usage WHERE uuid = ?";
            String insertSql = "INSERT OR REPLACE INTO promo_usage (uuid, promo_codes) VALUES (?, ?)";

            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql);
                 PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

                selectStmt.setString(1, uuid.toString());
                ResultSet rs = selectStmt.executeQuery();

                String existingCodes = "";
                if (rs.next()) {
                    existingCodes = rs.getString("promo_codes");
                    if (existingCodes == null) existingCodes = "";
                }

                String newCodes = existingCodes.isEmpty()
                        ? promoCode.toLowerCase()
                        : existingCodes + "," + promoCode.toLowerCase();

                insertStmt.setString(1, uuid.toString());
                insertStmt.setString(2, newCodes);
                insertStmt.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Error setting promo as used: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Boolean> hasClaimedTimeRewardAsync(UUID uuid, String rewardKey) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT claimed FROM time_rewards WHERE uuid = ? AND reward_key = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, rewardKey);
                ResultSet rs = stmt.executeQuery();

                return rs.next() && rs.getBoolean("claimed");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error checking time reward: " + e.getMessage());
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Void> setTimeRewardClaimedAsync(UUID uuid, String rewardKey) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO time_rewards (uuid, reward_key, claimed) VALUES (?, ?, TRUE)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, rewardKey);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error setting time reward as claimed: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Void> incrementPromoUsesAsync(String promoCode) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO promo_stats (promo_code, uses) VALUES (?, 1) " +
                    "ON CONFLICT(promo_code) DO UPDATE SET uses = uses + 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, promoCode.toLowerCase());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error incrementing promo uses: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Integer> getPromoUsesAsync(String promoCode) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT uses FROM promo_stats WHERE promo_code = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, promoCode.toLowerCase());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("uses");
                }
                return 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting promo uses: " + e.getMessage());
                return 0;
            }
        }, executor);
    }

    public CompletableFuture<Void> setPromoTimeBonusAsync(UUID uuid, String promoCode) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO promo_time_bonus (uuid, promo_code, activated_at) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, promoCode.toLowerCase());
                stmt.setLong(3, System.currentTimeMillis());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error setting promo time bonus: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Long> getPromoActivationTimeAsync(UUID uuid, String promoCode) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT activated_at FROM promo_time_bonus WHERE uuid = ? AND promo_code = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, promoCode.toLowerCase());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("activated_at");
                }
                return 0L;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting promo activation time: " + e.getMessage());
                return 0L;
            }
        }, executor);
    }

    public List<UUID> getPlayersByPromo(String promoCode) throws SQLException {
        List<UUID> players = new ArrayList<>();
        String sql = "SELECT uuid FROM promo_time_bonus WHERE promo_code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, promoCode.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    players.add(UUID.fromString(rs.getString("uuid")));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in database: " + rs.getString("uuid"));
                }
            }
        }
        return players;
    }

    public CompletableFuture<Void> recordTimeBonusClaimAsync(UUID uuid, String promoCode, int minutes) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR IGNORE INTO promo_time_bonus_stats (uuid, promo_code, minutes, claimed_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, promoCode.toLowerCase());
                stmt.setInt(3, minutes);
                stmt.setLong(4, System.currentTimeMillis());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error recording time bonus claim: " + e.getMessage());
            }
        }, executor);
    }

    public CompletableFuture<Integer> getTimeBonusClaimCountAsync(String promoCode, int minutes) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(DISTINCT uuid) as count FROM promo_time_bonus_stats WHERE promo_code = ? AND minutes = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, promoCode.toLowerCase());
                stmt.setInt(2, minutes);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("count");
                }
                return 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting time bonus claim count: " + e.getMessage());
                return 0;
            }
        }, executor);
    }

    public CompletableFuture<java.util.Map<Integer, Integer>> getAllTimeBonusStatsAsync(String promoCode) {
        return CompletableFuture.supplyAsync(() -> {
            java.util.Map<Integer, Integer> stats = new java.util.HashMap<>();
            String sql = "SELECT minutes, COUNT(DISTINCT uuid) as count FROM promo_time_bonus_stats WHERE promo_code = ? GROUP BY minutes";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, promoCode.toLowerCase());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    stats.put(rs.getInt("minutes"), rs.getInt("count"));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting all time bonus stats: " + e.getMessage());
            }
            return stats;
        }, executor);
    }

    public CompletableFuture<Void> resetPlayerDataAsync(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try {
                String deletePromoUsage = "DELETE FROM promo_usage WHERE uuid = ?";
                String deleteTimeRewards = "DELETE FROM time_rewards WHERE uuid = ?";
                String deletePromoTimeBonus = "DELETE FROM promo_time_bonus WHERE uuid = ?";
                String deleteTimeBonusStats = "DELETE FROM promo_time_bonus_stats WHERE uuid = ?";

                try (PreparedStatement stmt1 = connection.prepareStatement(deletePromoUsage);
                     PreparedStatement stmt2 = connection.prepareStatement(deleteTimeRewards);
                     PreparedStatement stmt3 = connection.prepareStatement(deletePromoTimeBonus);
                     PreparedStatement stmt4 = connection.prepareStatement(deleteTimeBonusStats)) {

                    stmt1.setString(1, uuid.toString());
                    stmt2.setString(1, uuid.toString());
                    stmt3.setString(1, uuid.toString());
                    stmt4.setString(1, uuid.toString());

                    stmt1.executeUpdate();
                    stmt2.executeUpdate();
                    stmt3.executeUpdate();
                    stmt4.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error resetting player data: " + e.getMessage());
            }
        }, executor);
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            executor.shutdown();
            plugin.getLogger().info("Database connection closed.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database: " + e.getMessage());
        }
    }
}
