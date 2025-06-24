package com.spence.drugcraft.utils;

import org.bukkit.entity.Player;

public class PermissionManager {
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
}