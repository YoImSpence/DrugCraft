package com.spence.drugcraft.gui;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DrugCommand implements CommandExecutor {
    private final DrugGUI drugGUI;

    public DrugCommand(DrugGUI drugGUI) {
        this.drugGUI = drugGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("drugcraft.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            drugGUI.openMainMenu(player);
        } else {
            switch (args[0].toLowerCase()) {
                case "buy":
                    drugGUI.openBuyMenu(player);
                    break;
                case "sell":
                    drugGUI.openSellMenu(player);
                    break;
                case "give":
                    drugGUI.openGiveMenu(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Usage: /drug [buy|sell|give]");
            }
        }
        return true;
    }
}