package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.gui.VehicleGUI;
import com.spence.drugcraft.vehicles.VehicleManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VehicleGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final VehicleGUI vehicleGUI;
    private final VehicleManager vehicleManager;

    public VehicleGUIHandler(DrugCraft plugin, VehicleGUI vehicleGUI) {
        this.plugin = plugin;
        this.vehicleGUI = vehicleGUI;
        this.vehicleManager = plugin.getVehicleManager();
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("VEHICLE")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        String subType = activeGUI.getMenuSubType();
        if (subType == null) {
            if (displayName.equals(MessageUtils.getMessage("gui.vehicle.item-purchase"))) {
                vehicleGUI.openPurchaseMenu(player);
                activeGUI.setMenuSubType("purchase");
            } else if (displayName.equals(MessageUtils.getMessage("gui.vehicle.item-summon"))) {
                summonSteed(player);
            }
        } else if (subType.equals("purchase")) {
            if (displayName.equals(MessageUtils.getMessage("gui.vehicle.item-back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            } else if (displayName.startsWith(MessageUtils.getMessage("gui.vehicle.steed"))) {
                String[] parts = displayName.split(" ");
                if (parts.length >= 2) {
                    String steedId = parts[1].toLowerCase();
                    purchaseSteed(player, steedId);
                }
            }
        }
    }

    private void purchaseSteed(Player player, String steedId) {
        double price = plugin.getConfig("vehicles.yml").getDouble("steeds." + steedId + ".price", 1000.0);
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        if (plugin.getEconomyManager().withdrawPlayer(player, price)) {
            ItemStack steedItem = new ItemStack(Material.SADDLE);
            de.tr7zw.nbtapi.NBTItem nbtItem = new de.tr7zw.nbtapi.NBTItem(steedItem);
            nbtItem.setString("steed_id", steedId);
            steedItem = nbtItem.getItem();
            ItemMeta meta = steedItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(MessageUtils.getMessage("gui.vehicle.steed", "steed_id", "#FFFF55" + steedId)); // HEX color
                steedItem.setItemMeta(meta);
            }
            player.getInventory().addItem(steedItem);
            MessageUtils.sendMessage(player, "vehicle.purchased", "steed_id", steedId, "price", String.valueOf(price));
        } else {
            MessageUtils.sendMessage(player, "vehicle.insufficient-funds");
        }
    }

    private void summonSteed(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (vehicleManager.isSteedItem(item)) {
            vehicleManager.summonSteed(player, item);
        } else {
            MessageUtils.sendMessage(player, "vehicle.no-steed-item");
        }
    }

    public void openMainMenu(Player player) {
        vehicleGUI.openMainMenu(player);
    }
}