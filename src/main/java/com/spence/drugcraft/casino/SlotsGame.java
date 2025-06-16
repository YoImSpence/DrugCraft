package com.spence.drugcraft.casino;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class SlotsGame extends CasinoGame {
    private static final List<Material> SLOT_SYMBOLS = Arrays.asList(
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
            Material.IRON_INGOT, Material.COAL, Material.REDSTONE
    );
    private List<Material> results;
    private double payout;

    public SlotsGame(UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.results = new ArrayList<>();
        this.payout = 0.0;
    }

    @Override
    public void start(Player player) {
        results.clear();
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            results.add(SLOT_SYMBOLS.get(random.nextInt(SLOT_SYMBOLS.size())));
        }
        calculatePayout();
        gameOver = true;
    }

    @Override
    public void handleAction(Player player, String action) {
        if (action.equalsIgnoreCase("spin")) {
            start(player); // Restart the game for a new spin
        }
    }

    @Override
    public double getPayout() {
        return payout;
    }

    @Override
    public String getResultMessage() {
        if (payout > 0) {
            return "You Win! Payout: $" + payout;
        }
        return "You Lose.";
    }

    @Override
    public String getGameType() {
        return "SLOTS";
    }

    public List<Material> getResults() {
        return results;
    }

    private void calculatePayout() {
        if (results.get(0) == results.get(1) && results.get(1) == results.get(2)) {
            Material symbol = results.get(0);
            switch (symbol) {
                case DIAMOND:
                    payout = bet * 10; // 10x bet
                    break;
                case EMERALD:
                    payout = bet * 8;
                    break;
                case GOLD_INGOT:
                    payout = bet * 6;
                    break;
                case IRON_INGOT:
                    payout = bet * 4;
                    break;
                case COAL:
                    payout = bet * 2;
                    break;
                case REDSTONE:
                    payout = bet * 1.5;
                    break;
                default:
                    payout = 0.0;
            }
        } else {
            payout = 0.0;
        }
    }
}