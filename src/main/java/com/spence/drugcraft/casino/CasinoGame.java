package com.spence.drugcraft.casino;

import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class CasinoGame {
    protected final UUID playerUUID;
    protected double bet;
    protected boolean gameOver;

    public CasinoGame(UUID playerUUID, double bet) {
        this.playerUUID = playerUUID;
        this.bet = bet;
        this.gameOver = false;
    }

    public abstract void start(Player player);
    public abstract void handleAction(Player player, String action);
    public abstract double getPayout();
    public abstract String getResultMessage();
    public abstract String getGameType();

    public boolean isGameOver() {
        return gameOver;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}