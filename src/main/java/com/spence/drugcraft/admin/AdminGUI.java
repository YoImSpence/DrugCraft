package com.spence.drugcraft.admin;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.drugs.Drug;
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

public class AdminGUI {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final List<String> qualities = Arrays.asList("Basic", "Standard", "Exotic", "Prime", "Legendary");

    public AdminGUI(DrugCraft plugin, DataManager dataManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
    }

    public void openMainMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-main")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack giveItems = new ItemStack(Material.CHEST);
        ItemMeta giveMeta = giveItems.getItemMeta();
        giveMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-give-items")).color(TextColor.fromHexString("#55FF55")));
        giveItems.setItemMeta(giveMeta);
        inventory.setItem(11, giveItems);

        ItemStack playerManagement = new ItemStack(Material.PAPER);
        ItemMeta playerMeta = playerManagement.getItemMeta();
        playerMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-player-management")).color(TextColor.fromHexString("#55FF55")));
        playerManagement.setItemMeta(playerMeta);
        inventory.setItem(13, playerManagement);

        ItemStack worldTeleport = new ItemStack(Material.ENDER_PEARL);
        ItemMeta worldMeta = worldTeleport.getItemMeta();
        worldMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-world-teleport")).color(TextColor.fromHexString("#55FF55")));
        worldTeleport.setItemMeta(worldMeta);
        inventory.setItem(15, worldTeleport);

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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "main"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened admin main menu for player " + player.getName());
    }

    public void openGiveItemsMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-item-category")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack drugSeeds = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta seedsMeta = drugSeeds.getItemMeta();
        seedsMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-drug-seeds")).color(TextColor.fromHexString("#FFD700")));
        drugSeeds.setItemMeta(seedsMeta);
        inventory.setItem(10, drugSeeds);

        ItemStack drugItems = new ItemStack(Material.SUGAR);
        ItemMeta itemsMeta = drugItems.getItemMeta();
        itemsMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-drug-items")).color(TextColor.fromHexString("#FFD700")));
        drugItems.setItemMeta(itemsMeta);
        inventory.setItem(12, drugItems);

        ItemStack pluginBlocks = new ItemStack(Material.LANTERN);
        ItemMeta blocksMeta = pluginBlocks.getItemMeta();
        blocksMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-plugin-blocks")).color(TextColor.fromHexString("#FFD700")));
        pluginBlocks.setItemMeta(blocksMeta);
        inventory.setItem(14, pluginBlocks);

        ItemStack pluginTools = new ItemStack(Material.SHEARS);
        ItemMeta toolsMeta = pluginTools.getItemMeta();
        toolsMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-plugin-tools")).color(TextColor.fromHexString("#FFD700")));
        pluginTools.setItemMeta(toolsMeta);
        inventory.setItem(16, pluginTools);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "give_items"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened give items menu for player " + player.getName());
    }

    public void openDrugSeedsMenu(Player player, int page) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-drug-seeds")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int itemsPerPage = 36;
        int startIndex = page * itemsPerPage;
        List<ItemStack> seedItems = new ArrayList<>();
        for (Drug drug : drugManager.getSortedDrugs()) {
            if (drug.hasSeed()) {
                for (String quality : qualities) {
                    ItemStack seed = drugManager.getSeedItem(drug.getId(), quality, player);
                    if (seed != null) {
                        ItemMeta meta = seed.getItemMeta();
                        meta.displayName(MessageUtils.color(drug.getId() + " (" + quality + ")").color(TextColor.fromHexString("#FFD700")));
                        seed.setItemMeta(meta);
                        seedItems.add(seed);
                    }
                }
            }
        }

        if (seedItems.isEmpty()) {
            ItemStack noSeeds = new ItemStack(Material.BARRIER);
            ItemMeta noSeedsMeta = noSeeds.getItemMeta();
            noSeedsMeta.displayName(MessageUtils.color("No Seeds Available").color(TextColor.fromHexString("#FF5555")));
            noSeeds.setItemMeta(noSeedsMeta);
            inventory.setItem(22, noSeeds);
            plugin.getLogger().warning("No seeds available for admin drug seeds menu for player " + player.getName());
        }

        int totalPages = (int) Math.ceil((double) seedItems.size() / itemsPerPage);
        for (int i = startIndex; i < startIndex + itemsPerPage && i < seedItems.size(); i++) {
            inventory.setItem(i - startIndex, seedItems.get(i));
        }

        if (page > 0) {
            ItemStack previousPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = previousPage.getItemMeta();
            prevMeta.displayName(MessageUtils.color("Previous Page").color(TextColor.fromHexString("#FFD700")));
            previousPage.setItemMeta(prevMeta);
            inventory.setItem(45, previousPage);
        }

        if (page < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.displayName(MessageUtils.color("Next Page").color(TextColor.fromHexString("#FFD700")));
            nextPage.setItemMeta(nextMeta);
            inventory.setItem(53, nextPage);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "drug_seeds"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened drug seeds menu for player " + player.getName() + ", page " + page);
    }

    public void openDrugItemsMenu(Player player, int page) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-drug-items")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int itemsPerPage = 36;
        int startIndex = page * itemsPerPage;
        List<ItemStack> drugItems = new ArrayList<>();
        for (Drug drug : drugManager.getSortedDrugs()) {
            for (String quality : qualities) {
                ItemStack drugItem = drugManager.getDrugItem(drug.getId(), quality, player);
                if (drugItem != null) {
                    ItemMeta meta = drugItem.getItemMeta();
                    meta.displayName(MessageUtils.color(drug.getId() + " (" + quality + ")").color(TextColor.fromHexString("#FFD700")));
                    drugItem.setItemMeta(meta);
                    drugItems.add(drugItem);
                }
            }
        }

        if (drugItems.isEmpty()) {
            ItemStack noItems = new ItemStack(Material.BARRIER);
            ItemMeta noItemsMeta = noItems.getItemMeta();
            noItemsMeta.displayName(MessageUtils.color("No Drug Items Available").color(TextColor.fromHexString("#FF5555")));
            noItems.setItemMeta(noItemsMeta);
            inventory.setItem(22, noItems);
            plugin.getLogger().warning("No drug items available for admin drug items menu for player " + player.getName());
        }

        int totalPages = (int) Math.ceil((double) drugItems.size() / itemsPerPage);
        for (int i = startIndex; i < startIndex + itemsPerPage && i < drugItems.size(); i++) {
            inventory.setItem(i - startIndex, drugItems.get(i));
        }

        if (page > 0) {
            ItemStack previousPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = previousPage.getItemMeta();
            prevMeta.displayName(MessageUtils.color("Previous Page").color(TextColor.fromHexString("#FFD700")));
            previousPage.setItemMeta(prevMeta);
            inventory.setItem(45, previousPage);
        }

        if (page < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.displayName(MessageUtils.color("Next Page").color(TextColor.fromHexString("#FFD700")));
            nextPage.setItemMeta(nextMeta);
            inventory.setItem(53, nextPage);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "drug_items"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened drug items menu for player " + player.getName() + ", page " + page);
    }

    public void openPluginBlocksMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-plugin-blocks")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        int slot = 10;
        for (String quality : qualities) {
            ItemStack growLight = new GrowLight(plugin).createGrowLightItem(quality);
            ItemMeta meta = growLight.getItemMeta();
            meta.displayName(MessageUtils.color("Grow Light (" + quality + ")").color(TextColor.fromHexString("#FFD700")));
            growLight.setItemMeta(meta);
            inventory.setItem(slot++, growLight);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "plugin_blocks"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened plugin blocks menu for player " + player.getName());
    }

    public void openPluginToolsMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-plugin-tools")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack trimmers = new ItemStack(Material.SHEARS);
        ItemMeta trimmerMeta = trimmers.getItemMeta();
        trimmerMeta.displayName(MessageUtils.color("Trimmers").color(TextColor.fromHexString("#FFD700")));
        trimmers.setItemMeta(trimmerMeta);
        inventory.setItem(13, trimmers);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "plugin_tools"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened plugin tools menu for player " + player.getName());
    }

    public void openPlayerSelectionMenu(Player player, String itemType, String itemId, String quality) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-player-selection")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int slot = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            skullMeta.setOwningPlayer(onlinePlayer);
            skullMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.player-name", "player_name", onlinePlayer.getName())).color(TextColor.fromHexString("#FFD700")));
            playerHead.setItemMeta(skullMeta);
            inventory.setItem(slot++, playerHead);
        }

        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-confirm")).color(TextColor.fromHexString("#00FF00")));
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(49, confirm);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(53, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "player_selection"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened player selection menu for player " + player.getName());
    }

    public void openPlayerManagementMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-player-management")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        int slot = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            skullMeta.setOwningPlayer(onlinePlayer);
            skullMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.player-name", "player_name", onlinePlayer.getName())).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("Level: " + dataManager.getPlayerLevel(onlinePlayer.getUniqueId())));
            skullMeta.lore(lore);
            playerHead.setItemMeta(skullMeta);
            inventory.setItem(slot++, playerHead);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 45; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "player_management"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened player management menu for player " + player.getName());
    }

    public void openPlayerStatsMenu(Player player, UUID targetUUID) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
        String targetName = target.getName() != null ? target.getName() : targetUUID.toString();
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-player-management")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack stats = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.displayName(MessageUtils.color("Stats for " + targetName).color(TextColor.fromHexString("#FFD700")));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtils.color("Level: " + dataManager.getPlayerLevel(targetUUID)));
        Map<String, Long> drugXPs = dataManager.getPlayerDrugXPs(targetUUID);
        for (Map.Entry<String, Long> entry : drugXPs.entrySet()) {
            lore.add(MessageUtils.color(entry.getKey() + ": " + entry.getValue() + " XP"));
        }
        statsMeta.lore(lore);
        stats.setItemMeta(statsMeta);
        inventory.setItem(10, stats);

        ItemStack setLevel = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta levelMeta = setLevel.getItemMeta();
        levelMeta.displayName(MessageUtils.color("Set Level").color(TextColor.fromHexString("#FFD700")));
        setLevel.setItemMeta(levelMeta);
        inventory.setItem(12, setLevel);

        ItemStack giveXP = new ItemStack(Material.EMERALD);
        ItemMeta xpMeta = giveXP.getItemMeta();
        xpMeta.displayName(MessageUtils.color("Give XP").color(TextColor.fromHexString("#FFD700")));
        giveXP.setItemMeta(xpMeta);
        inventory.setItem(14, giveXP);

        ItemStack resetXP = new ItemStack(Material.REDSTONE);
        ItemMeta resetMeta = resetXP.getItemMeta();
        resetMeta.displayName(MessageUtils.color("Reset XP").color(TextColor.fromHexString("#FFD700")));
        resetXP.setItemMeta(resetMeta);
        inventory.setItem(16, resetXP);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "player_stats"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened player stats menu for player " + player.getName() + ", target " + targetName);
    }

    public void openWorldTeleportMenu(Player player) {
        Component title = MessageUtils.color(MessageUtils.getMessage("gui.admin.title-world-teleport")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack placeholder = new ItemStack(Material.BARRIER);
        ItemMeta placeholderMeta = placeholder.getItemMeta();
        placeholderMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.world-teleport-placeholder")).color(TextColor.fromHexString("#FFD700")));
        placeholder.setItemMeta(placeholderMeta);
        inventory.setItem(13, placeholder);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(MessageUtils.color(MessageUtils.getMessage("gui.admin.item-back")).color(TextColor.fromHexString("#FF5555")));
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
        activeMenus.put(player.getUniqueId(), new ActiveGUI("ADMIN", inventory, "world_teleport"));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened world teleport menu for player " + player.getName());
    }
}