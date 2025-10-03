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
            MessageUtils.sendError(sender, "You don't have permission to use this command!");
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
                    MessageUtils.sendError(sender, "Usage: /adminpromo add <code> <reward1> [reward2] [reward3]...");
                }
                break;
            case "remove":
                if (args.length >= 2) {
                    removePromo(sender, args[1]);
                } else {
                    MessageUtils.sendError(sender, "Usage: /adminpromo remove <code>");
                }
                break;
            case "reset":
                if (args.length >= 2) {
                    resetPlayerPromo(sender, args[1]);
                } else {
                    MessageUtils.sendError(sender, "Usage: /adminpromo reset <player>");
                }
                break;
            case "reload":
                reloadConfig(sender);
                break;
            case "stats":
                showStats(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        MessageUtils.sendInfo(sender, "&6=== B0nusCode Admin Commands ===");
        MessageUtils.sendInfo(sender, "&e/adminpromo list &7- Show all promocodes");
        MessageUtils.sendInfo(sender, "&e/adminpromo add <code> <reward1> [reward2]... &7- Add new promocode");
        MessageUtils.sendInfo(sender, "&e/adminpromo remove <code> &7- Remove promocode");
        MessageUtils.sendInfo(sender, "&e/adminpromo reset <player> &7- Reset player's promocode usage");
        MessageUtils.sendInfo(sender, "&e/adminpromo reload &7- Reload configuration");
        MessageUtils.sendInfo(sender, "&e/adminpromo stats &7- Show promocode statistics");
    }

    private void listPromos(CommandSender sender) {
        if (configManager.getAllPromos().isEmpty()) {
            MessageUtils.sendWarning(sender, "No promocodes found!");
            return;
        }

        MessageUtils.sendInfo(sender, "&6=== Available Promocodes ===");
        for (String code : configManager.getAllPromos().keySet()) {
            MessageUtils.sendInfo(sender, "&e" + code);
        }
    }

    private void addPromo(CommandSender sender, String[] args) {
        String code = args[1].toLowerCase();

        if (!ValidationUtils.isValidPromoCode(code)) {
            MessageUtils.sendError(sender, "Invalid promocode format! Use only letters, numbers, and dashes.");
            return;
        }

        if (configManager.hasPromo(code)) {
            MessageUtils.sendError(sender, "Promocode '" + code + "' already exists!");
            return;
        }

        StringBuilder rewardsBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) rewardsBuilder.append(" ");
            rewardsBuilder.append(args[i]);
        }
        String rewardsString = rewardsBuilder.toString();

        MessageUtils.sendSuccess(sender, "Promocode '" + code + "' added with rewards: " + rewardsString);
        MessageUtils.sendWarning(sender, "Note: Changes are temporary. Edit config.yml to make them permanent.");
    }

    private void removePromo(CommandSender sender, String code) {
        code = code.toLowerCase();

        if (!configManager.hasPromo(code)) {
            MessageUtils.sendError(sender, "Promocode '" + code + "' not found!");
            return;
        }

        MessageUtils.sendSuccess(sender, "Promocode '" + code + "' removed!");
        MessageUtils.sendWarning(sender, "Note: Changes are temporary. Edit config.yml to make them permanent.");
    }

    private void resetPlayerPromo(CommandSender sender, String playerName) {
        Player target = sender.getServer().getPlayer(playerName);
        if (target == null) {
            MessageUtils.sendError(sender, "Player '" + playerName + "' not found!");
            return;
        }

        for (String promo : configManager.getAllPromos().keySet()) {
        }
        MessageUtils.sendSuccess(sender, "Reset promocode usage for " + target.getName());
    }

    private void reloadConfig(CommandSender sender) {
        configManager.reloadConfigs();
        MessageUtils.sendSuccess(sender, "Configuration reloaded successfully!");
    }

    private void showStats(CommandSender sender) {
        if (configManager.getAllPromos().isEmpty()) {
            MessageUtils.sendWarning(sender, "No promocodes found!");
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
            } catch (Exception e) {
                MessageUtils.sendError(sender, "Error getting stats for " + promoCode.getCode());
            }
        }

        String footer = configManager.getMessagesConfig().getString("messages.stats-footer", "&#6A0DAD&l===========================");
        sender.sendMessage(com.bobobo.plugins.b0nuscode.ut.ColorParser.parse(footer));
    }
}
