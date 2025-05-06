package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class AdminGUI {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Economy economy;
    private final Logger logger;
    private final Map<UUID, ItemStack> selectedItems = new HashMap<>();
    private final Map<UUID, Boolean> selectedIsSeed = new HashMap<>();

    public AdminGUI(DrugCraft plugin, DrugManager drugManager, Economy economy) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economy = economy;
        this.logger = plugin.getLogger();
    }

    public void openGiveMenu(Player player) {
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.admin")) {
            MessageUtils.sendMessage(player, "&cYou do not have permission to use this menu.");
            return;
        }
        String title = "&cAdmin Give Drugs";
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', title));
        List<Drug> drugs = new ArrayList<>(drugManager.getDrugs().values());
        logger.info("Opening Admin Give GUI for " + player.getName() + ": " + drugs.size() + " drugs available");

        // Add border with red stained glass panes
        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(ChatColor.RESET + "");
            border.setItemMeta(borderMeta);
        }
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        if (drugs.isEmpty()) {
            ItemStack placeholder = new ItemStack(Material.BARRIER);
            ItemMeta meta = placeholder.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "No Drugs Available");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Check drugs.yml and server logs for errors.");
                meta.setLore(lore);
                placeholder.setItemMeta(meta);
            }
            inventory.setItem(22, placeholder);
            player.openInventory(inventory);
            MessageUtils.sendMessage(player, "&cNo drugs loaded. Check server logs for errors.");
            return;
        }

        // Add all drugs and seeds
        int slot = 10; // Start at first non-border slot
        for (Drug drug : drugs) {
            ItemStack item = drug.getItem();
            if (item == null) {
                logger.warning("Null item for drug: " + drug.getId());
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>(meta != null && meta.getLore() != null ? meta.getLore() : new ArrayList<>());
            lore.add(ChatColor.GRAY + "Click to select quantity");
            if (meta != null) {
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
            logger.info("Added drug to Admin GUI: " + drug.getId());
            slot++;
            if (slot == 17) slot = 19; // Move to next row
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;

            if (drug.hasSeed()) {
                ItemStack seed = drug.getSeedItem();
                if (seed == null) {
                    logger.warning("Null seed item for drug: " + drug.getId());
                    continue;
                }
                meta = seed.getItemMeta();
                lore = new ArrayList<>(meta != null && meta.getLore() != null ? meta.getLore() : new ArrayList<>());
                lore.add(ChatColor.GRAY + "Click to select quantity");
                if (meta != null) {
                    meta.setLore(lore);
                    seed.setItemMeta(meta);
                }
                inventory.setItem(slot, seed);
                logger.info("Added seed to Admin GUI: " + drug.getId());
                slot++;
                if (slot == 17) slot = 19;
                else if (slot == 26) slot = 28;
                else if (slot == 35) slot = 37;
            }
        }

        player.openInventory(inventory);
    }

    public void promptQuantity(Player player, ItemStack item, boolean isSeed) {
        selectedItems.put(player.getUniqueId(), item.clone());
        selectedIsSeed.put(player.getUniqueId(), isSeed);
        MessageUtils.sendMessage(player, "&ePlease enter a quantity (1-64) in chat.");
        player.closeInventory();
    }

    public void handleGive(Player player, int quantity, boolean isSeed) {
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.admin")) {
            return;
        }
        ItemStack item = selectedItems.remove(player.getUniqueId());
        Boolean isSeedFlag = selectedIsSeed.remove(player.getUniqueId());
        if (item == null || isSeedFlag == null) {
            MessageUtils.sendMessage(player, "&cNo item selected for giving.");
            logger.warning("No item selected for player " + player.getName());
            return;
        }
        String drugId = isSeedFlag ? drugManager.getDrugIdFromSeed(item) : drugManager.getDrugIdFromItem(item);
        Drug drug = drugManager.getDrug(drugId);
        if (drug == null) {
            MessageUtils.sendMessage(player, "&cInvalid drug selected.");
            logger.warning("Invalid drug ID: " + drugId);
            return;
        }
        if (quantity < 1 || quantity > 64) {
            MessageUtils.sendMessage(player, "&cQuantity must be between 1 and 64.");
            return;
        }

        ItemStack giveItem = isSeedFlag ? drug.getSeedItem() : drug.getItem();
        giveItem.setAmount(quantity);
        player.getInventory().addItem(giveItem);
        String itemName = isSeedFlag ? drug.getName() + " Seed" : drug.getName();
        MessageUtils.sendMessage(player, "&aGave " + quantity + "x " + itemName);
        logger.info(player.getName() + " gave " + quantity + "x " + itemName);
    }

    public ItemStack getSelectedItem(UUID playerId) {
        return selectedItems.get(playerId);
    }

    public Boolean isSelectedSeed(UUID playerId) {
        return selectedIsSeed.get(playerId);
    }
}