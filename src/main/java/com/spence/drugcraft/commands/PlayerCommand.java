package com.spence.drugcraft.commands;

import com.spence.drugcraft.handlers.PlayerGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommand implements CommandExecutor {
    private final PlayerGUIHandler playerGUIHandler;

    public PlayerCommand(PlayerGUIHandler playerGUIHandler) {
        this.playerGUIHandler = playerGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }
        Player player = (Player) sender;
        playerGUIHandler.openMainMenu(player);
        return true;
    }
}