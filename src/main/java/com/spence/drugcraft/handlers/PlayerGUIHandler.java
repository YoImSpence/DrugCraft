package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.PlayerGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerGUIHandler {
    private final DrugCraft plugin;
    private final PlayerGUI playerGUI;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerGUIHandler(DrugCraft plugin, PlayerGUI playerGUI) {
        this.plugin = plugin;
        this.playerGUI = playerGUI;
    }

    public void openMainMenu(Player player) {
        playerGUI.openMainMenu(player);
    }

    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Component displayNameComp = meta.displayName();
        if (displayNameComp == null) return;

        String displayName = miniMessage.serialize(displayNameComp);
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        String guiType = activeGUI.getType();

        switch (guiType) {
            case "PLAYER":
                if (displayName.contains(MessageUtils.getMessage("gui.player.levels"))) {
                    playerGUI.openLevelsMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.player.settings"))) {
                    playerGUI.openSettingsMenu(player);
                }
                break;
            case "LEVELS":
                if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    playerGUI.openMainMenu(player);
                }
                break;
            case "SETTINGS":
                if (displayName.contains(MessageUtils.getMessage("gui.player.msg-toggle"))) {
                    // Placeholder: Toggle messages
                    MessageUtils.sendMessage(player, "player.setting-toggled", "setting", "Messages");
                } else if (displayName.contains(MessageUtils.getMessage("gui.player.tp-toggle"))) {
                    // Placeholder: Toggle teleports
                    MessageUtils.sendMessage(player, "player.setting-toggled", "setting", "Teleports");
                } else if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    playerGUI.openMainMenu(player);
                }
                break;
        }
    }
}