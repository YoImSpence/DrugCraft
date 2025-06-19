package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.AdminGUI;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final AdminGUI adminGUI;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final CartelManager cartelManager;
    private final BusinessManager businessManager;

    public AdminGUIHandler(DrugCraft plugin, AdminGUI adminGUI, DataManager dataManager, DrugManager drugManager, CartelManager cartelManager, BusinessManager businessManager) {
        this.plugin = plugin;
        this.adminGUI = adminGUI;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
        this.cartelManager = cartelManager;
        this.businessManager = businessManager;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("ADMIN")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        String subType = activeGUI.getMenuSubType();

        if (subType == null) {
            if (displayName.equals(MessageUtils.getMessage("gui.admin.player-manage"))) {
                adminGUI.openPlayerManageMenu(player);
                activeGUI.setMenuSubType("player-manage");
            } else if (displayName.equals(MessageUtils.getMessage("gui.admin.drug-manage"))) {
                adminGUI.openDrugManageMenu(player);
                activeGUI.setMenuSubType("drug-manage");
            } else if (displayName.equals(MessageUtils.getMessage("gui.admin.cartel-manage"))) {
                // Placeholder: Open cartel manage menu
                MessageUtils.sendMessage(player, "admin.cartel-manage");
            } else if (displayName.equals(MessageUtils.getMessage("gui.admin.business-manage"))) {
                // Placeholder: Open business manage menu
                MessageUtils.sendMessage(player, "admin.business-manage");
            } else if (displayName.equals(MessageUtils.getMessage("gui.admin.casino-manage"))) {
                // Placeholder: Open casino manage menu
                MessageUtils.sendMessage(player, "admin.casino-manage");
            } else if (displayName.equals(MessageUtils.getMessage("gui.admin.heist-manage"))) {
                // Placeholder: Open heist manage menu
                MessageUtils.sendMessage(player, "admin.heist-manage");
            }
        } else if (subType.equals("player-manage") || subType.equals("drug-manage")) {
            if (displayName.equals(MessageUtils.getMessage("gui.admin.back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            }
        }
    }

    public void openMainMenu(Player player) {
        adminGUI.openMainMenu(player);
    }
}