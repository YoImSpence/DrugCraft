package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.casino.CasinoGUI;
import com.spence.drugcraft.casino.CasinoGame;
import com.spence.drugcraft.casino.CasinoManager;
import com.spence.drugcraft.casino.SlotsGame;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.GUIHandler;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CasinoGUIHandler implements Listener, GUIHandler {
    private final DrugCraft plugin;
    private final CasinoGUI casinoGUI;
    private final CasinoManager casinoManager;
    private final EconomyManager economyManager;

    public CasinoGUIHandler(DrugCraft plugin, CasinoGUI casinoGUI, CasinoManager casinoManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.casinoGUI = casinoGUI;
        this.casinoManager = casinoManager;
        this.economyManager = economyManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onClick(Player player, ItemStack clickedItem, int slot, Inventory inventory) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("CASINO")) return;
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = MessageUtils.stripColor(clickedItem.getItemMeta().displayName());
        String menuSubType = activeGUI.getMenuSubType();
        if (menuSubType == null) {
            switch (displayName) {
                case "Blackjack":
                    casinoGUI.openBlackjack(player);
                    break;
                case "Slots":
                    casinoGUI.openSlots(player);
                    break;
                case "Poker":
                    casinoGUI.openPoker(player);
                    break;
                case "Roulette":
                    casinoGUI.openRoulette(player);
                    break;
                case "Baccarat":
                    casinoGUI.openBaccarat(player);
                    break;
            }
        } else {
            CasinoGame game = plugin.getCasinoManager().getGame(player.getUniqueId());
            switch (displayName) {
                case "Spin":
                    if (game instanceof SlotsGame slotsGame) {
                        plugin.getCasinoManager().startGame(player, "SLOTS", 5.0);
                        casinoGUI.openSlots(player);
                    }
                    break;
            }
        }
    }
}