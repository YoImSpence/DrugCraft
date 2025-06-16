package com.spence.drugcraft.dealer;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DealerGUI {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final DataManager dataManager;
    private final EconomyManager economyManager;

    public DealerGUI(DrugCraft plugin, DrugManager drugManager, DataManager dataManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.dataManager = dataManager;
        this.economyManager = new EconomyManager(plugin);
    }

    public Economy getEconomy() {
        return economyManager.getEconomy();
    }

    public DrugManager getDrugManager() {
        return drugManager;
    }

    public void openMainMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.dealer.title-main")).color(TextColor.fromHexString("#FFD700"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack buySeeds = new ItemStack(Material.MELON_SEEDS);
        ItemMeta buySeedsMeta = buySeeds.getItemMeta();
        buySeedsMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.dealer.item-buy-seeds")).color(TextColor.fromHexString("#55FF55")));
        buySeeds.setItemMeta(buySeedsMeta);
        inventory.setItem(13, buySeeds);

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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("DEALER", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened dealer main menu for player " + player.getName());
    }

    public void openBuySeedsMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.dealer.title-buy-seeds")).color(TextColor.fromHexString("#FFD700"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int slot = 0;
        boolean hasSeeds = false;
        for (String drugId : drugManager.getDrugs().keySet()) {
            if (slot >= 45) break;
            ItemStack seed = drugManager.getSeedItem(drugId, "Standard", player);
            if (seed != null) {
                NBTItem nbtItem = new NBTItem(seed);
                nbtItem.setString("drug_id", drugId);
                nbtItem.setString("quality", "Standard");
                inventory.setItem(slot++, nbtItem.getItem());
                hasSeeds = true;
                plugin.getLogger().info("Added seed for drug " + drugId + " to dealer menu for player " + player.getName());
            }
        }

        if (!hasSeeds) {
            ItemStack noSeeds = new ItemStack(Material.BARRIER);
            ItemMeta noSeedsMeta = noSeeds.getItemMeta();
            noSeedsMeta.displayName(MessageUtils.color("No Seeds Available").color(TextColor.fromHexString("#FF5555")));
            noSeeds.setItemMeta(noSeedsMeta);
            inventory.setItem(22, noSeeds);
            plugin.getLogger().warning("No seeds available for dealer menu for player " + player.getName());
        }

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.dealer.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("DEALER", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened buy seeds menu for player " + player.getName());
    }

    public int getMaxQuantityForLevel(Player player) {
        int level = dataManager.getPlayerLevel(player.getUniqueId());
        return switch (level) {
            case 1, 2 -> 16;
            case 3, 4 -> 32;
            default -> 64;
        };
    }
}