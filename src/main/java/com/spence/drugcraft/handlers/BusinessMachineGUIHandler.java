package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.Business;
import com.spence.drugcraft.gui.BusinessMachineGUI;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        Business business = businessManager.getBusinessByPlayer(player);
        if (business == null) return;

        if (displayName.equals(MessageUtils.getMessage("business.upgrades"))) {
            businessMachineGUI.openUpgradesMenu(player);
            activeGUI.setMenuSubType("upgrades");
        } else if (displayName.equals(MessageUtils.getMessage("business.admin-mode")) && player.hasPermission("drugcraft.admin")) {
            // Placeholder: Implement admin mode
        }
    }
}