package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.BusinessGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class BusinessGUIHandler {
    private final DrugCraft plugin;
    private final BusinessGUI businessGUI;
    private final BusinessManager businessManager;
    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BusinessGUIHandler(DrugCraft plugin, BusinessGUI businessGUI, BusinessManager businessManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.businessGUI = businessGUI;
        this.businessManager = businessManager;
        this.economyManager = economyManager;
    }

    public void openMainMenu(Player player) {
        businessGUI.openMainMenu(player);
    }

    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Component displayNameComp = meta.displayName();
        if (displayNameComp == null) return;

        String displayName = miniMessage.serialize(displayNameComp);
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        String guiType = activeGUI.getType();
        UUID playerUUID = player.getUniqueId();

        switch (guiType) {
            case "BUSINESS":
                if (displayName.contains(MessageUtils.getMessage("gui.business.buy"))) {
                    businessGUI.openBuyMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.business.upgrade"))) {
                    businessGUI.openUpgradeMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.business.stats"))) {
                    businessGUI.openStatsMenu(player);
                }
                break;
            case "BUSINESS_BUY":
                if (meta.hasLore()) {
                    String businessType = miniMessage.serialize(meta.lore().get(0)).substring(8); // Remove <yellow>
                    double price = businessType.equals("Drug Store") ? 10000.0 : 15000.0;
                    String businessId = UUID.randomUUID().toString();
                    if (economyManager.withdrawPlayer(player, price)) {
                        businessManager.addBusiness(playerUUID, businessId, businessType);
                        MessageUtils.sendMessage(player, "business.purchased", "business_id", businessType, "price", String.valueOf(price));
                        businessGUI.openMainMenu(player);
                    } else {
                        MessageUtils.sendMessage(player, "business.insufficient-funds");
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    businessGUI.openMainMenu(player);
                }
                break;
            case "BUSINESS_UPGRADE":
                if (meta.hasLore()) {
                    String upgradeType = displayName.contains("Production Rate") ? "production" : "capacity";
                    double price = upgradeType.equals("production") ? 5000.0 : 3000.0;
                    if (economyManager.withdrawPlayer(player, price)) {
                        businessManager.upgradeBusiness(playerUUID, upgradeType);
                        MessageUtils.sendMessage(player, "business.upgrade");
                        businessGUI.openMainMenu(player);
                    } else {
                        MessageUtils.sendMessage(player, "business.insufficient-funds");
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    businessGUI.openMainMenu(player);
                }
                break;
            case "BUSINESS_STATS":
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    businessGUI.openMainMenu(player);
                }
                break;
        }
    }
}