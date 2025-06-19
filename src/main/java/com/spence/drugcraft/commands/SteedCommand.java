package com.spence.drugcraft.commands;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.vehicles.VehicleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SteedsCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final VehicleManager vehicleManager;

    public SteedsCommand(DrugCraft plugin, VehicleManager vehicleManager) {
        this.plugin = plugin;
        this.vehicleManager = vehicleManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("drugcraft.vehicle")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return true;
        }

        plugin.getVehicleGUIHandler().openMainMenu(player);
        return true;
    }
}