package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.CasinoGUI;
import com.spence.drugcraft.casino.CasinoGame;
import com.spence.drugcraft.casino.CasinoManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

public class CasinoGUIHandler {
    private final DrugCraft plugin;
    private final CasinoGUI casinoGUI;
    private final CasinoManager casinoManager;
    private final Economy economy;
    private final Logger logger = Logger.getLogger(CasinoGUIHandler.class.getName());

    public CasinoGUIHandler(DrugCraft plugin, CasinoGUI casinoGUI, CasinoManager casinoManager) {
        this.plugin = plugin;
        this.casinoGUI = casinoGUI;
        this.casinoManager = casinoManager;
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        this.economy = economyProvider != null ? economyProvider.getProvider() : null;
        if (this.economy == null) {
            logger.warning("Economy service not found. Casino transactions will be disabled.");
        }
    }

    public void openMainMenu(Player player) {
        casinoGUI.openMainMenu(player);
    }

    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null || item.getType() == Material.AIR) return;

        String displayName = item.getItemMeta().getDisplayName();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        String guiType = activeGUI.getType();

        switch (guiType) {
            case "CASINO":
                if (displayName.contains(MessageUtils.getMessage("gui.casino.blackjack-title"))) {
                    casinoManager.startGame(player.getUniqueId(), "BLACKJACK", 100.0);
                    casinoGUI.openGameMenu(player, "BLACKJACK", casinoManager.getActiveGame(player.getUniqueId()));
                } else if (displayName.contains(MessageUtils.getMessage("gui.casino.slots-title"))) {
                    casinoManager.startGame(player.getUniqueId(), "SLOTS", 100.0);
                    casinoGUI.openGameMenu(player, "SLOTS", casinoManager.getActiveGame(player.getUniqueId()));
                } else if (displayName.contains(MessageUtils.getMessage("gui.casino.poker-title"))) {
                    casinoManager.startGame(player.getUniqueId(), "POKER", 100.0);
                    casinoGUI.openGameMenu(player, "POKER", casinoManager.getActiveGame(player.getUniqueId()));
                } else if (displayName.contains(MessageUtils.getMessage("gui.casino.roulette-title"))) {
                    casinoManager.startGame(player.getUniqueId(), "ROULETTE", 100.0);
                    casinoGUI.openGameMenu(player, "ROULETTE", casinoManager.getActiveGame(player.getUniqueId()));
                } else if (displayName.contains(MessageUtils.getMessage("gui.casino.baccarat-title"))) {
                    casinoManager.startGame(player.getUniqueId(), "BACCARAT", 100.0);
                    casinoGUI.openGameMenu(player, "BACCARAT", casinoManager.getActiveGame(player.getUniqueId()));
                }
                break;
            case "BLACKJACK":
            case "SLOTS":
            case "POKER":
            case "ROULETTE":
            case "BACCARAT":
                CasinoGame game = casinoManager.getActiveGame(player.getUniqueId());
                if (game != null) {
                    String action = switch (displayName) {
                        case "<#FFFF55>Hit" -> "hit";
                        case "<#FFFF55>Stand" -> "stand";
                        case "<#FFFF55>Double Down" -> "double";
                        case "<#FFFF55>Split" -> "split";
                        case "<#FFFF55>Call" -> "call";
                        case "<#FFFF55>Raise" -> "raise";
                        case "<#FFFF55>Fold" -> "fold";
                        case "<#FFFF55>Spin" -> "spin";
                        case "<#FFFF55>Deal" -> "deal";
                        case "<#FFFF55>Bet on Player" -> "bet:player";
                        case "<#FFFF55>Bet on Banker" -> "bet:banker";
                        case "<#FFFF55>Bet on Tie" -> "bet:tie";
                        case "<#FFFF55>Bet on Red" -> "bet:red";
                        case "<#FFFF55>Bet on Black" -> "bet:black";
                        case "<#FFFF55>Bet on Green" -> "bet:green";
                        case "<#FFFF55>Bet on Number" -> "bet:number";
                        default -> null;
                    };
                    if (action != null) {
                        game.handleAction(player, action);
                        casinoGUI.openGameMenu(player, guiType, game);
                    }
                    if (displayName.equals(MessageUtils.getMessage("gui.casino.back"))) {
                        casinoManager.endGame(player.getUniqueId());
                        casinoGUI.openMainMenu(player);
                    }
                }
                break;
        }
    }

    public void handleCasinoAction(Player player, String gameType, double bet) {
        if (economy == null) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        if (economy.has(player, bet)) {
            economy.withdrawPlayer(player, bet);
            CasinoGame game = casinoManager.startGame(player.getUniqueId(), gameType, bet);
            casinoGUI.openGameMenu(player, gameType, game);
        } else {
            MessageUtils.sendMessage(player, "casino.insufficient-funds");
        }
    }

    public void handleBetInput(Player player, String gameType, double bet) {
        if (bet <= 0) {
            MessageUtils.sendMessage(player, "casino.invalid-bet");
            return;
        }
        if (economy == null) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        if (economy.has(player, bet)) {
            economy.withdrawPlayer(player, bet);
            CasinoGame game = casinoManager.startGame(player.getUniqueId(), gameType, bet);
            casinoGUI.openGameMenu(player, gameType, game);
        } else {
            MessageUtils.sendMessage(player, "casino.insufficient-funds");
        }
    }

    public void handleRouletteNumberBet(Player player, int number) {
        if (number < 0 || number > 36) {
            MessageUtils.sendMessage(player, "casino.invalid-roulette-number");
            return;
        }
        CasinoGame game = casinoManager.getActiveGame(player.getUniqueId());
        if (game == null || !game.getGameType().equals("ROULETTE")) {
            MessageUtils.sendMessage(player, "casino.no-active-game");
            return;
        }
        game.handleAction(player, "bet:number:" + number);
        casinoGUI.openGameMenu(player, "ROULETTE", game);
    }
}