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
        // Register permissions if needed
        plugin.getServer().getPluginManager().addPermission(new org.bukkit.permissions.Permission("drugcraft.cartel.stash"));
        plugin.getServer().getPluginManager().addPermission(new org.bukkit.permissions.Permission("drugcraft.admin"));
        plugin.getServer().getPluginManager().addPermission(new org.bukkit.permissions.Permission("drugcraft.use"));
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
            // Fallback: Cannot dynamically add permissions without Vault
            plugin.getLogger().warning("Cannot add permission " + permission + " to " + player.getName() + " without Vault permissions");
        }
    }

    public void removePermission(Player player, String permission) {
        if (vaultPermission != null) {
            vaultPermission.playerRemove(null, player, permission);
        } else {
            // Fallback: Cannot dynamically remove permissions without Vault
            plugin.getLogger().warning("Cannot remove permission " + permission + " from " + player.getName() + " without Vault permissions");
        }
    }
}