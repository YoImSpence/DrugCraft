package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.Business;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.BusinessMachineGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BusinessMachineGUIHandler {
    private final DrugCraft plugin;
    private final BusinessMachineGUI businessMachineGUI;
    private final BusinessManager businessManager;

    public BusinessMachineGUIHandler(DrugCraft plugin, BusinessMachineGUI businessMachineGUI, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessMachineGUI = businessMachineGUI;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        businessMachineGUI.openMainMenu(player);
    }

    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == Material.AIR) return;

        String displayName = item.getItemMeta().getDisplayName();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        String guiType = activeGUI.getType();
        Business business = businessManager.getBusinessByPlayer(player);
        if (business == null) {
            MessageUtils.sendMessage(player, "business.not-owned");
            return;
        }

        if (guiType.equals("BUSINESS_MACHINE")) {
            String subType = activeGUI.getSubType();
            if (subType.equals("MAIN")) {
                if (displayName.equals(MessageUtils.getMessage("gui.business-machine.craft"))) {
                    activeGUI.setSubType("CRAFT");
                    businessMachineGUI.openCraftMenu(player);
                } else if (displayName.equals(MessageUtils.getMessage("gui.business-machine.process"))) {
                    // Placeholder: Open process menu
                } else if (displayName.equals(MessageUtils.getMessage("gui.business-machine.storage"))) {
                    // Placeholder: Open storage menu
                }
            } else if (subType.equals("CRAFT")) {
                if (displayName.equals(MessageUtils.getMessage("gui.business-machine.back"))) {
                    activeGUI.setSubType("MAIN");
                    businessMachineGUI.openMainMenu(player);
                }
            }
        }
    }
}