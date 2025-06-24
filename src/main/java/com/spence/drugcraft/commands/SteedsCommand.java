package com.spence.drugcraft.commands;

import com.spence.drugcraft.handlers.SteedGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SteedsCommand implements CommandExecutor {
    private final SteedGUIHandler steedGUIHandler;

    public SteedsCommand(SteedGUIHandler steedGUIHandler) {
        this.steedGUIHandler = steedGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }
        Player player = (Player) sender;
        steedGUIHandler.openMainMenu(player);
        return true;
    }
}