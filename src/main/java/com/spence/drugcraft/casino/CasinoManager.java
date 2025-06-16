package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CasinoManager implements Listener {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, CasinoGame> activeGames;

    public CasinoManager(DrugCraft plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.activeGames = new HashMap<>();
    }

    public void startGame(Player player, String gameType, double bet) {
        UUID playerUUID = player.getUniqueId();
        plugin.getLogger().info("Starting casino game: " + gameType + " for player " + player.getName() + " with bet $" + bet);
        if (!economyManager.isEconomyAvailable()) {
            plugin.getLogger().warning("Economy unavailable for casino game start by player " + player.getName());
            return;
        }
        Economy economy = economyManager.getEconomy();
        if (!economy.has(player, bet)) {
            plugin.getLogger().info("Player " + player.getName() + " lacks funds ($" + bet + ") for game " + gameType);
            return;
        }
        try {
            CasinoGame game;
            switch (gameType.toUpperCase()) {
                case "BLACKJACK":
                    game = new BlackjackGame(playerUUID, bet);
                    break;
                case "SLOTS":
                    game = new SlotsGame(playerUUID, bet);
                    break;
                case "POKER":
                    game = new PokerGame(playerUUID, bet);
                    break;
                case "ROULETTE":
                    game = new RouletteGame(playerUUID, bet);
                    break;
                case "BACCARAT":
                    game = new BaccaratGame(playerUUID, bet);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown game type: " + gameType);
            }
            activeGames.put(playerUUID, game);
            game.start(player);
            plugin.getLogger().info("Started " + gameType + " game for player " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start " + gameType + " game for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CasinoGame getGame(UUID playerUUID) {
        return activeGames.get(playerUUID);
    }

    public void endGame(UUID playerUUID) {
        activeGames.remove(playerUUID);
        plugin.getLogger().info("Ended game for player UUID: " + playerUUID);
    }

    public void saveCasinoData() {
        // No persistent data to save currently
        plugin.getLogger().info("Saved casino data (no persistent data)");
    }
}