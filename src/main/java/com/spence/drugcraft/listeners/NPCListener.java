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
import java.util.stream.Collectors;

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
        if (!event.getNPC().getName().equalsIgnoreCase("Dealer")) return;
        Player player = event.getClicker();
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.use")) {
            player.sendMessage(MessageUtils.color("&#FF4040You do not have permission to interact with this NPC."));
            return;
        }
        openMainMenu(player);
    }

    private void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(player, 9, MessageUtils.color("&#FFD700Drug Trade Hub"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, i == 0 || i == 8 ? border : filler);
        }
        ItemStack buySeeds = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta seedsMeta = buySeeds.getItemMeta();
        seedsMeta.setDisplayName(MessageUtils.color(" CD32Buy Crop Seeds"));
        seedsMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Purchase seeds for planting")));
        buySeeds.setItemMeta(seedsMeta);
        gui.setItem(2, buySeeds);

        ItemStack buyDrugs = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta drugsMeta = buyDrugs.getItemMeta();
        drugsMeta.setDisplayName(MessageUtils.color("&#FF5555Buy Drug Items"));
        drugsMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Purchase ready-to-use drugs")));
        buyDrugs.setItemMeta(drugsMeta);
        gui.setItem(4, buyDrugs);

        ItemStack sellDrugs = new ItemStack(Material.GOLD_INGOT);
        ItemMeta sellMeta = sellDrugs.getItemMeta();
        sellMeta.setDisplayName(MessageUtils.color("&#FFFF00Sell Drug Items"));
        sellMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Sell drugs for profit")));
        sellDrugs.setItemMeta(sellMeta);
        gui.setItem(6, sellDrugs);

        player.openInventory(gui);
        activeGUIs.put(player.getUniqueId(), "main");
    }

    private void openBuySeedsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, MessageUtils.color(" CD32Seed Market"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int index = 0;
        for (Drug drug : drugs) {
            if (drug.hasSeed() && index < slots.length) {
                String quality = drug.getQuality();
                ItemStack seedItem = drugManager.getSeedItem(drug.getDrugId(), quality);
                ItemMeta meta = seedItem.getItemMeta();
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(MessageUtils.color("&#FFFF00Click to Buy: �FF7F$" + drug.getBuyPrice(quality)));
                meta.setLore(lore);
                seedItem.setItemMeta(meta);
                gui.setItem(slots[index], seedItem);
                index++;
            }
        }
        logger.fine("Opened Buy Seeds GUI for " + player.getName() + " with " + index + " seeds: " +
                drugs.stream().filter(Drug::hasSeed).map(d -> d.getName() + " (" + d.getQuality() + ")").collect(Collectors.joining(", ")));
        player.openInventory(gui);
        activeGUIs.put(player.getUniqueId(), "buy_seeds");
    }

    private void openBuyDrugsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, MessageUtils.color("&#FF5555Drug Bazaar"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int index = 0;
        for (Drug drug : drugs) {
            if (index < slots.length) {
                String quality = drug.getQuality();
                ItemStack drugItem = drugManager.getDrugItem(drug.getDrugId(), quality);
                ItemMeta meta = drugItem.getItemMeta();
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(MessageUtils.color("&#FFFF00Click to Buy: �FF7F$" + drug.getBuyPrice(quality)));
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
                gui.setItem(slots[index], drugItem);
                index++;
            }
        }
        logger.fine("Opened Buy Drugs GUI for " + player.getName() + " with " + index + " drugs: " +
                drugs.stream().map(d -> d.getName() + " (" + d.getQuality() + ")").collect(Collectors.joining(", ")));
        player.openInventory(gui);
        activeGUIs.put(player.getUniqueId(), "buy_drugs");
    }

    private void openSellDrugsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, MessageUtils.color("&#FFFF00Drug Exchange"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        int index = 0;
        for (Drug drug : drugs) {
            if (index < slots.length) {
                String quality = drug.getQuality();
                ItemStack drugItem = drugManager.getDrugItem(drug.getDrugId(), quality);
                ItemMeta meta = drugItem.getItemMeta();
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(MessageUtils.color("&#FFFF00Click to Sell: �FF7F$" + drug.getSellPrice(quality)));
                meta.setLore(lore);
                drugItem.setItemMeta(meta);
                gui.setItem(slots[index], drugItem);
                index++;
            }
        }
        logger.fine("Opened Sell Drugs GUI for " + player.getName() + " with " + index + " drugs: " +
                drugs.stream().map(d -> d.getName() + " (" + d.getQuality() + ")").collect(Collectors.joining(", ")));
        player.openInventory(gui);
        activeGUIs.put(player.getUniqueId(), "sell_drugs");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String guiType = activeGUIs.get(player.getUniqueId());
        if (guiType == null) return;
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null ||
                clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.RED_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.YELLOW_STAINED_GLASS_PANE) return;

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
                if (drug.hasSeed() && clickedItem.getType() == drug.getSeedItem(drug.getQuality()).getType() &&
                        clickedItem.getItemMeta().getDisplayName().equals(drug.getSeedItem(drug.getQuality()).getItemMeta().getDisplayName())) {
                    String quality = drug.getQuality();
                    buyItem(player, drug.getSeedItem(quality), drug.getBuyPrice(quality), drug.getName() + " Seed");
                    return;
                }
            }
        } else if (guiType.equals("buy_drugs")) {
            List<Drug> drugs = drugManager.getSortedDrugs();
            for (Drug drug : drugs) {
                if (clickedItem.getType() == drug.getItem(drug.getQuality()).getType() &&
                        clickedItem.getItemMeta().getDisplayName().equals(drug.getItem(drug.getQuality()).getItemMeta().getDisplayName())) {
                    String quality = drug.getQuality();
                    buyItem(player, drug.getItem(quality), drug.getBuyPrice(quality), drug.getName());
                    return;
                }
            }
        } else if (guiType.equals("sell_drugs")) {
            List<Drug> drugs = drugManager.getSortedDrugs();
            for (Drug drug : drugs) {
                if (clickedItem.getType() == drug.getItem(drug.getQuality()).getType() &&
                        clickedItem.getItemMeta().getDisplayName().equals(drug.getItem(drug.getQuality()).getItemMeta().getDisplayName())) {
                    String quality = drug.getQuality();
                    sellItem(player, drug, quality, drug.getSellPrice(quality), drug.getName());
                    return;
                }
            }
        }
    }

    private void buyItem(Player player, ItemStack item, double price, String itemName) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            player.sendMessage(MessageUtils.color("&#FF4040Economy system is not available!"));
            return;
        }
        if (economy.has(player, price)) {
            economy.withdrawPlayer(player, price);
            player.getInventory().addItem(item);
            player.sendMessage(MessageUtils.color("�FF7FPurchased " + itemName + " for $" + price));
            logger.info("Player " + player.getName() + " bought " + itemName + " for $" + price);
        } else {
            player.sendMessage(MessageUtils.color("&#FF4040You do not have enough money to buy " + itemName + "!"));
        }
    }

    private void sellItem(Player player, Drug drug, String quality, double price, String itemName) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            player.sendMessage(MessageUtils.color("&#FF4040Economy system is not available!"));
            return;
        }
        ItemStack drugItem = drug.getItem(quality);
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == drugItem.getType() &&
                    item.getItemMeta().getDisplayName().equals(drugItem.getItemMeta().getDisplayName()) &&
                    drugManager.getQualityFromItem(item).equals(quality)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().remove(item);
                }
                economy.depositPlayer(player, price);
                player.sendMessage(MessageUtils.color("�FF7FSold " + itemName + " (" + quality + ") for $" + price));
                logger.info("Player " + player.getName() + " sold " + itemName + " (" + quality + ") for $" + price);
                return;
            }
        }
        player.sendMessage(MessageUtils.color("&#FF4040You do not have " + itemName + " (" + quality + ") to sell!"));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String guiType = activeGUIs.get(player.getUniqueId());
        if (guiType != null && (guiType.equals("main") || guiType.equals("buy_seeds") ||
                guiType.equals("buy_drugs") || guiType.equals("sell_drugs"))) {
            activeGUIs.remove(player.getUniqueId());
        }
    }
}