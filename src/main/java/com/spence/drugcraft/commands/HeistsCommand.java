package com.spence.drugcraft.commands;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.HeistGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HeistsCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final HeistGUI heistGUI;
    private final EconomyManager economyManager;

    public HeistsCommand() {
        this.plugin = DrugCraft.getInstance();
        this.heistGUI = new HeistGUI(plugin);
        this.economyManager = new EconomyManager(null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        heistGUI.openMainMenu(player);
        return true;
    }

    public void startHeist(Player player, String heistType) {
        double reward = heistType.equals("Bank Heist") ? 5000.0 : 10000.0;
        double successChance = heistType.equals("Bank Heist") ? 0.7 : 0.5;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Math.random() < successChance) {
                    economyManager.depositPlayer(player, reward);
                    MessageUtils.sendMessage(player, "heist.started", "reward", String.valueOf(reward));
                } else {
                    MessageUtils.sendMessage(player, "heist.started", "result", "Failed");
                }
            }
        }.runTaskLater(plugin, 60L);
    }
}