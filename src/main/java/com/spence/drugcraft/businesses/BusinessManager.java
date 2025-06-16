package com.spence.drugcraft.businesses;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class BusinessManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final EconomyManager economyManager;
    private final DrugManager drugManager;
    private final Map<String, Business> businesses;

    public BusinessManager(DrugCraft plugin, DataManager dataManager, EconomyManager economyManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
        this.drugManager = drugManager;
        this.businesses = new HashMap<>();
        loadBusinesses();
    }

    private void loadBusinesses() {
        File businessFile = new File(plugin.getDataFolder(), "businesses.yml");
        if (!businessFile.exists()) {
            plugin.saveResource("businesses.yml", false);
        }
        FileConfiguration businessConfig = YamlConfiguration.loadConfiguration(businessFile);
        ConfigurationSection businessSection = businessConfig.getConfigurationSection("businesses");
        if (businessSection == null) {
            plugin.getLogger().warning("No 'businesses' section found in businesses.yml");
            return;
        }
        for (String id : businessSection.getKeys(false)) {
            ConfigurationSection data = businessSection.getConfigurationSection(id);
            if (data != null) {
                String name = data.getString("name", "Unknown Business");
                UUID ownerUUID = null;
                String ownerString = data.getString("owner");
                if (ownerString != null && !ownerString.isEmpty()) {
                    try {
                        ownerUUID = UUID.fromString(ownerString);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID for business " + id + ": " + ownerString + ". Skipping owner assignment.");
                    }
                }
                double revenue = data.getDouble("revenue", 0.0);
                double price = data.getDouble("price", 1000.0);
                String regionId = data.getString("region", "");
                List<String> allowedDrugs = data.getStringList("allowedDrugs");
                Business business = new Business(id, name, ownerUUID, revenue, price, regionId, allowedDrugs);
                businesses.put(id, business);
                plugin.getLogger().info("Loaded business: " + id);
            }
        }
    }

    public void saveBusinesses() {
        File businessFile = new File(plugin.getDataFolder(), "businesses.yml");
        FileConfiguration businessConfig = new YamlConfiguration();
        ConfigurationSection businessSection = businessConfig.createSection("businesses");
        for (Map.Entry<String, Business> entry : businesses.entrySet()) {
            String id = entry.getKey();
            Business business = entry.getValue();
            ConfigurationSection data = businessSection.createSection(id);
            data.set("name", business.getName());
            if (business.getOwnerUUID() != null) {
                data.set("owner", business.getOwnerUUID().toString());
            }
            data.set("revenue", business.getRevenue());
            data.set("price", business.getPrice());
            data.set("region", business.getRegionId());
            data.set("allowedDrugs", business.getAllowedDrugs());
        }
        try {
            businessConfig.save(businessFile);
            plugin.getLogger().info("Saved " + businesses.size() + " businesses to businesses.yml");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save businesses.yml: " + e.getMessage());
        }
    }

    public Map<String, Business> getBusinesses() {
        return businesses;
    }

    public Business getBusiness(String id) {
        return businesses.get(id);
    }

    public Business getBusinessForPlayer(UUID playerUUID) {
        for (Business business : businesses.values()) {
            if (playerUUID.equals(business.getOwnerUUID())) {
                return business;
            }
        }
        return null;
    }

    public boolean purchaseBusiness(Player player, String businessId) {
        Business business = businesses.get(businessId);
        if (business == null) {
            plugin.getLogger().warning("Player " + player.getName() + " attempted to purchase unknown business: " + businessId);
            return false;
        }
        if (business.getOwnerUUID() != null) {
            plugin.getLogger().info("Business " + businessId + " already owned by " + business.getOwnerUUID());
            return false;
        }
        Economy economy = economyManager.getEconomy();
        if (economy == null) {
            plugin.getLogger().warning("No economy plugin found for business purchase by " + player.getName());
            return false;
        }
        double price = business.getPrice();
        if (!economy.has(player, price)) {
            plugin.getLogger().info("Player " + player.getName() + " has insufficient funds to purchase business " + businessId);
            return false;
        }
        int playerLevel = dataManager.getPlayerLevel(player.getUniqueId());
        String businessType = business.getName().toLowerCase();
        int requiredLevel = switch (businessType) {
            case "dispensary" -> 5;
            case "car wash" -> 7;
            case "grow house" -> 10;
            case "smuggler’s den" -> 15;
            default -> 1;
        };
        if (playerLevel < requiredLevel) {
            plugin.getLogger().info("Player " + player.getName() + " (level " + playerLevel + ") does not meet level requirement (" + requiredLevel + ") for business " + businessId);
            return false;
        }
        economy.withdrawPlayer(player, price);
        business.setOwnerUUID(player.getUniqueId());
        saveBusinesses();
        plugin.getLogger().info("Player " + player.getName() + " purchased business " + businessId + " for $" + price);
        return true;
    }

    public boolean isWithinBusinessRegion(Location location, Business business) {
        if (business.getRegionId().isEmpty()) {
            return false;
        }
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager == null) {
            return false;
        }
        ProtectedRegion region = regionManager.getRegion(business.getRegionId());
        if (region == null) {
            return false;
        }
        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean canAccessMachine(Player player, Business business) {
        return business.getOwnerUUID() != null && business.getOwnerUUID().equals(player.getUniqueId());
    }
}