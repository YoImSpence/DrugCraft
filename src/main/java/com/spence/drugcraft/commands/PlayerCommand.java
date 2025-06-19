package com.spence.drugcraft.commands;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.PlayerGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final PlayerGUIHandler playerGUIHandler;

    public PlayerCommand(DrugCraft plugin, PlayerGUIHandler playerGUIHandler) {
        this.plugin = plugin;
        this.playerGUIHandler = playerGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("drugcraft.player")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return true;
        }

        playerGUIHandler.openMainMenu(player);
        return true;
    }
}