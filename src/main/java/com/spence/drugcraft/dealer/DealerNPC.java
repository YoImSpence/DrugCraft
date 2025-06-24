package com.spence.drugcraft.dealer;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.DealerGUI;
import com.spence.drugcraft.utils.MessageUtils;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.logging.Logger;

public class DealerNPC {
    private final DrugCraft plugin;
    private final Logger logger = Logger.getLogger(DealerNPC.class.getName());
    private NPC dealerNPC;

    public DealerNPC(DrugCraft plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new DealerNPCListener(), plugin);
    }

    public void spawnNPCs() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        registry.forEach(npc -> {
            if (npc.hasTrait(DealerTrait.class)) {
                npc.despawn();
                npc.destroy();
            }
        });

        ConfigurationSection dealers = plugin.getConfigManager().getConfig("npcs.yml").getConfigurationSection("dealers");
        if (dealers == null) {
            logger.warning("No Dealer NPCs defined in npcs.yml");
            return;
        }

        ConfigurationSection npcConfig = dealers.getConfigurationSection("dealer_1");
        if (npcConfig == null) {
            logger.warning("Invalid configuration for Dealer NPC dealer_1");
            return;
        }

        String regionName = npcConfig.getString("region", "Town");
        World world = plugin.getServer().getWorld(npcConfig.getString("world", "Greenfield"));
        if (world == null) {
            logger.warning("Invalid world for Dealer NPC dealer_1: " + npcConfig.getString("world"));
            return;
        }

        Location location = getRandomLocationInRegion(world, regionName);
        if (location == null) {
            logger.warning("No valid location in region " + regionName + " for Dealer NPC dealer_1");
            return;
        }

        dealerNPC = registry.createNPC(EntityType.PLAYER, "Dealer Dan");
        dealerNPC.addTrait(DealerTrait.class);
        if (!dealerNPC.spawn(location)) {
            logger.warning("Failed to spawn Dealer NPC at " + location);
        } else {
            logger.info("Spawned Dealer NPC 'Dealer Dan' at " + location);
            startMovement(dealerNPC, regionName);
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

    public class DealerNPCListener implements Listener {
        @EventHandler
        public void onNPCRightClick(NPCRightClickEvent event) {
            if (event.getNPC().hasTrait(DealerTrait.class)) {
                Player player = event.getClicker();
                new DealerGUI(plugin, plugin.getDrugManager()).openMainMenu(player);
            }
        }
    }

    public static class DealerTrait extends Trait {
        public DealerTrait() {
            super("dealer");
        }
    }
}