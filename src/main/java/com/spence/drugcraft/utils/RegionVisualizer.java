package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegionVisualizer {
    private final DrugCraft plugin;

    public RegionVisualizer(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void visualizeRegion(Player player, ProtectedRegion region) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= 200 || !player.isOnline()) {
                    cancel();
                    return;
                }
                BlockVector3 minPoint = region.getMinimumPoint();
                BlockVector3 maxPoint = region.getMaximumPoint();
                Location min = new Location(player.getWorld(), minPoint.x(), minPoint.y(), minPoint.z());
                Location max = new Location(player.getWorld(), maxPoint.x(), maxPoint.y(), maxPoint.z());
                for (double x = min.getX(); x <= max.getX(); x++) {
                    for (double z = min.getZ(); z <= max.getZ(); z++) {
                        if (x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ()) {
                            player.spawnParticle(Particle.FLAME, x, min.getY(), z, 1, 0, 0, 0, 0);
                            player.spawnParticle(Particle.FLAME, x, max.getY(), z, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}