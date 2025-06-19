package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.casino.CasinoManager;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CasinoGUI {
    private final DrugCraft plugin;
    private final CasinoManager casinoManager;

    public CasinoGUI(DrugCraft plugin, CasinoManager casinoManager) {
        this.plugin = plugin;
        this.casinoManager = casinoManager;
    }

    public void openBlackjackMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.casino.blackjack-title")));
        ActiveGUI activeGUI = new ActiveGUI("CASINO", inv, "blackjack");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(MessageUtils.getMessage("gui.casino.blackjack-info"));
            inv.setItem(11, info);
        }

        player.openInventory(inv);
    }

    public void openSlotsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.casino.slots-title")));
        ActiveGUI activeGUI = new ActiveGUI("CASINO", inv, "slots");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack info = new ItemStack(Material.LEVER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(MessageUtils.getMessage("gui.casino.slots-info"));
            inv.setItem(13, info);
        }

        player.openInventory(inv);
    }

    public void openPokerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.casino.poker-title")));
        ActiveGUI activeGUI = new ActiveGUI("CASINO", inv, "poker");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack info = new ItemStack(Material.OAK_SIGN);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(MessageUtils.getMessage("gui.casino.poker-info"));
            inv.setItem(13, info);
        }

        player.openInventory(inv);
    }

    public void openRouletteMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.casino.roulette-title")));
        ActiveGUI activeGUI = new ActiveGUI("CASINO", inv, "roulette");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack info = new ItemStack(Material.COMPASS);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(MessageUtils.getMessage("gui.casino.roulette-info"));
            inv.setItem(13, info);
        }

        player.openInventory(inv);
    }

    public void openBaccaratMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.casino.baccarat-title")));
        ActiveGUI activeGUI = new ActiveGUI("CASINO", inv, "baccarat");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(MessageUtils.getMessage("gui.casino.baccarat-info"));
            inv.setItem(13, info);
        }

        player.openInventory(inv);
    }
}