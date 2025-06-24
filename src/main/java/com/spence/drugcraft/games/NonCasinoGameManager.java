package com.spence.drugcraft.games;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.*;

public class NonCasinoGameManager {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, GameState> activeGames = new HashMap<>();

    public NonCasinoGameManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.economyManager = new EconomyManager(null);
    }

    public void startChess(UUID playerUUID, String difficulty) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null) return;
        activeGames.put(playerUUID, new GameState("chess", difficulty));
        MessageUtils.sendMessage(player, "games.chess-start");
    }

    public void startChess(UUID playerUUID, UUID opponentUUID) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null) return;
        activeGames.put(playerUUID, new GameState("chess", opponentUUID));
        MessageUtils.sendMessage(player, "games.chess-start");
    }

    public void startCheckers(UUID playerUUID, String difficulty) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null) return;
        activeGames.put(playerUUID, new GameState("checkers", difficulty));
        MessageUtils.sendMessage(player, "games.checkers-start");
    }

    public void startCheckers(UUID playerUUID, UUID opponentUUID) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null) return;
        activeGames.put(playerUUID, new GameState("checkers", opponentUUID));
        MessageUtils.sendMessage(player, "games.checkers-start");
    }

    public void dropDisc(UUID playerUUID, int column) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null || !activeGames.containsKey(playerUUID)) return;
        GameState state = activeGames.get(playerUUID);
        if (!state.gameType.equals("connect4")) return;

        // Simplified Connect4 logic
        if (state.board == null) state.board = new int[6][7];
        for (int row = 5; row >= 0; row--) {
            if (state.board[row][column] == 0) {
                state.board[row][column] = 1; // Player disc
                MessageUtils.sendMessage(player, "games.connect4-disc", "column", String.valueOf(column + 1));
                if (checkConnect4Win(state.board, 1)) {
                    economyManager.depositPlayer(player, 100.0);
                    MessageUtils.sendMessage(player, "games.connect4-win", "reward", "100");
                    activeGames.remove(playerUUID);
                } else {
                    // AI move (random column)
                    Random random = new Random();
                    int aiColumn;
                    do {
                        aiColumn = random.nextInt(7);
                    } while (state.board[0][aiColumn] != 0);
                    for (int r = 5; r >= 0; r--) {
                        if (state.board[r][aiColumn] == 0) {
                            state.board[r][aiColumn] = 2; // AI disc
                            break;
                        }
                    }
                    if (checkConnect4Win(state.board, 2)) {
                        MessageUtils.sendMessage(player, "games.connect4-lose");
                        activeGames.remove(playerUUID);
                    }
                }
                break;
            }
        }
    }

    public void playRPS(UUID playerUUID, String choice) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null) return;
        String[] choices = {"rock", "paper", "scissors"};
        String aiChoice = choices[new Random().nextInt(3)];
        String result;
        if (choice.equals(aiChoice)) {
            result = "Draw";
            MessageUtils.sendMessage(player, "games.rps-result", "player_choice", choice, "ai_choice", aiChoice, "result", result);
        } else if ((choice.equals("rock") && aiChoice.equals("scissors")) ||
                (choice.equals("paper") && aiChoice.equals("rock")) ||
                (choice.equals("scissors") && aiChoice.equals("paper"))) {
            result = "Win";
            economyManager.depositPlayer(player, 50.0);
            MessageUtils.sendMessage(player, "games.rps-result", "player_choice", choice, "ai_choice", aiChoice, "result", result);
        } else {
            result = "Lose";
            MessageUtils.sendMessage(player, "games.rps-result", "player_choice", choice, "ai_choice", aiChoice, "result", result);
        }
        activeGames.remove(playerUUID);
    }

    private boolean checkConnect4Win(int[][] board, int player) {
        // Check horizontal
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] == player && board[r][c + 1] == player && board[r][c + 2] == player && board[r][c + 3] == player) {
                    return true;
                }
            }
        }
        // Check vertical
        for (int c = 0; c < 7; c++) {
            for (int r = 0; r < 3; r++) {
                if (board[r][c] == player && board[r + 1][c] == player && board[r + 2][c] == player && board[r + 3][c] == player) {
                    return true;
                }
            }
        }
        // Check diagonals
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] == player && board[r + 1][c + 1] == player && board[r + 2][c + 2] == player && board[r + 3][c + 3] == player) {
                    return true;
                }
            }
        }
        for (int r = 3; r < 6; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] == player && board[r - 1][c + 1] == player && board[r - 2][c + 2] == player && board[r - 3][c + 3] == player) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class GameState {
        String gameType;
        String difficulty;
        UUID opponentUUID;
        int[][] board; // For Connect4

        GameState(String gameType, String difficulty) {
            this.gameType = gameType;
            this.difficulty = difficulty;
            this.opponentUUID = null;
        }

        GameState(String gameType, UUID opponentUUID) {
            this.gameType = gameType;
            this.opponentUUID = opponentUUID;
            this.difficulty = null;
        }
    }
}