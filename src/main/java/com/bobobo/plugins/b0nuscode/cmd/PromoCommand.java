package com.bobobo.plugins.b0nuscode.cmd;

import com.bobobo.plugins.b0nuscode.cfg.ConfigManager;
import com.bobobo.plugins.b0nuscode.managers.PromoManager;
import com.bobobo.plugins.b0nuscode.models.PromoCode;
import com.bobobo.plugins.b0nuscode.ut.ColorParser;
import com.bobobo.plugins.b0nuscode.ut.MessageUtils;
import com.bobobo.plugins.b0nuscode.ut.NotificationUtils;
import com.bobobo.plugins.b0nuscode.ut.ValidationUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PromoCommand implements CommandExecutor {
    private final PromoManager promoManager;
    private final ConfigManager configManager;

    public PromoCommand(PromoManager promoManager, ConfigManager configManager) {
        this.promoManager = promoManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("b0nuscode.promo")) {
            MessageUtils.sendConfigMessage(sender, configManager, "no-permission", "§cУ вас нет прав для использования этой команды!");
            return true;
        }

        if (!(sender instanceof Player)) {
            MessageUtils.sendConfigMessage(sender, configManager, "no-console", "§cТолько игроки могут использовать эту команду!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            MessageUtils.sendConfigMessage(player, configManager, "usage", "§cИспользование: /promo <код>");
            return true;
        }

        String promoCode = args[0];

        if (!ValidationUtils.isValidPromoCode(promoCode)) {
            MessageUtils.sendConfigMessage(player, configManager, "invalid-promo", "§cНеверный промокод!");
            return true;
        }

        promoCode = ValidationUtils.normalizePromoCode(promoCode);

        PromoManager.PromoResult result = promoManager.activatePromo(player, promoCode);

        switch (result) {
            case SUCCESS:
                PromoCode promoCodeObj = configManager.getPromoCode(promoCode);
                String youtuber = promoCodeObj != null ? promoCodeObj.getYoutuber() : "Неизвестный";

                String successMessage = configManager.getMessage("promo-activated", "&aПромокод успешно активирован!")
                        .replace("%player%", player.getName())
                        .replace("%youtuber%", youtuber);
                NotificationUtils.sendFullNotification(player, configManager, "promo-success", successMessage);

                if (configManager.getMessagesConfig().getBoolean("messages.promo-broadcast.enabled", true)) {
                    String broadcastMessage = configManager.getMessagesConfig().getString("messages.promo-broadcast.message", "")
                            .replace("%player%", player.getName())
                            .replace("%youtuber%", youtuber);

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (!onlinePlayer.equals(player)) {
                            onlinePlayer.sendMessage(ColorParser.parse(broadcastMessage));
                        }
                    }
                }
                break;
            case INVALID_PROMO:
                String invalidMessage = configManager.getMessage("invalid-promo", "&cНеверный промокод!");
                NotificationUtils.playConfigSound(player, configManager, "promo-error");
                if (NotificationUtils.isChatMessagesEnabled(configManager)) {
                    MessageUtils.sendMessage(player, invalidMessage);
                }
                break;
            case ALREADY_USED:
                String usedMessage = configManager.getMessage("already-used", "&cВы уже использовали этот промокод!");
                NotificationUtils.playConfigSound(player, configManager, "promo-error");
                if (NotificationUtils.isChatMessagesEnabled(configManager)) {
                    MessageUtils.sendMessage(player, usedMessage);
                }
                break;
            case ALREADY_USED_ANY_PROMO:
                String usedAnyMessage = configManager.getMessage("already-used-any", "&cВы уже использовали промокод! Каждый игрок может использовать только один промокод.");
                NotificationUtils.playConfigSound(player, configManager, "promo-error");
                if (NotificationUtils.isChatMessagesEnabled(configManager)) {
                    MessageUtils.sendMessage(player, usedAnyMessage);
                }
                break;
            case NO_REWARDS:
                String noRewardsMessage = configManager.getMessage("no-rewards", "&cУ этого промокода нет наград!");
                NotificationUtils.playConfigSound(player, configManager, "promo-error");
                if (NotificationUtils.isChatMessagesEnabled(configManager)) {
                    MessageUtils.sendMessage(player, noRewardsMessage);
                }
                break;
            case MAX_USES_REACHED:
                String maxUsesMessage = configManager.getMessage("max-uses-reached", "&cЭтот промокод исчерпал максимальное количество использований!");
                NotificationUtils.playConfigSound(player, configManager, "promo-error");
                if (NotificationUtils.isChatMessagesEnabled(configManager)) {
                    MessageUtils.sendMessage(player, maxUsesMessage);
                }
                break;
            case YOUTUBER_CANNOT_USE_OWN:
                String youtuberOwnMessage = configManager.getMessage("youtuber-cannot-use-own", "&cВы не можете использовать свой собственный промокод!");
                NotificationUtils.playConfigSound(player, configManager, "promo-error");
                if (NotificationUtils.isChatMessagesEnabled(configManager)) {
                    MessageUtils.sendMessage(player, youtuberOwnMessage);
                }
                break;
        }

        return true;
    }
}
