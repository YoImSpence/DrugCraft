package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.admin.AdminGUI;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AdminGUIHandler implements Listener, ChatInputHandler, GUIHandler {
    private final DrugCraft plugin;
    private final AdminGUI adminGUI;
    private final DataManager dataManager;

    public AdminGUIHandler(DrugCraft plugin, AdminGUI adminGUI, DataManager dataManager) {
        this.plugin = plugin;
        this.adminGUI = adminGUI;
        this.dataManager = dataManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        adminGUI.openMainMenu(player);
    }

    public void setLevel(Player target, int level) {
        plugin.getDataManager().setPlayerLevel(target.getUniqueId(), level);
    }

    public void giveXP(Player target, String skill, long xp) {
        plugin.getDataManager().addXP(target.getUniqueId(), skill, xp);
    }

    public void resetXP(Player target, String skill) {
        plugin.getDataManager().resetPlayerXP(target.getUniqueId(), skill);
    }

    public void viewStats(Player viewer, Player target) {
        adminGUI.openPlayerStatsMenu(viewer, target);
    }

    public void viewInventory(Player viewer, Player target) {
        adminGUI.openPlayerInventoryMenu(viewer, target);
    }

    @Override
    public void onClick(Player player, ItemStack clickedItem, int slot, Inventory inventory) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("ADMIN")) return;
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String menuSubType = activeGUI.getMenuSubType();
        if (menuSubType == null) {
            if (!player.hasPermission("drugcraft.admin")) {
                MessageUtils.sendMessage(player, "no-permission");
                player.closeInventory();
                return;
            }
            String displayName = MessageUtils.stripColor(clickedItem.getItemMeta().displayName());
            switch (displayName) {
                case "Select Player":
                    String playerName = displayName.split(": ")[1];
                    Player target = Bukkit.getPlayerExact(playerName);
                    if (target == null) {
                        MessageUtils.sendMessage(player, "general.player-not-found");
                    } else {
                        activeGUI.setSelectedItem(new ActiveGUI.SelectedItem("player", target.getName(), clickedItem.clone()));
                        adminGUI.openPlayerMenu(player, target);
                    }
                    break;
                case "Player List":
                    String[] parts = displayName.split(": ");
                    if (parts.length == 2) {
                        String playerName = parts[1];
                        activeGUI.setSelectedItem(new ActiveGUI.SelectedItem("player", playerName, clickedItem.clone()));
                        adminGUI.openPlayerMenu(player, Bukkit.getPlayerExact(playerName));
                    }
                    break;
                case "Give Drug XP":
                    String[] parts = displayName.split(": ");
                    if (parts.length == 2) {
                        String drugName = parts[1];
                        activeGUI.setSelectedItem(new ActiveGUI.SelectedItem("drug", drugName, clickedItem.clone()));
                        activeGUI.setAwaitingChatInput(true);
                        plugin.getGuiListener().setAwaitingChatInput(player.getUniqueId(), "xp_amount", null);
                        MessageUtils.sendMessage(player, "admin.enter-xp-amount");
                        player.closeInventory();
                    }
                    break;
                case "Reset Drug XP":
                    String[] resetParts = displayName.split(": ");
                    if (resetParts.length == 2) {
                        String resetDrugName = resetParts[1];
                        Player target = Bukkit.getPlayerExact(activeGUI.getSelectedItem().category());
                        if (target != null) {
                            dataManager.resetPlayerDrugXP(target.getUniqueId(), resetDrugName);
                            MessageUtils.sendMessage(player, "admin.xp-reset", "drug", resetDrugName, "player", target.getName());
                        } else {
                            MessageUtils.sendMessage(player, "general.player-not-found");
                        }
                    }
                    player.closeInventory();
                    break;
            }
        }
    }

    @Override
    public void handleChatInput(Player player, String action, String message, Object context) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("ADMIN")) return;
        if (context instanceof ActiveGUI.SelectedItem selectedItem) {
            switch (action) {
                case "xp_amount":
                    try {
                        long xp = Long.parseLong(message.trim());
                        if (xp <= 0) {
                            MessageUtils.sendMessage(player, "general.quantity-positive");
                            return;
                        }
                        String drugName = selectedItem.id();
                        Player target = Bukkit.getPlayerExact(selectedItem.category());
                        if (target != null) {
                            dataManager.addPlayerDrugXP(target.getUniqueId(), drugName, xp);
                            MessageUtils.sendMessage(player, "admin.xp-added", "xp", String.valueOf(xp), "drug", drugName, "player", target.getName());
                        } else {
                            MessageUtils.sendMessage(player, "general.player-not-found");
                        }
                    } catch (NumberFormatException e) {
                        MessageUtils.sendMessage(player, "general.invalid-quantity");
                    }
                    break;
            }
        }
    }
}
