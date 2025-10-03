package com.bobobo.plugins.b0nuscode.cfg;

import com.bobobo.plugins.b0nuscode.models.PromoCode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration messagesConfig;
    private FileConfiguration promoConfig;
    private FileConfiguration promoCodesConfig;
    private Map<String, PromoCode> promoCodes;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.promoCodes = new HashMap<>();
        loadConfigs();
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        promoConfig = plugin.getConfig();

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        File promoCodesFile = new File(plugin.getDataFolder(), "promocodes.yml");
        if (!promoCodesFile.exists()) {
            plugin.saveResource("promocodes.yml", false);
        }
        promoCodesConfig = YamlConfiguration.loadConfiguration(promoCodesFile);

        loadPromoCodes();
    }

    private void loadPromoCodes() {
        promoCodes.clear();
        ConfigurationSection section = promoCodesConfig.getConfigurationSection("promocodes");
        if (section == null) {
            return;
        }

        for (String code : section.getKeys(false)) {
            String path = "promocodes." + code;
            String youtuber = promoCodesConfig.getString(path + ".youtuber", "Unknown");
            List<String> rewards = promoCodesConfig.getStringList(path + ".rewards");

            int maxUses = 0;
            Object maxUsesObj = promoCodesConfig.get(path + ".max-uses");
            if (maxUsesObj instanceof Boolean && !(Boolean) maxUsesObj) {
                maxUses = -1; // Безлимит
            } else if (maxUsesObj instanceof Integer) {
                maxUses = (Integer) maxUsesObj;
            }

            boolean timeBonusEnabled = promoCodesConfig.getBoolean(path + ".time-bonus.enabled", false);
            Map<Integer, List<String>> timeBonuses = new HashMap<>();

            if (timeBonusEnabled) {
                ConfigurationSection timeBonusSection = promoCodesConfig.getConfigurationSection(path + ".time-bonus");
                if (timeBonusSection != null) {
                    for (String minutesStr : timeBonusSection.getKeys(false)) {
                        if (minutesStr.equals("enabled")) continue;
                        try {
                            int minutes = Integer.parseInt(minutesStr);
                            List<String> bonusRewards = promoCodesConfig.getStringList(path + ".time-bonus." + minutesStr + ".rewards");
                            timeBonuses.put(minutes, bonusRewards);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            PromoCode promoCode = new PromoCode(code.toLowerCase(), youtuber, rewards, maxUses, timeBonusEnabled, timeBonuses);
            promoCodes.put(code.toLowerCase(), promoCode);
        }
    }

    public String getMessage(String key, String defaultValue) {
        return messagesConfig.getString("messages." + key, defaultValue);
    }

    public PromoCode getPromoCode(String code) {
        return promoCodes.get(code.toLowerCase());
    }

    public boolean hasPromo(String promo) {
        return promoCodes.containsKey(promo.toLowerCase());
    }

    public Map<String, PromoCode> getAllPromos() {
        return Map.copyOf(promoCodes);
    }

    public List<String> getPromoRewards(String promo) {
        PromoCode promoCode = getPromoCode(promo);
        return promoCode != null ? promoCode.getRewards() : null;
    }

    public FileConfiguration getTimeRewardsConfig() {
        return promoConfig;
    }

    public FileConfiguration getPromoCodesConfig() {
        return promoCodesConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        loadConfigs();
    }
}
