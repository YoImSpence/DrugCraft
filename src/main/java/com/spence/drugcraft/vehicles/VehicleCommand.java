package com.spence.drugcraft.vehicles;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VehicleCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final VehicleManager vehicleManager;
    private final VehicleGUI vehicleGUI;

    public VehicleCommand(DrugCraft plugin, VehicleManager vehicleManager) {
        this.plugin = plugin;
        this.vehicleManager = vehicleManager;
        this.vehicleGUI = new VehicleGUI(plugin, vehicleManager, plugin.getDataManager());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            plugin.getLogger().info("Steed command attempted by non-player: " + sender.getName());
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("drugcraft.vehicle")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            plugin.getLogger().info("Player " + player.getName() + " attempted steed command without permission");
            return true;
        }

        plugin.getLogger().info("Player " + player.getName() + " executed steed command");
        try {
            vehicleGUI.openMainMenu(player);
            plugin.getLogger().info("Opened Steed GUI for player " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Error opening Steed GUI for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            MessageUtils.sendMessage(player, "vehicle.error");
        }
        return true;
    }
}