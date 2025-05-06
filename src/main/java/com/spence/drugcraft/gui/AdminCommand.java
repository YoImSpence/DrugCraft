package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.PlayerAddictionData;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final AdminGUI adminGUI;

    public AdminCommand(DrugCraft plugin, AdminGUI adminGUI) {
        this.plugin = plugin;
        this.adminGUI = adminGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "&cThis command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("drugcraft.admin")) {
            MessageUtils.sendMessage(player, "&cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            adminGUI.openGiveMenu(player);
        } else if (args[0].equalsIgnoreCase("stats")) {
            displayAddictionStats(player);
        } else {
            MessageUtils.sendMessage(player, "&cUsage: /drugadmin [stats]");
        }
        return true;
    }

    private void displayAddictionStats(Player player) {
        PlayerAddictionData data = plugin.getAddictionManager().getPlayerData(player.getUniqueId());
        if (data.getUsesMap().isEmpty()) {
            MessageUtils.sendMessage(player, "&eYou have not used any drugs yet.");
            return;
        }

        MessageUtils.sendMessage(player, "&6Your Drug Usage Stats:");
        for (String drugId : data.getUsesMap().keySet()) {
            int uses = data.getUses(drugId);
            MessageUtils.sendMessage(player, "&e" + drugId + ": &a" + uses + " uses");
        }
    }
}