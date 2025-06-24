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

public class SteedGUI {
    private final DrugCraft plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SteedGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.vehicle.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("VEHICLE", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.PURPLE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.vehicle.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack horse = createItem(Material.SADDLE, "<gold>Horse ($2000)");
        ItemMeta horseMeta = horse.getItemMeta();
        if (horseMeta != null) {
            horseMeta.setLore(List.of("<yellow>Speed: 0.2, Health: 15"));
            horse.setItemMeta(horseMeta);
        }
        inv.setItem(21, horse);

        ItemStack donkey = createItem(Material.SADDLE, "<gold>Donkey ($3000)");
        ItemMeta donkeyMeta = donkey.getItemMeta();
        if (donkeyMeta != null) {
            donkeyMeta.setLore(List.of("<yellow>Speed: 0.18, Health: 18"));
            donkey.setItemMeta(donkeyMeta);
        }
        inv.setItem(23, donkey);

        ItemStack mule = createItem(Material.SADDLE, "<gold>Mule ($4000)");
        ItemMeta muleMeta = mule.getItemMeta();
        if (muleMeta != null) {
            muleMeta.setLore(List.of("<yellow>Speed: 0.19, Health: 20"));
            mule.setItemMeta(muleMeta);
        }
        inv.setItem(25, mule);

        ItemStack warhorse = createItem(Material.SADDLE, "<gold>Warhorse ($6000)");
        ItemMeta warhorseMeta = warhorse.getItemMeta();
        if (warhorseMeta != null) {
            warhorseMeta.setLore(List.of("<yellow>Speed: 0.22, Health: 25"));
            warhorse.setItemMeta(warhorseMeta);
        }
        inv.setItem(27, warhorse);

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