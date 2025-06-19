package com.spence.drugcraft.commands;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CasinoCommand implements CommandExecutor {
    private final DrugCraft plugin;

    public CasinoCommand(DrugCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("drugcraft.casino")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return true;
        }

        MessageUtils.sendMessage(player, "casino.use-region");
        return true;
    }
}