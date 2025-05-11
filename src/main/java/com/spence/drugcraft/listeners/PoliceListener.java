package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PoliceListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final PoliceManager policeManager;
    private static final Map<UUID, Long> recentDrugActions = new HashMap<>();

    public PoliceListener(DrugCraft plugin, DrugManager drugManager, PoliceManager policeManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.policeManager = policeManager;
    }

    @EventHandler
    public void onPlayerHoldItem(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null || !drugManager.isDrugItem(item)) return;

        String drugId = null;
        for (Drug drug : drugManager.getSortedDrugs()) {
            if (item.getType() == drug.getItem(drug.getQuality()).getType() &&
                    item.getItemMeta().getDisplayName().equals(drug.getItem(drug.getQuality()).getItemMeta().getDisplayName())) {
                drugId = drug.getDrugId();
                break;
            }
        }
        if (drugId == null) return;

        recentDrugActions.put(player.getUniqueId(), System.currentTimeMillis());
        if (player.hasPermission("drugcraft.police")) {
            player.sendMessage(MessageUtils.color("&#FF4040You are holding an illegal item: " + drugId + "!"));
        }
        policeManager.detectIllegalActivity(player, player.getLocation(), true);
    }

    public static Long getRecentDrugAction(UUID playerId) {
        return recentDrugActions.get(playerId);
    }

    public static void clearRecentDrugAction(UUID playerId) {
        recentDrugActions.remove(playerId);
    }
}