package com.spence.drugcraft.commands;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.handlers.GamesGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamesCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final GamesGUIHandler gamesGUIHandler;

    public GamesCommand(DrugCraft plugin, GamesGUIHandler gamesGUIHandler) {
        this.plugin = plugin;
        this.gamesGUIHandler = gamesGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("drugcraft.games")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return true;
        }

        gamesGUIHandler.openMainMenu(player);
        return true;
    }
}