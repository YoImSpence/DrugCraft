package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DealerGUI {
    private final DrugCraft plugin;
    private final DrugManager drugManager;

    public DealerGUI(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.dealer.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("DEALER", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.GREEN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.dealer.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ConfigurationSection seeds = plugin.getConfigManager().getConfig("drugs.yml").getConfigurationSection("seeds");
        if (seeds != null) {
            int slot = 10;
            for (String drugId : seeds.getKeys(false)) {
                if (slot >= 44) break;
                ItemStack seedItem = drugManager.createSeedItem(drugId);
                if (seedItem != null) {
                    inv.setItem(slot, seedItem);
                }
                slot++;
                if (slot % 9 == 0) slot += 2;
            }
        }

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