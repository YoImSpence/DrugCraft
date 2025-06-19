package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.handlers.GUIHandler;
import com.spence.drugcraft.town.DealRequest;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {
    private final DrugCraft plugin;
    private final Map<UUID, String> awaitingChatInput = new HashMap<>();

    public GUIListener(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public DrugCraft getPlugin() {
        return plugin;
    }

    public Map<UUID, ActiveGUI> getActiveMenus() {
        return plugin.getActiveMenus();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        event.setCancelled(true);
        GUIHandler handler = getHandler(activeGUI.getGuiType());
        if (handler != null) {
            handler.onClick(player, event.getCurrentItem(), event.getSlot(), event.getInventory());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String inputType = awaitingChatInput.get(playerUUID);
        if (inputType == null) return;

        event.setCancelled(true);
        String input = event.getMessage();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ActiveGUI activeGUI = plugin.getActiveMenus().get(playerUUID);
            if (activeGUI == null) {
                clearAwaitingChatInput(playerUUID);
                return;
            }

            String chatAction = activeGUI.getChatAction();
            if (chatAction == null) {
                clearAwaitingChatInput(playerUUID);
                return;
            }

            switch (chatAction) {
                case "cartel-create":
                    plugin.getCartelGUIHandler().handlePermissionInput(player, input);
                    break;
                case "cartel-permission":
                    plugin.getCartelGUIHandler().handlePermissionInput(player, input);
                    break;
                case "admin-quantity":
                    plugin.getAdminGUIHandler().handleQuantityInput(player, input);
                    break;
                case "admin-xp":
                    plugin.getAdminGUIHandler().handleXPInput(player, input);
                    break;
                case "deal-request":
                    DealRequestGUI dealRequestGUI = plugin.getDealRequestGUI();
                    DealRequest dealRequest = dealRequestGUI.getCurrentDeal(player);
                    if (dealRequest != null) {
                        MessageUtils.sendMessage(player, "deal.accepted", "npc_name", dealRequest.getNpcName());
                    }
                    break;
                default:
                    MessageUtils.sendMessage(player, "general.invalid-input");
            }

            clearAwaitingChatInput(playerUUID);
            activeGUI.setAwaitingChatInput(false);
            activeGUI.setChatAction(null);
        });
    }

    private GUIHandler getHandler(String guiType) {
        switch (guiType) {
            case "ADMIN":
                return plugin.getAdminGUIHandler();
            case "CARTEL":
                return plugin.getCartelGUIHandler();
            case "CASINO":
                return plugin.getCasinoGUIHandler();
            case "DEALER":
                return plugin.getDealerGUIHandler();
            case "GAMES":
                return plugin.getGamesGUIHandler();
            case "LEVELS":
                return plugin.getLevelsGUIHandler();
            case "VEHICLE":
                return plugin.getVehicleGUIHandler();
            case "BUSINESS_MACHINE":
                return plugin.getBusinessMachineGUIHandler();
            case "HEIST":
                return plugin.getHeistGUIHandler();
            default:
                return null;
        }
    }

    public void setAwaitingChatInput(UUID playerUUID, String inputType) {
        awaitingChatInput.put(playerUUID, inputType);
    }

    public void clearAwaitingChatInput(UUID playerUUID) {
        awaitingChatInput.remove(playerUUID);
    }
}