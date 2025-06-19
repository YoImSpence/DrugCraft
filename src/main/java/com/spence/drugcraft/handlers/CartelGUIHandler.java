package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.gui.CartelGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CartelGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final CartelGUI cartelGUI;

    public CartelGUIHandler(DrugCraft plugin, CartelGUI cartelGUI) {
        this.plugin = plugin;
        this.cartelGUI = cartelGUI;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("CARTEL")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        String subType = activeGUI.getMenuSubType();

        if (subType == null) {
            if (displayName.equals(MessageUtils.getMessage("gui.cartel.item-info"))) {
                cartelGUI.openInfoMenu(player);
                activeGUI.setMenuSubType("info");
            } else if (displayName.equals(MessageUtils.getMessage("gui.cartel.item-members"))) {
                cartelGUI.openMembersMenu(player);
                activeGUI.setMenuSubType("members");
            } else if (displayName.equals(MessageUtils.getMessage("gui.cartel.item-upgrades"))) {
                cartelGUI.openUpgradeMenu(player);
                activeGUI.setMenuSubType("upgrades");
            } else if (displayName.equals(MessageUtils.getMessage("gui.cartel.item-permissions"))) {
                cartelGUI.openPermissionsMenu(player);
                activeGUI.setMenuSubType("permissions");
            } else if (displayName.equals(MessageUtils.getMessage("gui.cartel.item-create"))) {
                MessageUtils.sendMessage(player, "gui.cartel.create-prompt");
                activeGUI.setAwaitingChatInput(true);
                activeGUI.setChatAction("cartel-create");
                player.closeInventory();
            }
        } else if (subType.equals("upgrades")) {
            if (displayName.equals(MessageUtils.getMessage("gui.cartel.item-back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            } else if (displayName.startsWith(MessageUtils.getMessage("gui.cartel.upgrade"))) {
                String[] parts = displayName.split(" ");
                if (parts.length >= 2) {
                    String upgrade = parts[1].toLowerCase();
                    purchaseUpgrade(player, upgrade);
                }
            }
        } else if (subType.equals("permissions")) {
            if (displayName.equals(MessageUtils.getMessage("gui.cartel.item-back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            } else {
                String permission = displayName;
                MessageUtils.sendMessage(player, "gui.cartel.enter-permission", "permission", permission);
                activeGUI.setAwaitingChatInput(true);
                activeGUI.setChatAction("cartel-permission");
                player.closeInventory();
            }
        }
    }

    private void purchaseUpgrade(Player player, String upgrade) {
        Cartel cartel = plugin.getCartelManager().getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-in-cartel");
            return;
        }
        if (!cartel.hasPermission(player.getUniqueId(), "manage_upgrades")) {
            MessageUtils.sendMessage(player, "cartel.no-permission");
            return;
        }
        double cost = plugin.getConfig("cartels.yml").getDouble("upgrades." + upgrade + ".cost", 1000.0);
        if (plugin.getEconomyManager().withdrawPlayer(player, cost)) {
            // Placeholder: Implement upgrade logic (e.g., increase stash size)
            MessageUtils.sendMessage(player, "cartel.upgrade-purchased", "upgrade", upgrade, "cost", String.valueOf(cost));
        } else {
            MessageUtils.sendMessage(player, "cartel.insufficient-funds");
        }
    }

    public void handlePermissionInput(Player player, String input) {
        Cartel cartel = plugin.getCartelManager().getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-in-cartel");
            return;
        }
        // Placeholder: Implement permission setting logic
        MessageUtils.sendMessage(player, "gui.cartel.permission-set-success", "permission", input, "player_name", player.getName());
    }

    public void openMainMenu(Player player) {
        cartelGUI.openMainMenu(player);
    }
}