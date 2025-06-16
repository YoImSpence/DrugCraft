package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.dealer.DealerGUI;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

public class DealerGUIHandler implements GUIHandler, ChatInputHandler {
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
        List<String> lore = meta.lore() != null ? meta.lore().stream().map(MessageUtils::stripColor).toList() : List.of();

        if (displayName.equals("Buy Seeds")) {
            dealerGUI.openBuySeedsMenu(player);
            plugin.getLogger().info("Player " + player.getName() + " opened Dealer buy seeds menu");
            return;
        }
        if (displayName.equals("Back")) {
            dealerGUI.openMainMenu(player);
            plugin.getLogger().info("Player " + player.getName() + " returned to Dealer main menu");
            return;
        }

        // Handle seed purchase
        if (inventory.getViewers().get(0).getOpenInventory().title().equals(MessageUtils.color(MessageUtils.getMessage("gui.dealer.title-buy-seeds")))) {
            String drugId = lore.stream()
                    .filter(l -> l.startsWith("Drug ID: "))
                    .map(l -> l.replace("Drug ID: ", ""))
                    .findFirst()
                    .orElse(null);
            String quality = lore.stream()
                    .filter(l -> l.startsWith("Quality: "))
                    .map(l -> l.replace("Quality: ", ""))
                    .findFirst()
                    .orElse("Standard");
            if (drugId != null) {
                plugin.getGuiListener().setAwaitingChatInput(player.getUniqueId(), "purchase_seed", new PurchaseRequest(drugId, quality));
                MessageUtils.sendMessage(player, "dealer.quantity-prompt");
                player.closeInventory();
                plugin.getLogger().info("Player " + player.getName() + " initiated seed purchase for " + drugId + " (" + quality + ")");
            }
        }
    }

    @Override
    public void handleChatInput(Player player, String action, String message, Object context) {
        if (!action.equals("purchase_seed")) return;
        PurchaseRequest request = (PurchaseRequest) context;
        if (request == null) {
            MessageUtils.sendMessage(player, "general.error");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(message.trim());
            if (quantity <= 0) {
                MessageUtils.sendMessage(player, "general.quantity-positive");
                return;
            }
            int maxQuantity = dealerGUI.getMaxQuantityForLevel(player);
            if (quantity > maxQuantity) {
                MessageUtils.sendMessage(player, "general.invalid-quantity", "max_quantity", String.valueOf(maxQuantity));
                return;
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "general.invalid-quantity");
            return;
        }

        ItemStack seed = drugManager.getSeedItem(request.drugId(), request.quality(), player);
        if (seed == null) {
            MessageUtils.sendMessage(player, "general.error");
            plugin.getLogger().warning("Failed to create seed item for " + request.drugId() + " (" + request.quality() + ") for player " + player.getName());
            return;
        }

        double pricePerUnit = drugManager.getSeedPrice(request.drugId(), request.quality());
        double totalPrice = pricePerUnit * quantity;
        Economy economy = dealerGUI.getEconomy();
        if (economy == null || !economy.has(player, totalPrice)) {
            MessageUtils.sendMessage(player, "general.insufficient-funds", "amount", String.format("%.2f", totalPrice));
            plugin.getLogger().info("Player " + player.getName() + " has insufficient funds for " + quantity + " " + request.drugId() + " seeds ($" + totalPrice + ")");
            return;
        }

        seed.setAmount(quantity);
        if (!canAddToInventory(player, seed)) {
            MessageUtils.sendMessage(player, "general.inventory-full");
            player.getWorld().dropItemNaturally(player.getLocation(), seed);
            plugin.getLogger().info("Player " + player.getName() + "'s inventory full; dropped " + quantity + " " + request.drugId() + " seeds");
        } else {
            player.getInventory().addItem(seed);
        }

        economy.withdrawPlayer(player, totalPrice);
        MessageUtils.sendMessage(player, "dealer.purchase-success", "quantity", String.valueOf(quantity), "item", request.drugId() + " seeds", "amount", String.format("%.2f", totalPrice));
        plugin.getLogger().info("Player " + player.getName() + " purchased " + quantity + " " + request.drugId() + " (" + request.quality() + ") seeds for $" + totalPrice);
    }

    private boolean canAddToInventory(Player player, ItemStack item) {
        int remaining = item.getAmount();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem == null || slotItem.getType() == Material.AIR) {
                remaining -= item.getMaxStackSize();
            } else if (slotItem.isSimilar(item)) {
                remaining -= (item.getMaxStackSize() - slotItem.getAmount());
            }
            if (remaining <= 0) return true;
        }
        return false;
    }

    private record PurchaseRequest(String drugId, String quality) {
    }
}