package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.admin.AdminGUI;
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
    private final Map<UUID, List<ItemStack>> selectedItems = new HashMap<>();
    private final Map<UUID, List<Player>> selectedPlayers = new HashMap<>();
    private final Map<UUID, Map<ItemStack, Integer>> itemQuantities = new HashMap<>();
    private final Map<UUID, String> activeGUIs = new HashMap<>();

    public AdminGUIListener(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.startsWith(MessageUtils.color("&#FF5555&lDrugCraft Admin")) &&
                !title.startsWith(MessageUtils.color("&#FF5555&lSelect Items")) &&
                !title.startsWith(MessageUtils.color("&#FF5555&lSelect Players")) &&
                !title.startsWith(MessageUtils.color("&#FF5555&lSet Quantities"))) return;
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null ||
                clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        UUID playerId = player.getUniqueId();
        if (title.equals(MessageUtils.color("&#FF5555&lDrugCraft Admin"))) {
            if (clickedItem.getType() == Material.CHEST) {
                activeGUIs.put(playerId, "items");
                new AdminGUI(plugin, drugManager).openItemsMenu(player);
            } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
                activeGUIs.put(playerId, "players");
                new AdminGUI(plugin, drugManager).openPlayersMenu(player);
            } else if (clickedItem.getType() == Material.PAPER) {
                List<ItemStack> items = selectedItems.getOrDefault(playerId, new ArrayList<>());
                if (items.isEmpty()) {
                    player.sendMessage(MessageUtils.color("&#FF4040Please select items first."));
                    return;
                }
                activeGUIs.put(playerId, "quantities");
                new AdminGUI(plugin, drugManager).openQuantityMenu(player, items);
            }
        } else if (title.equals(MessageUtils.color("&#FF5555&lSelect Items"))) {
            List<ItemStack> items = selectedItems.computeIfAbsent(playerId, k -> new ArrayList<>());
            ItemStack itemClone = clickedItem.clone();
            ItemMeta meta = itemClone.getItemMeta();
            if (items.stream().anyMatch(i -> i.isSimilar(itemClone))) {
                items.removeIf(i -> i.isSimilar(itemClone));
                meta.setLore(new AdminGUI(plugin, drugManager).addSelectionLore(meta.getLore(), false));
            } else {
                items.add(itemClone);
                meta.setLore(new AdminGUI(plugin, drugManager).addSelectionLore(meta.getLore(), true));
            }
            itemClone.setItemMeta(meta);
            event.getInventory().setItem(event.getSlot(), itemClone);
        } else if (title.equals(MessageUtils.color("&#FF5555&lSelect Players"))) {
            List<Player> players = selectedPlayers.computeIfAbsent(playerId, k -> new ArrayList<>());
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
                meta.setLore(Arrays.asList(MessageUtils.color("�FF7FSelected")));
            }
            clickedItem.setItemMeta(meta);
            event.getInventory().setItem(event.getSlot(), clickedItem);
        } else if (title.equals(MessageUtils.color("&#FF5555&lSet Quantities"))) {
            if (clickedItem.getType() == Material.LIME_DYE) {
                List<ItemStack> items = selectedItems.getOrDefault(playerId, new ArrayList<>());
                List<Player> players = selectedPlayers.getOrDefault(playerId, new ArrayList<>());
                Map<ItemStack, Integer> quantities = itemQuantities.getOrDefault(playerId, new HashMap<>());
                if (items.isEmpty()) {
                    player.sendMessage(MessageUtils.color("&#FF4040No items selected."));
                    return;
                }
                if (players.isEmpty()) {
                    player.sendMessage(MessageUtils.color("&#FF4040No players selected."));
                    return;
                }
                for (Player target : players) {
                    for (ItemStack item : items) {
                        int quantity = quantities.getOrDefault(item, 1);
                        ItemStack giveItem = item.clone();
                        giveItem.setAmount(quantity);
                        target.getInventory().addItem(giveItem);
                        player.sendMessage(MessageUtils.color("�FF7FGave " + quantity + " " + item.getItemMeta().getDisplayName() + " to " + target.getName()));
                    }
                }
                player.closeInventory();
                clearPlayerData(player);
            } else {
                List<ItemStack> items = selectedItems.getOrDefault(playerId, new ArrayList<>());
                ItemStack itemClone = clickedItem.clone();
                Map<ItemStack, Integer> quantities = itemQuantities.computeIfAbsent(playerId, k -> new HashMap<>());
                int quantity = quantities.getOrDefault(itemClone, 1);
                if (event.getClick() == ClickType.LEFT) {
                    quantity++;
                } else if (event.getClick() == ClickType.RIGHT && quantity > 1) {
                    quantity--;
                } else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                    items.removeIf(i -> i.isSimilar(itemClone));
                    quantities.remove(itemClone);
                    new AdminGUI(plugin, drugManager).openQuantityMenu(player, items);
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
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        if (title.startsWith(MessageUtils.color("&#FF5555&lDrugCraft Admin")) ||
                title.startsWith(MessageUtils.color("&#FF5555&lSelect Items")) ||
                title.startsWith(MessageUtils.color("&#FF5555&lSelect Players")) ||
                title.startsWith(MessageUtils.color("&#FF5555&lSet Quantities"))) {
            new AdminGUI(plugin, drugManager).removePlayerData(player);
            activeGUIs.remove(player.getUniqueId());
        }
    }

    public void clearPlayerData(Player player) {
        selectedItems.remove(player.getUniqueId());
        selectedPlayers.remove(player.getUniqueId());
        itemQuantities.remove(player.getUniqueId());
        activeGUIs.remove(player.getUniqueId());
    }

    public Map<UUID, List<ItemStack>> getSelectedItems() {
        return selectedItems;
    }

    public Map<UUID, List<Player>> getSelectedPlayers() {
        return selectedPlayers;
    }

    public Map<UUID, Map<ItemStack, Integer>> getItemQuantities() {
        return itemQuantities;
    }

    public Map<UUID, String> getActiveGUIs() {
        return activeGUIs;
    }
}