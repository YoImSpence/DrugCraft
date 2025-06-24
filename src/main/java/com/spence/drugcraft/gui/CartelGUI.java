package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartelGUI {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CartelGUI(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    public void openMainMenu(Player player) {
        Cartel cartel = cartelManager.getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            openCreateMenu(player);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.cartel.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.cartel.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.PAPER, MessageUtils.getMessage("gui.cartel.info")));
        inv.setItem(23, createItem(Material.PLAYER_HEAD, MessageUtils.getMessage("gui.cartel.members")));
        inv.setItem(25, createItem(Material.IRON_INGOT, MessageUtils.getMessage("gui.cartel.permissions")));
        inv.setItem(27, createItem(Material.DIAMOND, MessageUtils.getMessage("gui.cartel.upgrades")));

        player.openInventory(inv);
    }

    public void openCreateMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.cartel.create-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL_CREATE", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.cartel.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(22, createItem(Material.EMERALD, MessageUtils.getMessage("gui.cartel.create")));

        player.openInventory(inv);
    }

    public void openInfoMenu(Player player) {
        Cartel cartel = cartelManager.getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            openCreateMenu(player);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.cartel.info-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL_INFO", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.cartel.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack info = createItem(Material.BOOK, MessageUtils.getMessage("gui.cartel.info"));
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            String ownerName = Bukkit.getOfflinePlayer(cartel.getOwner()).getName();
            meta.setLore(List.of(
                    "<yellow>Name: " + cartel.getName(),
                    "<yellow>Level: " + cartel.getLevel(),
                    "<yellow>Members: " + cartel.getMembers().size(),
                    "<yellow>Owner: " + (ownerName != null ? ownerName : "Unknown")
            ));
            info.setItemMeta(meta);
        }
        inv.setItem(22, info);

        ItemStack disband = createItem(Material.BARRIER, MessageUtils.getMessage("gui.cartel.disband"));
        ItemMeta disbandMeta = disband.getItemMeta();
        if (disbandMeta != null) {
            disbandMeta.setLore(List.of("<yellow>ID: " + cartel.getId()));
            disband.setItemMeta(disbandMeta);
        }
        inv.setItem(24, disband);

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openMembersMenu(Player player) {
        Cartel cartel = cartelManager.getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            openCreateMenu(player);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.cartel.members-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL_MEMBERS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.cartel.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        int slot = 10;
        for (UUID member : cartel.getMembers()) {
            if (slot >= 44) break;
            Player memberPlayer = Bukkit.getPlayer(member);
            String memberName = memberPlayer != null ? memberPlayer.getName() : "Offline Player";
            ItemStack item = createItem(Material.PLAYER_HEAD, "<yellow>" + memberName);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setLore(List.of("<yellow>UUID: " + member));
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
            if (slot % 9 == 0) slot += 2;
        }

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openPermissionsMenu(Player player) {
        Cartel cartel = cartelManager.getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            openCreateMenu(player);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.cartel.permissions-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL_PERMISSIONS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.cartel.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        int slot = 10;
        for (UUID member : cartel.getMembers()) {
            if (slot >= 44) break;
            Player memberPlayer = Bukkit.getPlayer(member);
            String memberName = memberPlayer != null ? memberPlayer.getName() : "Offline Player";
            ItemStack buildPerm = createItem(Material.BRICKS, "<yellow>Build Permission: " + memberName);
            ItemMeta buildMeta = buildPerm.getItemMeta();
            if (buildMeta != null) {
                buildMeta.setLore(List.of("<yellow>UUID: " + member, "<yellow>Status: " + (cartelManager.hasPermission(member, "build") ? "Enabled" : "Disabled")));
                buildPerm.setItemMeta(buildMeta);
            }
            inv.setItem(slot, buildPerm);
            slot++;

            ItemStack interactPerm = createItem(Material.LEVER, "<yellow>Interact Permission: " + memberName);
            ItemMeta interactMeta = interactPerm.getItemMeta();
            if (interactMeta != null) {
                interactMeta.setLore(List.of("<yellow>UUID: " + member, "<yellow>Status: " + (cartelManager.hasPermission(member, "interact") ? "Enabled" : "Disabled")));
                interactPerm.setItemMeta(interactMeta);
            }
            inv.setItem(slot, interactPerm);
            slot++;
            if (slot % 9 == 0) slot += 2;
        }

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openUpgradesMenu(Player player) {
        Cartel cartel = cartelManager.getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            openCreateMenu(player);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.cartel.upgrades-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL_UPGRADES", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.cartel.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack levelUp = createItem(Material.DIAMOND, "<aqua>Level Up ($10000)");
        ItemMeta levelUpMeta = levelUp.getItemMeta();
        if (levelUpMeta != null) {
            levelUpMeta.setLore(List.of("<yellow>Increases cartel level by 1"));
            levelUp.setItemMeta(levelUpMeta);
        }
        inv.setItem(21, levelUp);

        ItemStack stashCapacity = createItem(Material.CHEST, "<gold>Stash Capacity ($5000)");
        ItemMeta stashMeta = stashCapacity.getItemMeta();
        if (stashMeta != null) {
            stashMeta.setLore(List.of("<yellow>Increases stash capacity by 100 units"));
            stashCapacity.setItemMeta(stashMeta);
        }
        inv.setItem(23, stashCapacity);

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