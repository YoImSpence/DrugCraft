package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.casino.CasinoManager;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.CartelGUI;
import com.spence.drugcraft.gui.CasinoGUI;
import com.spence.drugcraft.handlers.CasinoGUIHandler;
import com.spence.drugcraft.handlers.CartelGUIHandler;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {
    private final DrugCraft plugin;

    public AsyncPlayerChatListener(DrugCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.isAwaitingChatInput()) return;

        event.setCancelled(true);
        String message = event.getMessage();
        String chatAction = activeGUI.getChatAction();
        activeGUI.setAwaitingChatInput(false);
        activeGUI.setChatAction(null);

        if (chatAction == null) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (chatAction.startsWith("casino-bet_")) {
                String gameType = chatAction.substring(11);
                try {
                    double bet = Double.parseDouble(message);
                    if (bet <= 0) throw new NumberFormatException();
                    CasinoGUIHandler handler = new CasinoGUIHandler(plugin, new CasinoGUI(plugin, new CasinoManager(plugin)), new CasinoManager(plugin));
                    handler.handleBetInput(player, gameType, bet);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, "casino.invalid-bet");
                    new CasinoGUI(plugin, new CasinoManager(plugin)).openMainMenu(player);
                }
            } else if (chatAction.equals("roulette-number-bet")) {
                try {
                    int number = Integer.parseInt(message);
                    if (number < 0 || number > 36) throw new NumberFormatException();
                    CasinoGUIHandler handler = new CasinoGUIHandler(plugin, new CasinoGUI(plugin, new CasinoManager(plugin)), new CasinoManager(plugin));
                    handler.handleRouletteNumberBet(player, number);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(player, "casino.invalid-roulette-number");
                    new CasinoGUI(plugin, new CasinoManager(plugin)).openMainMenu(player);
                }
            } else if (chatAction.equals("cartel-create")) {
                CartelGUIHandler handler = new CartelGUIHandler(plugin, new CartelGUI(plugin, plugin.getCartelManager()), plugin.getCartelManager(), new EconomyManager(null));
                handler.handleCartelNameInput(player, message);
            }
        });
    }
}