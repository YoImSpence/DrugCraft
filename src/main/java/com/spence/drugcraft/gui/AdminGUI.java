package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminGUI {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final CartelManager cartelManager;
    private final BusinessManager businessManager;

    public AdminGUI(DrugCraft plugin, DataManager dataManager, DrugManager drugManager, CartelManager cartelManager, BusinessManager businessManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
        this.cartelManager = cartelManager;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.admin.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("ADMIN", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        // Glass pane border
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "gui.admin.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack playerManage = createItem(Material.PLAYER_HEAD, "gui.admin.player-manage");
        ItemStack drugManage = createItem(Material.GREEN_DYE, "gui.admin.drug-manage");
        ItemStack cartelManage = createItem(Material.EMERALD, "gui.admin.cartel-manage");
        ItemStack businessManage = createItem(Material.GOLD_INGOT, "gui.admin.business-manage");
        ItemStack casinoManage = createItem(Material.DIAMOND, "gui.admin.casino-manage");
        ItemStack heistManage = createItem(Material.TNT, "gui.admin.heist-manage");

        inv.setItem(20, playerManage);
        inv.setItem(22, drugManage);
        inv.setItem(24, cartelManage);
        inv.setItem(29, businessManage);
        inv.setItem(31, casinoManage);
        inv.setItem(33, heistManage);

        player.openInventory(inv);
    }

    public void openPlayerManageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.admin.player-manage-title")));
        ActiveGUI activeGUI = new ActiveGUI("ADMIN", inv, "player-manage");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "gui.admin.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack back = createItem(Material.BARRIER, "gui.admin.back");
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public void openDrugManageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.admin.drug-manage-title")));
        ActiveGUI activeGUI = new ActiveGUI("ADMIN", inv, "drug-manage");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "gui.admin.border");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack back = createItem(Material.BARRIER, "gui.admin.back");
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