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
    private final Map<UUID, PlayerAddictionData> playerData = new HashMap<>();
    private final Logger logger;

    public AddictionManager(DrugCraft plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.logger = plugin.getLogger();
    }

    public void addDrugUse(Player player, String drugId) {
        UUID playerId = player.getUniqueId();
        PlayerAddictionData data = playerData.computeIfAbsent(playerId, k -> dataManager.loadPlayerData(playerId));
        data.addUse(drugId);
        dataManager.savePlayerData(playerId, data);
        logger.info("Added drug use for player " + player.getName() + ": " + drugId);
    }

    public PlayerAddictionData getPlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> dataManager.loadPlayerData(playerId));
    }
}