package com.bobobo.plugins.b0nuscode.ut.up;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UP {

    private static final String VERSION_URL = "https://b0b0b0.dev/pl/b0nusRaw.txt";
    private static final String PREFIX = "\u001B[37m[\u001B[90mB0nusCode\u001B[37m]\u001B[0m ";
    private static final ConsoleCommandSender console = Bukkit.getConsoleSender();

    public static void checkVersion(String currentVersion) {
        try {
            String latestVersion = fetchLatestVersion();

            if (latestVersion == null) {
                logError("Failed to fetch the latest version. Skipping version check.");
                return;
            }

            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                console.sendMessage(PREFIX + " ");
                logWarning("You are using an outdated version!");
                logWarning("Current version: \u001B[90m" + currentVersion +
                        "\u001B[33m, latest version: \u001B[32m" + latestVersion + "\u001B[0m.");
                logWarning("Please update the plugin. Choose one of the following links to download:");
                console.sendMessage(PREFIX + "\u001B[36m1. Black-Minecraft: \u001B[0mhttps://bm.wtf/resources/9373/");
                console.sendMessage(PREFIX + "\u001B[36m2. SpigotMc.ru: \u001B[0mhttps://spigotmc.ru/resources/4445/");
                console.sendMessage(PREFIX + " ");
                console.sendMessage(PREFIX + "\u001B[32m[Source]\u001B[0m \u001B[36mhttps://github.com/b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0/B0nusCode\u001B[0m");
                console.sendMessage(PREFIX + " ");
            } else {
                logInfo("You are using the latest version of the plugin! Version: \u001B[32m" + currentVersion + "\u001B[0m.");
            }
        } catch (Exception e) {
            logError("Error during version check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String fetchLatestVersion() {
        try {
            URL url = new URL(VERSION_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return in.readLine().trim();
            }
        } catch (IOException e) {
            logError("Connection error to " + VERSION_URL + ": " + e.getMessage());
            return null;
        }
    }

    private static void logInfo(String message) {
        console.sendMessage(PREFIX + "\u001B[32m" + message + "\u001B[0m");
    }

    private static void logWarning(String message) {
        console.sendMessage(PREFIX + "\u001B[33m" + message + "\u001B[0m");
    }

    private static void logError(String message) {
        console.sendMessage(PREFIX + "\u001B[31m" + message + "\u001B[0m");
    }
}
