package com.spence.drugcraft.police;

import com.spence.drugcraft.DrugCraft;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.logging.Logger;

public class PoliceNPC {
    private final DrugCraft plugin;
    private final Logger logger = Logger.getLogger(PoliceNPC.class.getName());

    public PoliceNPC(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void spawnNPCs() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        registry.forEach(npc -> {
            if (npc.hasTrait(PoliceTrait.class)) {
                npc.despawn();
                npc.destroy();
            }
        });

        ConfigurationSection police = plugin.getConfigManager().getConfig("npcs.yml").getConfigurationSection("police");
        if (police == null) {
            logger.warning("No Police NPCs defined in npcs.yml");
            return;
        }

        for (String key : police.getKeys(false)) {
            ConfigurationSection npcConfig = police.getConfigurationSection(key);
            if (npcConfig == null) {
                logger.warning("Invalid configuration for Police NPC " + key);
                continue;
            }

            String regionName = npcConfig.getString("region", "police" + key.split("_")[1]);
            World world = Bukkit.getWorld(npcConfig.getString("world", "Greenfield"));
            if (world == null) {
                logger.warning("Invalid world for Police NPC " + key + ": " + npcConfig.getString("world"));
                continue;
            }

            Location location = getRandomLocationInRegion(world, regionName);
            if (location == null) {
                logger.warning("No valid location in region " + regionName + " for Police NPC " + key);
                continue;
            }

            NPC npc = registry.createNPC(EntityType.PLAYER, "Officer_" + key);
            npc.addTrait(PoliceTrait.class);
            if (!npc.spawn(location)) {
                logger.warning("Failed to spawn Police NPC at " + location);
            } else {
                logger.info("Spawned Police NPC 'Officer_" + key + "' at " + location);
                startMovement(npc, regionName);
            }
        }
    }

    private Location getRandomLocationInRegion(World world, String regionName) {
        WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
        ProtectedRegion region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world)).getRegion(regionName);
        if (region == null) return null;

        Random random = new Random();
        double minX = region.getMinimumPoint().getBlockX();
        double maxX = region.getMaximumPoint().getBlockX();
        double minZ = region.getMinimumPoint().getBlockZ();
        double maxZ = region.getMaximumPoint().getBlockZ();
        double y = world.getHighestBlockYAt((int) minX, (int) minZ) + 1;
        double x = minX + (maxX - minX) * random.nextDouble();
        double z = minZ + (maxZ - minZ) * random.nextDouble();
        return new Location(world, x, y, z);
    }

    private void startMovement(NPC npc, String regionName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!npc.isSpawned()) {
                    cancel();
                    return;
                }
                WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
                Location location = npc.getStoredLocation();
                ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld())).getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));
                boolean canTraverse = false;
                for (ProtectedRegion region : regions) {
                    if (region.getId().equalsIgnoreCase("Town")) {
                        Object npcTraverseFlag = WorldGuard.getInstance().getFlagRegistry().get("npc-traverse");
                        if (npcTraverseFlag == null || region.getFlag(WorldGuard.getInstance().getFlagRegistry().get("npc-traverse")) == null) {
                            canTraverse = true;
                            break;
                        }
                    }
                }
                if (canTraverse) {
                    Location target = getRandomLocationInRegion(npc.getStoredLocation().getWorld(), regionName);
                    if (target != null) {
                        npc.getNavigator().setTarget(target);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    public static class PoliceTrait extends Trait {
        public PoliceTrait() {
            super("police");
        }
    }
}