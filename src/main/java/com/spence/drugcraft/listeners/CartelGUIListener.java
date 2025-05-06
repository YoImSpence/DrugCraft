package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.CartelGUI;
import com.spence.drugcraft.utils.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CartelGUIListener implements Listener {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;
    private final Map<UUID, String> activeGUIs = new HashMap<>();
    private final Map<UUID, UUID> selectedMembers = new HashMap<>();

    public CartelGUIListener(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.startsWith(MessageUtils.color("{#00FFFF}Cartel Management")) &&
                !title.startsWith(MessageUtils.color("{#00FFFF}Cartel Info")) &&
                !title.startsWith(MessageUtils.color("{#00FFFF}Members")) &&
                !title.startsWith(MessageUtils.color("{#00FFFF}Permissions")) &&
                !title.startsWith(MessageUtils.color("{#00FFFF}Upgrades"))) {
            return;
        }
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null ||
                clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.CYAN_STAINED_GLASS_PANE) {
            return;
        }

        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName == null) {
            player.sendMessage(MessageUtils.color("{#FF5555}You are not in a cartel."));
            player.closeInventory();
            return;
        }
        CartelManager.Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) {
            player.sendMessage(MessageUtils.color("{#FF5555}Cartel not found."));
            player.closeInventory();
            return;
        }

        if (title.equals(MessageUtils.color("{#00FFFF}Cartel Management"))) {
            if (clickedItem.getType() == Material.BOOK) {
                player.openInventory(new CartelGUI(plugin, cartelManager).createInfoGUI(cartel));
                activeGUIs.put(player.getUniqueId(), "info");
            } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
                player.openInventory(new CartelGUI(plugin, cartelManager).createMembersGUI(cartel));
                activeGUIs.put(player.getUniqueId(), "members");
            } else if (clickedItem.getType() == Material.EMERALD) {
                player.openInventory(new CartelGUI(plugin, cartelManager).createUpgradesGUI(cartel));
                activeGUIs.put(player.getUniqueId(), "upgrades");
            }
        } else if (title.startsWith(MessageUtils.color("{#00FFFF}Members"))) {
            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                String playerName = clickedItem.getItemMeta().getDisplayName().replace(MessageUtils.color("{#FFD700}"), "");
                Player target = Bukkit.getPlayer(playerName);
                if (target == null) {
                    player.sendMessage(MessageUtils.color("{#FF5555}Player is offline."));
                    return;
                }
                selectedMembers.put(player.getUniqueId(), target.getUniqueId());
                player.openInventory(new CartelGUI(plugin, cartelManager).createPermissionsGUI(cartel, target.getUniqueId()));
                activeGUIs.put(player.getUniqueId(), "permissions");
            }
        } else if (title.startsWith(MessageUtils.color("{#00FFFF}Permissions"))) {
            if (clickedItem.getType() == Material.LIME_DYE || clickedItem.getType() == Material.RED_DYE) {
                String permission = clickedItem.getItemMeta().getDisplayName().replace(MessageUtils.color("{#FFD700}"), "");
                UUID memberId = selectedMembers.get(player.getUniqueId());
                if (memberId == null) {
                    player.sendMessage(MessageUtils.color("{#FF5555}No member selected."));
                    player.closeInventory();
                    return;
                }
                Map<String, Boolean> memberPermissions = cartel.getPermissions().computeIfAbsent(memberId, k -> new HashMap<>());
                boolean current = memberPermissions.getOrDefault(permission, false);
                memberPermissions.put(permission, !current);
                cartelManager.updatePermissions(cartelName, memberId, memberPermissions);
                player.openInventory(new CartelGUI(plugin, cartelManager).createPermissionsGUI(cartel, memberId));
                player.sendMessage(MessageUtils.color("{#00FF00}Toggled " + permission + " for member to " + (!current ? "enabled" : "disabled")));
            }
        } else if (title.startsWith(MessageUtils.color("{#00FFFF}Upgrades"))) {
            if (clickedItem.getType() == Material.EMERALD) {
                String upgrade = clickedItem.getItemMeta().getDisplayName().replace(MessageUtils.color("{#FFD700}"), "");
                int level = cartel.getUpgrades().getOrDefault(upgrade, 0);
                double cost = (level + 1) * 1000;
                if (cartel.getStashedMoney() >= cost) {
                    cartelManager.upgradeCartel(cartelName, upgrade, level + 1, cost);
                    player.openInventory(new CartelGUI(plugin, cartelManager).createUpgradesGUI(cartel));
                    player.sendMessage(MessageUtils.color("{#00FF00}Upgraded " + upgrade + " to level " + (level + 1)));
                } else {
                    player.sendMessage(MessageUtils.color("{#FF5555}Insufficient stashed money for upgrade."));
                }
            }
        }
    }

    public void clearPlayerData(Player player) {
        activeGUIs.remove(player.getUniqueId());
        selectedMembers.remove(player.getUniqueId());
    }
}