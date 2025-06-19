package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.businesses.Business;
import com.spence.drugcraft.businesses.BusinessManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.gui.PlayerGUI;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.handlers.GUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final PlayerGUI playerGUI;
    private final DataManager dataManager;
    private final BusinessManager businessManager;

    public PlayerGUIHandler(DrugCraft plugin, PlayerGUI playerGUI) {
        this.plugin = plugin;
        this.playerGUI = playerGUI;
        this.dataManager = plugin.getDataManager();
        this.businessManager = plugin.getBusinessManager();
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("PLAYER")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        String subType = activeGUI.getMenuSubType();

        if (subType == null) {
            if (displayName.equals(MessageUtils.getMessage("gui.player.levels"))) {
                playerGUI.openLevelsMenu(player);
                activeGUI.setMenuSubType("levels");
            } else if (displayName.equals(MessageUtils.getMessage("gui.player.world-tp"))) {
                playerGUI.openWorldTpMenu(player);
                activeGUI.setMenuSubType("world-tp");
            } else if (displayName.equals(MessageUtils.getMessage("gui.player.games"))) {
                plugin.getGamesGUIHandler().openMainMenu(player);
            } else if (displayName.equals(MessageUtils.getMessage("gui.player.settings"))) {
                playerGUI.openSettingsMenu(player);
                activeGUI.setMenuSubType("settings");
            } else if (displayName.equals(MessageUtils.getMessage("gui.player.business"))) {
                playerGUI.openBusinessMenu(player);
                activeGUI.setMenuSubType("business");
            }
        } else if (subType.equals("levels")) {
            if (displayName.equals(MessageUtils.getMessage("gui.player.back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            }
        } else if (subType.equals("world-tp")) {
            if (displayName.equals(MessageUtils.getMessage("gui.player.world-greenfield"))) {
                teleportToWorld(player, "Greenfield", "worldtp.Greenfield");
            } else if (displayName.equals(MessageUtils.getMessage("gui.player.world-nether"))) {
                teleportToWorld(player, "Nether", "worldtp.Nether");
            } else if (displayName.equals(MessageUtils.getMessage("gui.player.back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            }
        } else if (subType.equals("settings")) {
            if (displayName.equals(MessageUtils.getMessage("gui.player.msg-toggle"))) {
                toggleSetting(player, "msgtoggle");
            } else if (displayName.equals(MessageUtils.getMessage("gui.player.tp-toggle"))) {
                toggleSetting(player, "tptoggle");
            } else if (displayName.equals(MessageUtils.getMessage("gui.player.back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            }
        } else if (subType.equals("business")) {
            if (displayName.equals(MessageUtils.getMessage("gui.player.back"))) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            }
        }
    }

    private void teleportToWorld(Player player, String worldName, String permission) {
        if (!player.hasPermission(permission)) {
            MessageUtils.sendMessage(player, "player.no-world-permission", "world", worldName);
            return;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            MessageUtils.sendMessage(player, "player.world-not-found", "world", worldName);
            return;
        }
        player.teleport(world.getSpawnLocation());
        MessageUtils.sendMessage(player, "player.world-teleported", "world", worldName);
    }

    private void toggleSetting(Player player, String setting) {
        // Placeholder: Execute EssentialsX command (e.g., /msgtoggle)
        Bukkit.dispatchCommand(player, setting);
        MessageUtils.sendMessage(player, "player.setting-toggled", "setting", setting);
    }

    public void openMainMenu(Player player) {
        playerGUI.openMainMenu(player);
    }
}