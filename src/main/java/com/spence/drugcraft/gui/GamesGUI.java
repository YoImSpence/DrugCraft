package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GamesGUI {
    private final DrugCraft plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GamesGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.games.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("GAMES", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.WHITE_BANNER, MessageUtils.getMessage("gui.games.chess-title")));
        inv.setItem(23, createItem(Material.BLACK_BANNER, MessageUtils.getMessage("gui.games.checkers-title")));
        inv.setItem(25, createItem(Material.YELLOW_BANNER, MessageUtils.getMessage("gui.games.connect4-title")));
        inv.setItem(27, createItem(Material.GREEN_BANNER, MessageUtils.getMessage("gui.games.rps-title")));
        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openChessMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.games.chess-title")));
        ActiveGUI activeGUI = new ActiveGUI("CHESS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.PAPER, MessageUtils.getMessage("gui.games.chess-ai-easy")));
        inv.setItem(23, createItem(Material.PAPER, MessageUtils.getMessage("gui.games.chess-ai-medium")));
        inv.setItem(25, createItem(Material.PAPER, MessageUtils.getMessage("gui.games.chess-ai-hard")));
        inv.setItem(27, createItem(Material.PLAYER_HEAD, MessageUtils.getMessage("gui.games.chess-invite")));
        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openCheckersMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.games.checkers-title")));
        ActiveGUI activeGUI = new ActiveGUI("CHECKERS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.PAPER, MessageUtils.getMessage("gui.games.checkers-ai-easy")));
        inv.setItem(23, createItem(Material.PAPER, MessageUtils.getMessage("gui.games.checkers-ai-medium")));
        inv.setItem(25, createItem(Material.PAPER, MessageUtils.getMessage("gui.games.checkers-ai-hard")));
        inv.setItem(27, createItem(Material.PLAYER_HEAD, MessageUtils.getMessage("gui.games.checkers-invite")));
        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openConnect4Menu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.games.connect4-title")));
        ActiveGUI activeGUI = new ActiveGUI("CONNECT4", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        for (int i = 19; i <= 25; i++) {
            inv.setItem(i, createItem(Material.YELLOW_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.connect4-drop")));
        }
        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openRPSMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.games.rps-title")));
        ActiveGUI activeGUI = new ActiveGUI("RPS", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.games.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.STONE, MessageUtils.getMessage("gui.games.rps-rock")));
        inv.setItem(23, createItem(Material.PAPER, MessageUtils.getMessage("gui.games.rps-paper")));
        inv.setItem(25, createItem(Material.SHEARS, MessageUtils.getMessage("gui.games.rps-scissors")));
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