package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.HeistGUI;
import com.spence.drugcraft.heists.HeistManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HeistGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final HeistGUI heistGUI;
    private final HeistManager heistManager;

    public HeistGUIHandler(DrugCraft plugin, HeistGUI heistGUI, HeistManager heistManager) {
        this.plugin = plugin;
        this.heistGUI = heistGUI;
        this.heistManager = heistManager;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("HEIST")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        // Placeholder: Implement heist selection logic
    }

    public void openMainMenu(Player player) {
        heistGUI.openMainMenu(player);
    }
}