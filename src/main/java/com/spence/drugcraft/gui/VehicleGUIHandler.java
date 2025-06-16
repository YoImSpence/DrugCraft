package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.vehicles.Steed;
import com.spence.drugcraft.vehicles.VehicleGUI;
import com.spence.drugcraft.vehicles.VehicleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VehicleGUIHandler implements Listener, GUIHandler {
    private final DrugCraft plugin;
    private final VehicleGUI vehicleGUI;
    private final VehicleManager vehicleManager;
    private final EconomyManager economyManager;

    public VehicleGUIHandler(DrugCraft plugin, VehicleGUI vehicleGUI, VehicleManager vehicleManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.vehicleGUI = vehicleGUI;
        this.vehicleManager = vehicleManager;
        this.economyManager = economyManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onClick(Player player, ItemStack clickedItem, int slot, Inventory inventory) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("VEHICLE")) return;

        String displayName = MessageUtils.stripColor(clickedItem.getItemMeta().displayName());
        Steed steed = vehicleManager.getPlayerSteed(player);
        if (displayName.equals("Purchase Steed")) {
            vehicleGUI.openPurchaseMenu(player);
        } else if (displayName.startsWith("Steed: ")) {
            String steedType = displayName.split(": ")[1];
            vehicleManager.purchaseSteed(player, steedType);
            player.closeInventory();
        }
    }
}