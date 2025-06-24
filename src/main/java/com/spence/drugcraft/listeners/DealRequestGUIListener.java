package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.DealRequestGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DealRequestGUIListener implements Listener {
    private final DrugCraft plugin;
    private final DealRequestGUI dealRequestGUI;

    public DealRequestGUIListener(DrugCraft plugin, DealRequestGUI dealRequestGUI) {
        this.plugin = plugin;
        this.dealRequestGUI = dealRequestGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getInventory().equals(inventory)) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        String displayName = item.getItemMeta().getDisplayName();
        String guiType = activeGUI.getType();

        if (guiType.equals("DEAL_REQUEST") && displayName.equals(MessageUtils.getMessage("gui.deal-request.request-deal"))) {
            dealRequestGUI.openMeetupGUI(player);
        } else if (guiType.equals("DEAL_MEETUP") && displayName.equals(MessageUtils.getMessage("gui.deal-request.confirm-meetup"))) {
            MessageUtils.sendMessage(player, "general.invalid-input"); // Placeholder: Confirm deal logic
        } else if (guiType.equals("DEAL_MEETUP") && displayName.equals(MessageUtils.getMessage("gui.deal-request.back"))) {
            dealRequestGUI.openDealRequestGUI(player);
        }
    }
}