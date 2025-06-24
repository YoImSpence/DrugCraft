package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.CasinoGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BaccaratGame extends CasinoGame {
    private final List<Card> playerHand;
    private final List<Card> bankerHand;
    private String betType;
    private final DrugCraft plugin;

    public BaccaratGame(DrugCraft plugin, UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.plugin = plugin;
        this.playerHand = new ArrayList<>();
        this.bankerHand = new ArrayList<>();
        this.betType = null;
    }

    @Override
    public void start(Player player) {
        MessageUtils.sendMessage(player, "casino.baccarat-info", "message", "Place your bet: Player, Banker, or Tie");
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver) return;

        if (action.startsWith("bet:")) {
            betType = action.substring(4);
            MessageUtils.sendMessage(player, getResultMessage());
            new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "BACCARAT", this);
        } else if (action.equalsIgnoreCase("deal") && betType != null) {
            new BukkitRunnable() {
                int step = 0;
                List<Card> deck = createDeck();
                @Override
                public void run() {
                    if (step == 0) {
                        Collections.shuffle(deck);
                        playerHand.add(deck.remove(0));
                        MessageUtils.sendMessage(player, "casino.baccarat-info", "message", "Dealing Player card 1");
                    } else if (step == 1) {
                        bankerHand.add(deck.remove(0));
                        MessageUtils.sendMessage(player, "casino.baccarat-info", "message", "Dealing Banker card 1");
                    } else if (step == 2) {
                        playerHand.add(deck.remove(0));
                        MessageUtils.sendMessage(player, "casino.baccarat-info", "message", "Dealing Player card 2");
                    } else if (step == 3) {
                        bankerHand.add(deck.remove(0));
                        MessageUtils.sendMessage(player, "casino.baccarat-info", "message", "Dealing Banker card 2");
                        applyThirdCardRules(deck);
                        gameOver = true;
                        new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "BACCARAT", BaccaratGame.this);
                        cancel();
                    }
                    step++;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    private void applyThirdCardRules(List<Card> deck) {
        int playerValue = calculateHandValue(playerHand) % 10;
        if (playerValue <= 5 && playerHand.size() < 3) {
            playerHand.add(deck.remove(0));
            playerValue = calculateHandValue(playerHand) % 10;
        }

        int bankerValue = calculateHandValue(bankerHand) % 10;
        if (bankerHand.size() < 3) {
            if (bankerValue <= 2) {
                bankerHand.add(deck.remove(0));
            } else if (bankerValue == 3 && (playerHand.size() < 3 || playerHand.get(2).getValue() != 8)) {
                bankerHand.add(deck.remove(0));
            } else if (bankerValue == 4 && playerHand.size() >= 3 && List.of(2, 3, 4, 5, 6, 7).contains(playerHand.get(2).getValue())) {
                bankerHand.add(deck.remove(0));
            } else if (bankerValue == 5 && playerHand.size() >= 3 && List.of(4, 5, 6, 7).contains(playerHand.get(2).getValue())) {
                bankerHand.add(deck.remove(0));
            } else if (bankerValue == 6 && playerHand.size() >= 3 && List.of(6, 7).contains(playerHand.get(2).getValue())) {
                bankerHand.add(deck.remove(0));
            }
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
        return "Player: " + playerValue + ", Banker: " + bankerValue + " - " +
                (playerValue > bankerValue ? "Player Wins!" : bankerValue > playerValue ? "Banker Wins!" : "Tie!");
    }

    @Override
    public String getGameType() {
        return "BACCARAT";
    }

    @Override
    public Map<String, Object> getState() {
        Map<String, Object> state = new HashMap<>();
        state.put("playerHand", new ArrayList<>(playerHand));
        state.put("bankerHand", new ArrayList<>(bankerHand));
        state.put("betType", betType);
        return state;
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
            int cardValue = card.getValue();
            if (cardValue > 9) value += 0; // Face cards count as 0 in baccarat
            else value += cardValue;
        }
        return value % 10;
    }
}