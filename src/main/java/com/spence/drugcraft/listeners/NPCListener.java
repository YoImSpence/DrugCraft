package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.milkbowl.vault.economy.Economy;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class NPCListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Economy economy;
    private final Map<UUID, String> activeGUIs = new HashMap<>();
    private final Logger logger;

    public NPCListener(DrugCraft plugin, DrugManager drugManager, Economy economy) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economy = economy;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().getName().equalsIgnoreCase("Dealer")) {
            return;
        }
        Player player = event.getClicker();
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.use")) {
            player.sendMessage(MessageUtils.color("&cYou do not have permission to interact with this NPC."));
            return;
        }
        openMainMenu(player);
    }

    private void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(player, 9, MessageUtils.color("&eDrug Trade Hub"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, i == 0 || i == 8 ? border : filler);
        }
        ItemStack buySeeds = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta seedsMeta = buySeeds.getItemMeta();
        seedsMeta.setDisplayName(MessageUtils.color("&aBuy Crop Seeds"));
        seedsMeta.setLore(Arrays.asList(MessageUtils.color("&7Purchase seeds for planting")));
        buySeeds.setItemMeta(seedsMeta);
        gui.setItem(2, buySeeds);

        ItemStack buyDrugs = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta drugsMeta = buyDrugs.getItemMeta();
        drugsMeta.setDisplayName(MessageUtils.color("&cBuy Drug Items"));
        drugsMeta.setLore(Arrays.asList(MessageUtils.color("&7Purchase ready-to-use drugs")));
        buyDrugs.setItemMeta(drugsMeta);
        gui.setItem(4, buyDrugs);

        ItemStack sellDrugs = new ItemStack(Material.GOLD_INGOT);
        ItemMeta sellMeta = sellDrugs.getItemMeta();
        sellMeta.setDisplayName(MessageUtils.color("&eSell Drug Items"));
        sellMeta.setLore(Arrays.asList(MessageUtils.color("&7Sell drugs for profit")));
        sellDrugs.setItemMeta(sellMeta);
        gui.setItem(6, sellDrugs);

        player.openInventory(gui);
        activeGUIs.put(player.getUniqueId(), "main");
    }

    private void openBuySeedsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, MessageUtils.color("&aSeed Market"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int index = 0;
        for (Drug drug : drugs) {
            if (drug.hasSeed() && index < slots.length) {
                ItemStack seedItem = drug.getSeedItem(drug.getQuality());
                ItemMeta meta = seedItem.getItemMeta();
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(MessageUtils.color("&eClick to Buy: &a$" + drug.getBuyPrice()));
                meta.setLore(lore);
                seedItem.setItemMeta(meta);
                gui.setItem(slots[index], seedItem);
                index++;
            }
        }
        logger.fine("Opened Buy Seeds GUI for " + player.getName() + " with " + index + " seeds");
        player.openInventory(gui);
        activeGUIs.put(player.getUniqueId(), "buy_seeds");
    }

    private void openBuyDrugsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, MessageUtils.color("&cDrug Bazaar"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int index = 0;
        for (Drug drug : drugs) {
            if (index < slots.length) {
                ItemStack drugItem = drug.getItem(drug.getQuality());
                ItemMeta meta = drugItem.getItemMeta();
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(MessageUtils.color("&eClick to Buy: &a$" + drug.getBuyPrice()));
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
                gui.setItem(slots[index], drugItem);
                index++;
            }
        }
        logger.fine("Opened Buy Drugs GUI for " + player.getName() + " with " + index + " drugs");
        player.openInventory(gui);
        activeGUIs.put(player.getUniqueId(), "buy_drugs");
    }

    private void openSellDrugsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, MessageUtils.color("&eDrug Exchange"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int index = 0;
        for (Drug drug : drugs) {
            if (index < slots.length) {
                ItemStack drugItem = drug.getItem(null);
                ItemMeta meta = drugItem.getItemMeta();
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(MessageUtils.color("&eClick to Sell: &a$" + drug.sellPrice()));
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
                gui.setItem(slots[index], drugItem);
                index++;
            }
        }
        logger.fine("Opened Sell Drugs GUI for " + player.getName() + " with " + index + " drugs");
        player.openInventory(gui);
        activeGUIs.put(player.getUniqueId(), "sell_drugs");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String guiType = activeGUIs.get(player.getUniqueId());
        if (guiType == null) {
            return;
        }
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null ||
                clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.RED_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.YELLOW_STAINED_GLASS_PANE) {
            return;
        }

        if (guiType.equals("main")) {
            if (clickedItem.getType() == Material.WHEAT_SEEDS) {
                openBuySeedsGUI(player);
            } else if (clickedItem.getType() == Material.BLAZE_POWDER) {
                openBuyDrugsGUI(player);
            } else if (clickedItem.getType() == Material.GOLD_INGOT) {
                openSellDrugsGUI(player);
            }
        } else if (guiType.equals("buy_seeds")) {
            List<Drug> drugs = drugManager.getSortedDrugs();
            for (Drug drug : drugs) {
                if (drug.hasSeed() && clickedItem.getType() == drug.getSeedItem(null).getType() &&
                        clickedItem.getItemMeta().getDisplayName().equals(drug.getSeedItem(null).getItemMeta().getDisplayName())) {
                    buyItem(player, drug.getSeedItem(drug.getQuality()), drug.getBuyPrice(), drug.getName() + " Seed");
                    return;
                }
            }
        } else if (guiType.equals("buy_drugs")) {
            List<Drug> drugs = drugManager.getSortedDrugs();
            for (Drug drug : drugs) {
                if (clickedItem.getType() == drug.getItem(null).getType() &&
                        clickedItem.getItemMeta().getDisplayName().equals(drug.getItem(null).getItemMeta().getDisplayName())) {
                    buyItem(player, drug.getItem(drug.getQuality()), drug.getBuyPrice(), drug.getName());
                    return;
                }
            }
        } else if (guiType.equals("sell_drugs")) {
            List<Drug> drugs = drugManager.getSortedDrugs();
            for (Drug drug : drugs) {
                if (clickedItem.getType() == drug.getItem(null).getType() &&
                        clickedItem.getItemMeta().getDisplayName().equals(drug.getItem(null).getItemMeta().getDisplayName())) {
                    sellItem(player, drug, drug.sellPrice(), drug.getName());
                    return;
                }
            }
        }
    }

    private void buyItem(Player player, ItemStack item, double price, String itemName) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            player.sendMessage(MessageUtils.color("&cEconomy system is not available!"));
            return;
        }
        if (economy.has(player, price)) {
            economy.withdrawPlayer(player, price);
            player.getInventory().addItem(item);
            player.sendMessage(MessageUtils.color("&aPurchased " + itemName + " for $" + price));
        } else {
            player.sendMessage(MessageUtils.color("&cYou do not have enough money to buy " + itemName + "!"));
        }
    }

    private void sellItem(Player player, Drug drug, double price, String itemName) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            player.sendMessage(MessageUtils.color("&cEconomy system is not available!"));
            return;
        }
        ItemStack drugItem = drug.getItem(null);
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == drugItem.getType() &&
                    item.getItemMeta().getDisplayName().equals(drugItem.getItemMeta().getDisplayName())) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().remove(item);
                }
                economy.depositPlayer(player, price);
                player.sendMessage(MessageUtils.color("&aSold " + itemName + " for $" + price));
                return;
            }
        }
        player.sendMessage(MessageUtils.color("&cYou do not have " + itemName + " to sell!"));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        String guiType = activeGUIs.get(player.getUniqueId());
        if (guiType != null && (guiType.equals("main") || guiType.equals("buy_seeds") ||
                guiType.equals("buy_drugs") || guiType.equals("sell_drugs"))) {
            activeGUIs.remove(player.getUniqueId());
        }
    }
}