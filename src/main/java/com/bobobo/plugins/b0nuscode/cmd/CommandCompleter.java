package com.bobobo.plugins.b0nuscode.cmd;

import com.bobobo.plugins.b0nuscode.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandCompleter implements TabCompleter {
    private final ConfigManager configManager;

    public CommandCompleter(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("promo")) {
            return completions;
        }
        else if (command.getName().equalsIgnoreCase("adminpromo")) {
            if (!sender.hasPermission("b0nuscode.admin")) {
                return completions;
            }

            if (args.length == 1) {
                String[] subCommands = {"list", "add", "remove", "reset", "reload", "stats"};
                String input = args[0].toLowerCase();

                for (String subCommand : subCommands) {
                    if (subCommand.startsWith(input)) {
                        completions.add(subCommand);
                    }
                }
            }
            else if (args.length == 2) {
                String subCommand = args[0].toLowerCase();
                String input = args[1].toLowerCase();

                switch (subCommand) {
                    case "remove":
                        for (String promo : configManager.getAllPromos().keySet()) {
                            if (promo.toLowerCase().startsWith(input)) {
                                completions.add(promo);
                            }
                        }
                        break;
                    case "reset":
                        for (org.bukkit.entity.Player player : sender.getServer().getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(input)) {
                                completions.add(player.getName());
                            }
                        }
                        break;
                }
            }
            else if (args.length >= 3 && args[0].equalsIgnoreCase("add")) {
                if (args.length == 3) {
                    String[] exampleRewards = {
                            "eco give %player% 10000",
                            "kit titan %player%",
                            "cases give %player% donate 1",
                            "give %player% diamond 5"
                    };

                    String input = args[2].toLowerCase();
                    for (String reward : exampleRewards) {
                        if (reward.toLowerCase().startsWith(input)) {
                            completions.add(reward);
                        }
                    }
                }
            }
        }

        return completions;
    }
}
