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

public class ChessGUI {
    private final DrugCraft plugin;

    public ChessGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openChessMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.games.chess-title")));
        ActiveGUI activeGUI = new ActiveGUI("CHESS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.border"));
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(13, createItem(Material.PAPER, "<#FFFF55>Move (e.g., e4)"));
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