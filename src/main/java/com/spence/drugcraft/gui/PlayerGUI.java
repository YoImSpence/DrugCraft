package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.businesses.BusinessManager;
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
import java.util.UUID;

public class PlayerGUI {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final BusinessManager businessManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerGUI(DrugCraft plugin, DataManager dataManager, BusinessManager businessManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.player.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("PLAYER", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.player.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.EXPERIENCE_BOTTLE, MessageUtils.getMessage("gui.player.levels")));
        inv.setItem(23, createItem(Material.COMPASS, MessageUtils.getMessage("gui.player.settings")));
        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openLevelsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.player.levels-title")));
        ActiveGUI activeGUI = new ActiveGUI("LEVELS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.player.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        UUID playerUUID = player.getUniqueId();
        int level = dataManager.getPlayerLevel(playerUUID);
        int xp = dataManager.getPlayerXP(playerUUID);
        int requiredXP = level * 1000;

        ItemStack levelItem = createItem(Material.EXPERIENCE_BOTTLE, "<aqua>Level " + level);
        ItemMeta meta = levelItem.getItemMeta();
        if (meta != null) {
            meta.setLore(List.of(
                    "<yellow>XP: " + xp + "/" + requiredXP,
                    "<yellow>Unlocked Drugs: " + dataManager.getUnlockedDrugs(playerUUID),
                    "<yellow>Unlocked Features: " + dataManager.getUnlockedFeatures(playerUUID)
            ));
            levelItem.setItemMeta(meta);
        }
        inv.setItem(22, levelItem);

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openSettingsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.player.settings-title")));
        ActiveGUI activeGUI = new ActiveGUI("SETTINGS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.player.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.PAPER, MessageUtils.getMessage("gui.player.msg-toggle")));
        inv.setItem(23, createItem(Material.COMPASS, MessageUtils.getMessage("gui.player.tp-toggle")));
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