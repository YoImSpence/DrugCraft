package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
        loadAddictionData();
    }

    private void loadAddictionData() {
        FileConfiguration config = dataManager.getAddictionConfig();
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) {
            logger.info("No addiction data found in addiction.yml");
            return;
        }
        for (String uuid : playersSection.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(uuid);
                ConfigurationSection playerSection = playersSection.getConfigurationSection(uuid);
                if (playerSection == null) continue;
                Map<String, Integer> addictionLevels = new HashMap<>();
                ConfigurationSection levelsSection = playerSection.getConfigurationSection("addiction_levels");
                if (levelsSection != null) {
                    for (String drugId : levelsSection.getKeys(false)) {
                        addictionLevels.put(drugId, levelsSection.getInt(drugId));
                    }
                }
                PlayerAddictionData data = new PlayerAddictionData(playerId, addictionLevels);
                playerData.put(playerId, data);
                logger.fine("Loaded addiction data for player: " + uuid);
            } catch (IllegalArgumentException e) {
                logger.warning("Failed to load addiction data for player " + uuid + ": Invalid UUID format (" + e.getMessage() + ")");
            }
        }
        logger.info("Loaded addiction data for " + playerData.size() + " players");
    }

    public void saveAddictionData() {
        FileConfiguration config = dataManager.getAddictionConfig();
        for (PlayerAddictionData data : playerData.values()) {
            String path = "players." + data.getPlayerId().toString();
            ConfigurationSection levelsSection = config.createSection(path + ".addiction_levels");
            for (Map.Entry<String, Integer> entry : data.getAddictionLevels().entrySet()) {
                levelsSection.set(entry.getKey(), entry.getValue());
            }
        }
        dataManager.saveAddiction();
    }

    public PlayerAddictionData getPlayerAddictionData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> new PlayerAddictionData(playerId, new HashMap<>()));
    }

    public static class PlayerAddictionData {
        private final UUID playerId;
        private final Map<String, Integer> addictionLevels;

        public PlayerAddictionData(UUID playerId, Map<String, Integer> addictionLevels) {
            this.playerId = playerId;
            this.addictionLevels = addictionLevels;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public Map<String, Integer> getAddictionLevels() {
            return addictionLevels;
        }

        public int getAddictionLevel(String drugId) {
            return addictionLevels.getOrDefault(drugId, 0);
        }

        public void setAddictionLevel(String drugId, int level) {
            addictionLevels.put(drugId, level);
        }
    }
}