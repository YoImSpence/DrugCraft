package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.vehicles.VehicleManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VehicleGUI {
    private final DrugCraft plugin;
    private final VehicleManager vehicleManager;
    private final DataManager dataManager;

    public VehicleGUI(DrugCraft plugin, VehicleManager vehicleManager, DataManager dataManager) {
        this.plugin = plugin;
        this.vehicleManager = vehicleManager;
        this.dataManager = dataManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(MessageUtils.getMessage("<gradient:#00FF00:#FFFFFF>Steed Menu</gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("VEHICLE", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack purchase = createItem(Material.SADDLE, "gui.vehicle.item-purchase");
        inv.setItem(13, purchase);

        player.openInventory(inv);
    }

    public void openPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(MessageUtils.getMessage("<gradient:#00FF00:#FFFFFF>Purchase Steed</gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("VEHICLE", inv, "purchase");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack fastHorse = createItem(Material.SADDLE, "gui.vehicle.fast-horse");
        inv.setItem(13, fastHorse);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String messageKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage(messageKey));
            item.setItemMeta(meta);
        }
        return item;
    }
}