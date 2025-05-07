package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CartelGUI {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;
    private final Inventory mainGUI;

    public CartelGUI(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
        this.mainGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&bCartel Management"));
        initializeMainGUI();
    }

    private void initializeMainGUI() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            mainGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(MessageUtils.color("&eCartel Info"));
        infoMeta.setLore(Arrays.asList(MessageUtils.color("&7View cartel level, money, and stats")));
        info.setItemMeta(infoMeta);
        mainGUI.setItem(10, info);

        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = members.getItemMeta();
        membersMeta.setDisplayName(MessageUtils.color("&eManage Members"));
        membersMeta.setLore(Arrays.asList(MessageUtils.color("&7View members and set permissions")));
        members.setItemMeta(membersMeta);
        mainGUI.setItem(12, members);

        ItemStack upgrades = new ItemStack(Material.EMERALD);
        ItemMeta upgradesMeta = upgrades.getItemMeta();
        upgradesMeta.setDisplayName(MessageUtils.color("&eCartel Upgrades"));
        upgradesMeta.setLore(Arrays.asList(MessageUtils.color("&7Purchase upgrades for your cartel")));
        upgrades.setItemMeta(upgradesMeta);
        mainGUI.setItem(14, upgrades);
    }

    public void openGUI(Player player) {
        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName == null) {
            player.sendMessage(MessageUtils.color("&cYou are not in a cartel."));
            return;
        }
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.cartel")) {
            player.sendMessage(MessageUtils.color("&cYou do not have permission to manage this cartel."));
            return;
        }
        player.openInventory(mainGUI);
    }

    public Inventory createInfoGUI(CartelManager.Cartel cartel) {
        Inventory infoGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&bCartel Info: " + cartel.getName()));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            infoGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(MessageUtils.color("&eCartel Info"));
        infoMeta.setLore(Arrays.asList(
                MessageUtils.color("&7Name: " + cartel.getName()),
                MessageUtils.color("&7Level: " + cartel.getLevel()),
                MessageUtils.color("&7Stashed Money: $" + cartel.getStashedMoney()),
                MessageUtils.color("&7Members: " + cartel.getMembers().size())
        ));
        info.setItemMeta(infoMeta);
        infoGUI.setItem(13, info);
        return infoGUI;
    }

    public Inventory createMembersGUI(CartelManager.Cartel cartel) {
        Inventory membersGUI = Bukkit.createInventory(null, 54, MessageUtils.color("&bMembers: " + cartel.getName()));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            membersGUI.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int index = 0;
        Player leader = Bukkit.getPlayer(cartel.getLeader());
        ItemStack leaderItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta leaderMeta = leaderItem.getItemMeta();
        leaderMeta.setDisplayName(MessageUtils.color("&e" + (leader != null ? leader.getName() : "Offline Leader")));
        leaderMeta.setLore(Arrays.asList(MessageUtils.color("&7Role: Leader"), MessageUtils.color("&eClick to manage permissions")));
        leaderItem.setItemMeta(leaderMeta);
        membersGUI.setItem(slots[index++], leaderItem);

        for (UUID memberId : cartel.getMembers()) {
            if (!memberId.equals(cartel.getLeader()) && index < slots.length) {
                Player member = Bukkit.getPlayer(memberId);
                ItemStack memberItem = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta memberMeta = memberItem.getItemMeta();
                memberMeta.setDisplayName(MessageUtils.color("&e" + (member != null ? member.getName() : "Offline Member")));
                memberMeta.setLore(Arrays.asList(MessageUtils.color("&7Role: Member"), MessageUtils.color("&eClick to manage permissions")));
                memberItem.setItemMeta(memberMeta);
                membersGUI.setItem(slots[index++], memberItem);
            }
        }
        return membersGUI;
    }

    public Inventory createPermissionsGUI(CartelManager.Cartel cartel, UUID memberId) {
        Player member = Bukkit.getPlayer(memberId);
        String memberName = member != null ? member.getName() : "Offline Member";
        Inventory permissionsGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&bPermissions: " + memberName));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            permissionsGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        String[] permissions = {"Harvest Crops", "Plant Crops", "Access Stash"};
        int[] slots = {10, 12, 14};
        Map<String, Boolean> memberPermissions = cartel.getPermissions().getOrDefault(memberId, new HashMap<>());
        for (int i = 0; i < permissions.length; i++) {
            ItemStack permItem = new ItemStack(memberPermissions.getOrDefault(permissions[i], false) ? Material.LIME_DYE : Material.RED_DYE);
            ItemMeta permMeta = permItem.getItemMeta();
            permMeta.setDisplayName(MessageUtils.color("&e" + permissions[i]));
            permMeta.setLore(Arrays.asList(
                    MessageUtils.color("&7Status: " + (memberPermissions.getOrDefault(permissions[i], false) ? "Enabled" : "Disabled")),
                    MessageUtils.color("&eClick to toggle")
            ));
            permItem.setItemMeta(permMeta);
            permissionsGUI.setItem(slots[i], permItem);
        }
        return permissionsGUI;
    }

    public Inventory createUpgradesGUI(CartelManager.Cartel cartel) {
        Inventory upgradesGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&bUpgrades: " + cartel.getName()));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            upgradesGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        String[] upgrades = {"Crop Growth Speed", "Police Reduction", "Stash Capacity"};
        int[] slots = {10, 12, 14};
        Map<String, Integer> cartelUpgrades = cartel.getUpgrades();
        for (int i = 0; i < upgrades.length; i++) {
            ItemStack upgradeItem = new ItemStack(Material.EMERALD);
            ItemMeta upgradeMeta = upgradeItem.getItemMeta();
            int level = cartelUpgrades.getOrDefault(upgrades[i], 0);
            upgradeMeta.setDisplayName(MessageUtils.color("&e" + upgrades[i]));
            upgradeMeta.setLore(Arrays.asList(
                    MessageUtils.color("&7Level: " + level),
                    MessageUtils.color("&7Cost: $" + (level + 1) * 1000),
                    MessageUtils.color("&eClick to upgrade")
            ));
            upgradeItem.setItemMeta(upgradeMeta);
            upgradesGUI.setItem(slots[i], upgradeItem);
        }
        return upgradesGUI;
    }
}