package com.spence.drugcraft.police;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PoliceLockupManager {
    private final DrugCraft plugin;
    private final Map<UUID, List<ItemStack>> lockupItems = new HashMap<>();

    public PoliceLockupManager(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void storeItems(Player player, List<ItemStack> items) {
        lockupItems.put(player.getUniqueId(), new ArrayList<>(items));
        MessageUtils.sendMessage(player, "police.lockup-stored");
    }

    public void retrieveItems(Player player, Location lockupLocation) {
        // Placeholder: Check if player is at lockup location
        List<ItemStack> items = lockupItems.remove(player.getUniqueId());
        if (items == null || items.isEmpty()) {
            MessageUtils.sendMessage(player, "police.lockup-access-denied");
            return;
        }
        for (ItemStack item : items) {
            if (item != null) {
                player.getInventory().addItem(item);
            }
        }
    }

    public boolean canAccessLockup(Player player, Location location) {
        // Placeholder: Check if location is a valid lockup
        return true;
    }
}