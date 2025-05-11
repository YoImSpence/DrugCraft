package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.admin.AdminGUI;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AdminGUIListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final GrowLight growLight;
    private final Map<UUID, String> activeGUIs = new HashMap<>();
    private final Map<UUID, AdminGUI> activeGUIInstances = new HashMap<>();

    public AdminGUIListener(DrugCraft plugin, DrugManager drugManager, GrowLight growLight) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.growLight = growLight;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.startsWith(MessageUtils.color("&#FF5555&lDrugCraft Admin")) &&
                !title.startsWith(MessageUtils.color("&#FF5555&lSelect Players")) &&
                !title.startsWith(MessageUtils.color("&#FF5555&lSelect Items")) &&
                !title.startsWith(MessageUtils.color("&#FF5555&lSet Quantities")) &&
                !title.startsWith(MessageUtils.color("&#FF5555&lConfirm Distribution"))) return;
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        UUID playerId = player.getUniqueId();
        AdminGUI adminGUI = activeGUIInstances.computeIfAbsent(playerId, k -> new AdminGUI(plugin, drugManager, growLight));
        if (title.equals(MessageUtils.color("&#FF5555&lDrugCraft Admin"))) {
            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                activeGUIs.put(playerId, "players");
                adminGUI.openPlayersMenu(player);
            }
        } else if (title.equals(MessageUtils.color("&#FF5555&lSelect Players"))) {
            if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                    clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;
            if (clickedItem.getType() == Material.ARROW) {
                List<Player> selected = adminGUI.getSelectedPlayers().getOrDefault(playerId, new ArrayList<>());
                if (selected.isEmpty()) {
                    player.sendMessage(MessageUtils.color("&#FF4040Please select at least one player."));
                    return;
                }
                activeGUIs.put(playerId, "items");
                adminGUI.openItemsMenu(player);
                return;
            }
            List<Player> players = adminGUI.getSelectedPlayers().computeIfAbsent(playerId, k -> new ArrayList<>());
            String playerName = clickedItem.getItemMeta().getDisplayName().replace(MessageUtils.color("&#FFFF00"), "");
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                player.sendMessage(MessageUtils.color("&#FF4040Player is offline."));
                return;
            }
            ItemMeta meta = clickedItem.getItemMeta();
            if (players.contains(target)) {
                players.remove(target);
                meta.setLore(Arrays.asList(MessageUtils.color("&#FFFF00Click to toggle selection")));
            } else {
                players.add(target);
                meta.setLore(Arrays.asList(MessageUtils.color("&#32CD32Selected")));
            }
            clickedItem.setItemMeta(meta);
            event.getInventory().setItem(event.getSlot(), clickedItem);
        } else if (title.equals(MessageUtils.color("&#FF5555&lSelect Items"))) {
            if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                    clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;
            if (clickedItem.getType() == Material.ARROW) {
                if (clickedItem.getItemMeta().getDisplayName().equals(MessageUtils.color("&#FFFF00Back to Player Selection"))) {
                    activeGUIs.put(playerId, "players");
                    adminGUI.openPlayersMenu(player);
                    return;
                } else if (clickedItem.getItemMeta().getDisplayName().equals(MessageUtils.color("&#FFFF00Next: Set Quantities"))) {
                    List<ItemStack> selected = adminGUI.getSelectedItems().getOrDefault(playerId, new ArrayList<>());
                    if (selected.isEmpty()) {
                        player.sendMessage(MessageUtils.color("&#FF4040Please select at least one item."));
                        return;
                    }
                    activeGUIs.put(playerId, "quantities");
                    adminGUI.openQuantitiesMenu(player, selected);
                    return;
                }
            }
            List<ItemStack> items = adminGUI.getSelectedItems().computeIfAbsent(playerId, k -> new ArrayList<>());
            ItemStack itemClone = clickedItem.clone();
            ItemMeta meta = itemClone.getItemMeta();
            boolean isSelected = items.stream().anyMatch(i -> i.getType() == itemClone.getType() &&
                    i.getItemMeta().getDisplayName().equals(itemClone.getItemMeta().getDisplayName()));
            if (isSelected) {
                items.removeIf(i -> i.getType() == itemClone.getType() &&
                        i.getItemMeta().getDisplayName().equals(itemClone.getItemMeta().getDisplayName()));
                meta.setLore(adminGUI.addSelectionLore(meta.getLore(), false));
            } else {
                items.add(itemClone);
                meta.setLore(adminGUI.addSelectionLore(meta.getLore(), true));
            }
            itemClone.setItemMeta(meta);
            event.getInventory().setItem(event.getSlot(), itemClone);
        } else if (title.equals(MessageUtils.color("&#FF5555&lSet Quantities"))) {
            if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                    clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;
            if (clickedItem.getType() == Material.ARROW) {
                if (clickedItem.getItemMeta().getDisplayName().equals(MessageUtils.color("&#FFFF00Back to Item Selection"))) {
                    activeGUIs.put(playerId, "items");
                    adminGUI.openItemsMenu(player);
                    return;
                } else if (clickedItem.getItemMeta().getDisplayName().equals(MessageUtils.color("&#FFFF00Next: Confirm"))) {
                    List<ItemStack> selectedItems = adminGUI.getSelectedItems().getOrDefault(playerId, new ArrayList<>());
                    List<Player> selectedPlayers = adminGUI.getSelectedPlayers().getOrDefault(playerId, new ArrayList<>());
                    Map<ItemStack, Integer> quantities = adminGUI.getItemQuantities().getOrDefault(playerId, new HashMap<>());
                    activeGUIs.put(playerId, "confirm");
                    adminGUI.openConfirmMenu(player, selectedPlayers, selectedItems, quantities);
                    return;
                }
            }
            List<ItemStack> items = adminGUI.getSelectedItems().getOrDefault(playerId, new ArrayList<>());
            ItemStack itemClone = clickedItem.clone();
            Map<ItemStack, Integer> quantities = adminGUI.getItemQuantities().computeIfAbsent(playerId, k -> new HashMap<>());
            int quantity = quantities.getOrDefault(itemClone, 1);
            if (event.getClick() == ClickType.LEFT) {
                quantity++;
            } else if (event.getClick() == ClickType.RIGHT && quantity > 1) {
                quantity--;
            } else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                items.removeIf(i -> i.getType() == itemClone.getType() &&
                        i.getItemMeta().getDisplayName().equals(itemClone.getItemMeta().getDisplayName()));
                quantities.remove(itemClone);
                adminGUI.openQuantitiesMenu(player, items);
                return;
            }
            quantities.put(itemClone, quantity);
            ItemMeta meta = itemClone.getItemMeta();
            meta.setLore(Arrays.asList(
                    MessageUtils.color("&#FFFF00Quantity: " + quantity),
                    MessageUtils.color("&#FFFF00Left Click: +1"),
                    MessageUtils.color("&#FFFF00Right Click: -1"),
                    MessageUtils.color("&#FFFF00Shift+Click: Remove")
            ));
            itemClone.setItemMeta(meta);
            event.getInventory().setItem(event.getSlot(), itemClone);
        } else if (title.equals(MessageUtils.color("&#FF5555&lConfirm Distribution"))) {
            if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                    clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;
            if (clickedItem.getType() == Material.ARROW) {
                activeGUIs.put(playerId, "quantities");
                List<ItemStack> selectedItems = adminGUI.getSelectedItems().getOrDefault(playerId, new ArrayList<>());
                adminGUI.openQuantitiesMenu(player, selectedItems);
                return;
            }
            if (clickedItem.getType() == Material.LIME_DYE) {
                List<ItemStack> selectedItems = adminGUI.getSelectedItems().getOrDefault(playerId, new ArrayList<>());
                List<Player> selectedPlayers = adminGUI.getSelectedPlayers().getOrDefault(playerId, new ArrayList<>());
                Map<ItemStack, Integer> quantities = adminGUI.getItemQuantities().getOrDefault(playerId, new HashMap<>());
                for (Player target : selectedPlayers) {
                    for (ItemStack item : selectedItems) {
                        int quantity = quantities.getOrDefault(item, 1);
                        ItemStack giveItem = item.clone();
                        giveItem.setAmount(quantity);
                        target.getInventory().addItem(giveItem);
                        player.sendMessage(MessageUtils.color("&#FF7F00Gave " + quantity + " " + item.getItemMeta().getDisplayName() + " to " + target.getName()));
                    }
                }
                player.closeInventory();
                adminGUI.clearPlayerData(player);
                activeGUIs.remove(playerId);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        if (title.startsWith(MessageUtils.color("&#FF5555&lDrugCraft Admin")) ||
                title.startsWith(MessageUtils.color("&#FF5555&lSelect Players")) ||
                title.startsWith(MessageUtils.color("&#FF5555&lSelect Items")) ||
                title.startsWith(MessageUtils.color("&#FF5555&lSet Quantities")) ||
                title.startsWith(MessageUtils.color("&#FF5555&lConfirm Distribution"))) {
            activeGUIs.remove(player.getUniqueId());
            activeGUIInstances.remove(player.getUniqueId());
        }
    }

    public Map<UUID, String> getActiveGUIs() {
        return activeGUIs;
    }
}