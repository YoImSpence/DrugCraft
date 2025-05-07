package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class AddictionManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final Logger logger;
    private final Map<UUID, PlayerAddictionData> playerData = new HashMap<>();

    public AddictionManager(DrugCraft plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.logger = plugin.getLogger();
    }

    public void loadPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerAddictionData data = dataManager.loadPlayerData(playerId);
        playerData.put(playerId, data);
        logger.fine("Loaded addiction data for player: " + player.getName());
    }

    public void savePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerAddictionData data = playerData.get(playerId);
        if (data != null) {
            dataManager.savePlayerData(playerId, data);
            logger.fine("Saved addiction data for player: " + player.getName());
        }
    }

    public void incrementDrugUse(Player player, String drugId) {
        UUID playerId = player.getUniqueId();
        PlayerAddictionData data = playerData.computeIfAbsent(playerId, k -> new PlayerAddictionData());
        data.incrementUses(drugId);
        dataManager.savePlayerData(playerId, data);
        logger.info("Incremented drug use for player " + player.getName() + ": " + drugId);
    }

    public PlayerAddictionData getPlayerData(Player player) {
        return playerData.getOrDefault(player.getUniqueId(), new PlayerAddictionData());
    }

    public void clearPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        playerData.remove(playerId);
        logger.fine("Cleared addiction data for player: " + player.getName());
    }
}