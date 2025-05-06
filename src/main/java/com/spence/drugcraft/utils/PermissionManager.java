package com.spence.drugcraft.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class PermissionManager {
    private final JavaPlugin plugin;
    private final CartelManager cartelManager;

    public PermissionManager(JavaPlugin plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    public boolean hasPermission(Player player, String permission) {
        if (player.hasPermission(permission)) {
            return true;
        }
        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName != null) {
            CartelManager.Cartel cartel = cartelManager.getCartel(cartelName);
            Map<String, Boolean> permissions = cartel.getPermissions().getOrDefault(player.getUniqueId(), new HashMap<>());
            switch (permission) {
                case "drugcraft.cartel.harvest":
                    return permissions.getOrDefault("Harvest Crops", false);
                case "drugcraft.cartel.plant":
                    return permissions.getOrDefault("Plant Crops", false);
                case "drugcraft.cartel.stash":
                    return permissions.getOrDefault("Access Stash", false);
            }
        }
        return false;
    }
}