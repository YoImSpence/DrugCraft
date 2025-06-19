package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.Business;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BusinessMachineGUI {
    private final DrugCraft plugin;
    private final BusinessManager businessManager;

    public BusinessMachineGUI(DrugCraft plugin, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        Business business = businessManager.getBusinessByPlayer(player);
        if (business == null) {
            MessageUtils.sendMessage(player, "business.not-owned");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, Component.text(MessageUtils.getMessage("<gradient:#00FF00:#FFFFFF>Business Machine</gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS_MACHINE", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack income = new ItemStack(Material.GOLD_INGOT);
        ItemMeta incomeMeta = income.getItemMeta();
        incomeMeta.setDisplayName(MessageUtils.getMessage("business.income"));
        incomeMeta.setLore(List.of("Revenue: $" + business.getRevenue()));
        income.setItemMeta(incomeMeta);

        ItemStack upgrades = new ItemStack(Material.DIAMOND);
        ItemMeta upgradesMeta = upgrades.getItemMeta();
        upgradesMeta.setDisplayName(MessageUtils.getMessage("business.upgrades"));
        upgrades.setItemMeta(upgradesMeta);

        inv.setItem(11, income);
        inv.setItem(15, upgrades);

        if (player.hasPermission("drugcraft.admin")) {
            ItemStack adminMode = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta adminMeta = adminMode.getItemMeta();
            adminMeta.setDisplayName(MessageUtils.getMessage("business.admin-mode"));
            adminMode.setItemMeta(adminMeta);
            inv.setItem(22, adminMode);
        }

        player.openInventory(inv);
    }

    public void openUpgradesMenu(Player player) {
        Business business = businessManager.getBusinessByPlayer(player);
        if (business == null) {
            MessageUtils.sendMessage(player, "business.not-owned");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, Component.text(MessageUtils.getMessage("<gradient:#00FF00:#FFFFFF>Business Upgrades</gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS_MACHINE", inv, "upgrades");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack incomeUpgrade = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta incomeMeta = incomeUpgrade.getItemMeta();
        incomeMeta.setDisplayName(MessageUtils.getMessage("business.upgrade-income"));
        incomeUpgrade.setItemMeta(incomeMeta);

        inv.setItem(13, incomeUpgrade);
        player.openInventory(inv);
    }
}