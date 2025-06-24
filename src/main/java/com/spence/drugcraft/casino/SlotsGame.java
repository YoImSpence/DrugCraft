package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.CasinoGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SlotsGame extends CasinoGame {
    private final String[] symbols = {"Cherry", "Lemon", "Seven", "Bar", "Diamond"};
    private String[] reels;
    private final Random random;
    private final DrugCraft plugin;

    public SlotsGame(DrugCraft plugin, UUID playerUUID, double bet) {
        super(playerUUID, bet);
        this.plugin = plugin;
        this.random = new Random();
        this.reels = new String[]{"?", "?", "?"};
    }

    @Override
    public void start(Player player) {
        new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "SLOTS", this);
    }

    @Override
    public void handleAction(Player player, String action) {
        if (gameOver || !action.equalsIgnoreCase("spin")) return;

        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (step < 3) {
                    reels[step] = symbols[random.nextInt(symbols.length)];
                    new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "SLOTS", SlotsGame.this);
                    step++;
                } else {
                    gameOver = true;
                    new CasinoGUI(plugin, new CasinoManager(plugin)).openGameMenu(player, "SLOTS", SlotsGame.this);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public double getPayout() {
        if (!gameOver) return 0.0;

        if (reels[0].equals(reels[1]) && reels[1].equals(reels[2])) {
            return switch (reels[0]) {
                case "Diamond" -> bet * 50;
                case "Seven" -> bet * 20;
                case "Bar" -> bet * 10;
                case "Cherry" -> bet * 5;
                case "Lemon" -> bet * 3;
                default -> 0.0;
            };
        }
        return 0.0;
    }

    @Override
    public String getResultMessage() {
        if (!gameOver) return "Click Spin to play!";
        String result = getPayout() > 0 ? "You Win!" : "You Lose!";
        return "Result: " + reels[0] + " | " + reels[1] + " | " + reels[2] + " - " + result;
    }

    @Override
    public String getGameType() {
        return "SLOTS";
    }

    @Override
    public Map<String, Object> getState() {
        Map<String, Object> state = new HashMap<>();
        state.put("reels", reels.clone());
        return state;
    }
}