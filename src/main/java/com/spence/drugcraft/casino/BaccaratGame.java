package com.spence.drugcraft.casino;

import org.bukkit.entity.Player;

import java.util.*;

public class BaccaratGame extends CasinoGame {
    private final List<Card> playerHand;
    private final List<Card> bankerHand;
    private String betType;

    public BaccaratGame(UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.playerHand = new ArrayList<>();
        this.bankerHand = new ArrayList<>();
        this.betType = null;
    }

    @Override
    public void start(Player player) {
        // Player selects bet before cards are dealt
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver) return;

        if (action.startsWith("bet:")) {
            betType = action.substring(4);
        } else if (action.equalsIgnoreCase("deal")) {
            List<Card> deck = createDeck();
            Collections.shuffle(deck);
            playerHand.add(deck.remove(0));
            playerHand.add(deck.remove(0));
            bankerHand.add(deck.remove(0));
            bankerHand.add(deck.remove(0));
            gameOver = true;
        }
    }

    @Override
    public double getPayout() {
        if (!gameOver || betType == null) return 0.0;

        int playerValue = calculateHandValue(playerHand) % 10;
        int bankerValue = calculateHandValue(bankerHand) % 10;

        if (betType.equals("Player") && playerValue > bankerValue) return bet * 2;
        if (betType.equals("Banker") && bankerValue > playerValue) return bet * 1.95;
        if (betType.equals("Tie") && playerValue == bankerValue) return bet * 8;
        return 0.0;
    }

    @Override
    public String getResultMessage() {
        if (!gameOver) return betType == null ? "Place your bet!" : "Bet placed on: " + betType;
        int playerValue = calculateHandValue(playerHand) % 10;
        int bankerValue = calculateHandValue(bankerHand) % 10;
        if (playerValue > bankerValue) return "Player Wins!";
        if (bankerValue > playerValue) return "Banker Wins!";
        return "Tie!";
    }

    @Override
    public String getGameType() {
        return "BACCARAT";
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getBankerHand() {
        return bankerHand;
    }

    public String getBetType() {
        return betType;
    }

    private List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(suit, rank));
            }
        }
        return deck;
    }

    private int calculateHandValue(List<Card> hand) {
        int value = 0;
        for (Card card : hand) {
            value += card.getValue();
        }
        return value;
    }
}