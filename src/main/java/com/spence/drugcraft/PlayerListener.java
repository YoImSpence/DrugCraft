package com.spence.drugcraft;

import com.spence.drugcraft.drugs.Drug; // Added import
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final DrugCraft plugin;

    public PlayerListener(DrugCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
                event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // Line 35 (fixed)
        Drug drug = plugin.getDrugManager().getDrugByItem(item);
        if (drug != null) {
            event.setCancelled(true);
            item.setAmount(item.getAmount() - 1);
            drug.applyEffect(player);
            plugin.getAddictionManager().addAddiction(player, drug.getAddictionStrength());
            player.sendMessage("§aYou used " + drug.getName() + "!");
        }
    }
}