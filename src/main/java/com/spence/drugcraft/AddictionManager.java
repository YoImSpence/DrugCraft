package com.spence.drugcraft;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddictionManager {
    private final DrugCraft plugin;
    private final Map<UUID, Integer> addictionLevels;

    public AddictionManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.addictionLevels = new HashMap<>();
        startWithdrawalTask();
    }

    public void addAddiction(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentLevel = addictionLevels.getOrDefault(uuid, 0);
        int newLevel = Math.max(0, currentLevel + amount);
        addictionLevels.put(uuid, newLevel);
        player.sendMessage("§eAddiction level increased to " + newLevel);
    }

    public int getAddictionLevel(Player player) {
        return addictionLevels.getOrDefault(player.getUniqueId(), 0);
    }

    public void resetAddiction(Player player) {
        addictionLevels.remove(player.getUniqueId());
        player.sendMessage("§aAddiction reset!");
    }

    private void startWithdrawalTask() {
        new WithdrawalTask(this).runTaskTimer(plugin, 1200L, 1200L); // Every 60 seconds
    }

    public Map<UUID, Integer> getAddictionLevels() {
        return addictionLevels;
    }
}