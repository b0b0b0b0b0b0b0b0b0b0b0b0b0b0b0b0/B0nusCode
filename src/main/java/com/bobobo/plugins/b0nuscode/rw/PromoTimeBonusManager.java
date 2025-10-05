package com.bobobo.plugins.b0nuscode.rw;

import com.bobobo.plugins.b0nuscode.cfg.ConfigManager;
import com.bobobo.plugins.b0nuscode.db.DatabaseManager;
import com.bobobo.plugins.b0nuscode.models.PromoCode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PromoTimeBonusManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    private final RewardExecutor rewardExecutor;
    private final Map<String, Integer> claimedBonuses;
    private BukkitRunnable bonusChecker;

    public PromoTimeBonusManager(JavaPlugin plugin, ConfigManager configManager, DatabaseManager databaseManager, RewardExecutor rewardExecutor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.rewardExecutor = rewardExecutor;
        this.claimedBonuses = new ConcurrentHashMap<>();
    }

    public void start() {
        if (bonusChecker != null) {
            bonusChecker.cancel();
        }

        bonusChecker = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkAndGiveBonuses(player);
                }
            }
        };

        bonusChecker.runTaskTimerAsynchronously(plugin, 1200L, 1200L);
    }

    public void stop() {
        if (bonusChecker != null) {
            bonusChecker.cancel();
            bonusChecker = null;
        }
    }

    private void checkAndGiveBonuses(Player player) {
        for (PromoCode promoCode : configManager.getAllPromos().values()) {
            if (!promoCode.isTimeBonusEnabled()) {
                continue;
            }

            try {
                long activationTime = databaseManager.getPromoActivationTimeAsync(player.getUniqueId(), promoCode.getCode()).get();
                if (activationTime == 0) {
                    continue;
                }

                long currentTime = System.currentTimeMillis();
                long minutesPassed = (currentTime - activationTime) / (1000 * 60);

                for (Map.Entry<Integer, List<String>> bonus : promoCode.getTimeBonuses().entrySet()) {
                    int requiredMinutes = bonus.getKey();
                    List<String> rewards = bonus.getValue();

                    if (minutesPassed >= requiredMinutes) {
                        String key = player.getUniqueId() + ":" + promoCode.getCode();
                        int lastClaimed = claimedBonuses.getOrDefault(key, 0);

                        if (lastClaimed < requiredMinutes) {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                rewardExecutor.executeRewards(player, rewards);

                                String formattedTime = com.bobobo.plugins.b0nuscode.ut.TimeFormatter.formatMinutes(requiredMinutes);
                                String message = configManager.getMessage("time-bonus-received", "")
                                        .replace("%player%", player.getName())
                                        .replace("%minutes%", formattedTime)
                                        .replace("%youtuber%", promoCode.getYoutuber());

                                if (!message.isEmpty()) {
                                    player.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
                                }
                            });

                            claimedBonuses.put(key, requiredMinutes);

                            plugin.getLogger().info("Time bonus given to " + player.getName() +
                                    " for promo " + promoCode.getCode() + " (" + requiredMinutes + " minutes)");
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking time bonus for " + player.getName() + ": " + e.getMessage());
            }
        }
    }
}

