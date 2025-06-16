package com.spence.drugcraft.casino;

import org.bukkit.entity.Player;

import java.util.*;

public class PokerGame extends CasinoGame {
    private final List<Card> playerHand;
    private final List<Card> dealerHand;
    private final List<Card> communityCards;
    private int round;
    private boolean playerFolded;

    public PokerGame(UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.communityCards = new ArrayList<>();
        this.round = 0;
        this.playerFolded = false;
    }

    @Override
    public void start(Player player) {
        List<Card> deck = createDeck();
        Collections.shuffle(deck);
        playerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        round = 1;
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver) return;

        if (action.equalsIgnoreCase("fold")) {
            playerFolded = true;
            gameOver = true;
            return;
        }

        if (action.equalsIgnoreCase("call")) {
            List<Card> deck = createDeck();
            Collections.shuffle(deck);
            if (round == 1) {
                communityCards.add(deck.remove(0));
                communityCards.add(deck.remove(0));
                communityCards.add(deck.remove(0));
                round = 2;
            } else if (round == 2) {
                communityCards.add(deck.remove(0));
                round = 3;
            } else if (round == 3) {
                communityCards.add(deck.remove(0));
                round = 4;
                gameOver = true;
            }
        }
    }

    @Override
    public double getPayout() {
        if (!gameOver) return 0.0;
        if (playerFolded) return 0.0;

        int playerScore = evaluateHand(playerHand, communityCards);
        int dealerScore = evaluateHand(dealerHand, communityCards);
        if (playerScore > dealerScore) return bet * 2;
        return 0.0;
    }

    @Override
    public String getResultMessage() {
        if (!gameOver) return "Round " + round + ": " + (round == 1 ? "Pre-Flop" : round == 2 ? "Flop" : round == 3 ? "Turn" : "River");
        if (playerFolded) return "You Folded! You Lose.";
        int playerScore = evaluateHand(playerHand, communityCards);
        int dealerScore = evaluateHand(dealerHand, communityCards);
        if (playerScore > dealerScore) return "You Win!";
        return "You Lose.";
    }

    @Override
    public String getGameType() {
        return "POKER";
    }

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public List<Card> getDealerHand() {
        return dealerHand;
    }

    public List<Card> getCommunityCards() {
        return communityCards;
    }

    public int getRound() {
        return round;
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

    private int evaluateHand(List<Card> hand, List<Card> community) {
        List<Card> allCards = new ArrayList<>(hand);
        allCards.addAll(community);
        List<Integer> values = new ArrayList<>();
        for (Card card : allCards) {
            int value = card.getValue();
            values.add(value);
            if (card.toString().contains("Ace")) {
                values.add(1); // Ace-low
            }
        }
        return Collections.max(values);
    }
}