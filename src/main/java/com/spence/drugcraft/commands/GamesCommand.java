package com.spence.drugcraft.commands;

import com.spence.drugcraft.handlers.GamesGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamesCommand implements CommandExecutor {
    private final GamesGUIHandler gamesGUIHandler;

    public GamesCommand(GamesGUIHandler gamesGUIHandler) {
        this.gamesGUIHandler = gamesGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }
        Player player = (Player) sender;
        gamesGUIHandler.openMainMenu(player);
        return true;
    }
}