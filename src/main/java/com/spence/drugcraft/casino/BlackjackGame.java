package com.spence.drugcraft.casino;

import org.bukkit.entity.Player;

import java.util.*;

public class BlackjackGame extends CasinoGame {
    private final List<Card> playerHand;
    private final List<Card> dealerHand;
    private boolean playerBust;

    public BlackjackGame(UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.playerBust = false;
    }

    @Override
    public void start(Player player) {
        List<Card> deck = createDeck();
        Collections.shuffle(deck);
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver) return;

        if (action.equalsIgnoreCase("hit")) {
            List<Card> deck = createDeck();
            Collections.shuffle(deck);
            playerHand.add(deck.get(0));
            if (calculateHandValue(playerHand) > 21) {
                playerBust = true;
                gameOver = true;
            }
        } else if (action.equalsIgnoreCase("stand")) {
            List<Card> deck = createDeck();
            Collections.shuffle(deck);
            while (calculateHandValue(dealerHand) < 17) {
                dealerHand.add(deck.remove(0));
            }
            gameOver = true;
        }
    }

    @Override
    public double getPayout() {
        if (!gameOver) return 0.0;
        if (playerBust) return 0.0;

        int playerValue = calculateHandValue(playerHand);
        int dealerValue = calculateHandValue(dealerHand);

        if (playerValue > 21) return 0.0;
        if (dealerValue > 21) return bet * 2; // Player wins 2x bet
        if (playerValue > dealerValue) return bet * 2;
        if (playerValue == dealerValue) return bet; // Push, return bet
        return 0.0;
    }

    @Override
    public String getResultMessage() {
        if (!gameOver) return "Game in Progress";
        if (playerBust) return "Bust! You Lose.";
        int playerValue = calculateHandValue(playerHand);
        int dealerValue = calculateHandValue(dealerHand);
        if (playerValue > 21) return "Bust! You Lose.";
        if (dealerValue > 21) return "Dealer Bust! You Win!";
        if (playerValue > dealerValue) return "You Win!";
        if (playerValue == dealerValue) return "Push!";
        return "You Lose.";
    }

    @Override
    public String getGameType() {
        return "BLACKJACK";
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getDealerHand() {
        return dealerHand;
    }

    public boolean isPlayerBust() {
        return playerBust;
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
        int aces = 0;
        for (Card card : hand) {
            value += card.getValue();
            if (card.getValue() == 11) aces++;
        }
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }
        return value;
    }
}