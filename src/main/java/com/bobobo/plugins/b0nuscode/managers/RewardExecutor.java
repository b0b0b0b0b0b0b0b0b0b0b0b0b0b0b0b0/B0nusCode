package com.bobobo.plugins.b0nuscode.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RewardExecutor {
    private final JavaPlugin plugin;

    public RewardExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void executeRewards(Player player, List<String> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            return;
        }

        for (String reward : rewards) {
            executeReward(player, reward);
        }
    }

    public void executeReward(Player player, String reward) {
        if (reward == null || reward.trim().isEmpty()) {
            return;
        }

        String command = reward.replace("%player%", player.getName());
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        plugin.getLogger().info("Executed reward for " + player.getName() + ": " + command);
    }

    public boolean isValidReward(String reward) {
        if (reward == null || reward.trim().isEmpty()) {
            return false;
        }

        return reward.contains("%player%");
    }
}
