package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelGUI;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.startsWith(MessageUtils.color("&#4682B4Cartel Management")) &&
                !title.startsWith(MessageUtils.color("&#4682B4Cartel Info")) &&
                !title.startsWith(MessageUtils.color("&#4682B4Members")) &&
                !title.startsWith(MessageUtils.color("&#4682B4Permissions")) &&
                !title.startsWith(MessageUtils.color("&#4682B4Upgrades")) &&
                !title.startsWith(MessageUtils.color("&#4682B4Stash"))) return;
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null ||
                clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                clickedItem.getType() == Material.CYAN_STAINED_GLASS_PANE) return;

        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName == null) {
            player.sendMessage(MessageUtils.color("&#FF4040You are not in a cartel."));
            player.closeInventory();
            return;
        }
        CartelManager.Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) {
            player.sendMessage(MessageUtils.color("&#FF4040Cartel not found."));
            player.closeInventory();
            return;
        }

        if (title.equals(MessageUtils.color("&#4682B4Cartel Management"))) {
            if (clickedItem.getType() == Material.BOOK) {
                player.openInventory(new CartelGUI(plugin, cartelManager).createInfoGUI(cartel));
                activeGUIs.put(player.getUniqueId(), "info");
            } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
                player.openInventory(new CartelGUI(plugin, cartelManager).createMembersGUI(cartel));
                activeGUIs.put(player.getUniqueId(), "members");
            } else if (clickedItem.getType() == Material.EMERALD) {
                player.openInventory(new CartelGUI(plugin, cartelManager).createUpgradesGUI(cartel));
                activeGUIs.put(player.getUniqueId(), "upgrades");
            } else if (clickedItem.getType() == Material.CHEST) {
                if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.cartel.stash")) {
                    player.sendMessage(MessageUtils.color("&#FF4040You do not have permission to access the stash."));
                    return;
                }
                player.openInventory(new CartelGUI(plugin, cartelManager).createStashGUI(cartel));
                activeGUIs.put(player.getUniqueId(), "stash");
            }
        } else if (title.startsWith(MessageUtils.color("&#4682B4Members"))) {
            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                String playerName = clickedItem.getItemMeta().getDisplayName().replace(MessageUtils.color("&#FFFF00"), "");
                Player target = Bukkit.getPlayer(playerName);
                if (target == null) {
                    player.sendMessage(MessageUtils.color("&#FF4040Player is offline."));
                    return;
                }
                if (target.getUniqueId().equals(cartel.getLeader())) {
                    player.sendMessage(MessageUtils.color("&#FF4040Cannot manage leader permissions."));
                    return;
                }
                selectedMembers.put(player.getUniqueId(), target.getUniqueId());
                player.openInventory(new CartelGUI(plugin, cartelManager).createPermissionsGUI(cartel, target.getUniqueId()));
                activeGUIs.put(player.getUniqueId(), "permissions");
            }
        } else if (title.startsWith(MessageUtils.color("&#4682B4Permissions"))) {
            if (clickedItem.getType() == Material.LIME_DYE || clickedItem.getType() == Material.RED_DYE) {
                String permission = clickedItem.getItemMeta().getDisplayName().replace(MessageUtils.color("&#FFFF00"), "");
                UUID memberId = selectedMembers.get(player.getUniqueId());
                if (memberId == null) {
                    player.sendMessage(MessageUtils.color("&#FF4040No member selected."));
                    player.closeInventory();
                    return;
                }
                Map<String, Boolean> memberPermissions = cartel.getPermissions().computeIfAbsent(memberId, k -> new HashMap<>());
                boolean current = memberPermissions.getOrDefault(permission, false);
                memberPermissions.put(permission, !current);
                cartelManager.updatePermissions(cartelName, memberId, memberPermissions);
                player.openInventory(new CartelGUI(plugin, cartelManager).createPermissionsGUI(cartel, memberId));
                player.sendMessage(MessageUtils.color("&#FF7F00Toggled " + permission + " for member to " + (!current ? "enabled" : "disabled")));
            }
        } else if (title.startsWith(MessageUtils.color("&#4682B4Upgrades"))) {
            if (clickedItem.getType() == Material.EMERALD) {
                String upgrade = clickedItem.getItemMeta().getDisplayName().replace(MessageUtils.color("&#FFFF00"), "");
                int level = cartel.getUpgrades().getOrDefault(upgrade, 0);
                double cost = (level + 1) * 1000;
                if (cartel.getStashedMoney() >= cost) {
                    cartelManager.upgradeCartel(cartelName, upgrade, level + 1, cost);
                    player.openInventory(new CartelGUI(plugin, cartelManager).createUpgradesGUI(cartel));
                    player.sendMessage(MessageUtils.color("&#FF7F00Upgraded " + upgrade + " to level " + (level + 1)));
                } else {
                    player.sendMessage(MessageUtils.color("&#FF4040Insufficient stashed money for upgrade."));
                }
            }
        } else if (title.startsWith(MessageUtils.color("&#4682B4Stash"))) {
            if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.cartel.stash")) {
                player.sendMessage(MessageUtils.color("&#FF4040You do not have permission to access the stash."));
                return;
            }
            Economy economy = plugin.getEconomyManager().getEconomy();
            if (clickedItem.getType() == Material.GOLD_INGOT) {
                if (economy.has(player, 1000)) {
                    economy.withdrawPlayer(player, 1000);
                    cartel.setStashedMoney(cartel.getStashedMoney() + 1000);
                    plugin.getDataManager().saveStash(cartelName, cartel.getStash());
                    player.sendMessage(MessageUtils.color("&#FF7F00Deposited $1000 to the cartel stash."));
                    player.openInventory(new CartelGUI(plugin, cartelManager).createStashGUI(cartel));
                } else {
                    player.sendMessage(MessageUtils.color("&#FF4040You do not have enough money to deposit."));
                }
            } else if (clickedItem.getType() == Material.GOLD_NUGGET) {
                if (cartel.getStashedMoney() >= 1000) {
                    cartel.setStashedMoney(cartel.getStashedMoney() - 1000);
                    economy.depositPlayer(player, 1000);
                    plugin.getDataManager().saveStash(cartelName, cartel.getStash());
                    player.sendMessage(MessageUtils.color("&#FF7F00Withdrew $1000 from the cartel stash."));
                    player.openInventory(new CartelGUI(plugin, cartelManager).createStashGUI(cartel));
                } else {
                    player.sendMessage(MessageUtils.color("&#FF4040Insufficient money in the cartel stash."));
                }
            }
        }
    }

    public void clearPlayerData(Player player) {
        activeGUIs.remove(player.getUniqueId());
        selectedMembers.remove(player.getUniqueId());
    }
}