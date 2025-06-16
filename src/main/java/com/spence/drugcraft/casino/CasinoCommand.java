package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CasinoCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final CasinoGUI casinoGUI;

    public CasinoCommand(DrugCraft plugin) {
        this.plugin = plugin;
        this.casinoGUI = plugin.getCasinoGUI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }
        Player player = (Player) sender;
        casinoGUI.openMainMenu(player);
        return true;
    }
}