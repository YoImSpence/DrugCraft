package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BusinessGUI {
    private final DrugCraft plugin;
    private final BusinessManager businessManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public BusinessGUI(DrugCraft plugin, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.business.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.business.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.GOLD_INGOT, MessageUtils.getMessage("gui.business.buy")));
        inv.setItem(23, createItem(Material.EMERALD, MessageUtils.getMessage("gui.business.upgrade")));
        inv.setItem(25, createItem(Material.PAPER, MessageUtils.getMessage("gui.business.stats")));

        player.openInventory(inv);
    }

    public void openBuyMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.business.buy-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS_BUY", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.business.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack drugStore = createItem(Material.GOLD_INGOT, "<gold>Drug Store ($10000)");
        ItemMeta drugStoreMeta = drugStore.getItemMeta();
        if (drugStoreMeta != null) {
            drugStoreMeta.setLore(List.of("<yellow>Type: Drug Store"));
            drugStore.setItemMeta(drugStoreMeta);
        }
        inv.setItem(21, drugStore);

        ItemStack pharmacy = createItem(Material.GOLD_INGOT, "<gold>Pharmacy ($15000)");
        ItemMeta pharmacyMeta = pharmacy.getItemMeta();
        if (pharmacyMeta != null) {
            pharmacyMeta.setLore(List.of("<yellow>Type: Pharmacy"));
            pharmacy.setItemMeta(pharmacyMeta);
        }
        inv.setItem(23, pharmacy);

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openUpgradeMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.business.upgrade-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS_UPGRADE", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.business.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack production = createItem(Material.EMERALD, "<green>Production Rate ($5000)");
        ItemMeta productionMeta = production.getItemMeta();
        if (productionMeta != null) {
            productionMeta.setLore(List.of("<yellow>Increases drug production by 10%"));
            production.setItemMeta(productionMeta);
        }
        inv.setItem(21, production);

        ItemStack capacity = createItem(Material.CHEST, "<gold>Storage Capacity ($3000)");
        ItemMeta capacityMeta = capacity.getItemMeta();
        if (capacityMeta != null) {
            capacityMeta.setLore(List.of("<yellow>Increases storage by 100 units"));
            capacity.setItemMeta(capacityMeta);
        }
        inv.setItem(23, capacity);

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openStatsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.business.stats-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS_STATS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.business.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        List<String> businesses = businessManager.getPlayerBusinesses(player.getUniqueId());
        if (businesses.isEmpty()) {
            ItemStack noBusiness = createItem(Material.BARRIER, "<red>No Businesses Owned");
            inv.setItem(22, noBusiness);
        } else {
            ItemStack stats = createItem(Material.PAPER, "<yellow>Business Stats");
            ItemMeta statsMeta = stats.getItemMeta();
            if (statsMeta != null) {
                List<String> lore = new ArrayList<>();
                for (String business : businesses) {
                    lore.add("<yellow>" + business + ":");
                    lore.add("<yellow>Income: $" + businessManager.getBusinessIncome(business));
                    lore.add("<yellow>Drugs Produced: " + businessManager.getBusinessDrugProduction(business));
                }
                statsMeta.setLore(lore);
                stats.setItemMeta(statsMeta);
            }
            inv.setItem(22, stats);
        }

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(displayName));
            item.setItemMeta(meta);
        }
        return item;
    }
}