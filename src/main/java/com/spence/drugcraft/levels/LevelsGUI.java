package com.spence.drugcraft.levels;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LevelsGUI {
    private final DrugCraft plugin;
    private final DataManager dataManager;

    public LevelsGUI(DrugCraft plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    public void openMainMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.levels.title-main")).color(TextColor.fromHexString("#8B008B"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int playerLevel = dataManager.getPlayerLevel(player.getUniqueId());
        boolean hasLevels = false;
        for (int level = 1; level <= 20; level++) {
            ItemStack levelItem = new ItemStack(playerLevel >= level ? Material.EMERALD : Material.REDSTONE);
            ItemMeta levelMeta = levelItem.getItemMeta();
            levelMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.levels.item-level", "level", String.valueOf(level))).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            long xpRequired = dataManager.getXPRequiredForLevel(level);
            if (xpRequired == -1) {
                plugin.getLogger().warning("No XP requirement found for level " + level + " for player " + player.getName());
                continue;
            }
            if (playerLevel >= level) {
                lore.add(MessageUtils.color(MessageUtils.getMessage("gui.levels.selected")).color(TextColor.fromHexString("#00FF00")));
            } else {
                lore.add(MessageUtils.color(MessageUtils.getMessage("gui.levels.level-locked", "xp_required", String.valueOf(xpRequired))).color(TextColor.fromHexString("#FF5555")));
            }
            lore.add(MessageUtils.color("Click for Details").color(TextColor.fromHexString("#D3D3D3")));
            levelMeta.lore(lore);
            levelItem.setItemMeta(levelMeta);
            inventory.setItem((level - 1) % 9 + ((level - 1) / 9) * 9, levelItem);
            hasLevels = true;
        }

        if (!hasLevels) {
            ItemStack noLevels = new ItemStack(Material.BARRIER);
            ItemMeta noLevelsMeta = noLevels.getItemMeta();
            noLevelsMeta.displayName(MessageUtils.color("No Levels Available").color(TextColor.fromHexString("#FF5555")));
            noLevels.setItemMeta(noLevelsMeta);
            inventory.setItem(22, noLevels);
            plugin.getLogger().warning("No levels available for player " + player.getName());
        }

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("LEVELS", inventory, "main"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened levels main menu for player " + player.getName());
    }

    public void openLevelDetails(Player player, int level) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.levels.title-details", "level", String.valueOf(level))).color(TextColor.fromHexString("#8B008B"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack details = new ItemStack(Material.BOOK);
        ItemMeta detailsMeta = details.getItemMeta();
        detailsMeta.displayName(MessageUtils.color("Level " + level + " Details").color(TextColor.fromHexString("#FFD700")));
        List<Component> lore = new ArrayList<>();
        String unlocks = switch (level) {
            case 1 -> "Cannabis Seeds";
            case 2 -> "Cannabis Blue Dream Seeds";
            case 4 -> "Cannabis OG Kush Seeds";
            case 5 -> "Dispensary Business";
            case 6 -> "Cannabis Sour Diesel Seeds";
            case 7 -> "Car Wash Business";
            case 10 -> "Grow House Business";
            case 15 -> "Smuggler’s Den Business";
            default -> "No additional unlocks";
        };
        lore.add(MessageUtils.color(MessageUtils.getMessage("gui.levels.unlock-details", "features", unlocks)));
        long xpRequired = dataManager.getXPRequiredForLevel(level);
        if (xpRequired == -1) {
            lore.add(MessageUtils.color("XP Required: Unknown"));
            plugin.getLogger().warning("No XP requirement found for level " + level + " for player " + player.getName());
        } else {
            lore.add(MessageUtils.color("XP Required: " + xpRequired));
        }
        detailsMeta.lore(lore);
        details.setItemMeta(detailsMeta);
        inventory.setItem(13, details);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.levels.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(22, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("LEVELS", inventory, "details"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened level " + level + " details menu for player " + player.getName());
    }
}