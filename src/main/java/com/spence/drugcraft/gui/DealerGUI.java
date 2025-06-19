package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("<gradient:#FF0000:#FFFFFF>Dealer Menu</gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("DEALER", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        int slot = 0;
        for (Drug drug : drugManager.getSortedDrugs()) {
            ItemStack seed = drugManager.getSeedItem(drug.getId(), "standard", player);
            if (seed != null) {
                ItemMeta meta = seed.getItemMeta();
                meta.setDisplayName(MessageUtils.getMessage("gui.dealer.seed-name", "drug_name", drug.getName()));
                seed.setItemMeta(meta);
                inv.setItem(slot++, seed);
            }
        }

        player.openInventory(inv);
    }
}