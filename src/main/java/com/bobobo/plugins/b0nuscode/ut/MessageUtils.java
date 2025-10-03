package com.bobobo.plugins.b0nuscode.ut;

import com.bobobo.plugins.b0nuscode.cfg.ConfigManager;
import org.bukkit.command.CommandSender;

public class MessageUtils {

    public static void sendMessage(CommandSender sender, String message) {
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(ColorParser.parse(message));
        }
    }

    public static void sendConfigMessage(CommandSender sender, ConfigManager configManager, String key, String defaultValue) {
        String message = configManager.getMessage(key, defaultValue);
        sendMessage(sender, message);
    }

    public static void sendError(CommandSender sender, String message) {
        sendMessage(sender, "&c" + message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sendMessage(sender, "&a" + message);
    }

    public static void sendWarning(CommandSender sender, String message) {
        sendMessage(sender, "&e" + message);
    }

    public static void sendInfo(CommandSender sender, String message) {
        sendMessage(sender, "&b" + message);
    }

    public static void sendHexMessage(CommandSender sender, String message) {
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(ColorParser.hex(message));
        }
    }

    public static void sendPlainMessage(CommandSender sender, String message) {
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(ColorParser.stripColors(message));
        }
    }
}
