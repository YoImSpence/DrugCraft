package com.spence.drugcraft.casino;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class CasinoGUI {
    private final DrugCraft plugin;
    private final CasinoManager casinoManager;
    private static final int[] SLOT_MACHINE_POSITIONS = {12, 13, 14};
    private static final int[] BLACKJACK_PLAYER_POSITIONS = {29, 30, 31, 32, 33};
    private static final int[] BLACKJACK_DEALER_POSITIONS = {11, 12, 13, 14, 15};
    private static final int[] POKER_COMMUNITY_POSITIONS = {11, 12, 13, 14, 15};
    private static final int[] POKER_PLAYER_POSITIONS = {29, 30};
    private static final int[] POKER_DEALER_POSITIONS = {20, 21};
    private static final int[] ROULETTE_BET_POSITIONS = {10, 11, 12, 13, 14, 15, 16};
    private static final int[] ROULETTE_WHEEL_POSITIONS = {19, 20, 21, 22, 23, 24, 25};
    private static final int[] BACCARAT_PLAYER_POSITIONS = {29, 30};
    private static final int[] BACCARAT_BANKER_POSITIONS = {11, 12};
    private static final Material[] SLOT_FRUITS = {
            Material.APPLE, Material.MELON_SLICE, Material.PUMPKIN_PIE, Material.GOLDEN_APPLE, Material.CARROT
    };

    public CasinoGUI(DrugCraft plugin, CasinoManager casinoManager) {
        this.plugin = plugin;
        this.casinoManager = casinoManager;
    }

    public void openMainMenu(Player player) {
        String titleText = MessageUtils.getMessage("gui.casino.title-main");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack blackjack = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta blackjackMeta = blackjack.getItemMeta();
        blackjackMeta.displayName(MessageUtils.color("Blackjack").color(TextColor.fromHexString("#FFD700")));
        List<Component> blackjackLore = new ArrayList<>();
        blackjackLore.add(MessageUtils.color("Bet: $10").color(TextColor.fromHexString("#D3D3D3")));
        blackjackMeta.lore(blackjackLore);
        blackjack.setItemMeta(blackjackMeta);
        inventory.setItem(10, blackjack);

        ItemStack slots = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta slotsMeta = slots.getItemMeta();
        slotsMeta.displayName(MessageUtils.color("Slots").color(TextColor.fromHexString("#FFD700")));
        List<Component> slotsLore = new ArrayList<>();
        slotsLore.add(MessageUtils.color("Bet: $5").color(TextColor.fromHexString("#D3D3D3")));
        slotsMeta.lore(slotsLore);
        slots.setItemMeta(slotsMeta);
        inventory.setItem(12, slots);

        ItemStack poker = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta pokerMeta = poker.getItemMeta();
        pokerMeta.displayName(MessageUtils.color("Poker").color(TextColor.fromHexString("#FFD700")));
        List<Component> pokerLore = new ArrayList<>();
        pokerLore.add(MessageUtils.color("Bet: $20").color(TextColor.fromHexString("#D3D3D3")));
        pokerMeta.lore(pokerLore);
        poker.setItemMeta(pokerMeta);
        inventory.setItem(14, poker);

        ItemStack roulette = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta rouletteMeta = roulette.getItemMeta();
        rouletteMeta.displayName(MessageUtils.color("Roulette").color(TextColor.fromHexString("#FFD700")));
        List<Component> rouletteLore = new ArrayList<>();
        rouletteLore.add(MessageUtils.color("Bet: $5").color(TextColor.fromHexString("#D3D3D3")));
        rouletteMeta.lore(rouletteLore);
        roulette.setItemMeta(rouletteMeta);
        inventory.setItem(16, roulette);

        ItemStack baccarat = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta baccaratMeta = baccarat.getItemMeta();
        baccaratMeta.displayName(MessageUtils.color("Baccarat").color(TextColor.fromHexString("#FFD700")));
        List<Component> baccaratLore = new ArrayList<>();
        baccaratLore.add(MessageUtils.color("Bet: $15").color(TextColor.fromHexString("#D3D3D3")));
        baccaratMeta.lore(baccaratLore);
        baccarat.setItemMeta(baccaratMeta);
        inventory.setItem(18, baccarat);

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened casino main menu for player " + player.getName());
    }

    public void openBlackjack(Player player) {
        String titleText = MessageUtils.getMessage("gui.casino.title-blackjack");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 45, title);

        casinoManager.startGame(player, "BLACKJACK", 10.0);
        CasinoGame game = casinoManager.getGame(player.getUniqueId());
        if (game == null || !(game instanceof BlackjackGame)) {
            MessageUtils.sendMessage(player, "casino.blackjack.error-start");
            player.closeInventory();
            return;
        }

        BlackjackGame blackjackGame = (BlackjackGame) game;

        // Display player's cards
        List<Integer> playerCardSlots = Arrays.stream(BLACKJACK_PLAYER_POSITIONS).boxed().collect(Collectors.toList());
        int playerSlotIndex = 0;
        for (Card card : blackjackGame.getPlayerHand()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(playerCardSlots.get(playerSlotIndex++), cardItem);
        }

        // Display dealer's first card (second card hidden until player stands)
        List<Integer> dealerCardSlots = Arrays.stream(BLACKJACK_DEALER_POSITIONS).boxed().collect(Collectors.toList());
        ItemStack dealerCard1 = new ItemStack(Material.PAPER);
        ItemMeta dealerCard1Meta = dealerCard1.getItemMeta();
        dealerCard1Meta.displayName(MessageUtils.color(blackjackGame.getDealerHand().get(0).toString()).color(TextColor.fromHexString("#FFD700")));
        dealerCard1.setItemMeta(dealerCard1Meta);
        inventory.setItem(dealerCardSlots.get(0), dealerCard1);

        ItemStack dealerCard2 = new ItemStack(Material.PAPER);
        ItemMeta dealerCard2Meta = dealerCard2.getItemMeta();
        dealerCard2Meta.displayName(MessageUtils.color("Hidden Card").color(TextColor.fromHexString("#D3D3D3")));
        dealerCard2.setItemMeta(dealerCard2Meta);
        inventory.setItem(dealerCardSlots.get(1), dealerCard2);

        // Add action buttons
        ItemStack hit = new ItemStack(Material.GREEN_WOOL);
        ItemMeta hitMeta = hit.getItemMeta();
        hitMeta.displayName(MessageUtils.color("Hit").color(TextColor.fromHexString("#00FF00")));
        hit.setItemMeta(hitMeta);
        inventory.setItem(39, hit);

        ItemStack stand = new ItemStack(Material.RED_WOOL);
        ItemMeta standMeta = stand.getItemMeta();
        standMeta.displayName(MessageUtils.color("Stand").color(TextColor.fromHexString("#FF0000")));
        stand.setItemMeta(standMeta);
        inventory.setItem(41, stand);

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 45; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened blackjack menu for player " + player.getName());
    }

    public void updateBlackjack(Player player, BlackjackGame game, boolean revealDealer) {
        String titleText = MessageUtils.getMessage("gui.casino.title-blackjack");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 45, title);

        // Display player's cards
        List<Integer> playerCardSlots = Arrays.stream(BLACKJACK_PLAYER_POSITIONS).boxed().collect(Collectors.toList());
        int playerSlotIndex = 0;
        for (Card card : game.getPlayerHand()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("Value: " + card.getValue()).color(TextColor.fromHexString("#D3D3D3")));
            cardMeta.lore(lore);
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(playerCardSlots.get(playerSlotIndex++), cardItem);
        }

        // Display dealer's cards
        List<Integer> dealerCardSlots = Arrays.stream(BLACKJACK_DEALER_POSITIONS).boxed().collect(Collectors.toList());
        int dealerSlotIndex = 0;
        for (Card card : game.getDealerHand()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            if (dealerSlotIndex == 1 && !revealDealer) {
                cardMeta.displayName(MessageUtils.color("Hidden Card").color(TextColor.fromHexString("#D3D3D3")));
            } else {
                cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
                List<Component> lore = new ArrayList<>();
                lore.add(MessageUtils.color("Value: " + card.getValue()).color(TextColor.fromHexString("#D3D3D3")));
                cardMeta.lore(lore);
            }
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(dealerCardSlots.get(dealerSlotIndex++), cardItem);
        }

        if (!revealDealer) {
            // Add action buttons
            ItemStack hit = new ItemStack(Material.GREEN_WOOL);
            ItemMeta hitMeta = hit.getItemMeta();
            hitMeta.displayName(MessageUtils.color("Hit").color(TextColor.fromHexString("#00FF00")));
            hit.setItemMeta(hitMeta);
            inventory.setItem(39, hit);

            ItemStack stand = new ItemStack(Material.RED_WOOL);
            ItemMeta standMeta = stand.getItemMeta();
            standMeta.displayName(MessageUtils.color("Stand").color(TextColor.fromHexString("#FF0000")));
            stand.setItemMeta(standMeta);
            inventory.setItem(41, stand);
        } else {
            // Display result
            ItemStack result = new ItemStack(Material.PAPER);
            ItemMeta resultMeta = result.getItemMeta();
            resultMeta.displayName(MessageUtils.color(game.getResultMessage()));
            result.setItemMeta(resultMeta);
            inventory.setItem(40, result);
        }

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 45; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Updated blackjack menu for player " + player.getName() + "; Reveal Dealer: " + revealDealer);
    }

    public void openSlots(Player player) {
        String titleText = MessageUtils.getMessage("gui.casino.title-slots");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        casinoManager.startGame(player, "SLOTS", 5.0);
        CasinoGame game = casinoManager.getGame(player.getUniqueId());
        if (game == null || !(game instanceof SlotsGame)) {
            MessageUtils.sendMessage(player, "casino.slots.error-start");
            player.closeInventory();
            return;
        }

        SlotsGame slotsGame = (SlotsGame) game;

        // Start spinning animation
        for (int pos : SLOT_MACHINE_POSITIONS) {
            ItemStack symbol = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta symbolMeta = symbol.getItemMeta();
            symbolMeta.displayName(MessageUtils.color("Spinning...").color(TextColor.fromHexString("#D3D3D3")));
            symbol.setItemMeta(symbolMeta);
            inventory.setItem(pos, symbol);
        }

        ItemStack spin = new ItemStack(Material.LEVER);
        ItemMeta spinMeta = spin.getItemMeta();
        spinMeta.displayName(MessageUtils.color("Spin ($5)").color(TextColor.fromHexString("#FFD700")));
        spin.setItemMeta(spinMeta);
        inventory.setItem(22, spin);

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i == 13) { // Keep side borders
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened slot machine menu for player " + player.getName());

        // Animate slots
        new BukkitRunnable() {
            int ticks = 0;
            final Random random = new Random();

            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds (20 ticks/second)
                    updateSlots(player, slotsGame);
                    cancel();
                    return;
                }
                for (int pos : SLOT_MACHINE_POSITIONS) {
                    ItemStack symbol = new ItemStack(SLOT_FRUITS[random.nextInt(SLOT_FRUITS.length)]);
                    ItemMeta symbolMeta = symbol.getItemMeta();
                    symbolMeta.displayName(Component.text(symbol.getType().name()).color(TextColor.fromHexString("#FFD700")));
                    symbol.setItemMeta(symbolMeta);
                    inventory.setItem(pos, symbol);
                }
                player.playSound(player.getLocation(), "minecraft:ui.button.click", 0.5f, 1.0f);
                ticks += 5; // Update every 0.25 seconds
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public void updateSlots(Player player, SlotsGame game) {
        String titleText = MessageUtils.getMessage("gui.casino.title-slots");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        int index = 0;
        for (int pos : SLOT_MACHINE_POSITIONS) {
            ItemStack symbol = new ItemStack(game.getResults().get(index++));
            ItemMeta symbolMeta = symbol.getItemMeta();
            symbolMeta.displayName(Component.text(symbol.getType().name()).color(TextColor.fromHexString("#FFD700")));
            symbol.setItemMeta(symbolMeta);
            inventory.setItem(pos, symbol);
        }

        ItemStack result = new ItemStack(Material.PAPER);
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.displayName(MessageUtils.color(game.getResultMessage()));
        result.setItemMeta(resultMeta);
        inventory.setItem(4, result);

        ItemStack spin = new ItemStack(Material.LEVER);
        ItemMeta spinMeta = spin.getItemMeta();
        spinMeta.displayName(MessageUtils.color("Spin Again ($5)").color(TextColor.fromHexString("#FFD700")));
        spin.setItemMeta(spinMeta);
        inventory.setItem(22, spin);

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i > 17 || i == 13) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Updated slot machine menu for player " + player.getName() + "; Payout: $" + game.getPayout());
    }

    public void openPoker(Player player) {
        String titleText = MessageUtils.getMessage("gui.casino.title-poker");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 45, title);

        casinoManager.startGame(player, "POKER", 20.0);
        CasinoGame game = casinoManager.getGame(player.getUniqueId());
        if (game == null || !(game instanceof PokerGame)) {
            MessageUtils.sendMessage(player, "casino.poker.error-start");
            player.closeInventory();
            return;
        }

        PokerGame pokerGame = (PokerGame) game;

        // Display player's cards
        List<Integer> playerCardSlots = Arrays.stream(POKER_PLAYER_POSITIONS).boxed().collect(Collectors.toList());
        int playerSlotIndex = 0;
        for (Card card : pokerGame.getPlayerHand()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(playerCardSlots.get(playerSlotIndex++), cardItem);
        }

        // Dealer cards hidden until end
        List<Integer> dealerCardSlots = Arrays.stream(POKER_DEALER_POSITIONS).boxed().collect(Collectors.toList());
        for (int pos : dealerCardSlots) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            cardMeta.displayName(MessageUtils.color("Hidden Card").color(TextColor.fromHexString("#D3D3D3")));
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(pos, cardItem);
        }

        // Add action buttons
        ItemStack call = new ItemStack(Material.GREEN_WOOL);
        ItemMeta callMeta = call.getItemMeta();
        callMeta.displayName(MessageUtils.color("Call").color(TextColor.fromHexString("#00FF00")));
        call.setItemMeta(callMeta);
        inventory.setItem(39, call);

        ItemStack fold = new ItemStack(Material.RED_WOOL);
        ItemMeta foldMeta = fold.getItemMeta();
        foldMeta.displayName(MessageUtils.color("Fold").color(TextColor.fromHexString("#FF0000")));
        fold.setItemMeta(foldMeta);
        inventory.setItem(41, fold);

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 45; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened poker menu for player " + player.getName());
    }

    public void updatePoker(Player player, PokerGame game) {
        String titleText = MessageUtils.getMessage("gui.casino.title-poker");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 45, title);

        // Display player's cards
        List<Integer> playerCardSlots = Arrays.stream(POKER_PLAYER_POSITIONS).boxed().collect(Collectors.toList());
        int playerSlotIndex = 0;
        for (Card card : game.getPlayerHand()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("Value: " + card.getValue()).color(TextColor.fromHexString("#D3D3D3")));
            cardMeta.lore(lore);
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(playerCardSlots.get(playerSlotIndex++), cardItem);
        }

        // Display community cards based on round
        List<Integer> communityCardSlots = Arrays.stream(POKER_COMMUNITY_POSITIONS).boxed().collect(Collectors.toList());
        int communitySlotIndex = 0;
        for (Card card : game.getCommunityCards()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("Value: " + card.getValue()).color(TextColor.fromHexString("#D3D3D3")));
            cardMeta.lore(lore);
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(communityCardSlots.get(communitySlotIndex++), cardItem);
        }

        // Display dealer cards if game is over
        List<Integer> dealerCardSlots = Arrays.stream(POKER_DEALER_POSITIONS).boxed().collect(Collectors.toList());
        int dealerSlotIndex = 0;
        for (Card card : game.getDealerHand()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            if (!game.isGameOver()) {
                cardMeta.displayName(MessageUtils.color("Hidden Card").color(TextColor.fromHexString("#D3D3D3")));
            } else {
                cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
                List<Component> lore = new ArrayList<>();
                lore.add(MessageUtils.color("Value: " + card.getValue()).color(TextColor.fromHexString("#D3D3D3")));
                cardMeta.lore(lore);
            }
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(dealerCardSlots.get(dealerSlotIndex++), cardItem);
        }

        if (!game.isGameOver()) {
            // Add action buttons
            ItemStack call = new ItemStack(Material.GREEN_WOOL);
            ItemMeta callMeta = call.getItemMeta();
            callMeta.displayName(MessageUtils.color("Call").color(TextColor.fromHexString("#00FF00")));
            call.setItemMeta(callMeta);
            inventory.setItem(39, call);

            ItemStack fold = new ItemStack(Material.RED_WOOL);
            ItemMeta foldMeta = fold.getItemMeta();
            foldMeta.displayName(MessageUtils.color("Fold").color(TextColor.fromHexString("#FF0000")));
            fold.setItemMeta(foldMeta);
            inventory.setItem(41, fold);
        } else {
            // Display result
            ItemStack result = new ItemStack(Material.PAPER);
            ItemMeta resultMeta = result.getItemMeta();
            resultMeta.displayName(MessageUtils.color(game.getResultMessage()));
            result.setItemMeta(resultMeta);
            inventory.setItem(40, result);
        }

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 45; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Updated poker menu for player " + player.getName());
    }

    public void openRoulette(Player player) {
        String titleText = MessageUtils.getMessage("gui.casino.title-roulette");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 36, title);

        casinoManager.startGame(player, "ROULETTE", 5.0);
        CasinoGame game = casinoManager.getGame(player.getUniqueId());
        if (game == null || !(game instanceof RouletteGame)) {
            MessageUtils.sendMessage(player, "casino.roulette.error-start");
            player.closeInventory();
            return;
        }

        // Add betting options
        List<Integer> betSlots = Arrays.stream(ROULETTE_BET_POSITIONS).boxed().collect(Collectors.toList());
        String[] betOptions = {"Red", "Black", "Odd", "Even", "Number:17", "Number:0", "Spin"};
        int betSlotIndex = 0;
        for (String betOption : betOptions) {
            ItemStack betItem = new ItemStack(betOption.equals("Spin") ? Material.COMPASS : Material.PAPER);
            ItemMeta betMeta = betItem.getItemMeta();
            betMeta.displayName(MessageUtils.color(betOption).color(TextColor.fromHexString("#FFD700")));
            betItem.setItemMeta(betMeta);
            inventory.setItem(betSlots.get(betSlotIndex++), betItem);
        }

        // Initialize wheel
        for (int pos : ROULETTE_WHEEL_POSITIONS) {
            ItemStack wheelSlot = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta wheelMeta = (SkullMeta) wheelSlot.getItemMeta();
            wheelMeta.displayName(MessageUtils.color("Wheel").color(TextColor.fromHexString("#D3D3D3")));
            wheelSlot.setItemMeta(wheelMeta);
            inventory.setItem(pos, wheelSlot);
        }

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened roulette menu for player " + player.getName());

        // Animate wheel
        new BukkitRunnable() {
            int ticks = 0;
            int offset = 0;
            final Random random = new Random();

            @Override
            public void run() {
                if (ticks >= 60) { // 3 seconds
                    cancel();
                    return;
                }
                for (int i = 0; i < ROULETTE_WHEEL_POSITIONS.length; i++) {
                    int pos = ROULETTE_WHEEL_POSITIONS[i];
                    int number = (i + offset) % 37; // 0-36
                    String color = number == 0 ? "Green" : (number % 2 == 0) ? "Red" : "Black";
                    ItemStack wheelSlot = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta wheelMeta = (SkullMeta) wheelSlot.getItemMeta();
                    wheelMeta.displayName(MessageUtils.color(number + " (" + color + ")").color(
                            TextColor.fromHexString(color.equals("Red") ? "#FF5555" : color.equals("Black") ? "#333333" : "#55FF55")
                    ));
                    // Note: Custom head textures require external setup (e.g., via resource pack)
                    wheelSlot.setItemMeta(wheelMeta);
                    inventory.setItem(pos, wheelSlot);
                }
                player.playSound(player.getLocation(), "minecraft:ui.button.click", 0.5f, 1.0f);
                offset = (offset + 1) % 37;
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public void updateRoulette(Player player, RouletteGame game) {
        String titleText = MessageUtils.getMessage("gui.casino.title-roulette");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 36, title);

        // Display result
        ItemStack result = new ItemStack(Material.PAPER);
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.displayName(MessageUtils.color(game.getResultMessage()));
        result.setItemMeta(resultMeta);
        inventory.setItem(13, result);

        if (!game.isGameOver()) {
            // Re-display betting options
            List<Integer> betSlots = Arrays.stream(ROULETTE_BET_POSITIONS).boxed().collect(Collectors.toList());
            String[] betOptions = {"Red", "Black", "Odd", "Even", "Number:17", "Number:0", "Spin"};
            int betSlotIndex = 0;
            for (String betOption : betOptions) {
                ItemStack betItem = new ItemStack(betOption.equals("Spin") ? Material.COMPASS : Material.PAPER);
                ItemMeta betMeta = betItem.getItemMeta();
                betMeta.displayName(MessageUtils.color(betOption).color(TextColor.fromHexString("#FFD700")));
                betItem.setItemMeta(betMeta);
                inventory.setItem(betSlots.get(betSlotIndex++), betItem);
            }

            // Re-display wheel
            for (int pos : ROULETTE_WHEEL_POSITIONS) {
                ItemStack wheelSlot = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta wheelMeta = (SkullMeta) wheelSlot.getItemMeta();
                wheelMeta.displayName(MessageUtils.color("Wheel").color(TextColor.fromHexString("#D3D3D3")));
                wheelSlot.setItemMeta(wheelMeta);
                inventory.setItem(pos, wheelSlot);
            }
        }

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Updated roulette menu for player " + player.getName());
    }

    public void openBaccarat(Player player) {
        String titleText = MessageUtils.getMessage("gui.casino.title-baccarat");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 45, title);

        casinoManager.startGame(player, "BACCARAT", 15.0);
        CasinoGame game = casinoManager.getGame(player.getUniqueId());
        if (game == null || !(game instanceof BaccaratGame)) {
            MessageUtils.sendMessage(player, "casino.baccarat.error-start");
            player.closeInventory();
            return;
        }

        // Add betting options
        ItemStack betPlayer = new ItemStack(Material.PAPER);
        ItemMeta betPlayerMeta = betPlayer.getItemMeta();
        betPlayerMeta.displayName(MessageUtils.color("Bet on Player").color(TextColor.fromHexString("#FFD700")));
        betPlayer.setItemMeta(betPlayerMeta);
        inventory.setItem(10, betPlayer);

        ItemStack betBanker = new ItemStack(Material.PAPER);
        ItemMeta betBankerMeta = betBanker.getItemMeta();
        betBankerMeta.displayName(MessageUtils.color("Bet on Banker").color(TextColor.fromHexString("#FFD700")));
        betBanker.setItemMeta(betBankerMeta);
        inventory.setItem(12, betBanker);

        ItemStack betTie = new ItemStack(Material.PAPER);
        ItemMeta betTieMeta = betTie.getItemMeta();
        betTieMeta.displayName(MessageUtils.color("Bet on Tie").color(TextColor.fromHexString("#FFD700")));
        betTie.setItemMeta(betTieMeta);
        inventory.setItem(14, betTie);

        ItemStack deal = new ItemStack(Material.COMPASS);
        ItemMeta dealMeta = deal.getItemMeta();
        dealMeta.displayName(MessageUtils.color("Deal").color(TextColor.fromHexString("#00FF00")));
        deal.setItemMeta(dealMeta);
        inventory.setItem(16, deal);

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 45; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened baccarat menu for player " + player.getName());
    }

    public void updateBaccarat(Player player, BaccaratGame game) {
        String titleText = MessageUtils.getMessage("gui.casino.title-baccarat");
        Component title = MessageUtils.color(titleText).color(TextColor.fromHexString("#00CED1"));
        Inventory inventory = Bukkit.createInventory(null, 45, title);

        // Display player's cards
        List<Integer> playerCardSlots = Arrays.stream(BACCARAT_PLAYER_POSITIONS).boxed().collect(Collectors.toList());
        int playerSlotIndex = 0;
        for (Card card : game.getPlayerHand()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("Value: " + card.getValue()).color(TextColor.fromHexString("#D3D3D3")));
            cardMeta.lore(lore);
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(playerCardSlots.get(playerSlotIndex++), cardItem);
        }

        // Display banker's cards
        List<Integer> bankerCardSlots = Arrays.stream(BACCARAT_BANKER_POSITIONS).boxed().collect(Collectors.toList());
        int bankerSlotIndex = 0;
        for (Card card : game.getBankerHand()) {
            ItemStack cardItem = new ItemStack(Material.PAPER);
            ItemMeta cardMeta = cardItem.getItemMeta();
            cardMeta.displayName(MessageUtils.color(card.toString()).color(TextColor.fromHexString("#FFD700")));
            List<Component> lore = new ArrayList<>();
            lore.add(MessageUtils.color("Value: " + card.getValue()).color(TextColor.fromHexString("#D3D3D3")));
            cardMeta.lore(lore);
            cardItem.setItemMeta(cardMeta);
            inventory.setItem(bankerCardSlots.get(bankerSlotIndex++), cardItem);
        }

        if (game.isGameOver()) {
            // Display result
            ItemStack result = new ItemStack(Material.PAPER);
            ItemMeta resultMeta = result.getItemMeta();
            resultMeta.displayName(MessageUtils.color(game.getResultMessage()));
            result.setItemMeta(resultMeta);
            inventory.setItem(40, result);
        }

        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(MessageUtils.color(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 45; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("CASINO", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Updated baccarat menu for player " + player.getName());
    }
}