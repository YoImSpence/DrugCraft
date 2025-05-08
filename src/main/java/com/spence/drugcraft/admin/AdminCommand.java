package com.spence.drugcraft.admin;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final CropManager cropManager;
    private final AdminGUI adminGUI;

    public AdminCommand(DrugCraft plugin, CropManager cropManager, AdminGUI adminGUI) {
        this.plugin = plugin;
        this.cropManager = cropManager;
        this.adminGUI = adminGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.color("&#FF4040This command can only be used by players!"));
            return true;
        }
        Player player = (Player) sender;
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.admin")) {
            player.sendMessage(MessageUtils.color("&#FF4040You do not have permission to use this command!"));
            return true;
        }
        if (args.length == 0) {
            adminGUI.openMainMenu(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("clear")) {
            cropManager.clearAllCrops(player);
            return true;
        }
        player.sendMessage(MessageUtils.color("&#FF4040Usage: /drugadmin [clear]"));
        return true;
    }
}