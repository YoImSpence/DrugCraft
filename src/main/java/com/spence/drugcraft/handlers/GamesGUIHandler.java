package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.games.NonCasinoGameManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.Random;

public class GamesGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final NonCasinoGameManager gameManager;
    private final Random random = new Random();

    public GamesGUIHandler(DrugCraft plugin, NonCasinoGameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("GAMES")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        String subType = activeGUI.getMenuSubType();
        if (subType == null) {
            if (displayName.equals(MessageUtils.getMessage("gui.games.item-chess"))) {
                startChess(player);
                activeGUI.setMenuSubType("chess");
            } else if (displayName.equals(MessageUtils.getMessage("gui.games.item-checkers"))) {
                startCheckers(player);
                activeGUI.setMenuSubType("checkers");
            } else if (displayName.equals(MessageUtils.getMessage("gui.games.item-connect4"))) {
                startConnect4(player);
                activeGUI.setMenuSubType("connect4");
            } else if (displayName.equals(MessageUtils.getMessage("gui.games.item-rps"))) {
                startRockPaperScissors(player);
            }
        } else if (subType.equals("chess")) {
            if (displayName.startsWith("Move")) {
                // Placeholder: Parse move (e.g., "e2-e4"), update board
                gameManager.updateChessMove(player.getUniqueId(), displayName);
                openChessMenu(player);
            }
        } else if (subType.equals("connect4")) {
            if (displayName.startsWith("Column")) {
                int column = Integer.parseInt(displayName.split(" ")[1]) - 1;
                gameManager.dropConnect4Disc(player.getUniqueId(), column);
                openConnect4Menu(player);
            }
        }
    }

    private void startChess(Player player) {
        gameManager.startChessGame(player.getUniqueId());
        openChessMenu(player);
    }

    private void openChessMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("<gradient:#FF5555:#5555FF><bold>Chess</bold></gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("GAMES", inv, "chess");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        // Placeholder: Render chess board with clickable moves
        ItemStack move = createItem(Material.PAPER, "#FFFF55Move e2-e4"); // Example move
        inv.setItem(13, move);

        player.openInventory(inv);
    }

    private void startCheckers(Player player) {
        gameManager.startCheckersGame(player.getUniqueId());
        MessageUtils.sendMessage(player, "games.checkers-start");
        // Placeholder: Open checkers GUI
    }

    private void startConnect4(Player player) {
        gameManager.startConnect4Game(player.getUniqueId());
        openConnect4Menu(player);
    }

    private void openConnect4Menu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("<gradient:#FF5555:#5555FF><bold>Connect 4</bold></gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("GAMES", inv, "connect4");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        for (int i = 1; i <= 7; i++) {
            ItemStack column = createItem(Material.YELLOW_WOOL, "#FFFF55Column " + i);
            inv.setItem(10 + i, column);
        }

        player.openInventory(inv);
    }

    private void startRockPaperScissors(Player player) {
        String[] choices = {"Rock", "Paper", "Scissors"};
        String playerChoice = choices[random.nextInt(3)];
        String aiChoice = choices[random.nextInt(3)];
        String result = determineRPSResult(playerChoice, aiChoice);
        MessageUtils.sendMessage(player, "games.rps-result", "player_choice", playerChoice, "ai_choice", aiChoice, "result", result);
    }

    private String determineRPSResult(String playerChoice, String aiChoice) {
        if (playerChoice.equals(aiChoice)) return "Tie";
        if ((playerChoice.equals("Rock") && aiChoice.equals("Scissors")) ||
                (playerChoice.equals("Paper") && aiChoice.equals("Rock")) ||
                (playerChoice.equals("Scissors") && aiChoice.equals("Paper"))) {
            return "Win";
        }
        return "Lose";
    }

    private ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage(displayName));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("<gradient:#FF5555:#5555FF><bold>Games</bold></gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("GAMES", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack chess = createItem(Material.BLACK_BANNER, "#FF5555gui.games.item-chess");
        ItemStack checkers = createItem(Material.RED_BANNER, "#FF5555gui.games.item-checkers");
        ItemStack connect4 = createItem(Material.YELLOW_BANNER, "#FFFF55gui.games.item-connect4");
        ItemStack rps = createItem(Material.PAPER, "#55FF55gui.games.item-rps");

        inv.setItem(10, chess);
        inv.setItem(12, checkers);
        inv.setItem(14, connect4);
        inv.setItem(16, rps);

        player.openInventory(inv);
    }
}