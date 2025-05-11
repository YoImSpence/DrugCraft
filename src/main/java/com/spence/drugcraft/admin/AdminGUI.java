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
import java.util.UUID;

public class AdminGUI {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final GrowLight growLight;
    private final Inventory mainMenu;
    private final Inventory playersMenu;
    private final Inventory itemsMenu;
    private final Inventory quantitiesMenu;
    private final Inventory confirmMenu;
    private final Map<UUID, List<Player>> selectedPlayers = new HashMap<>();
    private final Map<UUID, List<ItemStack>> selectedItems = new HashMap<>();
    private final Map<UUID, Map<ItemStack, Integer>> itemQuantities = new HashMap<>();

    public AdminGUI(DrugCraft plugin, DrugManager drugManager, GrowLight growLight) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.growLight = growLight;
        this.mainMenu = Bukkit.createInventory(null, 27, MessageUtils.color("&#FF5555&lDrugCraft Admin"));
        this.playersMenu = Bukkit.createInventory(null, 54, MessageUtils.color("&#FF5555&lSelect Players"));
        this.itemsMenu = Bukkit.createInventory(null, 54, MessageUtils.color("&#FF5555&lSelect Items"));
        this.quantitiesMenu = Bukkit.createInventory(null, 54, MessageUtils.color("&#FF5555&lSet Quantities"));
        this.confirmMenu = Bukkit.createInventory(null, 54, MessageUtils.color("&#FF5555&lConfirm Distribution"));
        initializeMainMenu();
        initializePlayersMenu();
        initializeItemsMenu();
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
        ItemStack players = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playersMeta = players.getItemMeta();
        playersMeta.setDisplayName(MessageUtils.color("&#FFFF00Select Players"));
        playersMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Choose who will receive the items")));
        players.setItemMeta(playersMeta);
        mainMenu.setItem(13, players);
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

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(MessageUtils.color("&#FFFF00Next: Select Items"));
        next.setItemMeta(nextMeta);
        playersMenu.setItem(53, next);

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

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(MessageUtils.color("&#FFFF00Back to Player Selection"));
        back.setItemMeta(backMeta);
        itemsMenu.setItem(45, back);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(MessageUtils.color("&#FFFF00Next: Set Quantities"));
        next.setItemMeta(nextMeta);
        itemsMenu.setItem(53, next);

        List<Drug> drugs = drugManager.getSortedDrugs();
        int[] drugSlots = {10, 11, 12, 13, 14, 15, 16};
        int drugIndex = 0;
        for (Drug drug : drugs) {
            if (drugIndex < drugSlots.length) {
                ItemStack drugItem = drugManager.getDrugItem(drug.getDrugId(), drug.getQuality());
                ItemMeta meta = drugItem.getItemMeta();
                meta.setLore(addSelectionLore(meta.getLore() != null ? meta.getLore() : new ArrayList<>(), false));
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
                meta.setLore(addSelectionLore(meta.getLore() != null ? meta.getLore() : new ArrayList<>(), false));
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
            meta.setLore(addSelectionLore(meta.getLore() != null ? meta.getLore() : new ArrayList<>(), false));
            trimmers[i].setItemMeta(meta);
            itemsMenu.setItem(trimmerSlots[i], trimmers[i]);
        }
        ItemStack[] growLights = {
                growLight.createGrowLightItem("Basic"),
                growLight.createGrowLightItem("Standard"),
                growLight.createGrowLightItem("Exotic"),
                growLight.createGrowLightItem("Prime"),
                growLight.createGrowLightItem("Legendary")
        };
        int[] growLightSlots = {37, 38, 39, 40, 41};
        for (int i = 0; i < growLights.length; i++) {
            ItemMeta meta = growLights[i].getItemMeta();
            meta.setLore(addSelectionLore(meta.getLore() != null ? meta.getLore() : new ArrayList<>(), false));
            growLights[i].setItemMeta(meta);
            itemsMenu.setItem(growLightSlots[i], growLights[i]);
        }
    }

    public Inventory createQuantitiesMenu(List<ItemStack> selectedItems) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            quantitiesMenu.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(MessageUtils.color("&#FFFF00Back to Item Selection"));
        back.setItemMeta(backMeta);
        quantitiesMenu.setItem(45, back);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(MessageUtils.color("&#FFFF00Next: Confirm"));
        next.setItemMeta(nextMeta);
        quantitiesMenu.setItem(53, next);

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
            quantitiesMenu.setItem(slots[i], item);
        }
        return quantitiesMenu;
    }

    public Inventory createConfirmMenu(List<Player> selectedPlayers, List<ItemStack> selectedItems, Map<ItemStack, Integer> quantities) {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        border.setItemMeta(borderMeta);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(MessageUtils.color("&#D3D3D3"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            confirmMenu.setItem(i, (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) ? border : filler);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(MessageUtils.color("&#FFFF00Back to Quantities"));
        back.setItemMeta(backMeta);
        confirmMenu.setItem(45, back);

        ItemStack confirm = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(MessageUtils.color("&#32CD32&lConfirm Distribution"));
        confirmMeta.setLore(Arrays.asList(MessageUtils.color("&#D3D3D3Distribute items to selected players")));
        confirm.setItemMeta(confirmMeta);
        confirmMenu.setItem(53, confirm);

        // Display selected players
        int[] playerSlots = {10, 11, 12, 13, 14};
        int playerIndex = 0;
        for (Player p : selectedPlayers) {
            if (playerIndex < playerSlots.length) {
                ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = playerItem.getItemMeta();
                meta.setDisplayName(MessageUtils.color("&#FFFF00" + p.getName()));
                playerItem.setItemMeta(meta);
                confirmMenu.setItem(playerSlots[playerIndex], playerItem);
                playerIndex++;
            }
        }

        // Display selected items and quantities
        int[] itemSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int itemIndex = 0;
        for (ItemStack item : selectedItems) {
            if (itemIndex < itemSlots.length) {
                ItemStack displayItem = item.clone();
                ItemMeta meta = displayItem.getItemMeta();
                List<String> lore = new ArrayList<>(meta.getLore() != null ? meta.getLore() : new ArrayList<>());
                lore.add(MessageUtils.color("&#FFFF00Quantity: " + quantities.getOrDefault(item, 1)));
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
                confirmMenu.setItem(itemSlots[itemIndex], displayItem);
                itemIndex++;
            }
        }

        return confirmMenu;
    }

    private ItemStack createTrimmer(String quality) {
        ItemStack trimmer = new ItemStack(Material.SHEARS);
        ItemMeta meta = trimmer.getItemMeta();
        String displayName = switch (quality) {
            case "Legendary" -> "&#FFD700Legendary Trimmer";
            case "Prime" -> "&#1E90FFPrime Trimmer";
            case "Exotic" -> "&#FF4500Exotic Trimmer";
            case "Standard" -> "&#00FF00Standard Trimmer";
            default -> "&#00FFFFBasic Trimmer";
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
        return qualityColors.getOrDefault(quality, "&#FFFFFF");
    }

    public List<String> addSelectionLore(List<String> lore, boolean selected) {
        List<String> newLore = new ArrayList<>(lore);
        newLore.add(MessageUtils.color(selected ? "&#32CD32Selected" : "&#FFFF00Click to Select"));
        return newLore;
    }

    public void openMainMenu(Player player) {
        player.openInventory(mainMenu);
    }

    public void openPlayersMenu(Player player) {
        player.openInventory(playersMenu);
    }

    public void openItemsMenu(Player player) {
        player.openInventory(itemsMenu);
    }

    public void openQuantitiesMenu(Player player, List<ItemStack> selectedItems) {
        player.openInventory(createQuantitiesMenu(selectedItems));
    }

    public void openConfirmMenu(Player player, List<Player> selectedPlayers, List<ItemStack> selectedItems, Map<ItemStack, Integer> quantities) {
        player.openInventory(createConfirmMenu(selectedPlayers, selectedItems, quantities));
    }

    public void clearPlayerData(Player player) {
        selectedPlayers.remove(player.getUniqueId());
        selectedItems.remove(player.getUniqueId());
        itemQuantities.remove(player.getUniqueId());
    }

    public Map<UUID, List<Player>> getSelectedPlayers() {
        return selectedPlayers;
    }

    public Map<UUID, List<ItemStack>> getSelectedItems() {
        return selectedItems;
    }

    public Map<UUID, Map<ItemStack, Integer>> getItemQuantities() {
        return itemQuantities;
    }

    private final Map<String, String> qualityColors = new HashMap<>();

    {
        qualityColors.put("Basic", "&#00FFFF");
        qualityColors.put("Standard", "&#00FF00");
        qualityColors.put("Exotic", "&#FF4500");
        qualityColors.put("Prime", "&#1E90FF");
        qualityColors.put("Legendary", "&#FFD700");
    }
}