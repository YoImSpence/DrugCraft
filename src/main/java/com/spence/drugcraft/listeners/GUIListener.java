package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.handlers.*;
import com.spence.drugcraft.steeds.SteedManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {
    private final DrugCraft plugin;
    private final AdminGUIHandler adminGUIHandler;
    private final BusinessGUIHandler businessGUIHandler;
    private final CartelGUIHandler cartelGUIHandler;
    private final CasinoGUIHandler casinoGUIHandler;
    private final GamesGUIHandler gamesGUIHandler;
    private final PlayerGUIHandler playerGUIHandler;
    private final SteedGUIHandler steedGUIHandler;

    public GUIListener(DrugCraft plugin) {
        this.plugin = plugin;
        this.adminGUIHandler = new AdminGUIHandler(plugin, plugin.getAdminGUI(), plugin.getDataManager(), plugin.getDrugManager(), plugin.getCartelManager(), plugin.getBusinessManager());
        this.businessGUIHandler = new BusinessGUIHandler(plugin, plugin.getBusinessGUI(), plugin.getBusinessManager(), plugin.getEconomyManager());
        this.cartelGUIHandler = new CartelGUIHandler(plugin, plugin.getCartelGUI(), plugin.getCartelManager(), plugin.getEconomyManager());
        this.casinoGUIHandler = new CasinoGUIHandler(plugin, plugin.getCasinoGUI(), plugin.getCasinoManager());
        this.gamesGUIHandler = new GamesGUIHandler(plugin, plugin.getGamesGUI(), plugin.getNonCasinoGameManager());
        this.playerGUIHandler = new PlayerGUIHandler(plugin, plugin.getPlayerGUI());
        this.steedGUIHandler = new SteedGUIHandler(plugin, plugin.getSteedGUI(), new SteedManager(plugin), plugin.getEconomyManager());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null) return;

        ItemStack item = event.getCurrentItem();
        int slot = event.getSlot();
        Inventory inventory = event.getInventory();

        event.setCancelled(true); // Prevent item removal

        switch (activeGUI.getType()) {
            case "ADMIN":
            case "PLAYER_MANAGE":
            case "PLAYER_OPTIONS":
            case "GIVE_ITEMS":
            case "CARTEL_MANAGE":
            case "CARTEL_DETAILS":
            case "BUSINESS_MANAGE":
            case "WORLD_TP":
                adminGUIHandler.onClick(player, item, slot, inventory);
                break;
            case "BUSINESS":
            case "BUSINESS_BUY":
            case "BUSINESS_UPGRADE":
            case "BUSINESS_STATS":
                businessGUIHandler.onClick(player, item, slot, inventory);
                break;
            case "CARTEL":
            case "CARTEL_CREATE":
            case "CARTEL_INFO":
            case "CARTEL_MEMBERS":
            case "CARTEL_PERMISSIONS":
            case "CARTEL_UPGRADES":
                cartelGUIHandler.onClick(player, item, slot, inventory);
                break;
            case "CASINO":
            case "BACCARAT":
            case "BLACKJACK":
            case "POKER":
            case "ROULETTE":
            case "SLOTS":
                casinoGUIHandler.onClick(player, item, slot, inventory);
                break;
            case "GAMES":
            case "CHESS":
            case "CHECKERS":
            case "CONNECT4":
            case "RPS":
                gamesGUIHandler.onClick(player, item, slot, inventory);
                break;
            case "PLAYER":
            case "LEVELS":
            case "SETTINGS":
                playerGUIHandler.onClick(player, item, slot, inventory);
                break;
            case "VEHICLE":
                NPC npc = CitizensAPI.getNPCRegistry().getNPC(player.getWorld().getNearbyEntities(player.getLocation(), 2, 2, 2)
                        .stream()
                        .filter(e -> e.getType() == EntityType.PLAYER && CitizensAPI.getNPCRegistry().isNPC(e))
                        .findFirst()
                        .orElse(null));
                if (npc != null && npc.getName().startsWith("Steeds")) {
                    steedGUIHandler.onClick(player, item, slot, inventory);
                } else {
                    player.closeInventory();
                }
                break;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI != null) {
            event.setCancelled(true);
        }
    }
}