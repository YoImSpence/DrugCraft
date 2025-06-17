package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.casino.CasinoGUI;
import com.spence.drugcraft.casino.CasinoManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CasinoGUIHandler implements GUIHandler {
    private final DrugCraft plugin;
    private final CasinoGUI casinoGUI;
    private final CasinoManager casinoManager;
    private final EconomyManager economyManager;

    public CasinoGUIHandler(DrugCraft plugin, CasinoGUI casinoGUI, CasinoManager casinoManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.casinoGUI = casinoGUI;
        this.casinoManager = casinoManager;
        this.economyManager = economyManager;
    }

    @Override
    public void onClick(Player player, ItemStack item, int slot, Inventory inventory) {
        if (item == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("CASINO")) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String displayName = MessageUtils.stripColor(meta.displayName());

        switch (displayName) {
            case "Blackjack":
                casinoGUI.openBlackjackMenu(player);
                break;
            case "Slots":
                casinoGUI.openSlotsMenu(player);
                break;
            case "Poker":
                casinoGUI.openPokerMenu(player);
                break;
            case "Roulette":
                casinoGUI.openRouletteMenu(player);
                break;
            case "Baccarat":
                casinoGUI.openBaccaratMenu(player);
                break;
            case "Back":
                casinoGUI.openMainMenu(player);
                activeGUI.setMenuSubType(null);
                break;
        }
    }
}