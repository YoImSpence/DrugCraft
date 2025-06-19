package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CartelGUI {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelGUI(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.cartel.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack info = createItem(Material.BOOK, "gui.cartel.item-info");
        ItemStack members = createItem(Material.PLAYER_HEAD, "gui.cartel.item-members");
        ItemStack upgrades = createItem(Material.DIAMOND, "gui.cartel.item-upgrades");
        ItemStack permissions = createItem(Material.PAPER, "gui.cartel.item-permissions");
        ItemStack create = createItem(Material.EMERALD, "gui.cartel.item-create");

        inv.setItem(10, info);
        inv.setItem(12, members);
        inv.setItem(14, upgrades);
        inv.setItem(16, permissions);
        inv.setItem(18, create);

        player.openInventory(inv);
    }

    public void openInfoMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.cartel.info-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL", inv, "info");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack back = createItem(Material.BARRIER, "gui.cartel.item-back");
        inv.setItem(26, back);

        player.openInventory(inv);
    }

    public void openMembersMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.cartel.members-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL", inv, "members");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack back = createItem(Material.BARRIER, "gui.cartel.item-back");
        inv.setItem(26, back);

        player.openInventory(inv);
    }

    public void openUpgradeMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.cartel.upgrades-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL", inv, "upgrades");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack back = createItem(Material.BARRIER, "gui.cartel.item-back");
        inv.setItem(26, back);

        player.openInventory(inv);
    }

    public void openPermissionsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.cartel.permissions-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL", inv, "permissions");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack back = createItem(Material.BARRIER, "gui.cartel.item-back");
        inv.setItem(26, back);

        player.openInventory(inv);
    }

    public void openConfirmationMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.cartel.confirmation-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL", inv, "confirmation");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack confirm = createItem(Material.GREEN_WOOL, "gui.cartel.confirm");
        ItemStack cancel = createItem(Material.RED_WOOL, "gui.cartel.cancel");
        inv.setItem(11, confirm);
        inv.setItem(15, cancel);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String messageKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage(messageKey));
            item.setItemMeta(meta);
        }
        return item;
    }
}