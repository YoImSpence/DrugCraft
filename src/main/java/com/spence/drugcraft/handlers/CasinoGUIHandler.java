package com.spence.drugcraft.handlers;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.CasinoGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.*;

public class CasinoGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final CasinoGUI casinoGUI;
    private final Random random = new Random();
    private final Map<UUID, BlackjackGame> blackjackGames = new HashMap<>();

    public CasinoGUIHandler(DrugCraft plugin, CasinoGUI casinoGUI) {
        this.plugin = plugin;
        this.casinoGUI = casinoGUI;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("CASINO")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        String subType = activeGUI.getMenuSubType();
        if (subType.equals("blackjack")) {
            if (displayName.equals(MessageUtils.getMessage("gui.casino.blackjack-hit"))) {
                hitBlackjack(player);
            } else if (displayName.equals(MessageUtils.getMessage("gui.casino.blackjack-stand"))) {
                standBlackjack(player);
            }
        }
    }

    public void openBlackjackMenu(Player player) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        double bet = plugin.getConfig("casino.yml").getDouble("games.blackjack.bet", 100.0);
        if (!plugin.getEconomyManager().withdrawPlayer(player, bet)) {
            MessageUtils.sendMessage(player, "casino.insufficient-funds");
            return;
        }

        BlackjackGame game = new BlackjackGame(bet);
        game.dealInitialCards();
        blackjackGames.put(player.getUniqueId(), game);

        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("gui.casino.blackjack-title")));
        ActiveGUI activeGUI = new ActiveGUI("CASINO", inv, "blackjack");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(MessageUtils.getMessage("#FFFF55gui.casino.blackjack-info"));
            infoMeta.setLore(Arrays.asList(
                    MessageUtils.getMessage("#FFFF55gui.casino.blackjack-player", "score", String.valueOf(game.getPlayerScore())),
                    MessageUtils.getMessage("#FFFF55gui.casino.blackjack-dealer", "score", String.valueOf(game.getDealerVisibleScore()))
            ));
            info.setItemMeta(infoMeta);
        }

        ItemStack hit = createItem(Material.GREEN_WOOL, "#55FF55gui.casino.blackjack-hit");
        ItemStack stand = createItem(Material.RED_WOOL, "#FF5555gui.casino.blackjack-stand");

        inv.setItem(11, info);
        inv.setItem(13, hit);
        inv.setItem(15, stand);

        player.openInventory(inv);
    }

    private void hitBlackjack(Player player) {
        BlackjackGame game = blackjackGames.get(player.getUniqueId());
        if (game == null) return;

        game.playerHit();
        if (game.getPlayerScore() > 21) {
            endBlackjack(player, false);
        } else {
            openBlackjackMenu(player);
        }
    }

    private void standBlackjack(Player player) {
        BlackjackGame game = blackjackGames.get(player.getUniqueId());
        if (game == null) return;

        game.dealerPlay();
        boolean playerWins = game.getPlayerScore() <= 21 && (game.getDealerScore() > 21 || game.getPlayerScore() > game.getDealerScore());
        endBlackjack(player, playerWins);
    }

    private void endBlackjack(Player player, boolean playerWins) {
        BlackjackGame game = blackjackGames.remove(player.getUniqueId());
        if (game == null) return;

        if (playerWins) {
            double reward = game.getBet() * 2.5;
            plugin.getCasinoManager().awardPrize(player, reward);
            MessageUtils.sendMessage(player, "casino.blackjack-win", "reward", String.valueOf(reward));
        } else {
            MessageUtils.sendMessage(player, "casino.blackjack-lose");
        }
        player.closeInventory();
    }

    public void openSlotsMenu(Player player) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        double bet = plugin.getConfig("casino.yml").getDouble("games.slots.bet", 50.0);
        if (!plugin.getEconomyManager().withdrawPlayer(player, bet)) {
            MessageUtils.sendMessage(player, "casino.insufficient-funds");
            return;
        }

        String[] symbols = {"CHERRY", "LEMON", "DIAMOND"};
        String[] result = new String[3];
        for (int i = 0; i < 3; i++) {
            result[i] = symbols[random.nextInt(symbols.length)];
        }

        MessageUtils.sendMessage(player, "casino.slots-result", "slot1", result[0], "slot2", result[1], "slot3", result[2]);
        if (result[0].equals(result[1]) && result[1].equals(result[2])) {
            double reward = bet * plugin.getConfig("casino.yml").getDouble("games.slots.multiplier", 10.0);
            plugin.getCasinoManager().awardPrize(player, reward);
            MessageUtils.sendMessage(player, "casino.slots-win", "reward", String.valueOf(reward));
        } else {
            MessageUtils.sendMessage(player, "casino.slots-lose");
        }
        player.closeInventory();
    }

    public void openPokerMenu(Player player) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        double bet = plugin.getConfig("casino.yml").getDouble("games.poker.bet", 200.0);
        if (!plugin.getEconomyManager().withdrawPlayer(player, bet)) {
            MessageUtils.sendMessage(player, "casino.insufficient-funds");
            return;
        }

        MessageUtils.sendMessage(player, "casino.poker-start");
        player.closeInventory();
    }

    public void openRouletteMenu(Player player) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        double bet = plugin.getConfig("casino.yml").getDouble("games.roulette.bet", 100.0);
        if (!plugin.getEconomyManager().withdrawPlayer(player, bet)) {
            MessageUtils.sendMessage(player, "casino.insufficient-funds");
            return;
        }

        String[] colors = {"RED", "BLACK"};
        String result = colors[random.nextInt(colors.length)];
        MessageUtils.sendMessage(player, "casino.roulette-result", "color", result);
        if (result.equals("RED")) {
            double reward = bet * 2.0;
            plugin.getCasinoManager().awardPrize(player, reward);
            MessageUtils.sendMessage(player, "casino.roulette-win", "reward", String.valueOf(reward));
        } else {
            MessageUtils.sendMessage(player, "casino.roulette-lose");
        }
        player.closeInventory();
    }

    public void openBaccaratMenu(Player player) {
        if (!plugin.getEconomyManager().isEconomyAvailable()) {
            MessageUtils.sendMessage(player, "general.economy-unavailable");
            return;
        }
        double bet = plugin.getConfig("casino.yml").getDouble("games.baccarat.bet", 150.0);
        if (!plugin.getEconomyManager().withdrawPlayer(player, bet)) {
            MessageUtils.sendMessage(player, "casino.insufficient-funds");
            return;
        }

        int playerScore = random.nextInt(9) + 1;
        int bankerScore = random.nextInt(9) + 1;
        MessageUtils.sendMessage(player, "casino.baccarat-result", "player_score", String.valueOf(playerScore), "banker_score", String.valueOf(bankerScore));
        if (playerScore > bankerScore) {
            double reward = bet * 2.0;
            plugin.getCasinoManager().awardPrize(player, reward);
            MessageUtils.sendMessage(player, "casino.baccarat-win", "reward", String.valueOf(reward));
        } else {
            MessageUtils.sendMessage(player, "casino.baccarat-lose");
        }
        player.closeInventory();
    }

    private ItemStack createItem(Material material, String messageKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage(messageKey));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openMainMenu(Player player) {
        MessageUtils.sendMessage(player, "casino.use-region");
    }

    private static class BlackjackGame {
        private final double bet;
        private final List<Integer> playerCards = new ArrayList<>();
        private final List<Integer> dealerCards = new ArrayList<>();
        private final Random random = new Random();

        public BlackjackGame(double bet) {
            this.bet = bet;
        }

        public void dealInitialCards() {
            playerCards.add(random.nextInt(10) + 1);
            playerCards.add(random.nextInt(10) + 1);
            dealerCards.add(random.nextInt(10) + 1);
            dealerCards.add(random.nextInt(10) + 1);
        }

        public void playerHit() {
            playerCards.add(random.nextInt(10) + 1);
        }

        public void dealerPlay() {
            while (getDealerScore() < 17) {
                dealerCards.add(random.nextInt(10) + 1);
            }
        }

        public int getPlayerScore() {
            return playerCards.stream().mapToInt(Integer::intValue).sum();
        }

        public int getDealerScore() {
            return dealerCards.stream().mapToInt(Integer::intValue).sum();
        }

        public int getDealerVisibleScore() {
            return dealerCards.get(0);
        }

        public double getBet() {
            return bet;
        }
    }
}