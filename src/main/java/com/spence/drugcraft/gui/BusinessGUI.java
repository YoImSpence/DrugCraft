package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BusinessGUI {
    private final DrugCraft plugin;
    private final BusinessManager businessManager;

    public BusinessGUI(DrugCraft plugin, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.business.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, "gui.business.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack buy = createItem(Material.EMERALD, "gui.business.buy");
        ItemStack upgrade = createItem(Material.DIAMOND, "gui.business.upgrade");
        ItemStack stats = createItem(Material.PAPER, "gui.business.stats");

        inv.setItem(20, buy);
        inv.setItem(22, upgrade);
        inv.setItem(24, stats);

        player.openInventory(inv);
    }

    public void openBuyMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.business.buy-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS", inv, "buy");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, "gui.business.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack back = createItem(Material.BARRIER, "gui.business.back");
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String messageKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage(messageKey));
            item.setItemMeta(meta);
        }
        return item;
    }
}