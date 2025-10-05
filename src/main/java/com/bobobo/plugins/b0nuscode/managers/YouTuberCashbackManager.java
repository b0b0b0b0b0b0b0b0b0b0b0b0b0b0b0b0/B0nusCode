package com.bobobo.plugins.b0nuscode.managers;

import com.bobobo.plugins.b0nuscode.cfg.ConfigManager;
import com.bobobo.plugins.b0nuscode.db.DatabaseManager;
import com.bobobo.plugins.b0nuscode.models.PromoCode;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class YouTuberCashbackManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final Economy economy;
    private final Map<String, Double> lastPayouts;
    private BukkitRunnable cashbackTask;

    public YouTuberCashbackManager(JavaPlugin plugin, ConfigManager configManager, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.lastPayouts = new ConcurrentHashMap<>();
        this.economy = setupEconomy();
    }

    private Economy setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! YouTuber cashback disabled.");
            return null;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Economy plugin not found! YouTuber cashback disabled.");
            return null;
        }

        return rsp.getProvider();
    }

    public void start() {
        if (economy == null) {
            return;
        }

        if (!configManager.getTimeRewardsConfig().getBoolean("youtuber-cashback.enabled", false)) {
            return;
        }

        if (cashbackTask != null) {
            cashbackTask.cancel();
        }

        int intervalMinutes = configManager.getTimeRewardsConfig().getInt("youtuber-cashback.interval-minutes", 60);
        long intervalTicks = intervalMinutes * 60 * 20L;

        cashbackTask = new BukkitRunnable() {
            @Override
            public void run() {
                processCashback();
            }
        };

        cashbackTask.runTaskTimerAsynchronously(plugin, intervalTicks, intervalTicks);
        plugin.getLogger().info("YouTuber cashback system started (interval: " + intervalMinutes + " minutes)");
    }

    public void stop() {
        if (cashbackTask != null) {
            cashbackTask.cancel();
            cashbackTask = null;
        }
    }

    private void processCashback() {
        double percentage = configManager.getTimeRewardsConfig().getDouble("youtuber-cashback.percentage", 0.1) / 100.0;
        double minPayout = configManager.getTimeRewardsConfig().getDouble("youtuber-cashback.min-payout", 100.0);

        Map<String, List<UUID>> youtuberPlayers = new HashMap<>();

        for (PromoCode promo : configManager.getAllPromos().values()) {
            String youtuberNick = promo.getYoutuberPlayer();
            if (youtuberNick == null || youtuberNick.isEmpty()) {
                continue;
            }

            try {
                List<UUID> players = databaseManager.getPlayersByPromo(promo.getCode());
                youtuberPlayers.computeIfAbsent(youtuberNick, k -> new ArrayList<>()).addAll(players);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get players for promo " + promo.getCode() + ": " + e.getMessage());
            }
        }

        for (Map.Entry<String, List<UUID>> entry : youtuberPlayers.entrySet()) {
            String youtuberNick = entry.getKey();
            List<UUID> playerUUIDs = entry.getValue();

            if (playerUUIDs.isEmpty()) {
                continue;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                OfflinePlayer youtuber = Bukkit.getOfflinePlayer(youtuberNick);
                if (!youtuber.hasPlayedBefore()) {
                    return;
                }

                double totalBalance = 0.0;
                int onlineCount = 0;

                for (UUID uuid : playerUUIDs) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        totalBalance += economy.getBalance(player);
                        onlineCount++;
                    }
                }

                if (onlineCount == 0) {
                    return;
                }

                double cashback = totalBalance * percentage;
                if (cashback < minPayout) {
                    return;
                }

                economy.depositPlayer(youtuber, cashback);
                lastPayouts.put(youtuberNick, cashback);

                plugin.getLogger().info(String.format("Cashback paid to %s: %.2f (from %d online players, total balance: %.2f)",
                        youtuberNick, cashback, onlineCount, totalBalance));

                if (youtuber.isOnline()) {
                    Player youtuberPlayer = (Player) youtuber;
                    String message = configManager.getMessage("cashback-received", "")
                            .replace("%youtuber%", youtuberNick)
                            .replace("%cashback%", String.format("%.2f", cashback))
                            .replace("%players%", String.valueOf(onlineCount));

                    if (!message.isEmpty()) {
                        youtuberPlayer.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
                    }
                }
            });
        }
    }

    public boolean isEnabled() {
        return economy != null && configManager.getTimeRewardsConfig().getBoolean("youtuber-cashback.enabled", false);
    }

    public Map<String, Double> getLastPayouts() {
        return Map.copyOf(lastPayouts);
    }
}
