package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.SteedGUI;
import com.spence.drugcraft.steeds.SteedManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SteedGUIHandler {
    private final DrugCraft plugin;
    private final SteedGUI steedGUI;
    private final SteedManager steedManager;
    private final EconomyManager economyManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SteedGUIHandler(DrugCraft plugin, SteedGUI steedGUI, SteedManager steedManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.steedGUI = steedGUI;
        this.steedManager = steedManager;
        this.economyManager = economyManager;
    }

    public void openMainMenu(Player player) {
        steedGUI.openMainMenu(player);
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

        if (guiType.equals("VEHICLE")) {
            if (displayName.contains("Horse") || displayName.contains("Donkey") || displayName.contains("Mule") || displayName.contains("Warhorse")) {
                String steedType = displayName.split("\\(")[0].trim().substring(6); // Extract type from "<gold>Type ($price)"
                steedManager.purchaseSteed(player, steedType);
            } else if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                player.closeInventory();
            }
        }
    }
}