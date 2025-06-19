package com.spence.drugcraft.commands;

import com.spence.drugcraft.handlers.AdminGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    private final AdminGUIHandler adminGUIHandler;

    public AdminCommand(AdminGUIHandler adminGUIHandler) {
        this.adminGUIHandler = adminGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("drugcraft.admin")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return true;
        }

        adminGUIHandler.openMainMenu(player);
        return true;
    }
}