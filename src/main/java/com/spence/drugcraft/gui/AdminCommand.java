package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final DrugManager drugManager;

    public AdminCommand(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.color("&cThis command can only be used by players."));
            return true;
        }
        Player player = (Player) sender;
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.admin")) {
            player.sendMessage(MessageUtils.color("&cYou do not have permission to use this command."));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("clearcrops")) {
            plugin.getCropManager().clearAllCrops(player);
            return true;
        }
        new AdminGUI(plugin, drugManager).openGUI(player);
        return true;
    }
}