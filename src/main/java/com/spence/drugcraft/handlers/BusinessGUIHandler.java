package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.BusinessGUI;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BusinessGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final BusinessGUI businessGUI;
    private final BusinessManager businessManager;

    public BusinessGUIHandler(DrugCraft plugin, BusinessGUI businessGUI, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessGUI = businessGUI;
        this.businessManager = businessManager;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("BUSINESS")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        String subType = activeGUI.getMenuSubType();

        if (subType == null) {
            if (displayName.equals(MessageUtils.getMessage("gui.business.buy"))) {
                businessGUI.openBuyMenu(player);
                activeGUI.setMenuSubType("buy");
            } else if (displayName.equals(MessageUtils.getMessage("gui.business.upgrade"))) {
                // Placeholder: Open upgrade menu
                MessageUtils.sendMessage(player, "business.upgrade");
            } else if (displayName.equals(MessageUtils.getMessage("gui.business.stats"))) {
                // Placeholder: Open stats menu
                MessageUtils.sendMessage(player, "business.stats");
            }
        } else if (subType.equals("buy")) {
            if (displayName.equals(MessageUtils.getMessage("gui.business.back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            }
        }
    }

    public void openMainMenu(Player player) {
        businessGUI.openMainMenu(player);
    }
}