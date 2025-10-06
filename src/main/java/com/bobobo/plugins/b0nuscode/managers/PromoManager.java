package com.bobobo.plugins.b0nuscode.managers;

import com.bobobo.plugins.b0nuscode.cfg.ConfigManager;
import com.bobobo.plugins.b0nuscode.db.DatabaseManager;
import com.bobobo.plugins.b0nuscode.models.PromoCode;
import com.bobobo.plugins.b0nuscode.rw.RewardExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PromoManager {
    private final ConfigManager configManager;
    private final DataManager dataManager;
    private final DatabaseManager databaseManager;
    private final RewardExecutor rewardExecutor;

    public PromoManager(ConfigManager configManager, DataManager dataManager, DatabaseManager databaseManager, RewardExecutor rewardExecutor) {
        this.configManager = configManager;
        this.dataManager = dataManager;
        this.databaseManager = databaseManager;
        this.rewardExecutor = rewardExecutor;
    }

    public PromoResult activatePromo(Player player, String promoCode) {
        String promo = promoCode.toLowerCase();

        PromoCode promoCodeObj = configManager.getPromoCode(promo);
        if (promoCodeObj == null) {
            return PromoResult.INVALID_PROMO;
        }

        if (promoCodeObj.getYoutuberPlayer() != null &&
                player.getName().equalsIgnoreCase(promoCodeObj.getYoutuberPlayer())) {
            return PromoResult.YOUTUBER_CANNOT_USE_OWN;
        }

        if (dataManager.hasUsedPromo(player, promo)) {
            return PromoResult.ALREADY_USED;
        }

        if (dataManager.hasUsedAnyPromo(player)) {
            return PromoResult.ALREADY_USED_ANY_PROMO;
        }

        if (promoCodeObj.hasMaxUses()) {
            try {
                int currentUses = databaseManager.getPromoUsesAsync(promo).get();
                if (currentUses >= promoCodeObj.getMaxUses()) {
                    return PromoResult.MAX_USES_REACHED;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> rewards = promoCodeObj.getRewards();
        if (rewards == null || rewards.isEmpty()) {
            return PromoResult.NO_REWARDS;
        }

        rewardExecutor.executeRewards(player, rewards);
        dataManager.setPromoUsed(player, promo);
        databaseManager.incrementPromoUsesAsync(promo);

        if (promoCodeObj.isTimeBonusEnabled()) {
            databaseManager.setPromoTimeBonusAsync(player.getUniqueId(), promo);
        }

        return PromoResult.SUCCESS;
    }

    public boolean isValidPromo(String promoCode) {
        return configManager.hasPromo(promoCode.toLowerCase());
    }

    public List<String> getPromoRewards(String promoCode) {
        return configManager.getPromoRewards(promoCode.toLowerCase());
    }

    public CompletableFuture<Integer> getPromoUsesAsync(String promoCode) {
        return databaseManager.getPromoUsesAsync(promoCode.toLowerCase());
    }

    public CompletableFuture<java.util.Map<Integer, Integer>> getTimeBonusStatsAsync(String promoCode) {
        return databaseManager.getAllTimeBonusStatsAsync(promoCode.toLowerCase());
    }

    public void resetPlayerData(UUID uuid) {
        dataManager.resetPlayerData(uuid);
    }

    public enum PromoResult {
        SUCCESS,
        INVALID_PROMO,
        ALREADY_USED,
        ALREADY_USED_ANY_PROMO,
        NO_REWARDS,
        MAX_USES_REACHED,
        YOUTUBER_CANNOT_USE_OWN
    }
}
