package com.spence.drugcraft.businesses;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.EconomyManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BusinessManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final EconomyManager economyManager;
    private final DrugManager drugManager;

    public BusinessManager(DrugCraft plugin, DataManager dataManager, EconomyManager economyManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
        this.drugManager = drugManager;
    }

    public void addBusiness(UUID playerUUID, String businessId, String businessType) {
        FileConfiguration config = plugin.getConfigManager().getConfig("data.yml");
        List<String> businesses = config.getStringList("players." + playerUUID + ".businesses");
        businesses.add(businessId);
        config.set("players." + playerUUID + ".businesses", businesses);
        config.set("businesses." + businessId + ".type", businessType);
        config.set("businesses." + businessId + ".production", 1.0);
        config.set("businesses." + businessId + ".capacity", 100);
        plugin.getConfigManager().saveConfig("data.yml");
    }

    public List<String> getPlayerBusinesses(UUID playerUUID) {
        FileConfiguration config = plugin.getConfigManager().getConfig("data.yml");
        List<String> businesses = config.getStringList("players." + playerUUID + ".businesses");
        List<String> result = new ArrayList<>();
        for (String businessId : businesses) {
            String type = config.getString("businesses." + businessId + ".type");
            if (type != null) {
                result.add(type + " (" + businessId + ")");
            }
        }
        return result;
    }

    public void upgradeBusiness(UUID playerUUID, String upgradeType) {
        FileConfiguration config = plugin.getConfigManager().getConfig("data.yml");
        List<String> businesses = config.getStringList("players." + playerUUID + ".businesses");
        for (String businessId : businesses) {
            if (upgradeType.equals("production")) {
                double production = config.getDouble("businesses." + businessId + ".production", 1.0);
                config.set("businesses." + businessId + ".production", production * 1.1);
            } else if (upgradeType.equals("capacity")) {
                int capacity = config.getInt("businesses." + businessId + ".capacity", 100);
                config.set("businesses." + businessId + ".capacity", capacity + 100);
            }
        }
        plugin.getConfigManager().saveConfig("data.yml");
    }

    public double getBusinessIncome(String businessId) {
        FileConfiguration config = plugin.getConfigManager().getConfig("data.yml");
        String type = config.getString("businesses." + businessId + ".type");
        double production = config.getDouble("businesses." + businessId + ".production", 1.0);
        return type.equals("Drug Store") ? 1000.0 * production : 1500.0 * production;
    }

    public int getBusinessDrugProduction(String businessId) {
        FileConfiguration config = plugin.getConfigManager().getConfig("data.yml");
        double production = config.getDouble("businesses." + businessId + ".production", 1.0);
        return (int) (10 * production);
    }

    // Stub method to fix compilation errors
    public Business getBusinessByPlayer(Player player) {
        return null; // Placeholder
    }
}