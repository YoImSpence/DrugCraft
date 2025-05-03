package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import de.tr7zw.nbtapi.NBTItem;
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
        String title = plugin.getConfig().getString("gui.main_menu_title", "DrugCraft Menu");
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', title));
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
        String title = plugin.getConfig().getString("gui.buy_menu_title", "Buy Drugs");
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', title));
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
        String title = plugin.getConfig().getString("gui.sell_menu_title", "Sell Drugs");
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', title));
        ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Place drugs here to sell");
        placeholder.setItemMeta(meta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, placeholder);
        }
        player.openInventory(inventory);
    }

    public void openGiveMenu(Player player) {
        if (!player.hasPermission("drugcraft.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to access this menu.");
            return;
        }
        String title = plugin.getConfig().getString("gui.give_menu_title", "Give Drugs");
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', title));
        for (Drug drug : drugManager.getDrugs().values()) {
            inventory.addItem(drug.getItem());
            if (drug.hasSeed()) {
                inventory.addItem(drug.getSeedItem());
            }
        }
        player.openInventory(inventory);
    }

    public void handleBuy(Player player, ItemStack item) {
        if (!drugManager.isDrugItem(item)) {
            return;
        }
        NBTItem nbtItem = new NBTItem(item);
        String drugId = nbtItem.getString("drug_id");
        Drug drug = drugManager.getDrug(drugId);
        if (drug != null && economy.has(player, drug.getPrice())) {
            economy.withdrawPlayer(player, drug.getPrice());
            player.getInventory().addItem(drug.getItem());
            player.sendMessage(ChatColor.GREEN + "Purchased " + drug.getName() + " for $" + drug.getPrice());
            logger.info(player.getName() + " bought " + drug.getName());
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough money!");
        }
    }

    public void handleSell(Player player, ItemStack item) {
        if (!drugManager.isDrugItem(item)) {
            return;
        }
        NBTItem nbtItem = new NBTItem(item);
        String drugId = nbtItem.getString("drug_id");
        Drug drug = drugManager.getDrug(drugId);
        if (drug != null) {
            economy.depositPlayer(player, drug.getPrice());
            item.setAmount(item.getAmount() - 1);
            player.sendMessage(ChatColor.GREEN + "Sold " + drug.getName() + " for $" + drug.getPrice());
            logger.info(player.getName() + " sold " + drug.getName());
        }
    }

    public void handleGive(Player player, ItemStack item) {
        if (!player.hasPermission("drugcraft.admin")) {
            return;
        }
        if (drugManager.isDrugItem(item)) {
            player.getInventory().addItem(item.clone());
            player.sendMessage(ChatColor.GREEN + "Gave " + drugManager.getDrug(drugManager.getDrugIdFromItem(item)).getName());
            logger.info(player.getName() + " gave drug item");
        } else if (drugManager.isSeedItem(item)) {
            player.getInventory().addItem(item.clone());
            player.sendMessage(ChatColor.GREEN + "Gave " + drugManager.getDrug(drugManager.getDrugIdFromSeed(item)).getName() + " Seed");
            logger.info(player.getName() + " gave seed item");
        }
    }
}