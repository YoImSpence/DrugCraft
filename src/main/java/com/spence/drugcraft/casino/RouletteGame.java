package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.CasinoGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RouletteGame extends CasinoGame {
    private String betType;
    private int betNumber;
    private List<Integer> betNumbers; // For inside bets (e.g., split, street)
    private int resultNumber;
    private final Random random;
    private final DrugCraft plugin;

    public RouletteGame(DrugCraft plugin, UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.plugin = plugin;
        this.random = new Random();
        this.betType = null;
        this.betNumber = -1;
        this.betNumbers = new ArrayList<>();
        this.resultNumber = -1;
    }

    @Override
    public void start(Player player) {
        new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "ROULETTE", this);
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver) return;

        if (action.startsWith("bet:")) {
            String[] parts = action.substring(4).split(":");
            betType = parts[0];
            betNumbers.clear();
            if (betType.equals("Number")) {
                try {
                    betNumber = Integer.parseInt(parts[1]);
                    if (betNumber < 0 || betNumber > 36) throw new NumberFormatException();
                    betNumbers.add(betNumber);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, "casino.invalid-roulette-number");
                    return;
                }
            } else if (betType.equals("Split")) {
                // Example: Split bet on two adjacent numbers
                betNumbers.add(Integer.parseInt(parts[1]));
                betNumbers.add(Integer.parseInt(parts[2]));
            } else if (betType.equals("Street")) {
                // Example: Street bet on three numbers in a row
                int start = Integer.parseInt(parts[1]);
                betNumbers.add(start);
                betNumbers.add(start + 1);
                betNumbers.add(start + 2);
            } else {
                betNumber = -1;
            }
            MessageUtils.sendMessage(player, getResultMessage());
            new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "ROULETTE", this);
        } else if (action.equalsIgnoreCase("spin") && betType != null) {
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (ticks < 40) {
                        new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "ROULETTE", RouletteGame.this);
                        ticks += 10;
                    } else {
                        resultNumber = random.nextInt(37); // 0-36
                        gameOver = true;
                        new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "ROULETTE", RouletteGame.this);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }
    }

    @Override
    public double getPayout() {
        if (!gameOver || betType == null) return 0.0;

        String resultColor = resultNumber == 0 ? "Green" :
                Arrays.asList(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).contains(resultNumber) ? "Red" : "Black";

        if (betType.equals("Number") && betNumbers.contains(resultNumber)) {
            return bet * 35;
        } else if (betType.equals("Split") && betNumbers.contains(resultNumber)) {
            return bet * 17;
        } else if (betType.equals("Street") && betNumbers.contains(resultNumber)) {
            return bet * 11;
        } else if (betType.equals(resultColor)) {
            return bet * 2;
        }
        return 0.0;
    }

    @Override
    public String getResultMessage() {
        if (!gameOver) return betType == null ? "Place your bet!" : "Bet placed on: " + (betType.equals("Number") ? betNumber : betType);
        String color = resultNumber == 0 ? "Green" :
                Arrays.asList(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).contains(resultNumber) ? "Red" : "Black";
        String result = getPayout() > 0 ? "You Win!" : "You Lose!";
        return "Result: " + resultNumber + " (" + color + ") - " + result;
    }

    @Override
    public String getGameType() {
        return "ROULETTE";
    }

    @Override
    public Map<String, Object> getState() {
        Map<String, Object> state = new HashMap<>();
        state.put("betType", betType);
        state.put("betNumber", betNumber);
        state.put("resultNumber", resultNumber);
        return state;
    }
}