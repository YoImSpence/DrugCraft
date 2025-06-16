package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.levels.LevelsGUI;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class LevelsGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final LevelsGUI levelsGUI;
    private final DataManager dataManager;

    public LevelsGUIHandler(DrugCraft plugin, LevelsGUI levelsGUI, DataManager dataManager) {
        this.plugin = plugin;
        this.levelsGUI = levelsGUI;
        this.dataManager = dataManager;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("LEVELS")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String displayName = MessageUtils.stripColor(meta.displayName());

        plugin.getLogger().info("Player " + player.getName() + " clicked item: " + displayName + " in levels GUI");

        if (displayName.equals(MessageUtils.stripColor(MessageUtils.getMessage("gui.levels.item-back")))) {
            levelsGUI.openMainMenu(player);
            activeGUI.setMenuSubType("main");
            plugin.getLogger().info("Player " + player.getName() + " returned to levels main menu");
            return;
        }

        String menuSubType = activeGUI.getMenuSubType();
        if (menuSubType.equals("main")) {
            if (displayName.startsWith("Level ")) {
                try {
                    int level = Integer.parseInt(displayName.replace("Level ", ""));
                    levelsGUI.openLevelDetails(player, level);
                    activeGUI.setMenuSubType("details");
                    plugin.getLogger().info("Player " + player.getName() + " opened details for level " + level);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid level format clicked by player " + player.getName() + ": " + displayName);
                    MessageUtils.sendMessage(player, "general.error");
                }
            }
        }
    }
}