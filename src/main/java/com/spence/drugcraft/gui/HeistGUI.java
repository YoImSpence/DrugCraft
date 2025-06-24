package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class HeistGUI {
    private final DrugCraft plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public HeistGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.heist.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("HEIST", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.RED_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.heist.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack bankHeist = createItem(Material.GOLD_BLOCK, "<yellow>Bank Heist");
        ItemMeta bankMeta = bankHeist.getItemMeta();
        if (bankMeta != null) {
            bankMeta.setLore(List.of("<yellow>Reward: $5000", "<yellow>Risk: High"));
            bankHeist.setItemMeta(bankMeta);
        }
        inv.setItem(21, bankHeist);

        ItemStack vaultHeist = createItem(Material.DIAMOND_BLOCK, "<yellow>Vault Heist");
        ItemMeta vaultMeta = vaultHeist.getItemMeta();
        if (vaultMeta != null) {
            vaultMeta.setLore(List.of("<yellow>Reward: $10000", "<yellow>Risk: Very High"));
            vaultHeist.setItemMeta(vaultMeta);
        }
        inv.setItem(23, vaultHeist);

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