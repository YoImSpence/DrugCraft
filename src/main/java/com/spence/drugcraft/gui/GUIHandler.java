package com.spence.drugcraft.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface GUIHandler {
    void onClick(Player player, ItemStack item, int slot, Inventory inventory);
}