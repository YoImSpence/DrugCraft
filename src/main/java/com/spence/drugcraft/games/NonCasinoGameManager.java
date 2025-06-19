package com.spence.drugcraft.games;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class NonCasinoGameManager {
    private final DrugCraft plugin;
    private final Map<UUID, ChessGame> chessGames = new HashMap<>();
    private final Map<UUID, CheckersGame> checkersGames = new HashMap<>();
    private final Map<UUID, Connect4Game> connect4Games = new HashMap<>();

    public NonCasinoGameManager(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void startChessGame(UUID playerUUID) {
        chessGames.put(playerUUID, new ChessGame());
        saveGameState(playerUUID, "chess");
    }

    public void updateChessMove(UUID playerUUID, String move) {
        ChessGame game = chessGames.get(playerUUID);
        if (game != null) {
            // Placeholder: Update board with move (e.g., "e2-e4")
            saveGameState(playerUUID, "chess");
        }
    }

    public void startCheckersGame(UUID playerUUID) {
        checkersGames.put(playerUUID, new CheckersGame());
        saveGameState(playerUUID, "checkers");
    }

    public void startConnect4Game(UUID playerUUID) {
        connect4Games.put(playerUUID, new Connect4Game());
        saveGameState(playerUUID, "connect4");
    }

    public void dropConnect4Disc(UUID playerUUID, int column) {
        Connect4Game game = connect4Games.get(playerUUID);
        if (game != null) {
            game.dropDisc(column);
            if (game.checkWin()) {
                connect4Games.remove(playerUUID);
                // Notify win
            } else {
                game.aiMove();
                saveGameState(playerUUID, "connect4");
            }
        }
    }

    private void saveGameState(UUID playerUUID, String gameType) {
        FileConfiguration data = plugin.getConfig("data.yml");
        // Placeholder: Save game state
        plugin.saveConfig();
    }

    private static class ChessGame {
        // Placeholder: Chess board state
        public ChessGame() {
        }
    }

    private static class CheckersGame {
        // Placeholder: Checkers board state
        public CheckersGame() {
        }
    }

    private static class Connect4Game {
        private final int[][] board = new int[6][7]; // 6 rows, 7 columns
        private int currentPlayer = 1; // 1: Player, 2: AI
        private final Random random = new Random();

        public Connect4Game() {
        }

        public void dropDisc(int column) {
            for (int row = 5; row >= 0; row--) {
                if (board[row][column] == 0) {
                    board[row][column] = currentPlayer;
                    currentPlayer = currentPlayer == 1 ? 2 : 1;
                    break;
                }
            }
        }

        public void aiMove() {
            int column = random.nextInt(7);
            while (!isValidMove(column)) {
                column = random.nextInt(7);
            }
            dropDisc(column);
        }

        private boolean isValidMove(int column) {
            return board[0][column] == 0;
        }

        public boolean checkWin() {
            // Placeholder: Check for 4 in a row (horizontal, vertical, diagonal)
            return false;
        }
    }
}