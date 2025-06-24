package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.CartelGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class CartelGUIHandler {
    private final DrugCraft plugin;
    private final CartelGUI cartelGUI;
    private final CartelManager cartelManager;
    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CartelGUIHandler(DrugCraft plugin, CartelGUI cartelGUI, CartelManager cartelManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.cartelGUI = cartelGUI;
        this.cartelManager = cartelManager;
        this.economyManager = economyManager;
    }

    public void openMainMenu(Player player) {
        Cartel cartel = cartelManager.getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            cartelGUI.openCreateMenu(player);
        } else {
            cartelGUI.openMainMenu(player);
        }
    }

    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Component displayNameComp = meta.displayName();
        if (displayNameComp == null) return;

        String displayName = miniMessage.serialize(displayNameComp);
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        String guiType = activeGUI.getType();
        UUID playerUUID = player.getUniqueId();
        Cartel cartel = cartelManager.getCartelByPlayer(playerUUID);

        switch (guiType) {
            case "CARTEL":
                if (displayName.contains(MessageUtils.getMessage("gui.cartel.info"))) {
                    cartelGUI.openInfoMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.cartel.members"))) {
                    cartelGUI.openMembersMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.cartel.permissions"))) {
                    cartelGUI.openPermissionsMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.cartel.upgrades"))) {
                    cartelGUI.openUpgradesMenu(player);
                }
                break;
            case "CARTEL_CREATE":
                if (displayName.contains(MessageUtils.getMessage("gui.cartel.create"))) {
                    activeGUI.setAwaitingChatInput(true);
                    activeGUI.setChatAction("cartel-create");
                    MessageUtils.sendMessage(player, "cartel.create-prompt");
                    player.closeInventory();
                }
                break;
            case "CARTEL_INFO":
                if (meta.hasLore() && displayName.contains(MessageUtils.getMessage("gui.cartel.disband"))) {
                    String lore = miniMessage.serialize(meta.lore().get(0));
                    String cartelId = lore.split("ID: ")[1];
                    if (cartel != null && cartel.getOwner().equals(playerUUID)) {
                        cartelManager.disbandCartel(cartelId);
                        MessageUtils.sendMessage(player, "admin.cartel-disbanded", "name", cartel.getName());
                        cartelGUI.openCreateMenu(player);
                    } else {
                        MessageUtils.sendMessage(player, "cartel.no-permission");
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    cartelGUI.openMainMenu(player);
                }
                break;
            case "CARTEL_MEMBERS":
                if (meta.hasLore()) {
                    String lore = miniMessage.serialize(meta.lore().get(0));
                    String uuidStr = lore.split("UUID: ")[1];
                    UUID targetUUID = UUID.fromString(uuidStr);
                    if (cartel != null && cartel.getOwner().equals(playerUUID) && !targetUUID.equals(playerUUID)) {
                        cartelManager.kickMember(targetUUID);
                        MessageUtils.sendMessage(player, "cartel.member-kicked", "player", Bukkit.getOfflinePlayer(targetUUID).getName());
                        cartelGUI.openMembersMenu(player);
                    } else {
                        MessageUtils.sendMessage(player, "cartel.no-permission");
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    cartelGUI.openMainMenu(player);
                }
                break;
            case "CARTEL_PERMISSIONS":
                if (meta.hasLore()) {
                    String lore = miniMessage.serialize(meta.lore().get(0));
                    String uuidStr = lore.split("UUID: ")[1];
                    UUID targetUUID = UUID.fromString(uuidStr);
                    String permission = displayName.contains("Build") ? "build" : "interact";
                    if (cartel != null && cartel.getOwner().equals(playerUUID)) {
                        boolean value = !cartelManager.hasPermission(targetUUID, permission);
                        cartelManager.setPermission(targetUUID, permission, value);
                        MessageUtils.sendMessage(player, "cartel.permission-set-success", "permission", permission, "player_name", Bukkit.getOfflinePlayer(targetUUID).getName());
                        cartelGUI.openPermissionsMenu(player);
                    } else {
                        MessageUtils.sendMessage(player, "cartel.no-permission");
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    cartelGUI.openMainMenu(player);
                }
                break;
            case "CARTEL_UPGRADES":
                if (displayName.contains("Level Up")) {
                    if (cartel != null && economyManager.withdrawPlayer(player, 10000.0)) {
                        // Placeholder: Implement level up
                        MessageUtils.sendMessage(player, "cartel.upgrade-purchased", "upgrade", "Level Up", "cost", "10000");
                        cartelGUI.openMainMenu(player);
                    } else {
                        MessageUtils.sendMessage(player, "cartel.insufficient-funds");
                    }
                } else if (displayName.contains("Stash Capacity")) {
                    if (cartel != null && economyManager.withdrawPlayer(player, 5000.0)) {
                        // Placeholder: Implement stash capacity
                        MessageUtils.sendMessage(player, "cartel.upgrade-purchased", "upgrade", "Stash Capacity", "cost", "5000");
                        cartelGUI.openMainMenu(player);
                    } else {
                        MessageUtils.sendMessage(player, "cartel.insufficient-funds");
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    cartelGUI.openMainMenu(player);
                }
                break;
        }
    }

    public void handleCartelNameInput(Player player, String name) {
        if (cartelManager.isNameTaken(name)) {
            MessageUtils.sendMessage(player, "cartel.name-taken", "name", name);
            cartelGUI.openCreateMenu(player);
            return;
        }
        if (economyManager.withdrawPlayer(player, 1000.0)) {
            cartelManager.createCartel(player.getUniqueId(), name);
            MessageUtils.sendMessage(player, "cartel.created", "name", name, "cost", "1000");
            cartelGUI.openMainMenu(player);
        } else {
            MessageUtils.sendMessage(player, "cartel.insufficient-funds");
            cartelGUI.openCreateMenu(player);
        }
    }
}