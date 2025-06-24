package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminGUI {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final CartelManager cartelManager;
    private final BusinessManager businessManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AdminGUI(DrugCraft plugin, DataManager dataManager, DrugManager drugManager, CartelManager cartelManager, BusinessManager businessManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
        this.cartelManager = cartelManager;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.admin.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("ADMIN", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.admin.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.PLAYER_HEAD, MessageUtils.getMessage("gui.admin.player-manage")));
        inv.setItem(23, createItem(Material.CHEST, MessageUtils.getMessage("gui.admin.cartel-manage")));
        inv.setItem(25, createItem(Material.IRON_INGOT, MessageUtils.getMessage("gui.admin.business-manage")));
        inv.setItem(27, createItem(Material.COMPASS, MessageUtils.getMessage("gui.admin.world-tp")));

        player.openInventory(inv);
    }

    public void openPlayerManageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.admin.player-manage-title")));
        ActiveGUI activeGUI = new ActiveGUI("PLAYER_MANAGE", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.admin.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        int slot = 10;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (slot >= 44) break;
            ItemStack item = createItem(Material.PLAYER_HEAD, MessageUtils.getMessage("gui.admin.player-manage") + ": " + onlinePlayer.getName());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String ip = onlinePlayer.getAddress().getAddress().getHostAddress();
                meta.setLore(List.of("<yellow>IP: " + ip, "<yellow>UUID: " + onlinePlayer.getUniqueId()));
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
            if (slot % 9 == 0) slot += 2;
        }

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openPlayerOptionsMenu(Player player, UUID targetUUID) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.admin.player-options-title")));
        ActiveGUI activeGUI = new ActiveGUI("PLAYER_OPTIONS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.admin.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack giveItems = createItem(Material.PAPER, MessageUtils.getMessage("gui.admin.give-items"));
        ItemMeta giveMeta = giveItems.getItemMeta();
        if (giveMeta != null) {
            giveMeta.setLore(List.of("<yellow>UUID: " + targetUUID));
            giveItems.setItemMeta(giveMeta);
        }
        inv.setItem(21, giveItems);

        ItemStack kickPlayer = createItem(Material.IRON_SWORD, MessageUtils.getMessage("gui.admin.kick-player"));
        ItemMeta kickMeta = kickPlayer.getItemMeta();
        if (kickMeta != null) {
            kickMeta.setLore(List.of("<yellow>UUID: " + targetUUID));
            kickPlayer.setItemMeta(kickMeta);
        }
        inv.setItem(23, kickPlayer);

        ItemStack banAccount = createItem(Material.BARRIER, MessageUtils.getMessage("gui.admin.ban-account"));
        ItemMeta banAccountMeta = banAccount.getItemMeta();
        if (banAccountMeta != null) {
            banAccountMeta.setLore(List.of("<yellow>UUID: " + targetUUID));
            banAccount.setItemMeta(banAccountMeta);
        }
        inv.setItem(25, banAccount);

        ItemStack banIP = createItem(Material.BARRIER, MessageUtils.getMessage("gui.admin.ban-ip"));
        ItemMeta banIPMeta = banIP.getItemMeta();
        if (banIPMeta != null) {
            banIPMeta.setLore(List.of("<yellow>UUID: " + targetUUID));
            banIP.setItemMeta(banIPMeta);
        }
        inv.setItem(27, banIP);

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openGiveItemsMenu(Player player, UUID targetUUID) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.admin.give-items-title")));
        ActiveGUI activeGUI = new ActiveGUI("GIVE_ITEMS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.admin.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack blueDream = drugManager.createDrugItem("blue_dream");
        ItemMeta blueDreamMeta = blueDream.getItemMeta();
        if (blueDreamMeta != null) {
            blueDreamMeta.setLore(List.of("<yellow>UUID: " + targetUUID));
            blueDream.setItemMeta(blueDreamMeta);
        }
        inv.setItem(21, blueDream);

        ItemStack blueDreamSeed = drugManager.createSeedItem("blue_dream");
        ItemMeta seedMeta = blueDreamSeed.getItemMeta();
        if (seedMeta != null) {
            seedMeta.setLore(List.of("<yellow>UUID: " + targetUUID));
            blueDreamSeed.setItemMeta(seedMeta);
        }
        inv.setItem(23, blueDreamSeed);

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openCartelManageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.admin.cartel-manage-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL_MANAGE", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.admin.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        int slot = 10;
        for (Cartel cartel : cartelManager.getCartels().values()) {
            if (slot >= 44) break;
            ItemStack item = createItem(Material.CHEST, "<yellow>" + cartel.getName());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setLore(List.of("<yellow>ID: " + cartel.getId()));
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
            if (slot % 9 == 0) slot += 2;
        }

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openCartelDetailsMenu(Player player, String cartelId) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.admin.cartel-details-title")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL_DETAILS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.admin.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        ItemStack disband = createItem(Material.BARRIER, MessageUtils.getMessage("gui.admin.disband-cartel"));
        ItemMeta disbandMeta = disband.getItemMeta();
        if (disbandMeta != null) {
            disbandMeta.setLore(List.of("<yellow>ID: " + cartelId));
            disband.setItemMeta(disbandMeta);
        }
        inv.setItem(23, disband);

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openBusinessManageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.admin.business-manage-title")));
        ActiveGUI activeGUI = new ActiveGUI("BUSINESS_MANAGE", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.admin.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        int slot = 10;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            List<String> businesses = businessManager.getPlayerBusinesses(onlinePlayer.getUniqueId());
            if (!businesses.isEmpty()) {
                if (slot >= 44) break;
                ItemStack item = createItem(Material.IRON_INGOT, "<yellow>" + onlinePlayer.getName() + "'s Businesses");
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setLore(businesses.stream().map(b -> "<yellow>" + b).toList());
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
                slot++;
                if (slot % 9 == 0) slot += 2;
            }
        }

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openWorldTPMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.admin.world-tp-title")));
        ActiveGUI activeGUI = new ActiveGUI("WORLD_TP", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.admin.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        int slot = 10;
        for (World world : Bukkit.getWorlds()) {
            if (slot >= 44) break;
            ItemStack item = createItem(Material.COMPASS, "<aqua>" + world.getName());
            inv.setItem(slot, item);
            slot++;
            if (slot % 9 == 0) slot += 2;
        }

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