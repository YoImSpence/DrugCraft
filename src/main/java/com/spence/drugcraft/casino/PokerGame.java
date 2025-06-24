package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.CasinoGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PokerGame extends CasinoGame {
    private final List<Card> playerHand;
    private final List<Card> aiHand;
    private final List<Card> communityCards;
    private int round;
    private double pot;
    private double currentBet;
    private boolean playerTurn;
    private final DrugCraft plugin;
    private final EconomyManager economyManager;

    public PokerGame(DrugCraft plugin, UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.plugin = plugin;
        this.economyManager = new EconomyManager(null);
        this.playerHand = new ArrayList<>();
        this.aiHand = new ArrayList<>();
        this.communityCards = new ArrayList<>();
        this.round = 0;
        this.pot = bet * 2; // Player and AI ante
        this.currentBet = bet;
        this.playerTurn = true;
    }

    @Override
    public void start(Player player) {
        if (!economyManager.withdrawPlayer(player, bet)) {
            MessageUtils.sendMessage(player, "casino.insufficient-funds");
            gameOver = true;
            return;
        }

        List<Card> deck = createDeck();
        Collections.shuffle(deck);
        playerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        aiHand.add(deck.remove(0));
        aiHand.add(deck.remove(0));

        new BukkitRunnable() {
            @Override
            public void run() {
                new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "POKER", PokerGame.this);
            }
        }.runTaskLater(plugin, 20L);
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver || !playerTurn) return;

        List<Card> deck = createDeck();
        Collections.shuffle(deck);

        switch (action.toLowerCase()) {
            case "call":
                if (economyManager.withdrawPlayer(player, currentBet)) {
                    pot += currentBet;
                    advanceRound(player, deck);
                } else {
                    MessageUtils.sendMessage(player, "casino.insufficient-funds");
                    gameOver = true;
                }
                break;
            case "raise":
                double raiseAmount = currentBet * 2;
                if (economyManager.withdrawPlayer(player, raiseAmount)) {
                    pot += raiseAmount;
                    currentBet = raiseAmount;
                    aiRespond(player, "call");
                    advanceRound(player, deck);
                } else {
                    MessageUtils.sendMessage(player, "casino.insufficient-funds");
                    gameOver = true;
                }
                break;
            case "fold":
                gameOver = true;
                new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "POKER", this);
                break;
        }
    }

    private void aiRespond(Player player, String action) {
        if (action.equals("call")) {
            pot += currentBet;
        }
    }

    private void advanceRound(Player player, List<Card> deck) {
        round++;
        if (round == 1) {
            communityCards.add(deck.remove(0));
            communityCards.add(deck.remove(0));
            communityCards.add(deck.remove(0));
        } else if (round == 2) {
            communityCards.add(deck.remove(0));
        } else if (round == 3) {
            communityCards.add(deck.remove(0));
        } else if (round == 4) {
            gameOver = true;
        }
        playerTurn = true;
        new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "POKER", this);
    }

    @Override
    public double getPayout() {
        if (!gameOver) return 0.0;

        HandRank playerRank = evaluateHand(playerHand, communityCards);
        HandRank aiRank = evaluateHand(aiHand, communityCards);

        if (playerRank.rank > aiRank.rank) {
            return pot;
        } else if (playerRank.rank == aiRank.rank) {
            // Compare high cards
            return playerRank.highCard > aiRank.highCard ? pot : 0.0;
        }
        return 0.0;
    }

    @Override
    public String getResultMessage() {
        if (!gameOver) return "Round: " + (round == 0 ? "Preflop" : round == 1 ? "Flop" : round == 2 ? "Turn" : "River") + ", Pot: $" + pot;
        HandRank playerRank = evaluateHand(playerHand, communityCards);
        HandRank aiRank = evaluateHand(aiHand, communityCards);
        String result = getPayout() > 0 ? "You Win!" : "You Lose!";
        return "Your Hand: " + playerRank.name + ", AI Hand: " + aiRank.name + " - " + result;
    }

    @Override
    public String getGameType() {
        return "POKER";
    }

    @Override
    public Map<String, Object> getState() {
        Map<String, Object> state = new HashMap<>();
        state.put("playerHand", new ArrayList<>(playerHand));
        state.put("communityCards", new ArrayList<>(communityCards));
        state.put("pot", pot);
        state.put("round", round);
        return state;
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

    private static class HandRank {
        int rank;
        int highCard;
        String name;

        HandRank(int rank, int highCard, String name) {
            this.rank = rank;
            this.highCard = highCard;
            this.name = name;
        }
    }

    private HandRank evaluateHand(List<Card> hand, List<Card> community) {
        List<Card> allCards = new ArrayList<>(hand);
        allCards.addAll(community);
        allCards.sort((c1, c2) -> c2.getValue() - c1.getValue());

        // Simplified poker hand evaluation
        if (isRoyalFlush(allCards)) return new HandRank(10, getHighCard(allCards), "Royal Flush");
        if (isStraightFlush(allCards)) return new HandRank(9, getHighCard(allCards), "Straight Flush");
        if (isFourOfAKind(allCards)) return new HandRank(8, getHighCard(allCards), "Four of a Kind");
        if (isFullHouse(allCards)) return new HandRank(7, getHighCard(allCards), "Full House");
        if (isFlush(allCards)) return new HandRank(6, getHighCard(allCards), "Flush");
        if (isStraight(allCards)) return new HandRank(5, getHighCard(allCards), "Straight");
        if (isThreeOfAKind(allCards)) return new HandRank(4, getHighCard(allCards), "Three of a Kind");
        if (isTwoPair(allCards)) return new HandRank(3, getHighCard(allCards), "Two Pair");
        if (isPair(allCards)) return new HandRank(2, getHighCard(allCards), "Pair");
        return new HandRank(1, getHighCard(allCards), "High Card");
    }

    private boolean isRoyalFlush(List<Card> cards) {
        return isStraightFlush(cards) && cards.get(0).getValue() == 14;
    }

    private boolean isStraightFlush(List<Card> cards) {
        return isFlush(cards) && isStraight(cards);
    }

    private boolean isFourOfAKind(List<Card> cards) {
        Map<Integer, Integer> count = new HashMap<>();
        for (Card card : cards) {
            count.merge(card.getValue(), 1, Integer::sum);
        }
        return count.values().contains(4);
    }

    private boolean isFullHouse(List<Card> cards) {
        Map<Integer, Integer> count = new HashMap<>();
        for (Card card : cards) {
            count.merge(card.getValue(), 1, Integer::sum);
        }
        return count.values().contains(3) && count.values().contains(2);
    }

    private boolean isFlush(List<Card> cards) {
        Map<String, Integer> suitCount = new HashMap<>();
        for (Card card : cards) {
            suitCount.merge(card.getSuit(), 1, Integer::sum);
        }
        return suitCount.values().stream().anyMatch(c -> c >= 5);
    }

    private boolean isStraight(List<Card> cards) {
        Set<Integer> values = new HashSet<>();
        for (Card card : cards) {
            values.add(card.getValue());
        }
        int min = values.stream().min(Integer::compare).orElse(0);
        return values.contains(min) && values.contains(min + 1) && values.contains(min + 2) &&
                values.contains(min + 3) && values.contains(min + 4);
    }

    private boolean isThreeOfAKind(List<Card> cards) {
        Map<Integer, Integer> count = new HashMap<>();
        for (Card card : cards) {
            count.merge(card.getValue(), 1, Integer::sum);
        }
        return count.values().contains(3);
    }

    private boolean isTwoPair(List<Card> cards) {
        Map<Integer, Integer> count = new HashMap<>();
        for (Card card : cards) {
            count.merge(card.getValue(), 1, Integer::sum);
        }
        return count.values().stream().filter(c -> c == 2).count() >= 2;
    }

    private boolean isPair(List<Card> cards) {
        Map<Integer, Integer> count = new HashMap<>();
        for (Card card : cards) {
            count.merge(card.getValue(), 1, Integer::sum);
        }
        return count.values().contains(2);
    }

    private int getHighCard(List<Card> cards) {
        return cards.stream().mapToInt(Card::getValue).max().orElse(0);
    }
}