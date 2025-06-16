package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.Business;
import com.spence.drugcraft.businesses.BusinessGUI;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BusinessGUIHandler implements GUIHandler, ChatInputHandler {
    private final DrugCraft plugin;
    private final BusinessGUI businessGUI;
    private final BusinessManager businessManager;
    private final DataManager dataManager;
    private final EconomyManager economyManager;

    public BusinessGUIHandler(DrugCraft plugin, BusinessGUI businessGUI, BusinessManager businessManager, DataManager dataManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.businessGUI = businessGUI;
        this.businessManager = businessManager;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("BUSINESS")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String displayName = MessageUtils.stripColor(meta.displayName());
        List<String> lore = meta.lore() != null ? meta.lore().stream().map(MessageUtils::stripColor).toList() : List.of();

        plugin.getLogger().info("Player " + player.getName() + " clicked item: " + displayName + " in business GUI");

        if (displayName.equals(MessageUtils.stripColor(MessageUtils.getMessage("gui.business.item-back")))) {
            businessGUI.openMainMenu(player);
            activeGUI.setMenuSubType("main");
            plugin.getLogger().info("Player " + player.getName() + " returned to business main menu");
            return;
        }

        String menuSubType = activeGUI.getMenuSubType();
        switch (menuSubType) {
            case "main":
                if (displayName.equals(MessageUtils.stripColor(MessageUtils.getMessage("gui.business.item-purchase")))) {
                    businessGUI.openListMenu(player);
                    activeGUI.setMenuSubType("list");
                    plugin.getLogger().info("Player " + player.getName() + " opened business list menu");
                } else if (displayName.equals(MessageUtils.stripColor(MessageUtils.getMessage("gui.business.item-manage")))) {
                    Business business = businessManager.getBusinessForPlayer(player.getUniqueId());
                    if (business == null) {
                        MessageUtils.sendMessage(player, "business.error");
                        plugin.getLogger().warning("Player " + player.getName() + " attempted to manage non-owned business");
                        return;
                    }
                    // Placeholder: Send message until management menu is implemented
                    MessageUtils.sendMessage(player, "business.manage-placeholder");
                    activeGUI.setMenuSubType("manage");
                    plugin.getLogger().info("Player " + player.getName() + " attempted to manage business");
                }
                break;
            case "list":
                String businessId = lore.stream()
                        .filter(l -> l.startsWith("ID: "))
                        .map(l -> l.replace("ID: ", ""))
                        .findFirst()
                        .orElse(null);
                if (businessId != null) {
                    purchaseBusiness(player, businessId);
                }
                break;
        }
    }

    private void purchaseBusiness(Player player, String businessId) {
        Business business = businessManager.getBusiness(businessId);
        if (business == null) {
            MessageUtils.sendMessage(player, "business.error");
            plugin.getLogger().warning("Business " + businessId + " not found for purchase by player " + player.getName());
            return;
        }
        if (business.getOwnerUUID() != null) {
            MessageUtils.sendMessage(player, "business.already-owned");
            plugin.getLogger().info("Business " + businessId + " already owned, purchase denied for player " + player.getName());
            return;
        }
        if (!economyManager.isEconomyAvailable()) {
            MessageUtils.sendMessage(player, "general.no-economy");
            plugin.getLogger().warning("Economy unavailable for business purchase by player " + player.getName());
            return;
        }
        Economy economy = economyManager.getEconomy();
        if (!economy.has(player, business.getPrice())) {
            MessageUtils.sendMessage(player, "general.insufficient-funds", "amount", String.format("%.2f", business.getPrice()));
            plugin.getLogger().info("Player " + player.getName() + " lacks funds ($" + business.getPrice() + ") for business " + businessId);
            return;
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
            MessageUtils.sendMessage(player, "business.purchase-failed", "reason", "Level " + requiredLevel + " required");
            plugin.getLogger().info("Player " + player.getName() + " level " + playerLevel + " too low for business " + businessId + " (requires level " + requiredLevel + ")");
            return;
        }
        if (businessManager.purchaseBusiness(player, businessId)) {
            MessageUtils.sendMessage(player, "business.purchase-success", "name", business.getName(), "id", businessId);
            plugin.getLogger().info("Player " + player.getName() + " purchased business " + businessId);
        } else {
            MessageUtils.sendMessage(player, "business.purchase-failed", "reason", "Purchase failed");
            plugin.getLogger().warning("Business purchase failed for player " + player.getName() + " for business " + businessId);
        }
    }

    @Override
    public void handleChatInput(Player player, String action, String message, Object context) {
        // Placeholder for future chat input handling
        plugin.getLogger().info("Chat input received from player " + player.getName() + ": action=" + action + ", message=" + message);
    }
}