package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class NPCListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Economy economy;
    private final Map<UUID, Inventory> activeInventories = new HashMap<>();
    private final Map<UUID, String> activeMenuTypes = new HashMap<>();

    public NPCListener(DrugCraft plugin, DrugManager drugManager, Economy economy) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economy = economy;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().getName().equalsIgnoreCase("Dealer")) return;
        Player player = event.getClicker();
        openMainMenu(player);
    }

    private void openMainMenu(Player player) {
        Inventory mainMenu = Bukkit.createInventory(null, 27, MessageUtils.color("&#4682B4&lDrug Dealer"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            mainMenu.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack buySeeds = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta buySeedsMeta = buySeeds.getItemMeta();
        buySeedsMeta.setDisplayName(MessageUtils.color("&#FFFF00Buy Seeds"));
        buySeedsMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Purchase drug seeds")));
        buySeeds.setItemMeta(buySeedsMeta);
        mainMenu.setItem(11, buySeeds);

        ItemStack buyDrugs = new ItemStack(Material.SUGAR);
        ItemMeta buyDrugsMeta = buyDrugs.getItemMeta();
        buyDrugsMeta.setDisplayName(MessageUtils.color("&#FFFF00Buy Drugs"));
        buyDrugsMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Purchase drug items")));
        buyDrugs.setItemMeta(buyDrugsMeta);
        mainMenu.setItem(13, buyDrugs);

        ItemStack sellDrugs = new ItemStack(Material.GOLD_INGOT);
        ItemMeta sellDrugsMeta = sellDrugs.getItemMeta();
        sellDrugsMeta.setDisplayName(MessageUtils.color("&#FFFF00Sell Drugs"));
        sellDrugsMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Sell your drug items")));
        sellDrugs.setItemMeta(sellDrugsMeta);
        mainMenu.setItem(15, sellDrugs);

        activeInventories.put(player.getUniqueId(), mainMenu);
        activeMenuTypes.put(player.getUniqueId(), "main");
        player.openInventory(mainMenu);
    }

    private void openBuySeedsMenu(Player player) {
        Inventory buySeedsInventory = Bukkit.createInventory(null, 54, MessageUtils.color("&#4682B4&lBuy Seeds"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            buySeedsInventory.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(MessageUtils.color("&#FFFF00Back to Main Menu"));
        back.setItemMeta(backMeta);
        buySeedsInventory.setItem(45, back);

        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int index = 0;
        for (Drug drug : drugs) {
            if (drug.hasSeed() && index < slots.length) {
                String quality = drug.getQuality();
                ItemStack seedItem = drugManager.getSeedItem(drug.getDrugId(), quality);
                if (seedItem == null) continue;
                ItemMeta meta = seedItem.getItemMeta();
                List<String> lore = new ArrayList<>(meta.getLore());
                lore.add(MessageUtils.color("&#D3D3D3Buy Price: $" + drug.getBuyPrice(quality)));
                lore.add(MessageUtils.color("&#FFFF00Click to buy"));
                meta.setLore(lore);
                seedItem.setItemMeta(meta);
                buySeedsInventory.setItem(slots[index], seedItem);
                index++;
            }
        }
        activeInventories.put(player.getUniqueId(), buySeedsInventory);
        activeMenuTypes.put(player.getUniqueId(), "buy_seeds");
        player.openInventory(buySeedsInventory);
    }

    private void openBuyDrugsMenu(Player player) {
        Inventory buyDrugsInventory = Bukkit.createInventory(null, 54, MessageUtils.color("&#4682B4&lBuy Drugs"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            buyDrugsInventory.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(MessageUtils.color("&#FFFF00Back to Main Menu"));
        back.setItemMeta(backMeta);
        buyDrugsInventory.setItem(45, back);

        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int index = 0;
        for (Drug drug : drugs) {
            if (index < slots.length) {
                String quality = drug.getQuality();
                ItemStack drugItem = drugManager.getDrugItem(drug.getDrugId(), quality);
                ItemMeta meta = drugItem.getItemMeta();
                List<String> lore = new ArrayList<>(meta.getLore());
                lore.add(MessageUtils.color("&#D3D3D3Buy Price: $" + drug.getBuyPrice(quality)));
                lore.add(MessageUtils.color("&#D3D3D3Sell Price: $" + drug.getSellPrice(quality)));
                lore.add(MessageUtils.color("&#FFFF00Click to buy"));
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
                buyDrugsInventory.setItem(slots[index], drugItem);
                index++;
            }
        }
        activeInventories.put(player.getUniqueId(), buyDrugsInventory);
        activeMenuTypes.put(player.getUniqueId(), "buy_drugs");
        player.openInventory(buyDrugsInventory);
    }

    private void openSellDrugsMenu(Player player) {
        Inventory sellDrugsInventory = Bukkit.createInventory(null, 54, MessageUtils.color("&#4682B4&lSell Drugs"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            sellDrugsInventory.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(MessageUtils.color("&#FFFF00Back to Main Menu"));
        back.setItemMeta(backMeta);
        sellDrugsInventory.setItem(45, back);

        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int index = 0;
        for (Drug drug : drugs) {
            if (index < slots.length) {
                String quality = drug.getQuality();
                ItemStack drugItem = drugManager.getDrugItem(drug.getDrugId(), quality);
                ItemMeta meta = drugItem.getItemMeta();
                List<String> lore = new ArrayList<>(meta.getLore());
                lore.add(MessageUtils.color("&#D3D3D3Sell Price: $" + drug.getSellPrice(quality)));
                lore.add(MessageUtils.color("&#FFFF00Click to sell from inventory"));
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
                sellDrugsInventory.setItem(slots[index], drugItem);
                index++;
            }
        }
        activeInventories.put(player.getUniqueId(), sellDrugsInventory);
        activeMenuTypes.put(player.getUniqueId(), "sell_drugs");
        player.openInventory(sellDrugsInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = activeInventories.get(player.getUniqueId());
        if (inventory == null || !event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        String menuType = activeMenuTypes.get(player.getUniqueId());
        if (menuType == null) return;

        if (menuType.equals("main")) {
            if (clickedItem.getType() == Material.WHEAT_SEEDS) {
                openBuySeedsMenu(player);
            } else if (clickedItem.getType() == Material.SUGAR) {
                openBuyDrugsMenu(player);
            } else if (clickedItem.getType() == Material.GOLD_INGOT) {
                openSellDrugsMenu(player);
            }
        } else if (menuType.equals("buy_seeds")) {
            if (clickedItem.getType() == Material.ARROW) {
                openMainMenu(player);
                return;
            }
            Drug drug = drugManager.getSortedDrugs().stream()
                    .filter(d -> d.hasSeed() && d.getSeedItem(d.getQuality()).getItemMeta().getDisplayName().equals(clickedItem.getItemMeta().getDisplayName()))
                    .findFirst()
                    .orElse(null);
            if (drug == null) return;
            String quality = drug.getQuality();
            double buyPrice = drug.getBuyPrice(quality);
            if (economy.has(player, buyPrice)) {
                economy.withdrawPlayer(player, buyPrice);
                ItemStack item = drugManager.getSeedItem(drug.getDrugId(), quality);
                player.getInventory().addItem(item);
                player.sendMessage(MessageUtils.color("&#FF7F00Bought " + drug.getName() + " Seed for $" + buyPrice));
            } else {
                player.sendMessage(MessageUtils.color("&#FF4040You do not have enough money to buy this."));
            }
        } else if (menuType.equals("buy_drugs")) {
            if (clickedItem.getType() == Material.ARROW) {
                openMainMenu(player);
                return;
            }
            Drug drug = drugManager.getSortedDrugs().stream()
                    .filter(d -> d.getItem(d.getQuality()).getItemMeta().getDisplayName().equals(clickedItem.getItemMeta().getDisplayName()))
                    .findFirst()
                    .orElse(null);
            if (drug == null) return;
            String quality = drug.getQuality();
            double buyPrice = drug.getBuyPrice(quality);
            if (economy.has(player, buyPrice)) {
                economy.withdrawPlayer(player, buyPrice);
                ItemStack item = drugManager.getDrugItem(drug.getDrugId(), quality);
                player.getInventory().addItem(item);
                player.sendMessage(MessageUtils.color("&#FF7F00Bought " + drug.getName() + " for $" + buyPrice));
            } else {
                player.sendMessage(MessageUtils.color("&#FF4040You do not have enough money to buy this."));
            }
        } else if (menuType.equals("sell_drugs")) {
            if (clickedItem.getType() == Material.ARROW) {
                openMainMenu(player);
                return;
            }
            Drug drug = drugManager.getSortedDrugs().stream()
                    .filter(d -> d.getItem(d.getQuality()).getItemMeta().getDisplayName().equals(clickedItem.getItemMeta().getDisplayName()))
                    .findFirst()
                    .orElse(null);
            if (drug == null) return;
            String quality = drug.getQuality();
            ItemStack toSell = null;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && drugManager.isDrugItem(item) &&
                        item.getItemMeta().getDisplayName().equals(drug.getItem(quality).getItemMeta().getDisplayName())) {
                    toSell = item;
                    break;
                }
            }
            if (toSell != null) {
                double sellPrice = drug.getSellPrice(quality);
                economy.depositPlayer(player, sellPrice);
                toSell.setAmount(toSell.getAmount() - 1);
                player.sendMessage(MessageUtils.color("&#FF7F00Sold " + drug.getName() + " for $" + sellPrice));
            } else {
                player.sendMessage(MessageUtils.color("&#FF4040You do not have this drug in your inventory."));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        activeInventories.remove(player.getUniqueId());
        activeMenuTypes.remove(player.getUniqueId());
    }
}