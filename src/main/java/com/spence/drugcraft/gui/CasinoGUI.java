package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.casino.*;
import com.spence.drugcraft.utils.MessageUtils;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class CasinoGUI {
    private final DrugCraft plugin;
    private final CasinoManager casinoManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CasinoGUI(DrugCraft plugin, CasinoManager casinoManager) {
        this.plugin = plugin;
        this.casinoManager = casinoManager;
    }

    public void openMainMenu(Player player) {
        WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
        Location location = player.getLocation();
        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
        boolean inCasinoRegion = false;
        for (ProtectedRegion region : regions) {
            if (region.getId().equalsIgnoreCase("Casino")) {
                inCasinoRegion = true;
                break;
            }
        }
        if (!inCasinoRegion) {
            MessageUtils.sendMessage(player, "casino.not-in-region");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.casino.main-title")));
        ActiveGUI activeGUI = new ActiveGUI("CASINO", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.RED_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.casino.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        inv.setItem(21, createItem(Material.DIAMOND, MessageUtils.getMessage("gui.casino.blackjack-title")));
        inv.setItem(23, createItem(Material.EMERALD, MessageUtils.getMessage("gui.casino.slots-title")));
        inv.setItem(25, createItem(Material.PAPER, MessageUtils.getMessage("gui.casino.poker-title")));
        inv.setItem(27, createItem(Material.REDSTONE, MessageUtils.getMessage("gui.casino.roulette-title")));
        inv.setItem(29, createItem(Material.GOLD_INGOT, MessageUtils.getMessage("gui.casino.baccarat-title")));

        inv.setItem(49, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.back")));

        player.openInventory(inv);
    }

    public void openGameMenu(Player player, String gameType, CasinoGame game) {
        if (game == null || game.isGameOver()) {
            casinoManager.endGame(player.getUniqueId());
            openMainMenu(player);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, miniMessage.deserialize(MessageUtils.getMessage("gui.casino." + gameType.toLowerCase() + "-title")));
        ActiveGUI activeGUI = new ActiveGUI(gameType, inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack border = createItem(Material.RED_STAINED_GLASS_PANE, MessageUtils.getMessage("gui.casino.border"));
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, border);
            }
        }

        Map<String, Object> state = game.getState();
        switch (gameType) {
            case "BACCARAT":
                String betType = (String) state.get("betType");
                if (betType == null) {
                    inv.setItem(21, createItem(Material.GREEN_WOOL, MessageUtils.getMessage("gui.casino.baccarat-player")));
                    inv.setItem(23, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.casino.baccarat-banker")));
                    inv.setItem(25, createItem(Material.YELLOW_WOOL, MessageUtils.getMessage("gui.casino.baccarat-tie")));
                } else {
                    inv.setItem(22, createItem(Material.EMERALD, MessageUtils.getMessage("gui.casino.baccarat-deal")));
                }
                List<Card> playerHand = (List<Card>) state.get("playerHand");
                List<Card> bankerHand = (List<Card>) state.get("bankerHand");
                for (int i = 0; i < 3; i++) {
                    if (playerHand != null && i < playerHand.size()) {
                        inv.setItem(28 + i, createItem(Material.PAPER, "<white>" + playerHand.get(i).toString()));
                    }
                    if (bankerHand != null && i < bankerHand.size()) {
                        inv.setItem(34 + i, createItem(Material.PAPER, "<white>" + bankerHand.get(i).toString()));
                    }
                }
                break;
            case "BLACKJACK":
                if (!game.isGameOver()) {
                    inv.setItem(21, createItem(Material.GREEN_WOOL, MessageUtils.getMessage("gui.casino.blackjack-hit")));
                    inv.setItem(23, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.casino.blackjack-stand")));
                    List<Card> blackjackPlayerHand = (List<Card>) state.get("playerHand"); // Renamed to avoid duplicate
                    if (blackjackPlayerHand != null && blackjackPlayerHand.size() == 2) {
                        inv.setItem(25, createItem(Material.YELLOW_WOOL, MessageUtils.getMessage("gui.casino.blackjack-double")));
                        if (blackjackPlayerHand.get(0).getValue() == blackjackPlayerHand.get(1).getValue()) {
                            inv.setItem(27, createItem(Material.BLUE_WOOL, MessageUtils.getMessage("gui.casino.blackjack-split")));
                        }
                    }
                }
                List<Card> blackjackPlayerHand = (List<Card>) state.get("playerHand"); // Fixed: Renamed to avoid duplicate
                List<Card> dealerHand = (List<Card>) state.get("dealerHand");
                List<Card> splitHand = (List<Card>) state.get("playerSplitHand");
                for (int i = 0; i < 5; i++) {
                    if (blackjackPlayerHand != null && i < blackjackPlayerHand.size()) {
                        inv.setItem(28 + i, createItem(Material.PAPER, "<white>" + blackjackPlayerHand.get(i).toString()));
                    }
                    if (dealerHand != null && i < dealerHand.size()) {
                        inv.setItem(34 + i, createItem(Material.PAPER, "<white>" + dealerHand.get(i).toString()));
                    }
                    if (splitHand != null && i < splitHand.size()) {
                        inv.setItem(40 + i, createItem(Material.PAPER, "<white>Split: " + splitHand.get(i).toString()));
                    }
                }
                break;
            case "POKER":
                if (!game.isGameOver()) {
                    inv.setItem(21, createItem(Material.GREEN_WOOL, MessageUtils.getMessage("gui.casino.poker-call")));
                    inv.setItem(23, createItem(Material.YELLOW_WOOL, MessageUtils.getMessage("gui.casino.poker-raise")));
                    inv.setItem(25, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.casino.poker-fold")));
                }
                List<Card> pokerPlayerHand = (List<Card>) state.get("playerHand");
                List<Card> communityCards = (List<Card>) state.get("communityCards");
                for (int i = 0; i < 2; i++) {
                    if (pokerPlayerHand != null && i < pokerPlayerHand.size()) {
                        inv.setItem(28 + i, createItem(Material.PAPER, "<white>" + pokerPlayerHand.get(i).toString()));
                    }
                }
                for (int i = 0; i < 5; i++) {
                    if (communityCards != null && i < communityCards.size()) {
                        inv.setItem(34 + i, createItem(Material.PAPER, "<white>" + communityCards.get(i).toString()));
                    }
                }
                break;
            case "ROULETTE":
                String rouletteBetType = (String) state.get("betType");
                if (rouletteBetType == null) {
                    inv.setItem(21, createItem(Material.RED_WOOL, MessageUtils.getMessage("gui.casino.roulette-red")));
                    inv.setItem(23, createItem(Material.BLACK_WOOL, MessageUtils.getMessage("gui.casino.roulette-black")));
                    inv.setItem(25, createItem(Material.GREEN_WOOL, MessageUtils.getMessage("gui.casino.roulette-green")));
                    inv.setItem(27, createItem(Material.BLUE_WOOL, MessageUtils.getMessage("gui.casino.roulette-number")));
                } else {
                    inv.setItem(22, createItem(Material.EMERALD, MessageUtils.getMessage("gui.casino.roulette-spin")));
                    // Spinning animation
                    for (int i = 0; i < 9; i++) {
                        Material wool = i % 3 == 0 ? Material.RED_WOOL : i % 3 == 1 ? Material.BLACK_WOOL : Material.GREEN_WOOL;
                        inv.setItem(10 + i, createItem(wool, "<white>Spinning"));
                    }
                }
                Object resultNumberObj = state.get("resultNumber");
                if (resultNumberObj instanceof Integer resultNumber && resultNumber != -1) {
                    String color = resultNumber == 0 ? "Green" :
                            List.of(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).contains(resultNumber) ? "Red" : "Black";
                    inv.setItem(31, createItem(Material.PAPER, "<white>Result: " + resultNumber + " (" + color + ")"));
                }
                break;
            case "SLOTS":
                if (!game.isGameOver()) {
                    inv.setItem(22, createItem(Material.EMERALD, MessageUtils.getMessage("gui.casino.slots-spin")));
                    // Animation placeholders
                    for (int i = 0; i < 3; i++) {
                        Material[] items = {Material.DIAMOND, Material.EMERALD, Material.PAPER};
                        inv.setItem(30 + i, createItem(items[i % 3], "<white>Spinning"));
                    }
                }
                String[] reels = (String[]) state.get("reels");
                for (int i = 0; i < 3; i++) {
                    inv.setItem(30 + i, createItem(Material.PAPER, "<white>" + (reels != null && i < reels.length ? reels[i] : "?")));
                }
                break;
        }

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