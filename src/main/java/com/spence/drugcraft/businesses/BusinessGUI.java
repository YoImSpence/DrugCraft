package com.spence.drugcraft.businesses;

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

public class BusinessGUI {
    private final DrugCraft plugin;
    private final BusinessManager businessManager;
    private final DataManager dataManager;

    public BusinessGUI(DrugCraft plugin, BusinessManager businessManager, DataManager dataManager) {
        this.plugin = plugin;
        this.businessManager = businessManager;
        this.dataManager = dataManager;
    }

    public void openMainMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.business.title-main")).color(TextColor.fromHexString("#32CD32"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack listBusinesses = new ItemStack(Material.EMERALD);
        ItemMeta listMeta = listBusinesses.getItemMeta();
        listMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.business.item-purchase")).color(TextColor.fromHexString("#FFD700")));
        listBusinesses.setItemMeta(listMeta);
        inventory.setItem(12, listBusinesses);

        ItemStack manageBusiness = new ItemStack(Material.PAPER);
        ItemMeta manageMeta = manageBusiness.getItemMeta();
        manageMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.business.item-manage")).color(TextColor.fromHexString("#FFD700")));
        manageBusiness.setItemMeta(manageMeta);
        inventory.setItem(14, manageBusiness);

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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("BUSINESS", inventory, "main"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened business main menu for player " + player.getName());
    }

    public void openListMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.business.title-list")).color(TextColor.fromHexString("#32CD32"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int slot = 0;
        boolean hasBusinesses = false;
        for (Business business : businessManager.getBusinesses().values()) {
            if (slot >= 36) break;
            ItemStack businessItem = new ItemStack(business.getOwnerUUID() == null ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
            ItemMeta meta = businessItem.getItemMeta();
            meta.displayName(MessageUtils.color(business.getName()).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("ID: " + business.getId(), TextColor.fromHexString("#D3D3D3")));
            lore.add(Component.text("Type: " + business.getType(), TextColor.fromHexString("#D3D3D3")));
            lore.add(Component.text("Price: $" + String.format("%.2f", business.getPrice()), TextColor.fromHexString("#D3D3D3")));
            lore.add(Component.text("Required Level: " + business.getRequiredLevel(), TextColor.fromHexString("#D3D3D3")));
            if (business.getOwnerUUID() != null) {
                String ownerName = Bukkit.getOfflinePlayer(business.getOwnerUUID()).getName() != null ?
                        Bukkit.getOfflinePlayer(business.getOwnerUUID()).getName() : "Unknown";
                lore.add(Component.text("Owner: " + ownerName, TextColor.fromHexString("#D3D3D3")));
            }
            meta.lore(lore);
            businessItem.setItemMeta(meta);
            inventory.setItem(slot++, businessItem);
            hasBusinesses = true;
            plugin.getLogger().info("Added business " + business.getId() + " to list menu for player " + player.getName());
        }

        if (!hasBusinesses) {
            ItemStack noBusinesses = new ItemStack(Material.BARRIER);
            ItemMeta noBusinessesMeta = noBusinesses.getItemMeta();
            noBusinessesMeta.displayName(MessageUtils.color("No Businesses Available").color(TextColor.fromHexString("#FF5555")));
            noBusinesses.setItemMeta(noBusinessesMeta);
            inventory.setItem(22, noBusinesses);
            plugin.getLogger().warning("No businesses available for list menu for player " + player.getName());
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.business.item-back")).color(TextColor.fromHexString("#FF5555")));
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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("BUSINESS", inventory, "list"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened business list menu for player " + player.getName());
    }
}