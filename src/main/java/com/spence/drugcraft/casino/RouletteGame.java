package com.spence.drugcraft.casino;

import org.bukkit.entity.Player;

import java.util.*;

public class RouletteGame extends CasinoGame {
    private final List<String> bets; // e.g., "Red", "Black", "Odd", "Even", "Number:17"
    private int resultNumber;
    private String resultColor;
    private boolean resultOdd;

    public RouletteGame(UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.bets = new ArrayList<>();
        this.resultNumber = -1;
    }

    @Override
    public void start(Player player) {
        // Player selects bets via GUI before spinning
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver) return;

        if (action.startsWith("bet:")) {
            String betType = action.substring(4); // e.g., "Red", "Number:17"
            bets.add(betType);
        } else if (action.equalsIgnoreCase("spin")) {
            Random random = new Random();
            resultNumber = random.nextInt(37); // 0 to 36
            resultColor = (resultNumber == 0) ? "Green" : (resultNumber % 2 == 0) ? "Red" : "Black";
            resultOdd = resultNumber % 2 != 0;
            gameOver = true;
        }
    }

    @Override
    public double getPayout() {
        if (!gameOver) return 0.0;
        double totalPayout = 0.0;
        for (String betType : bets) {
            if (betType.startsWith("Number:")) {
                int number = Integer.parseInt(betType.split(":")[1]);
                if (number == resultNumber) totalPayout += this.bet * 35; // 35:1 payout
            } else if (betType.equals("Red") && resultColor.equals("Red")) {
                totalPayout += this.bet * 2; // 1:1 payout
            } else if (betType.equals("Black") && resultColor.equals("Black")) {
                totalPayout += this.bet * 2;
            } else if (betType.equals("Odd") && resultOdd) {
                totalPayout += this.bet * 2;
            } else if (betType.equals("Even") && !resultOdd && resultNumber != 0) {
                totalPayout += this.bet * 2;
            }
        }
        return totalPayout;
    }

    @Override
    public String getResultMessage() {
        if (!gameOver) return "Place your bets!";
        return "Result: " + resultNumber + " (" + resultColor + ", " + (resultOdd ? "Odd" : "Even") + ")";
    }

    @Override
    public String getGameType() {
        return "ROULETTE";
    }

    public List<String> getBets() {
        return bets;
    }

    public int getResultNumber() {
        return resultNumber;
    }

    public String getResultColor() {
        return resultColor;
    }

    public boolean isResultOdd() {
        return resultOdd;
    }
}