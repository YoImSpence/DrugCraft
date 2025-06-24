package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.DealerGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DealerGUIHandler {
    private final DrugCraft plugin;
    private final DealerGUI dealerGUI;
    private final DrugManager drugManager;
    private final EconomyManager economyManager;

    public DealerGUIHandler(DrugCraft plugin, DealerGUI dealerGUI, DrugManager drugManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.dealerGUI = dealerGUI;
        this.drugManager = drugManager;
        this.economyManager = economyManager;
    }

    public void openMainMenu(Player player) {
        dealerGUI.openMainMenu(player);
    }

    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == Material.AIR) return;

        String drugId = drugManager.getDrugIdFromItem(item);
        if (drugId == null) return;

        ConfigurationSection seeds = plugin.getConfigManager().getConfig("drugs.yml").getConfigurationSection("seeds." + drugId);
        if (seeds == null) return;

        double price = seeds.getDouble("price", 100.0);
        if (economyManager.isEconomyAvailable() && economyManager.withdrawPlayer(player, price)) {
            ItemStack seed = drugManager.createSeedItem(drugId);
            if (seed != null) {
                player.getInventory().addItem(seed);
                MessageUtils.sendMessage(player, "dealer.purchased", "item", drugId.replace("_", " "), "price", String.valueOf(price));
            }
        } else {
            MessageUtils.sendMessage(player, "dealer.insufficient-funds");
        }
    }
}