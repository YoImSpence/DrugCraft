package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.GamesGUI;
import com.spence.drugcraft.games.NonCasinoGameManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GamesGUIHandler {
    private final DrugCraft plugin;
    private final GamesGUI gamesGUI;
    private final NonCasinoGameManager gameManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GamesGUIHandler(DrugCraft plugin, GamesGUI gamesGUI, NonCasinoGameManager gameManager) {
        this.plugin = plugin;
        this.gamesGUI = gamesGUI;
        this.gameManager = gameManager;
    }

    public void openMainMenu(Player player) {
        gamesGUI.openMainMenu(player);
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
            case "GAMES":
                if (displayName.contains(MessageUtils.getMessage("gui.games.chess-title"))) {
                    gamesGUI.openChessMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.checkers-title"))) {
                    gamesGUI.openCheckersMenu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.connect4-title"))) {
                    gamesGUI.openConnect4Menu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.rps-title"))) {
                    gamesGUI.openRPSMenu(player);
                }
                break;
            case "CHESS":
                if (displayName.contains(MessageUtils.getMessage("gui.games.chess-ai-easy"))) {
                    gameManager.startChess(player.getUniqueId(), "easy");
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.chess-ai-medium"))) {
                    gameManager.startChess(player.getUniqueId(), "medium");
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.chess-ai-hard"))) {
                    gameManager.startChess(player.getUniqueId(), "hard");
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.chess-invite"))) {
                    activeGUI.setAwaitingChatInput(true);
                    activeGUI.setChatAction("chess-invite");
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "games.invite-sent", "target", "Enter player name");
                } else if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    gamesGUI.openMainMenu(player);
                }
                break;
            case "CHECKERS":
                if (displayName.contains(MessageUtils.getMessage("gui.games.checkers-ai-easy"))) {
                    gameManager.startCheckers(player.getUniqueId(), "easy");
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.checkers-ai-medium"))) {
                    gameManager.startCheckers(player.getUniqueId(), "medium");
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.checkers-ai-hard"))) {
                    gameManager.startCheckers(player.getUniqueId(), "hard");
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.checkers-invite"))) {
                    activeGUI.setAwaitingChatInput(true);
                    activeGUI.setChatAction("checkers-invite");
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "games.invite-sent", "target", "Enter player name");
                } else if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    gamesGUI.openMainMenu(player);
                }
                break;
            case "CONNECT4":
                if (displayName.contains(MessageUtils.getMessage("gui.games.connect4-drop"))) {
                    int column = slot - 19; // Slots 19-25 map to columns 0-6
                    gameManager.dropDisc(player.getUniqueId(), column);
                    gamesGUI.openConnect4Menu(player);
                } else if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    gamesGUI.openMainMenu(player);
                }
                break;
            case "RPS":
                if (displayName.contains(MessageUtils.getMessage("gui.games.rps-rock"))) {
                    gameManager.playRPS(player.getUniqueId(), "rock");
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.rps-paper"))) {
                    gameManager.playRPS(player.getUniqueId(), "paper");
                } else if (displayName.contains(MessageUtils.getMessage("gui.games.rps-scissors"))) {
                    gameManager.playRPS(player.getUniqueId(), "scissors");
                } else if (displayName.contains(MessageUtils.getMessage("gui.back"))) {
                    gamesGUI.openMainMenu(player);
                }
                break;
        }
    }

    public void handleInviteInput(Player player, String gameType, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            MessageUtils.sendMessage(player, "general.invalid-input");
            gamesGUI.openMainMenu(player);
            return;
        }
        if (gameType.equals("chess")) {
            gameManager.startChess(player.getUniqueId(), target.getUniqueId());
            MessageUtils.sendMessage(target, "games.invite-received", "sender", player.getName(), "game", "Chess");
        } else if (gameType.equals("checkers")) {
            gameManager.startCheckers(player.getUniqueId(), target.getUniqueId());
            MessageUtils.sendMessage(target, "games.invite-received", "sender", player.getName(), "game", "Checkers");
        }
        MessageUtils.sendMessage(player, "games.invite-sent", "target", targetName);
        gamesGUI.openMainMenu(player);
    }
}