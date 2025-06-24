package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.Business;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BusinessMachineGUI {
    private final DrugCraft plugin;
    private final BusinessManager businessManager;

    public BusinessMachineGUI(DrugCraft plugin, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        Business business = businessManager.getBusinessByPlayer(player);
        if (business == null) {
            MessageUtils.sendMessage(player, "business.not-owned");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.business-machine.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS_MACHINE", inv, "MAIN");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.business-machine.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(20, createItem(Material.CRAFTING_TABLE, MessageUtils.getMessage("gui.business-machine.craft")));
        inv.setItem(22, createItem(Material.FURNACE, MessageUtils.getMessage("gui.business-machine.process")));
        inv.setItem(24, createItem(Material.CHEST, MessageUtils.getMessage("gui.business-machine.storage")));

        player.openInventory(inv);
    }

    public void openCraftMenu(Player player) {
        Business business = businessManager.getBusinessByPlayer(player);
        if (business == null) {
            MessageUtils.sendMessage(player, "business.not-owned");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.business-machine.craft-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS_MACHINE", inv, "CRAFT");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.business-machine.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        // Placeholder: Add craftable items
        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.business-machine.back")));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }
}