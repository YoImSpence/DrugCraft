package com.spence.drugcraft.games;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Game {
    public Player player1, player2;
    public String difficulty, type;
    public int[][] board;
    public String lastPlayerMove, botMove;
    public boolean lastMoveByPlayer;
    public double betMoney;
    public List<ItemStack> betItems;

    public Game(Player player1, Player player2, String difficulty, String type, double betMoney, List<ItemStack> betItems) {
        this.player1 = player1;
        this.player2 = player2;
        this.difficulty = difficulty;
        this.type = type;
        this.board = type.equals("rps") ? null : type.equals("connect4") ? new int[6][7] : new int[8][8];
        this.betMoney = betMoney;
        this.betItems = betItems;
    }
}