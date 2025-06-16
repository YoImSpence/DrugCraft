package com.spence.drugcraft.levels;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelsCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final LevelsGUI levelsGUI;

    public LevelsCommand(DrugCraft plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.levelsGUI = new LevelsGUI(plugin, dataManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            plugin.getLogger().info("Levels command attempted by non-player: " + sender.getName());
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("drugcraft.levels")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            plugin.getLogger().info("Player " + player.getName() + " attempted levels command without permission");
            return true;
        }

        plugin.getLogger().info("Player " + player.getName() + " executed levels command");
        try {
            levelsGUI.openMainMenu(player);
            plugin.getLogger().info("Opened Levels GUI for player " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Error opening Levels GUI for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            MessageUtils.sendMessage(player, "general.error");
        }
        return true;
    }
}