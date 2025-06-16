package com.spence.drugcraft.levels;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerLevelsGUIListener implements Listener {
    private final DrugCraft plugin;
    private final LevelsGUI levelsGUI;

    public PlayerLevelsGUIListener(DrugCraft plugin, LevelsGUI levelsGUI) {
        this.plugin = plugin;
        this.levelsGUI = levelsGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !event.getInventory().equals(activeGUI.getInventory())) return;

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        // Example: Handle clicks in the levels GUI
        String guiType = activeGUI.getGuiType();
        if (!guiType.equals("LEVELS")) return;

        // Add click handling logic as needed
        MessageUtils.sendMessage(player, "levels.clicked");
    }
}