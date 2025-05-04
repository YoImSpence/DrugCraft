package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.entity.Player;

public class PermissionManager {
    private final DrugCraft plugin;

    public PermissionManager(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
}