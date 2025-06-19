package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HeistGUI {
    private final DrugCraft plugin;

    public HeistGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.heist.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("HEIST", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack startHeist = new ItemStack(Material.DIAMOND);
        ItemMeta meta = startHeist.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage("gui.heist.start-heist"));
            startHeist.setItemMeta(meta);
        }
        inv.setItem(13, startHeist);

        player.openInventory(inv);
    }
}