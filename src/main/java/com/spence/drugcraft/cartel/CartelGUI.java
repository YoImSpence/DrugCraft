package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
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
    private final Inventory mainMenu;

    public CartelGUI(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
        this.mainMenu = Bukkit.createInventory(null, 27, MessageUtils.color("&#4682B4&lCartel Management"));
        initializeMainMenu();
    }

    private void initializeMainMenu() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            mainMenu.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(MessageUtils.color("&#FFFF00Cartel Info"));
        infoMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3View cartel information")));
        info.setItemMeta(infoMeta);
        mainMenu.setItem(11, info);

        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = members.getItemMeta();
        membersMeta.setDisplayName(MessageUtils.color("&#FFFF00Members"));
        membersMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Manage cartel members")));
        members.setItemMeta(membersMeta);
        mainMenu.setItem(13, members);

        ItemStack upgrades = new ItemStack(Material.EMERALD);
        ItemMeta upgradesMeta = upgrades.getItemMeta();
        upgradesMeta.setDisplayName(MessageUtils.color("&#FFFF00Upgrades"));
        upgradesMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Upgrade your cartel")));
        upgrades.setItemMeta(upgradesMeta);
        mainMenu.setItem(15, upgrades);

        ItemStack stash = new ItemStack(Material.CHEST);
        ItemMeta stashMeta = stash.getItemMeta();
        stashMeta.setDisplayName(MessageUtils.color("&#DAA520Cartel Stash"));
        stashMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Access the cartel stash")));
        stash.setItemMeta(stashMeta);
        mainMenu.setItem(17, stash);
    }

    public Inventory createInfoGUI(CartelManager.Cartel cartel) {
        Inventory infoGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&#4682B4Cartel Info"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            infoGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        Player leader = Bukkit.getPlayer(cartel.getLeader());
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(MessageUtils.color("&#FFFF00Cartel Info"));
        infoMeta.setLore(Arrays.asList(
                MessageUtils.color("&#D3D3D3Name: " + cartel.getName()),
                MessageUtils.color("&#D3D3D3Leader: " + (leader != null ? leader.getName() : "Offline")),
                MessageUtils.color("&#D3D3D3Level: " + cartel.getLevel()),
                MessageUtils.color("&#D3D3D3Stashed Money: $" + cartel.getStashedMoney()),
                MessageUtils.color("&#D3D3D3Members: " + cartel.getMembers().size())
        ));
        infoItem.setItemMeta(infoMeta);
        infoGUI.setItem(13, infoItem);

        return infoGUI;
    }

    public Inventory createMembersGUI(CartelManager.Cartel cartel) {
        Inventory membersGUI = Bukkit.createInventory(null, 54, MessageUtils.color("&#4682B4Members"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            membersGUI.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int index = 0;

        // Add leader
        Player leader = Bukkit.getPlayer(cartel.getLeader());
        if (leader != null) {
            ItemStack leaderItem = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta leaderMeta = leaderItem.getItemMeta();
            leaderMeta.setDisplayName(MessageUtils.color("&#FFFF00" + leader.getName()));
            leaderMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Cartel Leader")));
            leaderItem.setItemMeta(leaderMeta);
            membersGUI.setItem(slots[index], leaderItem);
            index++;
        }

        // Add members
        for (UUID memberId : cartel.getMembers()) {
            if (index >= slots.length) break;
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                ItemStack memberItem = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta memberMeta = memberItem.getItemMeta();
                memberMeta.setDisplayName(MessageUtils.color("&#FFFF00" + member.getName()));
                memberMeta.setLore(Arrays.asList(MessageUtils.color("&#FFFF00Click to manage permissions")));
                memberItem.setItemMeta(memberMeta);
                membersGUI.setItem(slots[index], memberItem);
                index++;
            }
        }

        return membersGUI;
    }

    public Inventory createPermissionsGUI(CartelManager.Cartel cartel, UUID memberId) {
        Player member = Bukkit.getPlayer(memberId);
        if (member == null) return null;
        Inventory permissionsGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&#4682B4Permissions: " + member.getName()));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            permissionsGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        Map<String, Boolean> memberPermissions = cartel.getPermissions().getOrDefault(memberId, new HashMap<>());

        ItemStack harvest = new ItemStack(memberPermissions.getOrDefault("Harvest Crops", false) ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta harvestMeta = harvest.getItemMeta();
        harvestMeta.setDisplayName(MessageUtils.color("&#FFFF00Harvest Crops"));
        harvestMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Click to toggle")));
        harvest.setItemMeta(harvestMeta);
        permissionsGUI.setItem(11, harvest);

        ItemStack plant = new ItemStack(memberPermissions.getOrDefault("Plant Crops", false) ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta plantMeta = plant.getItemMeta();
        plantMeta.setDisplayName(MessageUtils.color("&#FFFF00Plant Crops"));
        plantMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Click to toggle")));
        plant.setItemMeta(plantMeta);
        permissionsGUI.setItem(13, plant);

        ItemStack stash = new ItemStack(memberPermissions.getOrDefault("Access Stash", false) ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta stashMeta = stash.getItemMeta();
        stashMeta.setDisplayName(MessageUtils.color("&#FFFF00Access Stash"));
        stashMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Click to toggle")));
        stash.setItemMeta(stashMeta);
        permissionsGUI.setItem(15, stash);

        return permissionsGUI;
    }

    public Inventory createUpgradesGUI(CartelManager.Cartel cartel) {
        Inventory upgradesGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&#4682B4Upgrades"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            upgradesGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack cropGrowth = new ItemStack(Material.EMERALD);
        ItemMeta cropGrowthMeta = cropGrowth.getItemMeta();
        int cropGrowthLevel = cartel.getUpgrades().getOrDefault("Crop Growth Speed", 0);
        cropGrowthMeta.setDisplayName(MessageUtils.color("&#FFFF00Crop Growth Speed"));
        cropGrowthMeta.setLore(Arrays.asList(
                MessageUtils.color("&#D3D3D3Level: " + cropGrowthLevel),
                MessageUtils.color("&#D3D3D3Cost: $" + (cropGrowthLevel + 1) * 1000),
                MessageUtils.color("&#FFFF00Click to upgrade")
        ));
        cropGrowth.setItemMeta(cropGrowthMeta);
        upgradesGUI.setItem(11, cropGrowth);

        ItemStack policeReduction = new ItemStack(Material.EMERALD);
        ItemMeta policeReductionMeta = policeReduction.getItemMeta();
        int policeReductionLevel = cartel.getUpgrades().getOrDefault("Police Reduction", 0);
        policeReductionMeta.setDisplayName(MessageUtils.color("&#FFFF00Police Reduction"));
        policeReductionMeta.setLore(Arrays.asList(
                MessageUtils.color("&#D3D3D3Level: " + policeReductionLevel),
                MessageUtils.color("&#D3D3D3Cost: $" + (policeReductionLevel + 1) * 1000),
                MessageUtils.color("&#FFFF00Click to upgrade")
        ));
        policeReduction.setItemMeta(policeReductionMeta);
        upgradesGUI.setItem(15, policeReduction);

        return upgradesGUI;
    }

    public Inventory createStashGUI(CartelManager.Cartel cartel) {
        Inventory stashGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&#4682B4Stash"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            stashGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(MessageUtils.color("&#FFFF00Stash Info"));
        infoMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Stashed Money: $" + cartel.getStashedMoney())));
        info.setItemMeta(infoMeta);
        stashGUI.setItem(13, info);

        ItemStack deposit = new ItemStack(Material.GOLD_INGOT);
        ItemMeta depositMeta = deposit.getItemMeta();
        depositMeta.setDisplayName(MessageUtils.color("&#FFFF00Deposit $1000"));
        depositMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Add money to the stash")));
        deposit.setItemMeta(depositMeta);
        stashGUI.setItem(11, deposit);

        ItemStack withdraw = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta withdrawMeta = withdraw.getItemMeta();
        withdrawMeta.setDisplayName(MessageUtils.color("&#FFFF00Withdraw $1000"));
        withdrawMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Take money from the stash")));
        withdraw.setItemMeta(withdrawMeta);
        stashGUI.setItem(15, withdraw);

        return stashGUI;
    }

    public void openMainMenu(Player player) {
        player.openInventory(mainMenu);
    }
}