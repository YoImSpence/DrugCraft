package com.spence.drugcraft.data;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final DrugCraft plugin;
    private final Map<UUID, Map<String, Boolean>> settings = new HashMap<>();

    public PlayerData(DrugCraft plugin) {
        this.plugin = plugin;
        loadSettings();
    }

    private void loadSettings() {
        ConfigurationSection players = plugin.getConfigManager().getConfig("data.yml").getConfigurationSection("players");
        if (players != null) {
            for (String uuid : players.getKeys(false)) {
                ConfigurationSection settingsSec = players.getConfigurationSection(uuid + ".settings");
                if (settingsSec != null) {
                    Map<String, Boolean> playerSettings = new HashMap<>();
                    for (String key : settingsSec.getKeys(false)) {
                        playerSettings.put(key, settingsSec.getBoolean(key));
                    }
                    settings.put(UUID.fromString(uuid), playerSettings);
                }
            }
        }
    }

    public void toggleSetting(UUID playerUUID, String setting) {
        Map<String, Boolean> playerSettings = settings.computeIfAbsent(playerUUID, k -> new HashMap<>());
        boolean current = playerSettings.getOrDefault(setting, true);
        playerSettings.put(setting, !current);
        ConfigurationSection config = plugin.getConfigManager().getConfig("data.yml");
        config.set("players." + playerUUID + ".settings." + setting, !current);
        plugin.getConfigManager().saveConfig("data.yml");
    }

    public boolean getSetting(UUID playerUUID, String setting) {
        return settings.getOrDefault(playerUUID, new HashMap<>()).getOrDefault(setting, true);
    }
}