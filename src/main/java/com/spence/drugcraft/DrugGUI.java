package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DrugGUI {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Economy economy;
    private final Logger logger;

    public DrugGUI(DrugCraft plugin, DrugManager drugManager, Economy economy) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economy = economy;
        this.logger = plugin.getLogger();
    }

    public void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + "DrugCraft Menu");
        ItemStack buyItem = new ItemStack(Material.EMERALD);
        ItemMeta buyMeta = buyItem.getItemMeta();
        buyMeta.setDisplayName(ChatColor.GREEN + "Buy Drugs");
        buyItem.setItemMeta(buyMeta);
        inventory.setItem(0, buyItem);

        ItemStack sellItem = new ItemStack(Material.DIAMOND);
        ItemMeta sellMeta = sellItem.getItemMeta();
        sellMeta.setDisplayName(ChatColor.YELLOW + "Sell Drugs");
        sellItem.setItemMeta(sellMeta);
        inventory.setItem(1, sellItem);

        if (player.hasPermission("drugcraft.admin")) {
            ItemStack giveItem = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta giveMeta = giveItem.getItemMeta();
            giveMeta.setDisplayName(ChatColor.RED + "Give Drugs");
            giveItem.setItemMeta(giveMeta);
            inventory.setItem(2, giveItem);
        }

        player.openInventory(inventory);
    }

    public void openBuyMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Buy Drugs");
        for (Drug drug : drugManager.getDrugs().values()) {
            ItemStack item = drug.getItem();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>(meta.getLore() != null ? meta.getLore() : new ArrayList<>());
            lore.add(ChatColor.GOLD + "Price: $" + drug.getPrice());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.addItem(item);
        }
        player.openInventory(inventory);
    }

    public void openSellMenu(Player player) {