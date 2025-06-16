package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermissionManager {
    private final DrugCraft plugin;
    private Permission vaultPermission;

    public PermissionManager(DrugCraft plugin) {
        this.plugin = plugin;
        setupVaultPermissions();
    }

    private void setupVaultPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            vaultPermission = rsp.getProvider();
            plugin.getLogger().info("Vault permissions hooked successfully");
        } else {
            plugin.getLogger().warning("Vault permissions not found, falling back to default permission checks");
        }
    }

    public boolean hasPermission(Player player, String permission) {
        if (vaultPermission != null) {
            return vaultPermission.has(player, permission);
        }
        return player.hasPermission(permission);
    }

    public void addPermission(Player player, String permission) {
        if (vaultPermission != null) {
            vaultPermission.playerAdd(null, player, permission);
        } else {
            plugin.getLogger().warning("Cannot add permission " + permission + " to " + player.getName() + " without Vault permissions");
        }
    }

    public void removePermission(Player player, String permission) {
        if (vaultPermission != null) {
            vaultPermission.playerRemove(null, player, permission);
        } else {
            plugin.getLogger().warning("Cannot remove permission " + permission + " from " + player.getName() + " without Vault permissions");
        }
    }

    public boolean isPolice(Player player) {
        return hasPermission(player, "drugcraft.police");
    }
}