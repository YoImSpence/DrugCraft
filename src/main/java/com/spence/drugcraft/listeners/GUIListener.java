package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.*;
import com.spence.drugcraft.town.DealRequestGUI;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class GUIListener implements Listener {
    private final DrugCraft plugin;
    private final AdminGUIHandler adminGUIHandler;
    private final CartelGUIHandler cartelGUIHandler;
    private final BusinessMachineGUIHandler businessMachineGUIHandler;
    private final VehicleGUIHandler vehicleGUIHandler;
    private final CasinoGUIHandler casinoGUIHandler;
    private final PlayerLevelsGUIHandler levelsGUIHandler;
    private final GamesGUIHandler gamesGUIHandler;
    private final DealerGUIHandler dealerGUIHandler;

    public GUIListener(DrugCraft plugin) {
        this.plugin = plugin;
        this.adminGUIHandler = new AdminGUIHandler(plugin, plugin.getAdminGUI(), plugin.getDataManager());
        this.cartelGUIHandler = new CartelGUIHandler(plugin, plugin.getCartelGUI(), plugin.getCartelManager());
        this.businessMachineGUIHandler = plugin.getBusinessMachineGUIHandler();
        this.vehicleGUIHandler = new VehicleGUIHandler(plugin, plugin.getVehicleManager(), plugin.getVehicleManager(), plugin.getEconomyManager());
        this.casinoGUIHandler = new CasinoGUIHandler(plugin, plugin.getCasinoGUI(), plugin.getCasinoManager(), plugin.getEconomyManager());
        this.levelsGUIHandler = new PlayerLevelsGUIHandler(plugin, plugin.getLevelsGUI(), plugin.getDataManager());
        this.gamesGUIHandler = new GamesGUIHandler(plugin, plugin.getGameManager());
        this.dealerGUIHandler = new DealerGUIHandler(plugin, plugin.getDealerGUI(), plugin.getDrugManager(), plugin.getEconomyManager());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        String guiType = activeGUI.getGuiType();
        switch (guiType) {
            case "ADMIN":
                adminGUIHandler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
                break;
            case "CARTEL":
                cartelGUIHandler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
                break;
            case "BUSINESS_MACHINE":
                businessMachineGUIHandler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
                break;
            case "VEHICLE":
                vehicleGUIHandler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
                break;
            case "CASINO":
                casinoGUIHandler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
                break;
            case "LEVELS":
                levelsGUIHandler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
                break;
            case "GAMES":
                gamesGUIHandler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
                break;
            case "DEALER":
                dealerGUIHandler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
                break;
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.isAwaitingChatInput()) return;

        event.setCancelled(true);
        String input = event.getMessage().trim();
        String action = activeGUI.getChatAction();

        if (input.equalsIgnoreCase("cancel")) {
            activeGUI.setAwaitingChatInput(false);
            activeGUI.setChatAction(null);
            MessageUtils.sendMessage(player, "general.error", "Action cancelled");
            return;
        }

        if (action != null) {
            switch (action) {
                case "select-player":
                    plugin.getGameManager().handlePlayerInput(player, input);
                    break;
                case "cartel-create":
                    plugin.getCartelManager().createCartel(player, input);
                    break;
                case "cartel-permission":
                    plugin.getCartelGUIHandler().handlePermissionInput(player, input);
                    break;
                case "admin-xp":
                    plugin.getAdminGUIHandler().handleXPInput(player, input);
                    break;
            }
            activeGUI.setAwaitingChatInput(false);
            activeGUI.setChatAction(null);
        }
    }

    public void setAwaitingChatInput(UUID playerUUID, String action, DealRequestGUI.DealRequest dealRequest) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(playerUUID);
        if (activeGUI != null) {
            activeGUI.setAwaitingChatInput(true);
            activeGUI.setChatAction(action);
            activeGUI.setDealRequest(dealRequest);
        }
    }

    public void setAwaitingChatInput(UUID playerUUID, String action, DealerGUIHandler.PurchaseRequest purchaseRequest) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(playerUUID);
        if (activeGUI != null) {
            activeGUI.setAwaitingChatInput(true);
            activeGUI.setChatAction(action);
            activeGUI.setPurchaseRequest(purchaseRequest);
        }
    }

    public void setAwaitingChatInput(UUID playerUUID, String action) {
        ActiveGUI activeGUI = plugin.getActiveMenus().get(playerUUID);
        if (activeGUI != null) {
            activeGUI.setAwaitingChatInput(true);
            activeGUI.setChatAction(action);
        }
    }
}