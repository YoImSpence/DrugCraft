package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CasinoManager {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, CasinoGame> activeGames = new HashMap<>();

    public CasinoManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.economyManager = new EconomyManager(null);
    }

    public CasinoGame startGame(UUID playerUUID, String gameType, double bet) {
        if (activeGames.containsKey(playerUUID)) {
            MessageUtils.sendMessage(plugin.getServer().getPlayer(playerUUID), "casino.no-active-game");
            return null;
        }
        CasinoGame game = switch (gameType.toUpperCase()) {
            case "BACCARAT" -> new BaccaratGame(plugin, playerUUID, bet);
            case "BLACKJACK" -> new BlackjackGame(plugin, playerUUID, bet, economyManager);
            case "POKER" -> new PokerGame(plugin, playerUUID, bet);
            case "ROULETTE" -> new RouletteGame(plugin, playerUUID, bet);
            case "SLOTS" -> new SlotsGame(plugin, playerUUID, bet);
            default -> null;
        };
        if (game != null) {
            activeGames.put(playerUUID, game);
            game.start(plugin.getServer().getPlayer(playerUUID));
        }
        return game;
    }

    public CasinoGame getActiveGame(UUID playerUUID) {
        return activeGames.get(playerUUID);
    }

    public void endGame(UUID playerUUID) {
        CasinoGame game = activeGames.get(playerUUID);
        if (game != null) {
            double payout = game.getPayout();
            if (payout > 0) {
                economyManager.depositPlayer(plugin.getServer().getPlayer(playerUUID), payout);
                MessageUtils.sendMessage(plugin.getServer().getPlayer(playerUUID), "casino." + game.getGameType().toLowerCase() + "-result", "result", "You Win!", "reward", String.valueOf(payout));
            } else {
                MessageUtils.sendMessage(plugin.getServer().getPlayer(playerUUID), "casino." + game.getGameType().toLowerCase() + "-result", "result", "You Lose!");
            }
            activeGames.remove(playerUUID);
        }
    }
}