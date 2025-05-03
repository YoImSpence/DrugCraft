package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.DrugGUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {
    private final DrugCraft plugin;
    private final DrugGUI drugGUI;
    private final DrugManager drugManager;
    private final Economy economy;

    public InventoryClickListener(DrugCraft plugin, DrugGUI drugGUI, DrugManager drugManager, Economy economy) {
        this.plugin = plugin;
        this.drugGUI = drugGUI;
        this.drugManager = drugManager;
        this.economy = economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String mainTitle = plugin.getConfig().getString("gui.main_menu_title", "DrugCraft Menu");
        String buyTitle = plugin.getConfig().getString("gui.buy_menu_title", "Buy Drugs");
        String sellTitle = plugin.getConfig().getString("gui.sell_menu_title", "Sell Drugs");
        String giveTitle = plugin.getConfig().getString("gui.give_menu_title", "Give Drugs");
        String title = event.getView().getTitle();

        if (!title.equals(ChatColor.translateAlternateColorCodes('&', mainTitle)) &&
                !title.equals(ChatColor.translateAlternateColorCodes('&', buyTitle)) &&
                !title.equals(ChatColor.translateAlternateColorCodes('&', sellTitle)) &&
                !title.equals(ChatColor.translateAlternateColorCodes('&', giveTitle))) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        if (title.equals(ChatColor.translateAlternateColorCodes('&', mainTitle))) {
            if (clickedItem.getType() == Material.EMERALD) {
                drugGUI.openBuyMenu(player);
            } else if (clickedItem.getType() == Material.DIAMOND) {
                drugGUI.openSellMenu(player);
            } else if (clickedItem.getType() == Material.COMMAND_BLOCK && player.hasPermission("drugcraft.admin")) {
                drugGUI.openGiveMenu(player);
            }
        } else if (title.equals(ChatColor.translateAlternateColorCodes('&', buyTitle))) {
            drugGUI.handleBuy(player, clickedItem);
        } else if (title.equals(ChatColor.translateAlternateColorCodes('&', sellTitle))) {
            if (clickedItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                drugGUI.handleSell(player, clickedItem);
            }
        } else if (title.equals(ChatColor.translateAlternateColorCodes('&', giveTitle))) {
            drugGUI.handleGive(player, clickedItem);
        }
    }
}