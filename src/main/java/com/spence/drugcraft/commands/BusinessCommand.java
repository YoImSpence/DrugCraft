package com.spence.drugcraft.commands;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BusinessCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final BusinessManager businessManager;

    public BusinessCommand(DrugCraft plugin, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessManager = businessManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("drugcraft.business")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return true;
        }

        plugin.getBusinessGUI().openMainMenu(player);
        return true;
    }
}