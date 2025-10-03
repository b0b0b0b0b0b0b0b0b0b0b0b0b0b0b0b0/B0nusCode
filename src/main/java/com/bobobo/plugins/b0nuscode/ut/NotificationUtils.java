package com.bobobo.plugins.b0nuscode.ut;

import com.bobobo.plugins.b0nuscode.managers.ConfigManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class NotificationUtils {

    public static void playSound(Player player, String soundName) {
        if (player == null || soundName == null || soundName.isEmpty()) {
            return;
        }

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
        }
    }

    public static void playConfigSound(Player player, ConfigManager configManager, String soundKey) {
        if (!configManager.getTimeRewardsConfig().getBoolean("notifications.sounds.enabled", true)) {
            return;
        }

        String soundName = configManager.getTimeRewardsConfig().getString("notifications.sounds." + soundKey);
        if (soundName != null && !soundName.isEmpty()) {
            playSound(player, soundName);
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }

        player.sendTitle(
                ColorParser.parse(title != null ? title : ""),
                ColorParser.parse(subtitle != null ? subtitle : ""),
                fadeIn, stay, fadeOut
        );
    }

    public static void sendConfigTitle(Player player, ConfigManager configManager, String titleKey) {
        if (!configManager.getTimeRewardsConfig().getBoolean("notifications.titles.enabled", true)) {
            return;
        }

        String title = configManager.getTimeRewardsConfig().getString("notifications.titles." + titleKey + ".title");
        String subtitle = configManager.getTimeRewardsConfig().getString("notifications.titles." + titleKey + ".subtitle");
        int fadeIn = configManager.getTimeRewardsConfig().getInt("notifications.titles." + titleKey + ".fade-in", 10);
        int stay = configManager.getTimeRewardsConfig().getInt("notifications.titles." + titleKey + ".stay", 40);
        int fadeOut = configManager.getTimeRewardsConfig().getInt("notifications.titles." + titleKey + ".fade-out", 10);

        sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    public static void sendFullNotification(Player player, ConfigManager configManager, String type, String message) {
        playConfigSound(player, configManager, type);
        sendConfigTitle(player, configManager, type);

        if (configManager.getTimeRewardsConfig().getBoolean("notifications.chat-messages.enabled", true)) {
            MessageUtils.sendMessage(player, message);
        }
    }

    public static boolean isChatMessagesEnabled(ConfigManager configManager) {
        return configManager.getTimeRewardsConfig().getBoolean("notifications.chat-messages.enabled", true);
    }

    public static boolean isSoundsEnabled(ConfigManager configManager) {
        return configManager.getTimeRewardsConfig().getBoolean("notifications.sounds.enabled", true);
    }

    public static boolean isTitlesEnabled(ConfigManager configManager) {
        return configManager.getTimeRewardsConfig().getBoolean("notifications.titles.enabled", true);
    }
}
