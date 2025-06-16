package com.spence.drugcraft.vehicles;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.*;

public class VehicleManager implements Listener {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, Steed> playerSteeds;

    public VehicleManager(DrugCraft plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.playerSteeds = new HashMap<>();
        loadPlayerSteeds();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadPlayerSteeds() {
        File steedFile = new File(plugin.getDataFolder(), "steeds.yml");
        if (!steedFile.exists()) {
            try {
                plugin.saveResource("steeds.yml", false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Embedded resource 'steeds.yml' not found. Creating empty steeds.yml.");
                FileConfiguration emptyConfig = new YamlConfiguration();
                emptyConfig.createSection("steeds");
                try {
                    emptyConfig.save(steedFile);
                } catch (Exception ex) {
                    plugin.getLogger().severe("Failed to create empty steeds.yml: " + ex.getMessage());
                }
            }
        }
        FileConfiguration steedConfig = YamlConfiguration.loadConfiguration(steedFile);
        ConfigurationSection steedSection = steedConfig.getConfigurationSection("steeds");
        if (steedSection == null) {
            plugin.getLogger().warning("No 'steeds' section found in steeds.yml");
            return;
        }
        for (String uuid : steedSection.getKeys(false)) {
            try {
                String type = steedSection.getString(uuid + ".type", "Swiftwind");
                playerSteeds.put(UUID.fromString(uuid), new Steed(UUID.fromString(uuid), type));
                plugin.getLogger().info("Loaded steed for player " + uuid + ": " + type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in steeds.yml: " + uuid + ". Skipping entry.");
            }
        }
    }

    public void savePlayerSteeds() {
        File steedFile = new File(plugin.getDataFolder(), "steeds.yml");
        FileConfiguration steedConfig = new YamlConfiguration();
        ConfigurationSection steedSection = steedConfig.createSection("steeds");
        for (Map.Entry<UUID, Steed> entry : playerSteeds.entrySet()) {
            UUID uuid = entry.getKey();
            Steed steed = entry.getValue();
            ConfigurationSection data = steedSection.createSection(uuid.toString());
            data.set("type", steed.getType());
        }
        try {
            steedConfig.save(steedFile);
            plugin.getLogger().info("Saved " + playerSteeds.size() + " steeds to steeds.yml");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save steeds.yml: " + e.getMessage());
        }
    }

    public Steed getPlayerSteed(Player player) {
        return playerSteeds.get(player.getUniqueId());
    }

    public void despawnSteed(Player player) {
        Steed steed = playerSteeds.get(player.getUniqueId());
        if (steed != null && steed.getEntity() != null) {
            steed.getEntity().remove();
            steed.setEntity(null);
            plugin.getLogger().info("Despawned steed for player " + player.getName());
        }
    }

    public boolean purchaseSteed(Player player, String steedType) {
        if (playerSteeds.containsKey(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "vehicle.already-owned");
            return false;
        }
        Economy economy = economyManager.getEconomy();
        if (economy == null) {
            MessageUtils.sendMessage(player, "general.no-economy");
            return false;
        }
        double price = switch (steedType.toLowerCase()) {
            case "swiftwind" -> 1000.0;
            case "ironhoof" -> 1500.0;
            case "shadowmare" -> 2000.0;
            case "drug mule" -> 1200.0;
            case "blazefury" -> 2500.0;
            case "starbolt" -> 2200.0;
            default -> 1000.0;
        };
        if (!economy.has(player, price)) {
            MessageUtils.sendMessage(player, "general.insufficient-funds", "amount", String.format("%.2f", price));
            return false;
        }
        int playerLevel = plugin.getDataManager().getPlayerLevel(player.getUniqueId());
        int requiredLevel = switch (steedType.toLowerCase()) {
            case "swiftwind" -> 1;
            case "ironhoof" -> 3;
            case "shadowmare" -> 5;
            case "drug mule" -> 2;
            case "blazefury" -> 7;
            case "starbolt" -> 6;
            default -> 1;
        };
        if (playerLevel < requiredLevel) {
            MessageUtils.sendMessage(player, "vehicle.level-required", "level", String.valueOf(requiredLevel));
            return false;
        }
        economy.withdrawPlayer(player, price);
        Steed steed = new Steed(player.getUniqueId(), steedType);
        playerSteeds.put(player.getUniqueId(), steed);
        savePlayerSteeds();
        MessageUtils.sendMessage(player, "vehicle.purchased", "steed", steedType, "price", String.format("%.2f", price));
        plugin.getLogger().info("Player " + player.getName() + " purchased steed " + steedType + " for $" + price);
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Placeholder for steed interaction logic
    }
}