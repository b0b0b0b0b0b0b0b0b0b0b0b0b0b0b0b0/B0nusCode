package com.bobobo.plugins.b0nuscode.models;

import java.util.List;
import java.util.Map;

public record PromoCode(
        String code,
        String youtuber,
        String youtuberPlayer,
        List<String> rewards,
        int maxUses,
        boolean timeBonusEnabled,
        Map<Integer, List<String>> timeBonuses
) {
    public boolean hasMaxUses() {
        return maxUses > 0;
    }

    public String getCode() {
        return code;
    }

    public String getYoutuber() {
        return youtuber;
    }

    public String getYoutuberPlayer() {
        return youtuberPlayer;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public boolean isTimeBonusEnabled() {
        return timeBonusEnabled;
    }

    public Map<Integer, List<String>> getTimeBonuses() {
        return timeBonuses;
    }
}