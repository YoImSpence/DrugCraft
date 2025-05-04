package com.spence.drugcraft.gui;

import com.spence.drugcraft.utils.MessageUtils;
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
            MessageUtils.sendMessage(sender, "&cThis command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("drugcraft.use")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to use this command.");
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
                    MessageUtils.sendMessage(player, "&cUsage: /drug [buy|sell|give]");
            }
        }
        return true;
    }
}