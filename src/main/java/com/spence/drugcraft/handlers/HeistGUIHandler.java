package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.HeistGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HeistGUIHandler {
    private final DrugCraft plugin;
    private final HeistGUI heistGUI;

    public HeistGUIHandler(DrugCraft plugin, HeistGUI heistGUI) {
        this.plugin = plugin;
        this.heistGUI = heistGUI;
    }

    public void openMainMenu(Player player) {
        heistGUI.openMainMenu(player);
    }

    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == Material.AIR) return;

        String displayName = item.getItemMeta().getDisplayName();
        if (displayName.equals(MessageUtils.getMessage("gui.heist.start-heist"))) {
            MessageUtils.sendMessage(player, "heist.started");
            // Placeholder: Implement heist logic
        }
    }
}