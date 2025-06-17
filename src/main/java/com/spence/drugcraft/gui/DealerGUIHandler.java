package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.dealer.DealerGUI;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class DealerGUIHandler implements GUIHandler {
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

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("DEALER")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String displayName = MessageUtils.stripColor(meta.displayName());

        if (displayName.equals("Buy Seeds")) {
            dealerGUI.openBuySeedsMenu(player);
            activeGUI.setMenuSubType("buy-seeds");
        } else if (displayName.equals("Back")) {
            dealerGUI.openMainMenu(player);
            activeGUI.setMenuSubType(null);
        } else if (activeGUI.getMenuSubType().equals("buy-seeds")) {
            String[] parts = displayName.split(" ");
            if (parts.length >= 2 && parts[0].equals("Buy")) {
                String drugId = parts[1].toLowerCase();
                MessageUtils.sendMessage(player, "gui.dealer.quantity-prompt", "item_name", drugId, "max_quantity", "64");
                plugin.getGuiListener().setAwaitingChatInput(player.getUniqueId(), "dealer-quantity", new PurchaseRequest(drugId));
                player.closeInventory();
            }
        }
    }

    public void handleQuantityInput(Player player, String input, PurchaseRequest request) {
        try {
            int quantity = Integer.parseInt(input);
            if (quantity <= 0) {
                MessageUtils.sendMessage(player, "gui.dealer.invalid-quantity");
                return;
            }
            if (quantity > 64) {
                MessageUtils.sendMessage(player, "gui.dealer.quantity-exceeded", "max_quantity", "64");
                return;
            }
            double price = drugManager.getBaseBuyPrice(request.drugId) * quantity;
            if (economyManager.withdrawPlayer(player, price)) {
                ItemStack seedItem = drugManager.getSeedItem(request.drugId, "standard", player);
                seedItem.setAmount(quantity);
                player.getInventory().addItem(seedItem);
                MessageUtils.sendMessage(player, "gui.dealer.purchase-success", "quantity", String.valueOf(quantity), "item", request.drugId, "price", String.format("%.2f", price));
            } else {
                MessageUtils.sendMessage(player, "general.insufficient-funds", "amount", String.format("%.2f", price));
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "gui.dealer.invalid-quantity");
        }
    }

    public static class PurchaseRequest {
        private final String drugId;

        public PurchaseRequest(String drugId) {
            this.drugId = drugId;
        }

        public String getDrugId() {
            return drugId;
        }
    }
}