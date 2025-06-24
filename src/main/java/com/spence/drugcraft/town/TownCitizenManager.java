package com.spence.drugcraft.town;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
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
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.logging.Logger;

public class TownCitizenManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Logger logger = Logger.getLogger(TownCitizenManager.class.getName());

    public TownCitizenManager(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        Bukkit.getPluginManager().registerEvents(new TownCitizenListener(), plugin);
    }

    public void spawnNPCs() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        registry.forEach(npc -> {
            if (npc.hasTrait(TownCitizenTrait.class)) {
                npc.despawn();
                npc.destroy();
            }
        });

        ConfigurationSection citizens = plugin.getConfigManager().getConfig("npcs.yml").getConfigurationSection("town_citizens");
        if (citizens == null) {
            logger.warning("No Town Citizen NPCs defined in npcs.yml");
            return;
        }

        for (String key : citizens.getKeys(false)) {
            ConfigurationSection npcConfig = citizens.getConfigurationSection(key);
            if (npcConfig == null) {
                logger.warning("Invalid configuration for Town Citizen NPC " + key);
                continue;
            }

            String regionName = npcConfig.getString("region", "Town");
            World world = Bukkit.getWorld(npcConfig.getString("world", "Greenfield"));
            if (world == null) {
                logger.warning("Invalid world for Town Citizen NPC " + key + ": " + npcConfig.getString("world"));
                continue;
            }

            Location location = getRandomLocationInRegion(world, regionName);
            if (location == null) {
                logger.warning("No valid location in region " + regionName + " for Town Citizen NPC " + key);
                continue;
            }

            NPC npc = registry.createNPC(EntityType.PLAYER, "Citizen_" + key);
            npc.addTrait(TownCitizenTrait.class);
            if (!npc.spawn(location)) {
                logger.warning("Failed to spawn Town Citizen NPC at " + location);
            } else {
                logger.info("Spawned Town Citizen NPC 'Citizen_" + key + "' at " + location);
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
                    if (region.getId().equalsIgnoreCase(regionName) && region.getFlag(WorldGuard.getInstance().getFlagRegistry().get("npc-traverse")) == null) {
                        canTraverse = true;
                        break;
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

    public class TownCitizenListener implements Listener {
        // Placeholder for citizen interactions
    }

    public static class TownCitizenTrait extends Trait {
        public TownCitizenTrait() {
            super("town_citizen");
        }
    }
}