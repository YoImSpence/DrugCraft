package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class AddictionListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final AddictionManager addictionManager;

    public AddictionListener(DrugCraft plugin, DrugManager drugManager, AddictionManager addictionManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.addictionManager = addictionManager;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!drugManager.isDrugItem(item)) {
            return;
        }
        String drugId = drugManager.getDrugIdFromItem(item);
        String quality = drugManager.getQualityFromItem(item);
        Drug drug = drugManager.getDrug(drugId);
        if (drug == null) {
            plugin.getLogger().warning("Consumed unknown drug: " + drugId + " by player " + player.getName());
            return;
        }
        addictionManager.addAddiction(player, drugId, quality);
        MessageUtils.sendMessage(player, "addiction.consumed", "drug", drug.getName(), "quality", quality);
        plugin.getLogger().info("Player " + player.getName() + " consumed " + drugId + " (" + quality + ")");
    }
}