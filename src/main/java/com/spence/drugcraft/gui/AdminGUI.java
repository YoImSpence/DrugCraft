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
        this.mainGUI = Bukkit.createInventory(null, 9, MessageUtils.color("{#FFA500}Admin Drug Control"));
        initializeMainGUI();
    }

    private void initializeMainGUI() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 9; i++) {
            mainGUI.setItem(i, i == 0 || i == 8 ? border : filler);
        }
        ItemStack give = new ItemStack(Material.EMERALD);
        ItemMeta giveMeta = give.getItemMeta();
        giveMeta.setDisplayName(MessageUtils.color("{#00FF00}Give Items"));
        giveMeta.setLore(Arrays.asList(MessageUtils.color("{#AAAAAA}Distribute drugs, seeds, trimmers, or grow lights")));
        give.setItemMeta(giveMeta);
        mainGUI.setItem(4, give);
    }

    public void openGUI(Player player) {
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.admin")) {
            player.sendMessage(MessageUtils.color("{#FF5555}You do not have permission to open this GUI."));
            return;
        }
        player.openInventory(mainGUI);
    }

    public Inventory createItemGUI() {
        Inventory itemGUI = Bukkit.createInventory(null, 54, MessageUtils.color("{#FFA500}Select Item"));
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

        // Drugs Section (Slots 10-16)
        ItemStack drugHeader = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta drugHeaderMeta = drugHeader.getItemMeta();
        drugHeaderMeta.setDisplayName(MessageUtils.color("{#FF5555}Drugs"));
        drugHeader.setItemMeta(drugHeaderMeta);
        itemGUI.setItem(1, drugHeader);

        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] drugSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < drugs.size() && i < drugSlots.length; i++) {
            Drug drug = drugs.get(i);
            ItemStack drugItem = drug.getItem(null);
            ItemMeta meta = drugItem.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.removeIf(line -> line.contains("Quality: "));
            lore.add(MessageUtils.color("{#FFD700}Click to configure"));
            meta.setLore(lore);
            drugItem.setItemMeta(meta);
            itemGUI.setItem(drugSlots[i], drugItem);
        }

        // Seeds Section (Slots 28-33)
        ItemStack seedHeader = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta seedHeaderMeta = seedHeader.getItemMeta();
        seedHeaderMeta.setDisplayName(MessageUtils.color("{#00FF00}Seeds"));
        seedHeader.setItemMeta(seedHeaderMeta);
        itemGUI.setItem(19, seedHeader);

        int[] seedSlots = {28, 29, 30, 31, 32, 33};
        int seedIndex = 0;
        for (Drug drug : drugs) {
            if (drug.hasSeed() && seedIndex < seedSlots.length) {
                ItemStack seedItem = drug.getSeedItem(null);
                ItemMeta meta = seedItem.getItemMeta();
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.removeIf(line -> line.contains("Quality: "));
                lore.add(MessageUtils.color("{#FFD700}Click to configure"));
                meta.setLore(lore);
                seedItem.setItemMeta(meta);
                itemGUI.setItem(seedSlots[seedIndex], seedItem);
                seedIndex++;
            }
        }

        // Trimmers Section (Slots 37-39)
        ItemStack trimmerHeader = new ItemStack(Material.SHEARS);
        ItemMeta trimmerHeaderMeta = trimmerHeader.getItemMeta();
        trimmerHeaderMeta.setDisplayName(MessageUtils.color("{#1E90FF}Trimmers"));
        trimmerHeader.setItemMeta(trimmerHeaderMeta);
        itemGUI.setItem(36, trimmerHeader);

        String[] trimmerQualities = {"Basic", "Standard", "Exotic"};
        int[] trimmerSlots = {37, 38, 39};
        for (int i = 0; i < trimmerQualities.length; i++) {
            ItemStack trimmer = new ItemStack(Material.SHEARS);
            ItemMeta meta = trimmer.getItemMeta();
            meta.setDisplayName(MessageUtils.color("{#FFD700}" + trimmerQualities[i] + " Trimmer"));
            meta.setLore(Arrays.asList(
                    MessageUtils.color(getQualityColor(trimmerQualities[i]) + "Quality: " + trimmerQualities[i]),
                    MessageUtils.color("{#FFD700}Click to configure")
            ));
            trimmer.setItemMeta(meta);
            itemGUI.setItem(trimmerSlots[i], trimmer);
        }

        // Grow Lights Section (Slots 41-43)
        ItemStack growLightHeader = new ItemStack(Material.REDSTONE_LAMP);
        ItemMeta growLightHeaderMeta = growLightHeader.getItemMeta();
        growLightHeaderMeta.setDisplayName(MessageUtils.color("{#FF00FF}Grow Lights"));
        growLightHeader.setItemMeta(growLightHeaderMeta);
        itemGUI.setItem(40, growLightHeader);

        String[] growLightQualities = {"Basic", "Standard", "Exotic"};
        int[] growLightSlots = {41, 42, 43};
        for (int i = 0; i < growLightQualities.length; i++) {
            ItemStack growLight = GrowLight.createGrowLight(growLightQualities[i]);
            ItemMeta meta = growLight.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add(MessageUtils.color("{#FFD700}Click to configure"));
            meta.setLore(lore);
            growLight.setItemMeta(meta);
            itemGUI.setItem(growLightSlots[i], growLight);
        }

        return itemGUI;
    }

    public Inventory createGiveGUI(ItemStack item, String itemName, boolean isSeed, boolean isGrowLight, String itemType, String quality, int quantity, String targetPlayer) {
        Inventory giveGUI = Bukkit.createInventory(null, 27, MessageUtils.color("{#FFA500}Configure " + itemName));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&7"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&7"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            giveGUI.setItem(i, (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        ItemStack qualityItem = new ItemStack(Material.DIAMOND);
        ItemMeta qualityMeta = qualityItem.getItemMeta();
        qualityMeta.setDisplayName(MessageUtils.color("{#FFD700}Select Quality"));
        qualityMeta.setLore(Arrays.asList(MessageUtils.color("{#AAAAAA}Current: " + quality)));
        qualityItem.setItemMeta(qualityMeta);
        giveGUI.setItem(10, qualityItem);

        ItemStack quantityItem = new ItemStack(Material.PAPER);
        ItemMeta quantityMeta = quantityItem.getItemMeta();
        quantityMeta.setDisplayName(MessageUtils.color("{#FFD700}Select Quantity"));
        quantityMeta.setLore(Arrays.asList(MessageUtils.color("{#AAAAAA}Current: " + quantity)));
        quantityItem.setItemMeta(quantityMeta);
        giveGUI.setItem(12, quantityItem);

        ItemStack playerSelect = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerMeta = playerSelect.getItemMeta();
        playerMeta.setDisplayName(MessageUtils.color("{#FFD700}Select Player"));
        playerMeta.setLore(Arrays.asList(MessageUtils.color("{#AAAAAA}Current: " + targetPlayer)));
        playerSelect.setItemMeta(playerMeta);
        giveGUI.setItem(14, playerSelect);

        ItemStack confirm = new ItemStack(Material.EMERALD);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(MessageUtils.color("{#00FF00}Confirm"));
        confirmMeta.setLore(Arrays.asList(
                MessageUtils.color("{#FFD700}Give: " + quantity + " " + quality + " " + itemName),
                MessageUtils.color("{#FFD700}To: " + targetPlayer),
                MessageUtils.color("{#FFD700}Type: " + itemType)
        ));
        confirm.setItemMeta(confirmMeta);
        giveGUI.setItem(16, confirm);

        return giveGUI;
    }

    private String getQualityColor(String quality) {
        return switch (quality) {
            case "Legendary" -> "{#FF00FF}";
            case "Prime" -> "{#1E90FF}";
            case "Exotic" -> "{#FFA500}";
            case "Standard" -> "{#00FF00}";
            default -> "{#AAAAAA}"; // Basic
        };
    }
}