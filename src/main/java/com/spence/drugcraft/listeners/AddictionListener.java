package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
                event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !drugManager.isDrugItem(item)) return;

        event.setCancelled(true);
        String drugId = drugManager.getDrugIdFromItem(item);
        addictionManager.applyDrugEffect(player, drugId);
        MessageUtils.sendMessage(player, "addiction.applied", "drug_id", drugId, "severity", "0.1"); // Placeholder severity
        item.setAmount(item.getAmount() - 1);
    }
}