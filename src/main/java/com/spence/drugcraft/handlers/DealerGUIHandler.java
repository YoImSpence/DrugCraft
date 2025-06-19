package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.DealerGUI;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        if (displayName.equals(MessageUtils.getMessage("gui.dealer.item-back"))) {
            dealerGUI.openMainMenu(player);
            activeGUI.setMenuSubType(null);
        } else if (displayName.startsWith(MessageUtils.getMessage("gui.dealer.seed-name"))) {
            String[] parts = displayName.split(" ");
            if (parts.length >= 2) {
                String drugId = parts[1].toLowerCase();
                purchaseSeed(player, drugId);
            }
        }
    }

    private void purchaseSeed(Player player, String drugId) {
        if (!economyManager.isEconomyAvailable()) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        double price = drugManager.getBaseBuyPrice(drugId);
        if (economyManager.withdrawPlayer(player, price)) {
            ItemStack seed = drugManager.getSeedItem(drugId, "standard", player);
            player.getInventory().addItem(seed);
            MessageUtils.sendMessage(player, "dealer.seed-purchased", "drug_id", drugId, "price", String.valueOf(price));
        } else {
            MessageUtils.sendMessage(player, "dealer.insufficient-funds");
        }
    }

    public void openMainMenu(Player player) {
        dealerGUI.openMainMenu(player);
    }
}