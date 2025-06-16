package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class AddictionManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final PoliceManager policeManager;
    private final Map<UUID, Map<String, Addiction>> playerAddictions;

    public AddictionManager(DrugCraft plugin, DataManager dataManager, DrugManager drugManager, PoliceManager policeManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
        this.policeManager = policeManager;
        this.playerAddictions = new HashMap<>();
        loadAddictions();
    }

    private void loadAddictions() {
        ConfigurationSection addictionSection = dataManager.getConfig().getConfigurationSection("players");
        if (addictionSection == null) {
            plugin.getLogger().warning("No 'players' section found in data.yml for addictions");
            return;
        }
        for (String uuid : addictionSection.getKeys(false)) {
            ConfigurationSection playerData = addictionSection.getConfigurationSection(uuid + ".addictions");
            if (playerData != null) {
                Map<String, Addiction> addictions = new HashMap<>();
                for (String drugId : playerData.getKeys(false)) {
                    double level = playerData.getDouble(drugId + ".level", 0.0);
                    long lastUsed = playerData.getLong(drugId + ".lastUsed", 0L);
                    addictions.put(drugId, new Addiction(drugId, level, lastUsed));
                }
                playerAddictions.put(UUID.fromString(uuid), addictions);
                plugin.getLogger().info("Loaded addictions for player: " + uuid);
            }
        }
    }

    public void saveAddictions() {
        for (Map.Entry<UUID, Map<String, Addiction>> entry : playerAddictions.entrySet()) {
            UUID playerUUID = entry.getKey();
            ConfigurationSection playerSection = dataManager.getConfig().createSection("players." + playerUUID + ".addictions");
            for (Addiction addiction : entry.getValue().values()) {
                playerSection.set(addiction.getDrugId() + ".level", addiction.getLevel());
                playerSection.set(addiction.getDrugId() + ".lastUsed", addiction.getLastUsed());
            }
        }
        dataManager.saveConfig();
        plugin.getLogger().info("Saved addictions for " + playerAddictions.size() + " players");
    }

    public void addAddiction(Player player, String drugId, String quality) {
        UUID playerUUID = player.getUniqueId();
        Map<String, Addiction> addictions = playerAddictions.computeIfAbsent(playerUUID, k -> new HashMap<>());
        Addiction addiction = addictions.computeIfAbsent(drugId, k -> new Addiction(drugId, 0.0, 0L));
        double increase = switch (quality.toLowerCase()) {
            case "standard" -> 0.1;
            case "exotic" -> 0.2;
            case "prime" -> 0.3;
            case "legendary" -> 0.5;
            default -> 0.1;
        };
        addiction.setLevel(addiction.getLevel() + increase);
        addiction.setLastUsed(System.currentTimeMillis());
        saveAddictions();
        if (addiction.getLevel() > 0.5) {
            policeManager.notifyPolice(player, "Drug Addiction Detected");
            MessageUtils.sendMessage(player, "addiction.warning");
        }
        plugin.getLogger().info("Increased addiction for player " + player.getName() + " for drug " + drugId + " to level " + addiction.getLevel());
    }

    public void resetAddiction(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerAddictions.remove(playerUUID);
        dataManager.getConfig().set("players." + playerUUID + ".addictions", null);
        dataManager.saveConfig();
        plugin.getLogger().info("Reset addictions for player: " + player.getName());
    }

    private static class Addiction {
        private final String drugId;
        private double level;
        private long lastUsed;

        public Addiction(String drugId, double level, long lastUsed) {
            this.drugId = drugId;
            this.level = level;
            this.lastUsed = lastUsed;
        }

        public String getDrugId() {
            return drugId;
        }

        public double getLevel() {
            return level;
        }

        public void setLevel(double level) {
            this.level = level;
        }

        public long getLastUsed() {
            return lastUsed;
        }

        public void setLastUsed(long lastUsed) {
            this.lastUsed = lastUsed;
        }
    }
}