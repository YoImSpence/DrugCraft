package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.CasinoGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BlackjackGame extends CasinoGame {
    private final List<Card> playerHand;
    private final List<Card> dealerHand;
    private final List<Card> playerSplitHand;
    private boolean isSplit;
    private boolean isDoubled;
    private final DrugCraft plugin;
    private final EconomyManager economyManager;

    public BlackjackGame(DrugCraft plugin, UUID playerUUID, double bet, EconomyManager economyManager) {
        super(playerUUID, bet);
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.playerSplitHand = new ArrayList<>();
        this.isSplit = false;
        this.isDoubled = false;
    }

    @Override
    public void start(Player player) {
        List<Card> deck = createDeck();
        Collections.shuffle(deck);
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (calculateScore(playerHand) == 21) {
                    gameOver = true;
                }
                new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "BLACKJACK", BlackjackGame.this);
            }
        }.runTaskLater(plugin, 20L);
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver) return;

        List<Card> deck = createDeck();
        Collections.shuffle(deck);

        switch (action.toLowerCase()) {
            case "hit":
                playerHand.add(deck.remove(0));
                if (calculateScore(playerHand) > 21) {
                    gameOver = true;
                }
                new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "BLACKJACK", this);
                break;
            case "stand":
                while (calculateScore(dealerHand) < 17) {
                    dealerHand.add(deck.remove(0));
                }
                gameOver = true;
                new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "BLACKJACK", this);
                break;
            case "double":
                if (playerHand.size() == 2 && economyManager.withdrawPlayer(player, bet)) {
                    isDoubled = true;
                    playerHand.add(deck.remove(0));
                    if (calculateScore(playerHand) <= 21) {
                        while (calculateScore(dealerHand) < 17) {
                            dealerHand.add(deck.remove(0));
                        }
                    }
                    gameOver = true;
                    new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "BLACKJACK", this);
                } else {
                    MessageUtils.sendMessage(player, "casino.insufficient-funds");
                }
                break;
            case "split":
                if (playerHand.size() == 2 && playerHand.get(0).getValue() == playerHand.get(1).getValue() && economyManager.withdrawPlayer(player, bet)) {
                    isSplit = true;
                    playerSplitHand.add(playerHand.remove(1));
                    playerHand.add(deck.remove(0));
                    playerSplitHand.add(deck.remove(0));
                    new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "BLACKJACK", this);
                } else {
                    MessageUtils.sendMessage(player, "casino.insufficient-funds");
                }
                break;
        }
    }

    @Override
    public double getPayout() {
        if (!gameOver) return 0.0;

        int playerScore = calculateScore(playerHand);
        int dealerScore = calculateScore(dealerHand);
        double payout = 0.0;

        if (playerScore == 21 && playerHand.size() == 2 && dealerScore != 21) {
            payout += bet * 2.5; // Blackjack
        } else if (playerScore <= 21 && (playerScore > dealerScore || dealerScore > 21)) {
            payout += bet * 2;
        } else if (playerScore == dealerScore && playerScore <= 21) {
            payout += bet; // Push
        }

        if (isSplit) {
            int splitScore = calculateScore(playerSplitHand);
            if (splitScore == 21 && playerSplitHand.size() == 2 && dealerScore != 21) {
                payout += bet * 2.5;
            } else if (splitScore <= 21 && (splitScore > dealerScore || dealerScore > 21)) {
                payout += bet * 2;
            } else if (splitScore == dealerScore && splitScore <= 21) {
                payout += bet;
            }
        }

        if (isDoubled) {
            payout *= 2;
        }

        return payout;
    }

    @Override
    public String getResultMessage() {
        if (!gameOver) return "Player Score: " + calculateScore(playerHand) + (isSplit ? ", Split Score: " + calculateScore(playerSplitHand) : "");
        int playerScore = calculateScore(playerHand);
        int dealerScore = calculateScore(dealerHand);
        String result = getPayout() > 0 ? "You Win!" : "You Lose!";
        return "Player: " + playerScore + ", Dealer: " + dealerScore + (isSplit ? ", Split: " + calculateScore(playerSplitHand) : "") + " - " + result;
    }

    @Override
    public String getGameType() {
        return "BLACKJACK";
    }

    @Override
    public Map<String, Object> getState() {
        Map<String, Object> state = new HashMap<>();
        state.put("playerHand", new ArrayList<>(playerHand));
        state.put("dealerHand", new ArrayList<>(dealerHand));
        state.put("playerSplitHand", new ArrayList<>(playerSplitHand));
        state.put("isSplit", isSplit);
        state.put("isDoubled", isDoubled);
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

    private int calculateScore(List<Card> hand) {
        int score = 0;
        int aces = 0;
        for (Card card : hand) {
            int value = card.getValue();
            if (value == 11) {
                aces++;
                score += 11;
            } else {
                score += value;
            }
        }
        while (score > 21 && aces > 0) {
            score -= 10;
            aces--;
        }
        return score;
    }
}