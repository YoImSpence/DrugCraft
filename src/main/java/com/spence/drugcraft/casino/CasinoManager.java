package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import org.bukkit.entity.Player;

public class CasinoManager {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;

    public CasinoManager(DrugCraft plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public boolean placeBet(Player player, double amount) {
        if (!economyManager.isEconomyAvailable()) {
            return false;
        }
        return economyManager.withdrawPlayer(player, amount);
    }

    public void awardPrize(Player player, double amount) {
        if (economyManager.isEconomyAvailable()) {
            economyManager.depositPlayer(player, amount);
        }
    }

    public void startGame(Player player, String gameType) {
        // Placeholder: Initialize game state if needed
        plugin.getLogger().info("Started " + gameType + " for player " + player.getName());
    }
}