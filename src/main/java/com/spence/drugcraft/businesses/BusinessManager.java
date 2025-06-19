package com.spence.drugcraft.businesses;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BusinessManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final EconomyManager economyManager;
    private final DrugManager drugManager;
    private final List<Business> businesses = new ArrayList<>();

    public BusinessManager(DrugCraft plugin, DataManager dataManager, EconomyManager economyManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
        this.drugManager = drugManager;
    }

    public Business getBusinessByPlayer(Player player) {
        return businesses.stream().filter(b -> b.getOwnerUUID() != null && b.getOwnerUUID().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    public Business getBusinessForPlayer(UUID playerUUID) {
        return businesses.stream().filter(b -> b.getOwnerUUID() != null && b.getOwnerUUID().equals(playerUUID)).findFirst().orElse(null);
    }

    public Business getBusiness(String businessId) {
        return businesses.stream().filter(b -> b.getId().equalsIgnoreCase(businessId)).findFirst().orElse(null);
    }

    public List<Business> getBusinesses() {
        return new ArrayList<>(businesses);
    }

    public void createBusiness(Player player, String name) {
        String id = name.toLowerCase().replace(" ", "_");
        Business business = new Business(player.getUniqueId(), id, name, "dispensary", "dispensary1", 1);
        businesses.add(business);
        MessageUtils.sendMessage(player, "business.purchased", "name", name);
    }

    public void manageBusiness(Player player, Business business) {
        if (business.getOwnerUUID() != null && business.getOwnerUUID().equals(player.getUniqueId())) {
            double revenue = business.getRevenue();
            double price = business.getPrice();
            List<String> allowedDrugs = business.getAllowedDrugs();
            // Placeholder: Implement management logic
        }
    }

    public void purchaseBusiness(Player player, String businessId) {
        Business business = getBusiness(businessId);
        if (business == null) {
            MessageUtils.sendMessage(player, "business.not-found");
            return;
        }
        if (business.getOwnerUUID() != null) {
            MessageUtils.sendMessage(player, "business.already-owned");
            return;
        }
        if (dataManager.getPlayerLevel(player.getUniqueId()) < business.getRequiredLevel()) {
            MessageUtils.sendMessage(player, "business.level-required", "level", String.valueOf(business.getRequiredLevel()));
            return;
        }
        if (economyManager.withdrawPlayer(player, business.getPrice())) {
            business.setOwnerUUID(player.getUniqueId());
            MessageUtils.sendMessage(player, "business.purchased", "name", business.getName());
        } else {
            MessageUtils.sendMessage(player, "business.insufficient-funds");
        }
    }
}