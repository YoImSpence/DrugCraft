package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.Cartel;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.AdminGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class AdminGUIHandler {
    private final DrugCraft plugin;
    private final AdminGUI adminGUI;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final CartelManager cartelManager;
    private final BusinessManager businessManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AdminGUIHandler(DrugCraft plugin, AdminGUI adminGUI, DataManager dataManager, DrugManager drugManager, CartelManager cartelManager, BusinessManager businessManager) {
        this.plugin = plugin;
        this.adminGUI = adminGUI;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
        this.cartelManager = cartelManager;
        this.businessManager = businessManager;
    }

    public void openMainMenu(Player player) {
        adminGUI.openMainMenu(player);
    }

    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == org.bukkit.Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Component displayNameComp = meta.displayName();
        if (displayNameComp == null) return;

        String displayName = miniMessage.serialize(displayNameComp);
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        String guiType = activeGUI.getType();

        switch (guiType) {
            case "ADMIN":
                if (displayName.contains(MessageUtils.getMessage("gui.admin.player-manage"))) {
                    adminGUI.openPlayerManageMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.admin.cartel-manage"))) {
                    adminGUI.openCartelManageMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.admin.business-manage"))) {
                    adminGUI.openBusinessManageMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.admin.world-tp"))) {
                    adminGUI.openWorldTPMenu(player);
                }
                break;
            case "PLAYER_MANAGE":
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    String uuidStr = lore.get(0).split("UUID: ")[1];
                    UUID targetUUID = UUID.fromString(uuidStr);
                    adminGUI.openPlayerOptionsMenu(player, targetUUID);
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    adminGUI.openMainMenu(player);
                }
                break;
            case "PLAYER_OPTIONS":
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    String uuidStr = lore.get(0).split("UUID: ")[1];
                    UUID targetUUID = UUID.fromString(uuidStr);
                    Player target = Bukkit.getPlayer(targetUUID);
                    if (target == null) {
                        MessageUtils.sendMessage(player, "general.invalid-input");
                        return;
                    }
                    boolean isAdmin = player.hasPermission("group.admin");
                    boolean isOwner = player.getName().equals("Deathball133");
                    boolean targetIsAdmin = target.hasPermission("group.admin");
                    boolean targetIsOwner = target.getName().equals("Deathball133");

                    if (displayName.contains(MessageUtils.getMessage("gui.admin.give-items"))) {
                        adminGUI.openGiveItemsMenu(player, targetUUID);
                    } else if (displayName.contains(MessageUtils.getMessage("gui.admin.kick-player"))) {
                        if (isOwner || (!targetIsAdmin && !targetIsOwner)) {
                            target.kickPlayer("Kicked by admin");
                            MessageUtils.sendMessage(player, "admin.kick-player", "player", target.getName(), "reason", "Admin decision");
                        } else {
                            MessageUtils.sendMessage(player, "general.no-permission");
                        }
                    } else if (displayName.contains(MessageUtils.getMessage("gui.admin.ban-account"))) {
                        if (isOwner || (!targetIsAdmin && !targetIsOwner)) {
                            Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), "Banned by admin", null, player.getName());
                            target.kickPlayer("Banned by admin");
                            MessageUtils.sendMessage(player, "admin.ban-account", "player", target.getName(), "time", "Permanent", "reason", "Admin decision");
                        } else {
                            MessageUtils.sendMessage(player, "general.no-permission");
                        }
                    } else if (displayName.contains(MessageUtils.getMessage("gui.admin.ban-ip"))) {
                        if (isOwner || (!targetIsAdmin && !targetIsOwner)) {
                            String ip = target.getAddress().getAddress().getHostAddress();
                            Bukkit.getBanList(BanList.Type.IP).addBan(ip, "IP Banned by admin", null, player.getName());
                            Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), "IP Banned by admin", null, player.getName());
                            target.kickPlayer("IP Banned by admin");
                            MessageUtils.sendMessage(player, "admin.ban-ip", "player", target.getName(), "time", "Permanent", "reason", "Admin decision");
                        } else {
                            MessageUtils.sendMessage(player, "general.no-permission");
                        }
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    adminGUI.openPlayerManageMenu(player);
                }
                break;
            case "GIVE_ITEMS":
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    String uuidStr = lore.get(0).split("UUID: ")[1];
                    UUID targetUUID = UUID.fromString(uuidStr);
                    Player target = Bukkit.getPlayer(targetUUID);
                    if (target != null) {
                        ItemStack clonedItem = item.clone();
                        ItemMeta clonedMeta = clonedItem.getItemMeta();
                        if (clonedMeta != null) {
                            clonedMeta.setLore(null); // Remove UUID lore
                            clonedItem.setItemMeta(clonedMeta);
                        }
                        target.getInventory().addItem(clonedItem);
                        MessageUtils.sendMessage(player, "general.invalid-input"); // Placeholder: Add specific message
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    adminGUI.openPlayerOptionsMenu(player, UUID.fromString(meta.getLore().get(0).split(": ")[1]));
                }
                break;
            case "CARTEL_MANAGE":
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    String cartelId = lore.get(0).split("ID: ")[1];
                    adminGUI.openCartelDetailsMenu(player, cartelId);
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    adminGUI.openMainMenu(player);
                }
                break;
            case "CARTEL_DETAILS":
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    String cartelId = lore.get(0).split("ID: ")[1];
                    if (displayName.contains(MessageUtils.getMessage("gui.admin.disband-cartel"))) {
                        cartelManager.disbandCartel(cartelId);
                        MessageUtils.sendMessage(player, "admin.cartel-disbanded", "name", cartelManager.getCartelById(cartelId).getName());
                        adminGUI.openCartelManageMenu(player);
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    adminGUI.openCartelManageMenu(player);
                }
                break;
            case "BUSINESS_MANAGE":
                if (meta.hasLore()) {
                    List<String> businesses = meta.getLore().stream()
                            .map(lore -> miniMessage.serialize(miniMessage.deserialize(lore)).substring(8)) // Remove <yellow>
                            .toList();
                    // Placeholder: Open business details menu
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    adminGUI.openMainMenu(player);
                }
                break;
            case "WORLD_TP":
                if (!displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    String worldName = displayName.substring(7); // Remove <aqua>
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        player.teleport(world.getSpawnLocation());
                        MessageUtils.sendMessage(player, "player.world-teleported", "world", worldName);
                    } else {
                        MessageUtils.sendMessage(player, "player.world-not-found", "world", worldName);
                    }
                }
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    adminGUI.openMainMenu(player);
                }
                break;
        }
    }
}