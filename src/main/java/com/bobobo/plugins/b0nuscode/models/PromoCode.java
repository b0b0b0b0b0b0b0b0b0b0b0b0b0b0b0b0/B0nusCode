package com.bobobo.plugins.b0nuscode.models;

import java.util.List;
import java.util.Map;

public class PromoCode {
    private final String code;
    private final String youtuber;
    private final List<String> rewards;
    private final int maxUses;
    private final boolean timeBonusEnabled;
    private final Map<Integer, List<String>> timeBonuses;

    public PromoCode(String code, String youtuber, List<String> rewards, int maxUses,
                     boolean timeBonusEnabled, Map<Integer, List<String>> timeBonuses) {
        this.code = code;
        this.youtuber = youtuber;
        this.rewards = rewards;
        this.maxUses = maxUses;
        this.timeBonusEnabled = timeBonusEnabled;
        this.timeBonuses = timeBonuses;
    }

    public String getCode() {
        return code;
    }

    public String getYoutuber() {
        return youtuber;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public boolean hasMaxUses() {
        return maxUses > 0;
    }

    public boolean isTimeBonusEnabled() {
        return timeBonusEnabled;
    }

    public Map<Integer, List<String>> getTimeBonuses() {
        return timeBonuses;
    }

    public List<String> getTimeBonusRewards(int minutes) {
        return timeBonuses.get(minutes);
    }
}

