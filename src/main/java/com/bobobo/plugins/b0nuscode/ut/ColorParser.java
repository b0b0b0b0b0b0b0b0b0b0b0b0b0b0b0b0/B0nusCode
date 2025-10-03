package com.bobobo.plugins.b0nuscode.ut;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorParser {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    public static String hex(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = parseHexColor(hexCode);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String parseHexColor(String hexCode) {
        char colorChar = ChatColor.COLOR_CHAR;
        return String.valueOf(colorChar) + 'x' +
                colorChar + hexCode.charAt(0) + colorChar + hexCode.charAt(1) +
                colorChar + hexCode.charAt(2) + colorChar + hexCode.charAt(3) +
                colorChar + hexCode.charAt(4) + colorChar + hexCode.charAt(5);
    }

    public static String parse(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return hex(message);
    }

    public static String parseLegacy(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String stripColors(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        return ChatColor.stripColor(parse(message));
    }
}