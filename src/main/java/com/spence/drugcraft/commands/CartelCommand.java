package com.spence.drugcraft.commands;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CartelCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelCommand(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("drugcraft.cartel")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return true;
        }

        plugin.getCartelGUIHandler().openMainMenu(player);
        return true;
    }
}