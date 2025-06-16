package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelGUI;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CartelGUIHandler implements Listener, ChatInputHandler, GUIHandler {
    private final DrugCraft plugin;
    private final CartelGUI cartelGUI;

    public CartelGUIHandler(DrugCraft plugin, CartelGUI cartelGUI) {
        this.plugin = plugin;
        this.cartelGUI = cartelGUI;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onClick(Player player, ItemStack clickedItem, int slot, Inventory inventory) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("CARTEL")) return;
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = MessageUtils.stripColor(clickedItem.getItemMeta().displayName());
        String menuSubType = activeGUI.getMenuSubType();
        if (menuSubType == null) {
            switch (displayName) {
                case "Create Cartel":
                    activeGUI.setAwaitingChatInput(true);
                    activeGUI.setChatAction("create_cartel");
                    MessageUtils.sendMessage(player, "cartel.enter-name");
                    player.closeInventory();
                    break;
                case "Upgrade Cartel":
                    Cartel cartel = plugin.getCartelManager().getPlayerCartel(player.getUniqueId());
                    if (cartel == null || !cartel.hasPermission(player.getUniqueId(), "purchase_upgrades")) {
                        MessageUtils.sendMessage(player, "cartel.no-permission");
                        return;
                    }
                    cartelGUI.openUpgradeMenu(player);
                    break;
            }
        }
    }

    @Override
    public void handleChatInput(Player player, String action, String message, Object context) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("CARTEL")) return;
        if (action.equals("create_cartel")) {
            CartelManager cartelManager = plugin.getCartelManager();
            if ("success".equals(cartelManager.createCartel(player, message.trim()))) {
                MessageUtils.sendMessage(player, "cartel.created", "cartel_name", message.trim());
                cartelGUI.openMainMenu(player);
                plugin.getLogger().info("Player " + player.getName() + " created cartel: " + message.trim());
            } else {
                MessageUtils.sendMessage(player, "cartel.create-failed");
            }
            activeGUI.setAwaitingChatInput(false);
            activeGUI.setChatAction(null);
        }
    }
}