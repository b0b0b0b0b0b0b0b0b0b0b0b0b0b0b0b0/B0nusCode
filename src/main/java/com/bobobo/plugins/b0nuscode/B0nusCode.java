package com.bobobo.plugins.b0nuscode;

import com.bobobo.plugins.b0nuscode.cmd.PromoCommand;
import com.bobobo.plugins.b0nuscode.cmd.CommandCompleter;
import com.bobobo.plugins.b0nuscode.cmd.AdminPromoCommand;
import com.bobobo.plugins.b0nuscode.managers.ConfigManager;
import com.bobobo.plugins.b0nuscode.managers.DataManager;
import com.bobobo.plugins.b0nuscode.managers.DatabaseManager;
import com.bobobo.plugins.b0nuscode.managers.PromoManager;
import com.bobobo.plugins.b0nuscode.managers.PromoTimeBonusManager;
import com.bobobo.plugins.b0nuscode.managers.RewardExecutor;
import com.bobobo.plugins.b0nuscode.ut.up.UP;
import org.bukkit.plugin.java.JavaPlugin;

public class B0nusCode extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private DataManager dataManager;
    private RewardExecutor rewardExecutor;
    private PromoManager promoManager;
    private PromoTimeBonusManager promoTimeBonusManager;

    @Override
    public void onEnable() {
        getLogger().info("B0nusCode plugin is starting...");

        initializeManagers();
        registerCommands();
        promoTimeBonusManager.start();

        if (configManager.getPromoCodesConfig().getBoolean("plugin.check-updates", true)) {
            String version = getDescription().getVersion();
            getServer().getScheduler().runTaskLater(this, () -> UP.checkVersion(version), 60L);
        }

        getLogger().info("B0nusCode plugin has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("B0nusCode plugin is shutting down...");

        if (promoTimeBonusManager != null) {
            promoTimeBonusManager.stop();
        }

        if (dataManager != null) {
            dataManager.close();
        }

        getLogger().info("B0nusCode plugin has been disabled!");
    }

    private void initializeManagers() {
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        dataManager = new DataManager(this, databaseManager);
        rewardExecutor = new RewardExecutor(this);
        promoManager = new PromoManager(configManager, dataManager, databaseManager, rewardExecutor);
        promoTimeBonusManager = new PromoTimeBonusManager(this, configManager, databaseManager, rewardExecutor);
    }

    private void registerCommands() {
        getCommand("promo").setExecutor(new PromoCommand(promoManager, configManager));
        getCommand("promo").setTabCompleter(new CommandCompleter(configManager));

        getCommand("adminpromo").setExecutor(new AdminPromoCommand(configManager, promoManager));
        getCommand("adminpromo").setTabCompleter(new CommandCompleter(configManager));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public PromoManager getPromoManager() {
        return promoManager;
    }

    public RewardExecutor getRewardExecutor() {
        return rewardExecutor;
    }
}