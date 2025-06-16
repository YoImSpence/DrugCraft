package com.spence.drugcraft.games;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GameManager {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, Game> activeGames;
    private final Random random;
    private final FileConfiguration config;

    public GameManager(DrugCraft plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.activeGames = new HashMap<>();
        this.random = new Random();
        this.config = plugin.getConfig("games.yml");
    }

    public boolean isPlayerInGame(Player player) {
        return activeGames.containsKey(player.getUniqueId());
    }

    public void startGame(Player player1, Player player2, String difficulty, String gameType) {
        Game game = new Game(player1, player2, difficulty, gameType);
        activeGames.put(player1.getUniqueId(), game);
        if (player2 != null) {
            activeGames.put(player2.getUniqueId(), game);
        }
        openGameGUI(player1, game);
        if (player2 == null) {
            makeBotMove(game);
        }
    }

    public void handlePlayerInput(Player player, String input) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("GAMES")) return;

        String subType = activeGUI.getMenuSubType();
        if (subType != null && subType.endsWith("_PLAYER")) {
            String game = subType.split("_")[0];
            Player opponent = Bukkit.getPlayer(input);
            if (opponent == null || !opponent.isOnline()) {
                MessageUtils.sendMessage(player, "gui.games.invalid-opponent");
                return;
            }
            if (isPlayerInGame(opponent)) {
                MessageUtils.sendMessage(player, "gui.games.opponent-in-game", "opponent", opponent.getName());
                return;
            }
            if (isPlayerInGame(player)) {
                MessageUtils.sendMessage(player, "gui.games.already-in-game");
                return;
            }
            startGame(player, opponent, null, game);
            MessageUtils.sendMessage(player, "gui.games.game-started", "game", game, "opponent", opponent.getName());
            MessageUtils.sendMessage(opponent, "gui.games.game-started", "game", game, "opponent", player.getName());
        }
    }

    private void openGameGUI(Player player, Game game) {
        String title = MessageUtils.color("gui.games.title-" + game.type.toLowerCase());
        Inventory inv;
        if (game.type.equals("chess-checkers")) {
            inv = Bukkit.createInventory(null, 54, title);
            initializeChessCheckersBoard(inv, game);
        } else if (game.type.equals("connect4")) {
            inv = Bukkit.createInventory(null, 54, title);
            initializeConnect4Board(inv, game);
        } else {
            inv = Bukkit.createInventory(null, 9, title);
            initializeRPSBoard(inv, game);
        }
        player.openInventory(inv);
        if (game.player2 != null) {
            game.player2.openInventory(inv);
        }
    }

    private void initializeChessCheckersBoard(Inventory inv, Game game) {
        for (int i = 0; i < 64; i++) {
            int row = i / 8;
            int col = i % 8;
            Material mat = (row + col) % 2 == 0 ? Material.WHITE_WOOL : Material.BLACK_WOOL;
            inv.setItem(i % 8 + (i / 8) * 9, new ItemStack(mat));
        }
        for (int i = 0; i < 12; i++) {
            int slot = (i % 4) * 2 + (i / 4) * 9 + ((i / 4) % 2 == 0 ? 1 : 0);
            ItemStack piece = new ItemStack(Material.RED_CONCRETE);
            ItemMeta meta = piece.getItemMeta();
            meta.displayName(MessageUtils.color("<#FF0000>Player 1 Piece"));
            piece.setItemMeta(meta);
            inv.setItem(slot, piece);
        }
        for (int i = 0; i < 12; i++) {
            int slot = (i % 4) * 2 + (7 - i / 4) * 9 + ((i / 4) % 2 == 0 ? 1 : 0);
            ItemStack piece = new ItemStack(Material.BLUE_CONCRETE);
            ItemMeta meta = piece.getItemMeta();
            meta.displayName(MessageUtils.color("<#0000FF>Player 2 Piece"));
            piece.setItemMeta(meta);
            inv.setItem(slot, piece);
        }
    }

    private void initializeConnect4Board(Inventory inv, Game game) {
        for (int i = 0; i < 42; i++) {
            inv.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        for (int col = 0; col < 7; col++) {
            ItemStack drop = new ItemStack(Material.YELLOW_CONCRETE);
            ItemMeta meta = drop.getItemMeta();
            meta.displayName(MessageUtils.color("<#FFFF00>Drop in Column " + (col + 1)));
            drop.setItemMeta(meta);
            inv.setItem(42 + col, drop);
        }
    }

    private void initializeRPSBoard(Inventory inv, Game game) {
        ItemStack rock = createItem(Material.STONE, "<#808080>Rock");
        ItemStack paper = createItem(Material.PAPER, "<#FFFFFF>Paper");
        ItemStack scissors = createItem(Material.SHEARS, "<#C0C0C0>Scissors");
        inv.setItem(2, rock);
        inv.setItem(4, paper);
        inv.setItem(6, scissors);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.color(name));
        item.setItemMeta(meta);
        return item;
    }

    private void makeBotMove(Game game) {
        if (game.player2 != null) return;
        double moveChance = config.getDouble("games." + game.type + ".bot-difficulties." + game.difficulty + ".move-chance");
        if (random.nextDouble() > moveChance) return;

        if (game.type.equals("rps")) {
            String[] choices = {"rock", "paper", "scissors"};
            String botChoice = choices[random.nextInt(3)];
            if (game.difficulty.equals("hard")) {
                botChoice = choices[(Arrays.asList(choices).indexOf(game.lastPlayerMove) + 1) % 3];
            }
            game.botMove = botChoice;
            checkRPSWin(game);
        } else if (game.type.equals("connect4")) {
            int col = random.nextInt(7);
            if (game.difficulty.equals("hard")) {
                col = findBestConnect4Move(game);
            }
            dropConnect4Piece(game, col, false);
            checkConnect4Win(game);
            openGameGUI(game.player1, game);
        } else if (game.type.equals("chess-checkers")) {
            int from = random.nextInt(64);
            int to = random.nextInt(64);
            if (game.difficulty.equals("hard")) {
                int[] move = findBestCheckersMove(game);
                from = move[0];
                to = move[1];
            }
            makeCheckersMove(game, from, to, false);
            checkCheckersWin(game);
            openGameGUI(game.player1, game);
        }
    }

    private void checkRPSWin(Game game) {
        String playerMove = game.lastPlayerMove;
        String botMove = game.botMove;
        Player player = game.player1;
        if (playerMove.equals(botMove)) {
            MessageUtils.sendMessage(player, "gui.games.game-tie", "game", "Rock Paper Scissors");
        } else if ((playerMove.equals("rock") && botMove.equals("scissors")) ||
                (playerMove.equals("paper") && botMove.equals("rock")) ||
                (playerMove.equals("scissors") && botMove.equals("paper"))) {
            awardReward(player, "rps");
            MessageUtils.sendMessage(player, "gui.games.game-won", "game", "Rock Paper Scissors", "reward", String.valueOf(config.getDouble("games.rps.reward-money")));
            activeGames.remove(player.getUniqueId());
        } else {
            MessageUtils.sendMessage(player, "gui.games.game-lost", "game", "Rock Paper Scissors");
            activeGames.remove(player.getUniqueId());
        }
        player.closeInventory();
    }

    private void checkConnect4Win(Game game) {
        Player player = game.player1;
        if (checkFourInRow(game.board)) {
            if (game.lastMoveByPlayer) {
                awardReward(player, "connect4");
                MessageUtils.sendMessage(player, "gui.games.game-won", "game", "Connect 4", "reward", String.valueOf(config.getDouble("games.connect4.reward-money")));
            } else {
                MessageUtils.sendMessage(player, "gui.games.game-lost", "game", "Connect 4");
            }
            activeGames.remove(player.getUniqueId());
            player.closeInventory();
        }
    }

    private void checkCheckersWin(Game game) {
        Player player = game.player1;
        if (countPieces(game.board, false) == 0) {
            awardReward(player, "chess-checkers");
            MessageUtils.sendMessage(player, "gui.games.game-won", "game", "Checkers", "reward", String.valueOf(config.getDouble("games.chess-checkers.reward-money")));
            activeGames.remove(player.getUniqueId());
            player.closeInventory();
        } else if (countPieces(game.board, true) == 0) {
            MessageUtils.sendMessage(player, "gui.games.game-lost", "game", "Checkers");
            activeGames.remove(player.getUniqueId());
            player.closeInventory();
        }
    }

    private boolean checkFourInRow(int[][] board) {
        return false; // Placeholder
    }

    private int countPieces(int[][] board, boolean player) {
        int count = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if ((player && cell == 1) || (!player && cell == 2)) count++;
            }
        }
        return count;
    }

    private int findBestConnect4Move(Game game) {
        return random.nextInt(7); // Placeholder
    }

    private int[] findBestCheckersMove(Game game) {
        return new int[]{random.nextInt(64), random.nextInt(64)}; // Placeholder
    }

    private void dropConnect4Piece(Game game, int col, boolean byPlayer) {
        game.lastMoveByPlayer = byPlayer;
    }

    private void makeCheckersMove(Game game, int from, int to, boolean byPlayer) {
        game.lastMoveByPlayer = byPlayer;
    }

    private void awardReward(Player player, String gameType) {
        double money = config.getDouble("games." + gameType + ".reward-money");
        economyManager.depositPlayer(player, money);
        List<?> items = config.getList("games." + gameType + ".reward-items");
        if (items != null) {
            for (Object obj : items) {
                Map<String, Object> item = (Map<String, Object>) obj;
                ItemStack stack = new ItemStack(Material.valueOf((String) item.get("material")), (Integer) item.get("amount"));
                player.getInventory().addItem(stack);
            }
        }
    }

    private class Game {
        Player player1, player2;
        String difficulty, type;
        int[][] board;
        String lastPlayerMove, botMove;
        boolean lastMoveByPlayer;

        Game(Player player1, Player player2, String difficulty, String type) {
            this.player1 = player1;
            this.player2 = player2;
            this.difficulty = difficulty;
            this.type = type;
            this.board = type.equals("rps") ? null : type.equals("connect4") ? new int[6][7] : new int[8][8];
        }
    }
}