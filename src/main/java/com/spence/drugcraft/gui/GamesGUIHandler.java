package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.games.GameManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GamesGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final GameManager gameManager;

    public GamesGUIHandler(DrugCraft plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MessageUtils.color("gui.games.title-main"));
        ActiveGUI activeGUI = new ActiveGUI("GAMES", null);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack chessCheckers = createItem(Material.BLACK_BANNER, "gui.games.item-chess-checkers");
        ItemStack connect4 = createItem(Material.RED_BANNER, "gui.games.item-connect4");
        ItemStack rps = createItem(Material.GREEN_BANNER, "gui.games.item-rps");

        inv.setItem(11, chessCheckers);
        inv.setItem(13, connect4);
        inv.setItem(15, rps);

        player.openInventory(inv);
    }

    public void openGameSubMenu(Player player, String game) {
        Inventory inv = Bukkit.createInventory(null, 27, MessageUtils.color("gui.games.title-" + game.toLowerCase()));
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        activeGUI.setMenuSubType(game);

        ItemStack vsPlayer = createItem(Material.PLAYER_HEAD, "gui.games.item-player");
        ItemStack vsBot = createItem(Material.IRON_HELMET, "gui.games.item-bot");
        ItemStack back = createItem(Material.BARRIER, "gui.games.item-back");

        inv.setItem(11, vsPlayer);
        inv.setItem(15, vsBot);
        inv.setItem(22, back);

        player.openInventory(inv);
    }

    public void openBotDifficultyMenu(Player player, String game) {
        Inventory inv = Bukkit.createInventory(null, 27, MessageUtils.color("gui.games.title-difficulty"));
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        activeGUI.setMenuSubType(game + "_DIFFICULTY");

        ItemStack easy = createItem(Material.LIME_WOOL, "gui.games.item-easy");
        ItemStack medium = createItem(Material.ORANGE_WOOL, "gui.games.item-medium");
        ItemStack hard = createItem(Material.RED_WOOL, "gui.games.item-hard");
        ItemStack back = createItem(Material.BARRIER, "gui.games.item-back");

        inv.setItem(11, easy);
        inv.setItem(13, medium);
        inv.setItem(15, hard);
        inv.setItem(22, back);

        player.openInventory(inv);
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("GAMES")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String displayName = MessageUtils.stripColor(meta.displayName());

        String subType = activeGUI.getMenuSubType();

        if (subType == null) {
            if (displayName.equals("Chess/Checkers")) {
                openGameSubMenu(player, "chess-checkers");
            } else if (displayName.equals("Connect 4")) {
                openGameSubMenu(player, "connect4");
            } else if (displayName.equals("Rock Paper Scissors")) {
                openGameSubMenu(player, "rps");
            }
        } else if (subType.equals("chess-checkers") || subType.equals("connect4") || subType.equals("rps")) {
            if (displayName.equals("Vs Player")) {
                MessageUtils.sendMessage(player, "gui.games.select-player");
                activeGUI.setMenuSubType(subType + "_PLAYER");
                player.closeInventory();
            } else if (displayName.equals("Vs Bot")) {
                openBotDifficultyMenu(player, subType);
            } else if (displayName.equals("Back")) {
                openMainMenu(player);
                activeGUI.setMenuSubType(null);
            }
        } else if (subType.endsWith("_DIFFICULTY")) {
            String game = subType.split("_")[0];
            String difficulty = displayName.toLowerCase();
            if (Arrays.asList("easy", "medium", "hard").contains(difficulty)) {
                if (gameManager.isPlayerInGame(player)) {
                    MessageUtils.sendMessage(player, "gui.games.already-in-game");
                    return;
                }
                gameManager.startGame(player, null, difficulty, game);
                MessageUtils.sendMessage(player, "gui.games.game-started", "game", game, "opponent", "Bot (" + difficulty + ")");
            } else if (displayName.equals("Back")) {
                openGameSubMenu(player, game);
            }
        }
    }

    private ItemStack createItem(Material material, String messageKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MessageUtils.color(messageKey));
            item.setItemMeta(meta);
        }
        return item;
    }
}