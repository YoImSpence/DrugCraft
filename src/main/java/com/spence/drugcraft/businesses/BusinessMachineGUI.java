package com.spence.drugcraft.businesses;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.EconomyManager;
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

public class BusinessMachineGUI {
    private final DrugCraft plugin;
    private final BusinessManager businessManager;

    public BusinessMachineGUI(DrugCraft plugin, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player, Business business) {
        if (!businessManager.canAccessMachine(player, business)) {
            MessageUtils.sendMessage(player, "business.no-access");
            return;
        }
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.business-machine.title-main")).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack stock = new ItemStack(Material.CHEST);
        ItemMeta stockMeta = stock.getItemMeta();
        stockMeta.displayName(MessageUtils.color("Manage Stock").color(TextColor.fromHexString("#55FF55")));
        stock.setItemMeta(stockMeta);
        inventory.setItem(10, stock);

        ItemStack revenue = new ItemStack(Material.EMERALD);
        ItemMeta revenueMeta = revenue.getItemMeta();
        revenueMeta.displayName(MessageUtils.color("Collect Revenue").color(TextColor.fromHexString("#55FF55")));
        List<Component> revenueLore = new ArrayList<>();
        revenueLore.add(MessageUtils.color("Revenue: $" + String.format("%.2f", business.getRevenue())));
        revenueMeta.lore(revenueLore);
        revenue.setItemMeta(revenueMeta);
        inventory.setItem(12, revenue);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        plugin.getActiveMenus().put(player.getUniqueId(), new ActiveGUI("BUSINESS_MACHINE", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened business machine main menu for player " + player.getName());
    }

    public void openStockMenu(Player player, Business business) {
        if (!businessManager.canAccessMachine(player, business)) {
            MessageUtils.sendMessage(player, "business.no-access");
            return;
        }
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.business-machine.title-stock")).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color("Back").color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        plugin.getActiveMenus().put(player.getUniqueId(), new ActiveGUI("BUSINESS_MACHINE", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened stock menu for player " + player.getName());
    }
}