package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.AdminGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class AdminGUIListener implements Listener {
    private final DrugCraft plugin;
    private final AdminGUI adminGUI;
    private final DrugManager drugManager;
    private final Logger logger;
    private final Map<UUID, Boolean> awaitingQuantity = new HashMap<>();

    public AdminGUIListener(DrugCraft plugin, AdminGUI adminGUI, DrugManager drugManager) {
        this.plugin = plugin;
        this.adminGUI = adminGUI;
        this.drugManager = drugManager;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.equals(ChatColor.translateAlternateColorCodes('&', "&cAdmin Give Drugs"))) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        boolean isSeed = drugManager.isSeedItem(clickedItem);
        if (drugManager.isDrugItem(clickedItem) || isSeed) {
            adminGUI.promptQuantity(player, clickedItem, isSeed);
            awaitingQuantity.put(player.getUniqueId(), true);
            logger.info("Player " + player.getName() + " selected item for quantity input: " + (isSeed ? "seed" : "drug"));
        }
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
            adminGUI.handleGive(player, quantity, adminGUI.isSelectedSeed(playerId));
        } catch (NumberFormatException e) {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "Invalid quantity. Please enter a number between 1 and 64."));
            logger.warning("Invalid quantity input by " + player.getName() + ": " + message);
        }
    }
}