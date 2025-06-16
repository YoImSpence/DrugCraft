package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.Business;
import com.spence.drugcraft.businesses.BusinessMachineGUI;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BusinessMachineGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final BusinessMachineGUI businessMachineGUI;
    private final BusinessManager businessManager;
    private final EconomyManager economyManager;

    public BusinessMachineGUIHandler(DrugCraft plugin, BusinessMachineGUI businessMachineGUI, BusinessManager businessManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.businessMachineGUI = businessMachineGUI;
        this.businessManager = businessManager;
        this.economyManager = economyManager;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("BUSINESS_MACHINE")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String displayName = MessageUtils.stripColor(meta.displayName());
        List<String> lore = meta.lore() != null ? meta.lore().stream().map(MessageUtils::stripColor).toList() : List.of();

        Business business = businessManager.getBusinessByPlayer(player);
        if (business == null) {
            MessageUtils.sendMessage(player, "business.error");
            return;
        }

        if (displayName.equals("Collect Revenue")) {
            activeGUI.setMenuSubType("collect_revenue");
            MessageUtils.sendMessage(player, "business.revenue-placeholder");
            plugin.getLogger().info("Player " + player.getName() + " attempted to collect revenue for business " + business.getId());
        } else if (displayName.equals("Back")) {
            businessMachineGUI.openMainMenu(player, business);
            activeGUI.setMenuSubType(null);
            plugin.getLogger().info("Player " + player.getName() + " returned to business machine main menu");
        }
    }
}