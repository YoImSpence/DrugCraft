package com.spence.drugcraft.vehicles;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class VehicleGUI {
    private final DrugCraft plugin;
    private final VehicleManager vehicleManager;
    private final DataManager dataManager;
    private static final String[] STEED_TYPES = {
            "Swiftwind", "Ironhoof", "Shadowmare", "Drug Mule", "Blazefury", "Starbolt"
    };
    private static final double[] STEED_PRICES = {
            5000.0, 7500.0, 6000.0, 4000.0, 10000.0, 8000.0
    };
    private static final int[] STEED_LEVELS = {
            1, 3, 2, 1, 5, 4
    };

    public VehicleGUI(DrugCraft plugin, VehicleManager vehicleManager, DataManager dataManager) {
        this.plugin = plugin;
        this.vehicleManager = vehicleManager;
        this.dataManager = dataManager;
    }

    public void openMainMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.vehicle.title-main")).color(TextColor.fromHexString("#1E90FF"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack purchaseSteed = new ItemStack(Material.SADDLE);
        ItemMeta purchaseMeta = purchaseSteed.getItemMeta();
        purchaseMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.vehicle.item-purchase")).color(TextColor.fromHexString("#FFD700")));
        purchaseSteed.setItemMeta(purchaseMeta);
        inventory.setItem(13, purchaseSteed);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("VEHICLE", inventory, "main"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened steed main menu for player " + player.getName());
    }

    public void openPurchaseMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.vehicle.title-purchase")).color(TextColor.fromHexString("#1E90FF"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int slot = 10;
        boolean hasSteeds = false;
        for (int i = 0; i < STEED_TYPES.length && slot <= 34; i++) {
            String steedType = STEED_TYPES[i];
            double price = STEED_PRICES[i];
            int requiredLevel = STEED_LEVELS[i];
            ItemStack steedItem = new ItemStack(Material.SADDLE);
            ItemMeta steedMeta = steedItem.getItemMeta();
            steedMeta.displayName(MessageUtils.color(steedType).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Price: $" + String.format("%.2f", price), TextColor.fromHexString("#D3D3D3")));
            lore.add(Component.text("Required Level: " + requiredLevel, TextColor.fromHexString("#D3D3D3")));
            steedMeta.lore(lore);
            steedItem.setItemMeta(steedMeta);
            inventory.setItem(slot, steedItem);
            slot += slot % 9 == 7 ? 2 : 1; // Skip 2 slots after column 7
            hasSteeds = true;
            plugin.getLogger().info("Added steed " + steedType + " to purchase menu for player " + player.getName());
        }

        if (!hasSteeds) {
            ItemStack noSteeds = new ItemStack(Material.BARRIER);
            ItemMeta noSteedsMeta = noSteeds.getItemMeta();
            noSteedsMeta.displayName(MessageUtils.color("No Steeds Available").color(TextColor.fromHexString("#FF5555")));
            noSteeds.setItemMeta(noSteedsMeta);
            inventory.setItem(22, noSteeds);
            plugin.getLogger().warning("No steeds available for purchase menu for player " + player.getName());
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.vehicle.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 36; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("VEHICLE", inventory, "purchase"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened steed purchase menu for player " + player.getName());
    }
}