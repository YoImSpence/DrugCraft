package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class NPCListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Economy economy;
    private final Logger logger;
    private final Map<UUID, ItemStack> selectedItems = new HashMap<>();
    private final Map<UUID, String> selectedAction = new HashMap<>();
    private final Map<UUID, Boolean> selectedIsSeed = new HashMap<>();
    private final Map<UUID, Boolean> awaitingQuantity = new HashMap<>();

    public NPCListener(DrugCraft plugin, DrugManager drugManager, Economy economy) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economy = economy;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        int npcId = event.getNPC().getId();
        ConfigurationSection npcConfig = plugin.getConfigManager().getConfig().getConfigurationSection("npcs." + npcId);
        if (npcConfig == null) {
            MessageUtils.sendMessage(player, "&cThis NPC is not configured for drug trading.");
            return;
        }

        String action = npcConfig.getString("action");
        if (action == null || (!action.equalsIgnoreCase("buy") && !action.equalsIgnoreCase("sell"))) {
            MessageUtils.sendMessage(player, "&cNPC configuration missing or invalid action.");
            return;
        }

        List<String> drugIds = npcConfig.getStringList("drugs");
        if (drugIds.isEmpty()) {
            MessageUtils.sendMessage(player, "&cNo drugs configured for this NPC.");
            return;
        }

        if (!player.hasPermission("drugcraft.use")) {
            MessageUtils.sendMessage(player, "&cYou don't have permission to trade with NPCs.");
            return;
        }

        openNPCMenu(player, action, drugIds);
    }

    private void openNPCMenu(Player player, String action, List<String> drugIds) {
        String title = action.equalsIgnoreCase("buy") ? "&2Buy Drugs" : "&eSell Drugs";
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', title));

        // Add border with colored glass panes
        ItemStack border = new ItemStack(action.equalsIgnoreCase("buy") ? Material.LIME_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE);
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

        // Add items (slots 10-16, 19-25, 28-34, 37-43)
        int slot = 10;
        for (String drugId : drugIds) {
            Drug drug = drugManager.getDrug(drugId);
            if (drug != null) {
                // Add drug item
                ItemStack item = drug.getItem();
                ItemMeta meta = item.getItemMeta();
                List<String> lore = new ArrayList<>(meta != null && meta.getLore() != null ? meta.getLore() : new ArrayList<>());
                lore.add(ChatColor.GOLD + (action.equalsIgnoreCase("buy") ? "Buy Price: $" : "Sell Price: $") + drug.getPrice());
                lore.add(ChatColor.GRAY + "Click to select quantity");
                if (meta != null) {
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(slot, item);
                logger.info("Added drug to " + action + " GUI: " + drugId);
                slot++;
                if (slot == 17) slot = 19;
                else if (slot == 26) slot = 28;
                else if (slot == 35) slot = 37;
                else if (slot == 44) break;

                // Add seed item for Buy GUI only
                if (action.equalsIgnoreCase("buy") && drug.hasSeed()) {
                    ItemStack seed = drug.getSeedItem();
                    meta = seed.getItemMeta();
                    lore = new ArrayList<>(meta != null && meta.getLore() != null ? meta.getLore() : new ArrayList<>());
                    lore.add(ChatColor.GOLD + "Buy Price: $" + drug.getPrice());
                    lore.add(ChatColor.GRAY + "Click to select quantity");
                    if (meta != null) {
                        meta.setLore(lore);
                        seed.setItemMeta(meta);
                    }
                    inventory.setItem(slot, seed);
                    logger.info("Added seed to Buy GUI: " + drugId);
                    slot++;
                    if (slot == 17) slot = 19;
                    else if (slot == 26) slot = 28;
                    else if (slot == 35) slot = 37;
                    else if (slot == 44) break;
                }
            } else {
                logger.warning("Invalid drug ID in NPC config: " + drugId);
            }
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.translateAlternateColorCodes('&', "&2Buy Drugs")) &&
                !title.equals(ChatColor.translateAlternateColorCodes('&', "&eSell Drugs"))) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        String action = title.contains("Buy") ? "buy" : "sell";
        String drugId = drugManager.isSeedItem(clickedItem) ? drugManager.getDrugIdFromSeed(clickedItem) : drugManager.getDrugIdFromItem(clickedItem);
        if (drugId == null) {
            logger.warning("No drug ID found for clicked item in " + action + " GUI");
            return;
        }
        Drug drug = drugManager.getDrug(drugId);
        if (drug == null) {
            logger.warning("Drug not found for ID: " + drugId + " in " + action + " GUI");
            return;
        }

        selectedItems.put(player.getUniqueId(), clickedItem.clone());
        selectedAction.put(player.getUniqueId(), action);
        selectedIsSeed.put(player.getUniqueId(), drugManager.isSeedItem(clickedItem));
        MessageUtils.sendMessage(player, "&ePlease enter a quantity (1-64) in chat.");
        player.closeInventory();
        awaitingQuantity.put(player.getUniqueId(), true);
        logger.info("Player " + player.getName() + " selected " + action + " for " + (drugManager.isSeedItem(clickedItem) ? "seed" : "drug") + ": " + drugId);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!awaitingQuantity.getOrDefault(playerId, false)) return;

        event.setCancelled(true);
        awaitingQuantity.remove(playerId);
        String message = event.getMessage().trim();

        try {
            int quantity = Integer.parseInt(message);
            if (quantity < 1 || quantity > 64) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "Quantity must be between 1 and 64."));
                return;
            }

            ItemStack item = selectedItems.remove(playerId);
            String action = selectedAction.remove(playerId);
            boolean isSeed = selectedIsSeed.remove(playerId);
            if (item == null || action == null) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "No item selected for transaction."));
                logger.warning("No item or action selected for player " + player.getName());
                return;
            }

            String drugId = isSeed ? drugManager.getDrugIdFromSeed(item) : drugManager.getDrugIdFromItem(item);
            if (drugId == null) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "Invalid drug selected."));
                logger.warning("Invalid drug ID for player " + player.getName() + " in " + action + " transaction");
                return;
            }
            Drug drug = drugManager.getDrug(drugId);
            if (drug == null) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "Invalid drug selected."));
                logger.warning("Drug not found for ID: " + drugId + " in " + action + " transaction");
                return;
            }

            if (action.equalsIgnoreCase("buy")) {
                double totalPrice = drug.getPrice() * quantity;
                if (economy.has(player, totalPrice)) {
                    economy.withdrawPlayer(player, totalPrice);
                    ItemStack giveItem = isSeed ? drug.getSeedItem() : drug.getItem();
                    giveItem.setAmount(quantity);
                    player.getInventory().addItem(giveItem);
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.GREEN + "Purchased " + quantity + "x " + (isSeed ? drug.getName() + " Seed" : drug.getName()) + " for $" + totalPrice));
                    logger.info(player.getName() + " bought " + quantity + "x " + (isSeed ? drug.getName() + " Seed" : drug.getName()) + " for $" + totalPrice);
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.RED + "You don't have enough money! Need $" + totalPrice));
                    logger.info(player.getName() + " failed to buy " + quantity + "x " + (isSeed ? drug.getName() + " Seed" : drug.getName()) + ": Insufficient funds");
                }
            } else {
                // Check player's inventory for the drug (not seeds)
                boolean hasEnough = false;
                int totalAmount = 0;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && drugManager.isDrugItem(invItem)) {
                        String invDrugId = drugManager.getDrugIdFromItem(invItem);
                        logger.fine("Checking inventory item with drug ID: " + invDrugId + " against target ID: " + drugId);
                        if (invDrugId != null && invDrugId.equals(drugId)) {
                            totalAmount += invItem.getAmount();
                            logger.fine("Found " + invItem.getAmount() + " items of drug ID: " + invDrugId);
                            if (totalAmount >= quantity) {
                                hasEnough = true;
                                break;
                            }
                        }
                    }
                }
                if (hasEnough) {
                    // Remove items from inventory
                    int remaining = quantity;
                    for (ItemStack invItem : player.getInventory().getContents()) {
                        if (remaining <= 0) break;
                        if (invItem != null && drugManager.isDrugItem(invItem)) {
                            String invDrugId = drugManager.getDrugIdFromItem(invItem);
                            if (invDrugId != null && invDrugId.equals(drugId)) {
                                int amount = Math.min(invItem.getAmount(), remaining);
                                invItem.setAmount(invItem.getAmount() - amount);
                                remaining -= amount;
                                logger.fine("Removed " + amount + " items of drug ID: " + invDrugId);
                            }
                        }
                    }
                    double totalPrice = drug.getPrice() * quantity;
                    economy.depositPlayer(player, totalPrice);
                    String sellMessage = plugin.getConfigManager().getConfig().getString("npc.sell_message", "&aSold %drug% for $%price%!");
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', sellMessage)
                                    .replace("%drug%", drug.getName() + " (" + quantity + "x)")
                                    .replace("%price%", String.valueOf(totalPrice))));
                    logger.info(player.getName() + " sold " + quantity + "x " + drug.getName() + " for $" + totalPrice);
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.RED + "You don't have " + quantity + "x " + drug.getName() + " to sell."));
                    logger.info(player.getName() + " failed to sell " + quantity + "x " + drug.getName() + ": Insufficient items");
                }
            }
        } catch (NumberFormatException e) {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "Invalid quantity. Please enter a number between 1 and 64."));
            logger.warning("Invalid quantity input by " + player.getName() + ": " + message);
        }
    }
}