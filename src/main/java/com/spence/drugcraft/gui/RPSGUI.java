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

public class RPSGUI {
    private final DrugCraft plugin;

    public RPSGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openRPSMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.games.rps-title")));
        ActiveGUI activeGUI = new ActiveGUI("RPS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.border"));
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack center = createItem(Material.GRAY_STAINED_GLASS_PANE, "<#FFFF55>Center Line");
        for (int i = 4; i <= 22; i += 9) {
            inv.setItem(i, center);
        }

        inv.setItem(1, createItem(Material.GRAY_STAINED_GLASS_PANE, "<#FFFF55>Waiting for player selection"));
        inv.setItem(7, createItem(Material.GRAY_STAINED_GLASS_PANE, "<#FFFF55>Waiting for opponent selection"));
        inv.setItem(19, createItem(Material.STONE, MessageUtils.getMessage("gui.games.rps-rock")));
        inv.setItem(20, createItem(Material.PAPER, MessageUtils.getMessage("gui.games.rps-paper")));
        inv.setItem(21, createItem(Material.SHEARS, MessageUtils.getMessage("gui.games.rps-scissors")));
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