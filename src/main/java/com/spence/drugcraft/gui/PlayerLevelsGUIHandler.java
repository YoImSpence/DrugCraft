package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.levels.PlayerLevelsGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerLevelsGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final PlayerLevelsGUI playerLevelsGUI;

    public PlayerLevelsGUIHandler(DrugCraft plugin, PlayerLevelsGUI playerLevelsGUI) {
        this.plugin = plugin;
        this.playerLevelsGUI = playerLevelsGUI;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("PLAYER_LEVELS")) return;

        playerLevelsGUI.openMainMenu(player);
    }
}