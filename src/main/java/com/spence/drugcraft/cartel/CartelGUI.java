package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class CartelGUI {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelGUI(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    public void openMainMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.cartel.title-main")).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        plugin.getLogger().info("Opening cartel main menu for player " + player.getName() + ", cartel: " + (cartelName != null ? cartelName : "none"));

        if (cartelName == null) {
            ItemStack createCartel = new ItemStack(Material.NAME_TAG);
            ItemMeta createMeta = createCartel.getItemMeta();
            createMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-create")).color(TextColor.fromHexString("#FFD700")));
            createCartel.setItemMeta(createMeta);
            inventory.setItem(13, createCartel);
        } else {
            ItemStack info = new ItemStack(Material.BOOK);
            ItemMeta infoMeta = info.getItemMeta();
            infoMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-info")).color(TextColor.fromHexString("#FFD700")));
            info.setItemMeta(infoMeta);
            inventory.setItem(10, info);

            ItemStack members = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta membersMeta = members.getItemMeta();
            membersMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-members")).color(TextColor.fromHexString("#FFD700")));
            members.setItemMeta(membersMeta);
            inventory.setItem(12, members);

            ItemStack upgrades = new ItemStack(Material.ANVIL);
            ItemMeta upgradesMeta = upgrades.getItemMeta();
            upgradesMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-upgrades")).color(TextColor.fromHexString("#FFD700")));
            upgrades.setItemMeta(upgradesMeta);
            inventory.setItem(14, upgrades);

            ItemStack permissions = new ItemStack(Material.NAME_TAG);
            ItemMeta permissionsMeta = permissions.getItemMeta();
            permissionsMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-permissions")).color(TextColor.fromHexString("#FFD700")));
            permissions.setItemMeta(permissionsMeta);
            inventory.setItem(16, permissions);
        }

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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CARTEL", inventory, "main"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened cartel main menu for player " + player.getName());
    }

    public void openInfoMenu(Player player) {
        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName == null) {
            MessageUtils.sendMessage(player, "cartel.not-member");
            plugin.getLogger().warning("Player " + player.getName() + " attempted to open cartel info without being in a cartel");
            return;
        }
        Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-found", "cartel_name", cartelName);
            plugin.getLogger().warning("Cartel " + cartelName + " not found for player " + player.getName());
            return;
        }

        Component title = MessageUtils.color(MessageUtils.getMessage("gui.cartel.title-info")).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-info")).color(TextColor.fromHexString("#FFD700")));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtils.color("Name: " + cartel.getName()));
        String ownerName = Bukkit.getOfflinePlayer(cartel.getOwner()).getName() != null ?
                Bukkit.getOfflinePlayer(cartel.getOwner()).getName() : "Unknown";
        lore.add(MessageUtils.color("Owner: " + ownerName));
        lore.add(MessageUtils.color("Members: " + cartel.getMembers().size()));
        infoMeta.lore(lore);
        info.setItemMeta(infoMeta);
        inventory.setItem(13, info);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(22, back);

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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CARTEL", inventory, "info"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened cartel info menu for player " + player.getName());
    }

    public void openMembersMenu(Player player) {
        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName == null) {
            MessageUtils.sendMessage(player, "cartel.not-member");
            plugin.getLogger().warning("Player " + player.getName() + " attempted to open cartel members without being in a cartel");
            return;
        }
        Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-found", "cartel_name", cartelName);
            plugin.getLogger().warning("Cartel " + cartelName + " not found for player " + player.getName());
            return;
        }

        Component title = MessageUtils.color(MessageUtils.getMessage("gui.cartel.title-members")).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int slot = 0;
        for (UUID memberUUID : cartel.getMembers()) {
            if (slot >= 36) break;
            ItemStack member = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta memberMeta = (SkullMeta) member.getItemMeta();
            OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberUUID);
            String memberName = memberPlayer.getName() != null ? memberPlayer.getName() : memberUUID.toString();
            memberMeta.setOwningPlayer(memberPlayer);
            memberMeta.displayName(MessageUtils.color(memberName).color(TextColor.fromHexString("#FFD700")));
            member.setItemMeta(memberMeta);
            inventory.setItem(slot++, member);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 36; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CARTEL", inventory, "members"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened cartel members menu for player " + player.getName());
    }

    public void openUpgradesMenu(Player player) {
        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName == null) {
            MessageUtils.sendMessage(player, "cartel.not-member");
            plugin.getLogger().warning("Player " + player.getName() + " attempted to open cartel upgrades without being in a cartel");
            return;
        }
        Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-found", "cartel_name", cartelName);
            plugin.getLogger().warning("Cartel " + cartelName + " not found for player " + player.getName());
            return;
        }

        Component title = MessageUtils.color(MessageUtils.getMessage("gui.cartel.title-upgrades")).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack stashUpgrade = new ItemStack(Material.CHEST);
        ItemMeta stashMeta = stashUpgrade.getItemMeta();
        stashMeta.displayName(MessageUtils.color("Upgrade Stash Level").color(TextColor.fromHexString("#FFD700")));
        List<Component> stashLore = new ArrayList<>();
        stashLore.add(MessageUtils.color("Current Level: " + cartel.getStashLevel()));
        stashMeta.lore(stashLore);
        stashUpgrade.setItemMeta(stashMeta);
        inventory.setItem(11, stashUpgrade);

        ItemStack growthUpgrade = new ItemStack(Material.OAK_SAPLING);
        ItemMeta growthMeta = growthUpgrade.getItemMeta();
        growthMeta.displayName(MessageUtils.color("Upgrade Growth Level").color(TextColor.fromHexString("#FFD700")));
        List<Component> growthLore = new ArrayList<>();
        growthLore.add(MessageUtils.color("Current Level: " + cartel.getGrowthLevel()));
        growthMeta.lore(growthLore);
        growthUpgrade.setItemMeta(growthMeta);
        inventory.setItem(15, growthUpgrade);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(22, back);

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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CARTEL", inventory, "upgrades"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened cartel upgrades menu for player " + player.getName());
    }

    public void openPermissionsMenu(Player player) {
        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName == null) {
            MessageUtils.sendMessage(player, "cartel.not-member");
            plugin.getLogger().warning("Player " + player.getName() + " attempted to open cartel permissions without being in a cartel");
            return;
        }
        Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-found", "cartel_name", cartelName);
            plugin.getLogger().warning("Cartel " + cartelName + " not found for player " + player.getName());
            return;
        }
        if (!cartel.isLeader(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "cartel.no-permission");
            plugin.getLogger().warning("Player " + player.getName() + " attempted to manage permissions without leader status");
            return;
        }

        Component title = MessageUtils.color(MessageUtils.getMessage("gui.cartel.title-permissions")).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int slot = 0;
        for (UUID memberUUID : cartel.getMembers()) {
            if (slot >= 36) break;
            ItemStack member = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta memberMeta = (SkullMeta) member.getItemMeta();
            OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberUUID);
            String memberName = memberPlayer.getName() != null ? memberPlayer.getName() : memberUUID.toString();
            memberMeta.setOwningPlayer(memberPlayer);
            memberMeta.displayName(MessageUtils.color(memberName).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("Member: " + memberName));
            lore.add(MessageUtils.color("Click to manage permissions"));
            memberMeta.lore(lore);
            member.setItemMeta(memberMeta);
            inventory.setItem(slot++, member);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.cartel.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 36; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CARTEL", inventory, "permissions"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened cartel permissions menu for player " + player.getName());
    }
}