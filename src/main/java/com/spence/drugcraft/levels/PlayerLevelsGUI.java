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

public class PlayerLevelsGUI {
    private final DrugCraft plugin;
    private final DataManager dataManager;

    public PlayerLevelsGUI(DrugCraft plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    public void openMainMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.player-levels.title-main")).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        Map<String, Long> drugXPs = dataManager.getPlayerDrugXPs(player.getUniqueId());
        int slot = 0;
        for (Map.Entry<String, Long> entry : drugXPs.entrySet()) {
            if (slot >= 18) break;
            String drugId = entry.getKey();
            long xp = entry.getValue();
            ItemStack drugItem = new ItemStack(Material.SUGAR);
            ItemMeta meta = drugItem.getItemMeta();
            meta.displayName(MessageUtils.color(drugId).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("XP: " + xp, TextColor.fromHexString("#D3D3D3")));
            meta.lore(lore);
            drugItem.setItemMeta(meta);
            inventory.setItem(slot++, drugItem);
        }

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 18; i < 27; i++) {
            inventory.setItem(i, border);
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("PLAYER_LEVELS", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened player levels main menu for player " + player.getName());
    }
}