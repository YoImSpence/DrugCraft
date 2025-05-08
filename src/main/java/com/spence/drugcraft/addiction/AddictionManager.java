package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class AddictionManager {
    private final DrugCraft plugin;
    private final Logger logger;
    private final Map<UUID, PlayerAddictionData> playerData = new HashMap<>();
    private File addictionFile;
    private FileConfiguration addictionConfig;

    public AddictionManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        addictionFile = new File(plugin.getDataFolder(), "addiction.yml");
        if (!addictionFile.exists()) {
            plugin.saveResource("addiction.yml", false);
        }
        addictionConfig = YamlConfiguration.loadConfiguration(addictionFile);
    }

    public void loadPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        ConfigurationSection section = addictionConfig.getConfigurationSection(playerId.toString());
        if (section != null) {
            PlayerAddictionData data = new PlayerAddictionData();
            for (String drugId : section.getKeys(false)) {
                int uses = section.getInt(drugId);
                data.setDrugUses(drugId, uses);
            }
            playerData.put(playerId, data);
            logger.fine("Loaded addiction data for player " + player.getName());
        }
    }

    public void savePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerAddictionData data = playerData.get(playerId);
        if (data != null) {
            ConfigurationSection section = addictionConfig.createSection(playerId.toString());
            for (Map.Entry<String, Integer> entry : data.getDrugUses().entrySet()) {
                section.set(entry.getKey(), entry.getValue());
            }
            saveAddictionConfig();
            logger.fine("Saved addiction data for player " + player.getName());
        }
    }

    public void saveAllPlayerData() {
        for (UUID playerId : playerData.keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                savePlayerData(player);
            }
        }
    }

    public void clearPlayerData(Player player) {
        playerData.remove(player.getUniqueId());
        logger.fine("Cleared addiction data for player " + player.getName());
    }

    public void incrementDrugUse(Player player, String drugId) {
        UUID playerId = player.getUniqueId();
        PlayerAddictionData data = playerData.computeIfAbsent(playerId, k -> new PlayerAddictionData());
        data.incrementDrugUse(drugId);
        logger.fine("Incremented drug use for " + player.getName() + ": " + drugId);
    }

    private void saveAddictionConfig() {
        try {
            addictionConfig.save(addictionFile);
        } catch (IOException e) {
            logger.severe("Failed to save addiction.yml: " + e.getMessage());
        }
    }
}