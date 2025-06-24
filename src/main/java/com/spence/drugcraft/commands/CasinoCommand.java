package com.spence.drugcraft.commands;

import com.spence.drugcraft.handlers.CasinoGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CasinoCommand implements CommandExecutor {
    private final CasinoGUIHandler casinoGUIHandler;

    public CasinoCommand(CasinoGUIHandler casinoGUIHandler) {
        this.casinoGUIHandler = casinoGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }
        Player player = (Player) sender;
        casinoGUIHandler.openMainMenu(player);
        return true;
    }
}