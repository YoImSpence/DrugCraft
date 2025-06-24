package com.spence.drugcraft.casino;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class for casino games, defining common functionality and state.
 */
public abstract class CasinoGame {
    protected final UUID playerUUID;
    protected double bet;
    protected boolean gameOver;

    public CasinoGame(UUID playerUUID, double bet) {
        this.playerUUID = playerUUID;
        this.bet = bet;
        this.gameOver = false;
    }

    /**
     * Starts the game, initializing state and prompting player actions.
     * @param player The player starting the game.
     */
    public abstract void start(Player player);

    /**
     * Handles player actions (e.g., bet, hit, spin) and updates game state.
     * @param player The player performing the action.
     * @param action The action command (e.g., "bet:Player", "hit").
     */
    public abstract void handleAction(Player player, String action);

    /**
     * Calculates the payout based on game outcome.
     * @return The payout amount (0 if loss).
     */
    public abstract double getPayout();

    /**
     * Returns the result message for the game outcome.
     * @return The result message (e.g., "Player Wins!").
     */
    public abstract String getResultMessage();

    /**
     * Returns the game type identifier for GUI and state management.
     * @return The game type (e.g., "BACCARAT").
     */
    public abstract String getGameType();

    /**
     * Returns the current game state for GUI display (e.g., hands, reels).
     * @return A map of state data.
     */
    public abstract Map<String, Object> getState();

    public boolean isGameOver() {
        return gameOver;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}