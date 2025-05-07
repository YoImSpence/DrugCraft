package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminGUI {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Inventory mainGUI;

    public AdminGUI(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.mainGUI = Bukkit.createInventory(null, 9, MessageUtils.color("&eAdmin Drug Control"));
        initializeMainGUI();
    }

    private void initializeMainGUI() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 9; i++) {
            mainGUI.setItem(i, i == 0 || i == 8 ? border : filler);
        }
        ItemStack give = new ItemStack(Material.EMERALD);
        ItemMeta giveMeta = give.getItemMeta();
        giveMeta.setDisplayName(MessageUtils.color("&aGive Items"));
        giveMeta.setLore(Arrays.asList(MessageUtils.color("&7Distribute drugs, seeds, trimmers, or grow lights")));
        give.setItemMeta(giveMeta);
        mainGUI.setItem(4, give);
    }

    public void openGUI(Player player) {
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.admin")) {
            player.sendMessage(MessageUtils.color("&cYou do not have permission to open this GUI."));
            return;
        }
        player.openInventory(mainGUI);
    }

    public Inventory createItemGUI() {
        Inventory itemGUI = Bukkit.createInventory(null, 54, MessageUtils.color("&eSelect Item"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            itemGUI.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        // Drugs Section (Row 1, Slots 10-16)
        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] drugSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < drugs.size() && i < drugSlots.length; i++) {
            Drug drug = drugs.get(i);
            ItemStack drugItem = drug.getItem(null);
            ItemMeta meta = drugItem.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.removeIf(line -> line.contains("Quality: "));
            lore.add(MessageUtils.color("&eClick to configure"));
            meta.setLore(lore);
            drugItem.setItemMeta(meta);
            itemGUI.setItem(drugSlots[i], drugItem);
        }

        // Seeds Section (Row 2, Slots 19-24)
        int[] seedSlots = {19, 20, 21, 22, 23, 24};
        int seedIndex = 0;
        for (Drug drug : drugs) {
            if (drug.hasSeed() && seedIndex < seedSlots.length) {
                ItemStack seedItem = drug.getSeedItem(null);
                ItemMeta meta = seedItem.getItemMeta();
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.removeIf(line -> line.contains("Quality: "));
                lore.add(MessageUtils.color("&eClick to configure"));
                meta.setLore(lore);
                seedItem.setItemMeta(meta);
                itemGUI.setItem(seedSlots[seedIndex], seedItem);
                seedIndex++;
            }
        }

        // Trimmers Section (Row 3, Slots 28-30)
        String[] trimmerQualities = {"Basic", "Standard", "Exotic"};
        int[] trimmerSlots = {28, 29, 30};
        for (int i = 0; i < trimmerQualities.length; i++) {
            ItemStack trimmer = new ItemStack(Material.SHEARS);
            ItemMeta meta = trimmer.getItemMeta();
            meta.setDisplayName(MessageUtils.color("&e" + trimmerQualities[i] + " Trimmer"));
            meta.setLore(Arrays.asList(
                    MessageUtils.color(getQualityColor(trimmerQualities[i]) + "Quality: " + trimmerQualities[i]),
                    MessageUtils.color("&eClick to configure")
            ));
            meta.setUnbreakable(true);
            trimmer.setItemMeta(meta);
            itemGUI.setItem(trimmerSlots[i], trimmer);
        }

        // Grow Lights Section (Row 4, Slots 37-39)
        String[] growLightQualities = {"Basic", "Standard", "Exotic"};
        int[] growLightSlots = {37, 38, 39};
        for (int i = 0; i < growLightQualities.length; i++) {
            ItemStack growLight = GrowLight.createGrowLight(growLightQualities[i]);
            ItemMeta meta = growLight.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add(MessageUtils.color("&eClick to configure"));
            meta.setLore(lore);
            growLight.setItemMeta(meta);
            itemGUI.setItem(growLightSlots[i], growLight);
        }

        return itemGUI;
    }

    public Inventory createGiveGUI(ItemStack item, String itemName, boolean isSeed, boolean isGrowLight, String itemType, String quality, int quantity, String targetPlayer) {
        Inventory giveGUI = Bukkit.createInventory(null, 27, MessageUtils.color("&eConfigure " + itemName));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            giveGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        ItemStack qualityItem = new ItemStack(Material.DIAMOND);
        ItemMeta qualityMeta = qualityItem.getItemMeta();
        qualityMeta.setDisplayName(MessageUtils.color("&eSelect Quality"));
        qualityMeta.setLore(Arrays.asList(MessageUtils.color("&7Current: " + quality)));
        qualityItem.setItemMeta(qualityMeta);
        giveGUI.setItem(10, qualityItem);

        ItemStack quantityItem = new ItemStack(Material.PAPER);
        ItemMeta quantityMeta = quantityItem.getItemMeta();
        quantityMeta.setDisplayName(MessageUtils.color("&eSelect Quantity"));
        quantityMeta.setLore(Arrays.asList(MessageUtils.color("&7Current: " + quantity)));
        quantityItem.setItemMeta(quantityMeta);
        giveGUI.setItem(12, quantityItem);

        ItemStack playerSelect = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerMeta = playerSelect.getItemMeta();
        playerMeta.setDisplayName(MessageUtils.color("&eSelect Player"));
        playerMeta.setLore(Arrays.asList(MessageUtils.color("&7Current: " + targetPlayer)));
        playerSelect.setItemMeta(playerMeta);
        giveGUI.setItem(14, playerSelect);

        ItemStack confirm = new ItemStack(Material.EMERALD);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(MessageUtils.color("&aConfirm"));
        confirmMeta.setLore(Arrays.asList(
                MessageUtils.color("&eGive: " + quantity + " " + quality + " " + itemName),
                MessageUtils.color("&eTo: " + targetPlayer),
                MessageUtils.color("&eType: " + itemType)
        ));
        confirm.setItemMeta(confirmMeta);
        giveGUI.setItem(16, confirm);

        return giveGUI;
    }

    private String getQualityColor(String quality) {
        return switch (quality) {
            case "Legendary" -> "&d"; // Magenta
            case "Prime" -> "&9"; // Blue
            case "Exotic" -> "&e"; // Yellow
            case "Standard" -> "&a"; // Green
            default -> "&b"; // Cyan (Basic)
        };
    }
}