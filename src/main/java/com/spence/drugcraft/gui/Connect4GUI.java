package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Connect4GUI {
    private final DrugCraft plugin;

    public Connect4GUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openConnect4Menu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.games.connect4-title")));
        ActiveGUI activeGUI = new ActiveGUI("CONNECT4", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.border"));
        for (int i = 0; i < 27; i++) {
            if (i >= 18 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        for (int i = 10; i <= 16; i++) {
            inv.setItem(i, createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<#FFFF55>Drop Disc"));
        }
        inv.setItem(22, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.games.back")));

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