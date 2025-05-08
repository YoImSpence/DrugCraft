package com.spence.drugcraft.admin;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminGUI {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Inventory mainMenu;
    private final Inventory itemsMenu;
    private final Inventory playersMenu;
    private final Map<String, Inventory> quantityMenus = new HashMap<>();

    public AdminGUI(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.mainMenu = Bukkit.createInventory(null, 27, MessageUtils.color("&#FF5555&lDrugCraft Admin"));
        this.itemsMenu = Bukkit.createInventory(null, 54, MessageUtils.color("&#FF5555&lSelect Items"));
        this.playersMenu = Bukkit.createInventory(null, 54, MessageUtils.color("&#FF5555&lSelect Players"));
        initializeMainMenu();
        initializeItemsMenu();
        initializePlayersMenu();
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
        ItemStack items = new ItemStack(Material.CHEST);
        ItemMeta itemsMeta = items.getItemMeta();
        itemsMeta.setDisplayName(MessageUtils.color("&#FFFF00Select Items"));
        itemsMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Choose drugs, seeds, trimmers, or grow lights")));
        items.setItemMeta(itemsMeta);
        mainMenu.setItem(11, items);

        ItemStack players = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playersMeta = players.getItemMeta();
        playersMeta.setDisplayName(MessageUtils.color("&#FFFF00Select Players"));
        playersMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Choose who will receive the items")));
        players.setItemMeta(playersMeta);
        mainMenu.setItem(13, players);

        ItemStack quantities = new ItemStack(Material.PAPER);
        ItemMeta quantitiesMeta = quantities.getItemMeta();
        quantitiesMeta.setDisplayName(MessageUtils.color("&#FFFF00Set Quantities"));
        quantitiesMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Specify quantities for each item")));
        quantities.setItemMeta(quantitiesMeta);
        mainMenu.setItem(15, quantities);
    }

    private void initializeItemsMenu() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            itemsMenu.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] drugSlots = {10, 11, 12, 13, 14, 15, 16};
        int drugIndex = 0;
        for (Drug drug : drugs) {
            if (drugIndex < drugSlots.length) {
                ItemStack drugItem = drugManager.getDrugItem(drug.getDrugId(), drug.getQuality());
                ItemMeta meta = drugItem.getItemMeta();
                meta.setLore(addSelectionLore(drugItem.getItemMeta().getLore(), false));
                drugItem.setItemMeta(meta);
                itemsMenu.setItem(drugSlots[drugIndex], drugItem);
                drugIndex++;
            }
        }
        int[] seedSlots = {19, 20, 21, 22, 23, 24, 25};
        int seedIndex = 0;
        for (Drug drug : drugs) {
            if (drug.hasSeed() && seedIndex < seedSlots.length) {
                ItemStack seedItem = drugManager.getSeedItem(drug.getDrugId(), drug.getQuality());
                ItemMeta meta = seedItem.getItemMeta();
                meta.setLore(addSelectionLore(seedItem.getItemMeta().getLore(), false));
                seedItem.setItemMeta(meta);
                itemsMenu.setItem(seedSlots[seedIndex], seedItem);
                seedIndex++;
            }
        }
        ItemStack[] trimmers = {
                createTrimmer("Basic"),
                createTrimmer("Standard"),
                createTrimmer("Exotic"),
                createTrimmer("Prime"),
                createTrimmer("Legendary")
        };
        int[] trimmerSlots = {28, 29, 30, 31, 32};
        for (int i = 0; i < trimmers.length; i++) {
            ItemMeta meta = trimmers[i].getItemMeta();
            meta.setLore(addSelectionLore(meta.getLore(), false));
            trimmers[i].setItemMeta(meta);
            itemsMenu.setItem(trimmerSlots[i], trimmers[i]);
        }
        ItemStack[] growLights = {
                GrowLight.createGrowLightItem("Basic"),
                GrowLight.createGrowLightItem("Standard"),
                GrowLight.createGrowLightItem("Exotic"),
                GrowLight.createGrowLightItem("Prime"),
                GrowLight.createGrowLightItem("Legendary")
        };
        int[] growLightSlots = {37, 38, 39, 40, 41};
        for (int i = 0; i < growLights.length; i++) {
            ItemMeta meta = growLights[i].getItemMeta();
            meta.setLore(addSelectionLore(meta.getLore(), false));
            growLights[i].setItemMeta(meta);
            itemsMenu.setItem(growLightSlots[i], growLights[i]);
        }
    }

    private void initializePlayersMenu() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            playersMenu.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        int index = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (index < slots.length) {
                ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = playerItem.getItemMeta();
                meta.setDisplayName(MessageUtils.color("&#FFFF00" + player.getName()));
                meta.setLore(Arrays.asList(MessageUtils.color("&#FFFF00Click to toggle selection")));
                playerItem.setItemMeta(meta);
                playersMenu.setItem(slots[index], playerItem);
                index++;
            }
        }
    }

    public Inventory createQuantityMenu(List<ItemStack> selectedItems) {
        Inventory quantityMenu = Bukkit.createInventory(null, 54, MessageUtils.color("&#FF5555&lSet Quantities"));
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            quantityMenu.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < Math.min(selectedItems.size(), slots.length); i++) {
            ItemStack item = selectedItems.get(i).clone();
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList(
                    MessageUtils.color("&#FFFF00Quantity: 1"),
                    MessageUtils.color("&#FFFF00Left Click: +1"),
                    MessageUtils.color("&#FFFF00Right Click: -1"),
                    MessageUtils.color("&#FFFF00Shift+Click: Remove")
            ));
            item.setItemMeta(meta);
            quantityMenu.setItem(slots[i], item);
        }
        ItemStack confirm = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(MessageUtils.color("�FF7F&lConfirm Distribution"));
        confirmMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Distribute items to selected players")));
        confirm.setItemMeta(confirmMeta);
        quantityMenu.setItem(53, confirm);
        return quantityMenu;
    }

    private ItemStack createTrimmer(String quality) {
        ItemStack trimmer = new ItemStack(Material.SHEARS);
        ItemMeta meta = trimmer.getItemMeta();
        String displayName = switch (quality) {
            case "Legendary" -> "&#FFD700Legendary Trimmer";
            case "Prime" -> "E90FFPrime Trimmer";
            case "Exotic" -> "&#FF4500Exotic Trimmer";
            case "Standard" -> "�FF00Standard Trimmer";
            default -> "�FFFFBasic Trimmer";
        };
        meta.setDisplayName(MessageUtils.color(displayName));
        meta.setLore(Arrays.asList(
                MessageUtils.color(getQualityColor(quality) + "Quality: " + quality),
                MessageUtils.color("&#D3D3D3Increases harvest quality")
        ));
        trimmer.setItemMeta(meta);
        return trimmer;
    }

    private String getQualityColor(String quality) {
        return switch (quality) {
            case "Legendary" -> "&#FFD700";
            case "Prime" -> "E90FF";
            case "Exotic" -> "&#FF4500";
            case "Standard" -> "�FF00";
            default -> "�FFFF";
        };
    }

    // Changed to public to allow access from AdminGUIListener
    public List<String> addSelectionLore(List<String> lore, boolean selected) {
        List<String> newLore = new ArrayList<>(lore);
        newLore.add(MessageUtils.color(selected ? "�FF7FSelected" : "&#FFFF00Click to Select"));
        return newLore;
    }

    public void openMainMenu(Player player) {
        player.openInventory(mainMenu);
    }

    public void openItemsMenu(Player player) {
        player.openInventory(itemsMenu);
    }

    public void openPlayersMenu(Player player) {
        player.openInventory(playersMenu);
    }

    public void openQuantityMenu(Player player, List<ItemStack> selectedItems) {
        Inventory quantityMenu = createQuantityMenu(selectedItems);
        quantityMenus.put(player.getUniqueId().toString(), quantityMenu);
        player.openInventory(quantityMenu);
    }

    public Inventory getQuantityMenu(Player player) {
        return quantityMenus.get(player.getUniqueId().toString());
    }

    public void removePlayerData(Player player) {
        quantityMenus.remove(player.getUniqueId().toString());
    }
}