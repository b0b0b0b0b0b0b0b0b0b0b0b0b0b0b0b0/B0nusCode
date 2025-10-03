package com.bobobo.plugins.b0nuscode.managers;

import com.bobobo.plugins.b0nuscode.db.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataManager {
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Long> playTime;

    public DataManager(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playTime = new HashMap<>();
    }

    public void close() {
        databaseManager.close();
    }

    public CompletableFuture<Boolean> hasUsedPromoAsync(Player player, String promo) {
        return databaseManager.hasUsedPromoAsync(player.getUniqueId(), promo);
    }

    public boolean hasUsedPromo(Player player, String promo) {
        try {
            return hasUsedPromoAsync(player, promo).get();
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking promo usage: " + e.getMessage());
            return false;
        }
    }

    public CompletableFuture<Boolean> hasUsedAnyPromoAsync(Player player) {
        return databaseManager.hasUsedAnyPromoAsync(player.getUniqueId());
    }

    public boolean hasUsedAnyPromo(Player player) {
        try {
            return hasUsedAnyPromoAsync(player).get();
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking any promo usage: " + e.getMessage());
            return false;
        }
    }

    public CompletableFuture<Void> setPromoUsedAsync(Player player, String promo) {
        return databaseManager.setPromoUsedAsync(player.getUniqueId(), promo);
    }

    public void setPromoUsed(Player player, String promo) {
        setPromoUsedAsync(player, promo).join();
    }

    public CompletableFuture<Boolean> hasClaimedTimeRewardAsync(Player player, String rewardKey) {
        return databaseManager.hasClaimedTimeRewardAsync(player.getUniqueId(), rewardKey);
    }

    public boolean hasClaimedTimeReward(Player player, String rewardKey) {
        try {
            return hasClaimedTimeRewardAsync(player, rewardKey).get();
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking time reward: " + e.getMessage());
            return false;
        }
    }

    public CompletableFuture<Void> setTimeRewardClaimedAsync(Player player, String rewardKey) {
        return databaseManager.setTimeRewardClaimedAsync(player.getUniqueId(), rewardKey);
    }

    public void setTimeRewardClaimed(Player player, String rewardKey) {
        setTimeRewardClaimedAsync(player, rewardKey).join();
    }

    public long getPlayTime(UUID uuid) {
        return playTime.getOrDefault(uuid, 0L);
    }

    public void setPlayTime(UUID uuid, long time) {
        playTime.put(uuid, time);
    }

    public void addPlayTime(UUID uuid, long additionalTime) {
        long currentTime = getPlayTime(uuid);
        setPlayTime(uuid, currentTime + additionalTime);
    }

    public Map<UUID, Long> getAllPlayTimes() {
        return new HashMap<>(playTime);
    }
}
