package com.spence.drugcraft.commands;

import com.spence.drugcraft.handlers.BusinessGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BusinessCommand implements CommandExecutor {
    private final BusinessGUIHandler businessGUIHandler;

    public BusinessCommand(BusinessGUIHandler businessGUIHandler) {
        this.businessGUIHandler = businessGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }
        Player player = (Player) sender;
        businessGUIHandler.openMainMenu(player);
        return true;
    }
}