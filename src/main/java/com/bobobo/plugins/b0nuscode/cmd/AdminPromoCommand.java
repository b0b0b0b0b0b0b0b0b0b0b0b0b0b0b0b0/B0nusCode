package com.bobobo.plugins.b0nuscode.cmd;

import com.bobobo.plugins.b0nuscode.cfg.ConfigManager;
import com.bobobo.plugins.b0nuscode.managers.PromoManager;
import com.bobobo.plugins.b0nuscode.models.PromoCode;
import com.bobobo.plugins.b0nuscode.ut.MessageUtils;
import com.bobobo.plugins.b0nuscode.ut.ValidationUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminPromoCommand implements CommandExecutor {
    private final ConfigManager configManager;
    private final PromoManager promoManager;

    public AdminPromoCommand(ConfigManager configManager, PromoManager promoManager) {
        this.configManager = configManager;
        this.promoManager = promoManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("b0nuscode.admin")) {
            String message = configManager.getMessagesConfig().getString("messages.admin.no-permission", "У вас нет прав для использования этой команды!");
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                listPromos(sender);
                break;
            case "add":
                if (args.length >= 3) {
                    addPromo(sender, args);
                } else {
                    String message = configManager.getMessagesConfig().getString("messages.admin.add-usage", "Использование: /adminpromo add <код> <награда1> [награда2] [награда3]...");
                    sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
                }
                break;
            case "remove":
                if (args.length >= 2) {
                    removePromo(sender, args[1]);
                } else {
                    String message = configManager.getMessagesConfig().getString("messages.admin.remove-usage", "Использование: /adminpromo remove <код>");
                    sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
                }
                break;
            case "reset":
                if (args.length >= 2) {
                    resetPlayerPromo(sender, args[1]);
                } else {
                    String message = configManager.getMessagesConfig().getString("messages.admin.reset-usage", "Использование: /adminpromo reset <игрок>");
                    sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
                }
                break;
            case "reload":
                reloadConfig(sender);
                break;
            case "stats":
                if (args.length >= 2) {
                    showPromoStats(sender, args[1]);
                } else {
                    showStats(sender);
                }
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(
                configManager.getMessagesConfig().getString("messages.admin.help-header", "=== Команды B0nusCode ===")));
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(
                configManager.getMessagesConfig().getString("messages.admin.help-list", "/adminpromo list - Показать все промокоды")));
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(
                configManager.getMessagesConfig().getString("messages.admin.help-add", "/adminpromo add <код> <награда1> [награда2]... - Добавить промокод")));
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(
                configManager.getMessagesConfig().getString("messages.admin.help-remove", "/adminpromo remove <код> - Удалить промокод")));
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(
                configManager.getMessagesConfig().getString("messages.admin.help-reset", "/adminpromo reset <игрок> - Сбросить использование промокодов игрока")));
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(
                configManager.getMessagesConfig().getString("messages.admin.help-reload", "/adminpromo reload - Перезагрузить конфигурацию")));
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(
                configManager.getMessagesConfig().getString("messages.admin.help-stats", "/adminpromo stats [промокод] - Показать статистику промокодов")));
    }

    private void listPromos(CommandSender sender) {
        if (configManager.getAllPromos().isEmpty()) {
            String message = configManager.getMessagesConfig().getString("messages.admin.list-empty", "Промокоды не найдены!");
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            return;
        }

        String header = configManager.getMessagesConfig().getString("messages.admin.list-header", "=== Доступные промокоды ===");
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(header));

        for (String code : configManager.getAllPromos().keySet()) {
            String itemFormat = configManager.getMessagesConfig().getString("messages.admin.list-item", "  ● %code%");
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(itemFormat.replace("%code%", code)));
        }
    }

    private void addPromo(CommandSender sender, String[] args) {
        String code = args[1].toLowerCase();

        if (!ValidationUtils.isValidPromoCode(code)) {
            String message = configManager.getMessagesConfig().getString("messages.admin.add-invalid-format", "Неверный формат промокода! Используйте только буквы, цифры и дефисы.");
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            return;
        }

        if (configManager.hasPromo(code)) {
            String message = configManager.getMessagesConfig().getString("messages.admin.add-already-exists", "Промокод '%code%' уже существует!")
                    .replace("%code%", code);
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            return;
        }

        StringBuilder rewardsBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) rewardsBuilder.append(" ");
            rewardsBuilder.append(args[i]);
        }
        String rewardsString = rewardsBuilder.toString();

        String successMsg = configManager.getMessagesConfig().getString("messages.admin.add-success", "Промокод '%code%' добавлен с наградами: %rewards%")
                .replace("%code%", code)
                .replace("%rewards%", rewardsString);
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(successMsg));

        String warningMsg = configManager.getMessagesConfig().getString("messages.admin.add-warning", "Примечание: Изменения временные. Отредактируйте config.yml для постоянного сохранения.");
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(warningMsg));
    }

    private void removePromo(CommandSender sender, String code) {
        code = code.toLowerCase();

        if (!configManager.hasPromo(code)) {
            String message = configManager.getMessagesConfig().getString("messages.admin.remove-not-found", "Промокод '%code%' не найден!")
                    .replace("%code%", code);
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            return;
        }

        String successMsg = configManager.getMessagesConfig().getString("messages.admin.remove-success", "Промокод '%code%' удален!")
                .replace("%code%", code);
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(successMsg));

        String warningMsg = configManager.getMessagesConfig().getString("messages.admin.remove-warning", "Примечание: Изменения временные. Отредактируйте config.yml для постоянного сохранения.");
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(warningMsg));
    }

    private void resetPlayerPromo(CommandSender sender, String playerName) {
        Player target = sender.getServer().getPlayer(playerName);
        if (target == null) {
            String message = configManager.getMessagesConfig().getString("messages.admin.reset-player-not-found", "Игрок '%player%' не найден!")
                    .replace("%player%", playerName);
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            return;
        }

        promoManager.resetPlayerData(target.getUniqueId());

        String successMsg = configManager.getMessagesConfig().getString("messages.admin.reset-success", "Сброшено использование промокодов для игрока %player%")
                .replace("%player%", target.getName());
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(successMsg));
    }

    private void reloadConfig(CommandSender sender) {
        configManager.reloadConfigs();
        String message = configManager.getMessagesConfig().getString("messages.admin.reload-success", "Конфигурация успешно перезагружена!");
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
    }

    private void showStats(CommandSender sender) {
        if (configManager.getAllPromos().isEmpty()) {
            String message = configManager.getMessagesConfig().getString("messages.admin.list-empty", "Промокоды не найдены!");
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            return;
        }

        String header = configManager.getMessagesConfig().getString("messages.stats-header", "&#6A0DAD&l=== Статистика промокодов ===");
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(header));

        for (PromoCode promoCode : configManager.getAllPromos().values()) {
            try {
                int uses = promoManager.getPromoUsesAsync(promoCode.getCode()).get();

                String line = configManager.getMessagesConfig()
                        .getString("messages.stats-line", "&#C4B0FB%code% &#7E57C2(Ютубер: &#AB47BC%youtuber%&#7E57C2) - &#9575CD%uses% использований")
                        .replace("%code%", promoCode.getCode())
                        .replace("%youtuber%", promoCode.getYoutuber())
                        .replace("%uses%", String.valueOf(uses));

                if (promoCode.hasMaxUses()) {
                    String maxUses = configManager.getMessagesConfig()
                            .getString("messages.stats-max-uses", " &#7E57C2(макс: &#AB47BC%max%&#7E57C2)")
                            .replace("%max%", String.valueOf(promoCode.getMaxUses()));
                    line += maxUses;
                }

                sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(line));

                if (promoCode.isTimeBonusEnabled() && !promoCode.getTimeBonuses().isEmpty()) {
                    java.util.Map<Integer, Integer> timeBonusStats = promoManager.getTimeBonusStatsAsync(promoCode.getCode()).get();

                    StringBuilder timeBonusLine = new StringBuilder();
                    timeBonusLine.append(configManager.getMessagesConfig()
                            .getString("messages.stats-time-bonus-prefix", "  &#7E57C2└─ Временные награды:"));

                    boolean first = true;
                    for (java.util.Map.Entry<Integer, java.util.List<String>> bonusEntry : promoCode.getTimeBonuses().entrySet()) {
                        int minutes = bonusEntry.getKey();
                        int playerCount = timeBonusStats.getOrDefault(minutes, 0);

                        String formattedTime = com.bobobo.plugins.b0nuscode.ut.TimeFormatter.formatMinutes(minutes);
                        String bonusLine = configManager.getMessagesConfig()
                                .getString("messages.stats-time-bonus-line", " &#AB47BC%time%&#7E57C2: &#9575CD%count% игроков")
                                .replace("%time%", formattedTime)
                                .replace("%count%", String.valueOf(playerCount));

                        if (!first) {
                            timeBonusLine.append(" &#7E57C2|");
                        }
                        timeBonusLine.append(bonusLine);
                        first = false;
                    }

                    sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(timeBonusLine.toString()));
                }
            } catch (Exception e) {
                String message = configManager.getMessagesConfig().getString("messages.admin.stats-error", "Ошибка получения статистики для промокода %code%")
                        .replace("%code%", promoCode.getCode());
                sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            }
        }

        String footer = configManager.getMessagesConfig().getString("messages.stats-footer", "&#6A0DAD&l===========================");
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(footer));
    }

    private void showPromoStats(CommandSender sender, String promoCodeName) {
        String promo = promoCodeName.toLowerCase();

        if (!configManager.hasPromo(promo)) {
            String message = configManager.getMessagesConfig().getString("messages.admin.stats-promo-not-found", "&#ff0000Промокод '&#ff6b35%code%&#ff0000' не найден!")
                    .replace("%code%", promoCodeName);
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
            return;
        }

        PromoCode promoCode = configManager.getPromoCode(promo);

        try {
            int uses = promoManager.getPromoUsesAsync(promo).get();

            String header = configManager.getMessagesConfig().getString("messages.stats-detailed-header", "&#6A0DAD&l=== &#C4B0FBСтатистика: &#B388EB%code% &#6A0DAD&l===")
                    .replace("%code%", promoCode.getCode());
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(header));

            String youtuberLine = configManager.getMessagesConfig().getString("messages.stats-detailed-youtuber", "&#7E57C2Ютубер: &#AB47BC%youtuber%")
                    .replace("%youtuber%", promoCode.getYoutuber());
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(youtuberLine));

            String usesLine = configManager.getMessagesConfig().getString("messages.stats-detailed-uses", "&#7E57C2Использований: &#9575CD%uses%")
                    .replace("%uses%", String.valueOf(uses));
            if (promoCode.hasMaxUses()) {
                usesLine += configManager.getMessagesConfig().getString("messages.stats-detailed-max", " &#7E57C2/ &#AB47BC%max%")
                        .replace("%max%", String.valueOf(promoCode.getMaxUses()));
            }
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(usesLine));

            sender.sendMessage("");

            String rewardsHeader = configManager.getMessagesConfig().getString("messages.stats-detailed-rewards-header", "&#C4B0FBНачальные награды:");
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(rewardsHeader));

            for (String reward : promoCode.getRewards()) {
                String rewardLine = configManager.getMessagesConfig().getString("messages.stats-detailed-reward-item", "  &#7E57C2• &#9575CD%reward%")
                        .replace("%reward%", reward);
                sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(rewardLine));
            }

            if (promoCode.isTimeBonusEnabled() && !promoCode.getTimeBonuses().isEmpty()) {
                sender.sendMessage("");

                String timeBonusHeader = configManager.getMessagesConfig().getString("messages.stats-detailed-time-bonus-header", "&#C4B0FBВременные награды:");
                sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(timeBonusHeader));

                java.util.Map<Integer, Integer> timeBonusStats = promoManager.getTimeBonusStatsAsync(promo).get();

                java.util.List<Integer> sortedMinutes = new java.util.ArrayList<>(promoCode.getTimeBonuses().keySet());
                java.util.Collections.sort(sortedMinutes);

                for (int minutes : sortedMinutes) {
                    java.util.List<String> rewards = promoCode.getTimeBonuses().get(minutes);
                    int playerCount = timeBonusStats.getOrDefault(minutes, 0);

                    String formattedTime = com.bobobo.plugins.b0nuscode.ut.TimeFormatter.formatMinutes(minutes);
                    String timeLine = configManager.getMessagesConfig().getString("messages.stats-detailed-time-bonus-time", "  &#AB47BC⏱ %time% &#7E57C2- &#9575CD%count% игроков получили")
                            .replace("%time%", formattedTime)
                            .replace("%count%", String.valueOf(playerCount));
                    sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(timeLine));

                    for (String reward : rewards) {
                        String rewardLine = configManager.getMessagesConfig().getString("messages.stats-detailed-time-bonus-reward", "     &#7E57C2└─ &#9575CD%reward%")
                                .replace("%reward%", reward);
                        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(rewardLine));
                    }
                }
            }

            String footer = configManager.getMessagesConfig().getString("messages.stats-detailed-footer", "&#6A0DAD&l================================");
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(footer));

        } catch (Exception e) {
            String message = configManager.getMessagesConfig().getString("messages.admin.stats-error", "&#ff0000Ошибка получения статистики для промокода %code%")
                    .replace("%code%", promoCode.getCode());
            sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(message));
        }
    }
}
