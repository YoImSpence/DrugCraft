package com.spence.drugcraft.police;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.listeners.PoliceListener;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.utils.PermissionManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.EntityType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class PoliceManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final EconomyManager economyManager;
    private final PermissionManager permissionManager;
    private final CartelManager cartelManager;
    private final Logger logger;
    private final List<NPC> policeNPCs = new ArrayList<>();
    private final NPCRegistry npcRegistry;
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 30000;

    public PoliceManager(DrugCraft plugin, DrugManager drugManager, EconomyManager economyManager,
                         PermissionManager permissionManager, CartelManager cartelManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economyManager = economyManager;
        this.permissionManager = permissionManager;
        this.cartelManager = cartelManager;
        this.logger = plugin.getLogger();
        this.npcRegistry = CitizensAPI.getNPCRegistry();
        startPatrolTask();
    }

    public void spawnPoliceNPC(Location location, String name) {
        if (location == null || location.getWorld() == null) {
            logger.warning("Invalid location for spawning police NPC: " + location);
            return;
        }
        for (NPC existing : policeNPCs) {
            if (existing.isSpawned() && existing.getEntity().getLocation().distanceSquared(location) < 1) {
                logger.warning("NPC already exists at " + location + ", skipping spawn");
                return;
            }
        }
        NPC npc = npcRegistry.createNPC(EntityType.PLAYER, name);
        npc.setName("Officer");
        npc.setAlwaysUseNameHologram(false);
        try {
            npc.spawn(location);
            policeNPCs.add(npc);
            logger.info("Spawned police NPC at " + location + " with ID " + npc.getId());
        } catch (Exception e) {
            logger.severe("Failed to spawn police NPC at " + location + ": " + e.getMessage());
        }
    }

    public void cleanupNPCs() {
        for (NPC npc : new ArrayList<>(policeNPCs)) {
            if (npc != null && npc.isSpawned()) {
                npc.despawn();
                npc.destroy();
                logger.info("Cleaned up police NPC with ID " + npc.getId());
            }
        }
        policeNPCs.clear();
        playerCooldowns.clear();
    }

    public void detectIllegalActivity(Player player, Location location, boolean isInventoryCheck) {
        if (player == null || location == null || location.getWorld() == null) {
            logger.warning("Null player or location in detectIllegalActivity: player=" + player + ", location=" + location);
            return;
        }
        if (!permissionManager.hasPermission(player, "drugcraft.bypass.police")) {
            long currentTime = System.currentTimeMillis();
            Long lastDetection = playerCooldowns.get(player.getUniqueId());
            if (lastDetection != null && (currentTime - lastDetection) < COOLDOWN_MS) {
                logger.fine("Player " + player.getName() + " is on police detection cooldown");
                return;
            }
            Long lastAction = PoliceListener.getRecentDrugAction(player.getUniqueId());
            if (lastAction == null || (currentTime - lastAction) > 10000) {
                logger.fine("No recent drug-related action for " + player.getName());
                return;
            }
            String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
            double policeReduction = cartelName != null ? cartelManager.getCartel(cartelName).getPoliceReduction() : 0.0;
            if (Math.random() < policeReduction) {
                logger.info("Police detection skipped for " + player.getName() + " due to cartel upgrade (reduction: " + policeReduction + ")");
                return;
            }
            for (NPC npc : new ArrayList<>(policeNPCs)) {
                if (npc == null || !npc.isSpawned() || npc.getEntity() == null) {
                    logger.warning("Police NPC is null or not spawned");
                    continue;
                }
                if (npc.getName() == null) {
                    logger.warning("Police NPC ID " + npc.getId() + " has null name");
                    continue;
                }
                Location npcLoc = npc.getEntity().getLocation();
                if (npc.getName().equalsIgnoreCase("Officer") &&
                        npcLoc.getWorld() != null &&
                        npcLoc.getWorld().equals(location.getWorld()) &&
                        npcLoc.distanceSquared(location) < 400) {
                    Vector start = npcLoc.clone().add(0, 1.6, 0).toVector();
                    Vector end = location.clone().add(0, 1.6, 0).toVector();
                    Vector direction = end.subtract(start).normalize();
                    RayTraceResult result = location.getWorld().rayTraceBlocks(start.toLocation(location.getWorld()), direction, 20);
                    if (result == null || result.getHitBlock() == null) {
                        logger.info("Police NPC " + npc.getId() + " detected " + (isInventoryCheck ? "inventory" : "activity") + " by " + player.getName() + " at " + location);
                        applyConsequence(player, isInventoryCheck);
                        playerCooldowns.put(player.getUniqueId(), currentTime);
                        PoliceListener.clearRecentDrugAction(player.getUniqueId());
                        break;
                    } else {
                        logger.fine("Police NPC " + npc.getId() + " has no line of sight to " + player.getName() + " at " + location);
                    }
                }
            }
        } else {
            logger.fine("Player " + player.getName() + " bypassed police detection at " + location);
        }
    }

    private void applyConsequence(Player player, boolean isInventoryCheck) {
        if (!economyManager.isEconomyAvailable()) {
            player.sendMessage(MessageUtils.color("&#FF4040Economy system is not available, police cannot fine you!"));
            logger.severe("Economy not available for police consequence to " + player.getName());
            return;
        }
        Economy economy = economyManager.getEconomy();
        logger.fine("Applying consequence to " + player.getName() + ", inventory check: " + isInventoryCheck);

        boolean hasIllegalItem = false;
        if (isInventoryCheck) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && drugManager.isDrugItem(item)) {
                    hasIllegalItem = true;
                    break;
                }
            }
        }
        if (!hasIllegalItem && isInventoryCheck) {
            logger.fine("No illegal items found in " + player.getName() + "'s inventory, skipping consequence");
            return;
        }

        boolean confiscated = false;
        if (isInventoryCheck) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && drugManager.isDrugItem(item)) {
                    int amount = item.getAmount();
                    String itemName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
                    if (amount > 1) {
                        item.setAmount(amount - 1);
                    } else {
                        player.getInventory().remove(item);
                    }
                    confiscated = true;
                    player.sendMessage(MessageUtils.color("&#FF4040Police confiscated one " + itemName + "!"));
                    logger.info("Confiscated one " + itemName + " from " + player.getName());
                    break;
                }
            }
        }

        if (confiscated || !isInventoryCheck) {
            double fine = 500.0;
            try {
                if (economy.has(player, fine)) {
                    economy.withdrawPlayer(player, fine);
                    player.sendMessage(MessageUtils.color("&#FF4040You were fined $500 by police for illegal activity!"));
                    logger.info("Fined player " + player.getName() + " $500 for illegal activity");
                } else {
                    Location jailLocation = new Location(player.getWorld(), 0, 100, 0);
                    while (!jailLocation.getBlock().isPassable() || !jailLocation.clone().add(0, 1, 0).getBlock().isPassable()) {
                        jailLocation = jailLocation.clone().add(0, 1, 0);
                    }
                    player.teleport(jailLocation);
                    player.sendMessage(MessageUtils.color("&#FF4040You were jailed for illegal activity due to insufficient funds!"));
                    logger.info("Jailed player " + player.getName() + " at " + jailLocation + " for illegal activity");
                }
            } catch (Exception e) {
                logger.severe("Failed to apply fine or jail to " + player.getName() + ": " + e.getMessage());
                player.sendMessage(MessageUtils.color("&#FF4040Police action failed due to an error!"));
            }
        }
    }

    private void startPatrolTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc : new ArrayList<>(policeNPCs)) {
                    if (npc == null || !npc.isSpawned() || npc.getEntity() == null) {
                        logger.warning("Police NPC is null, not spawned, or has no entity");
                        continue;
                    }
                    if (!npc.getName().equalsIgnoreCase("Officer")) continue;
                    Location current = npc.getEntity().getLocation();
                    if (current.getWorld() == null) {
                        logger.warning("Police NPC ID " + npc.getId() + " is in an invalid world");
                        continue;
                    }
                    Location target = current.clone().add(
                            (Math.random() - 0.5) * 20,
                            0,
                            (Math.random() - 0.5) * 20
                    );
                    try {
                        npc.getNavigator().setTarget(target);
                    } catch (Exception e) {
                        logger.warning("Failed to set navigation target for NPC ID " + npc.getId() + ": " + e.getMessage());
                    }
                    for (Player player : current.getWorld().getPlayers()) {
                        if (player.getLocation().distanceSquared(current) < 400 &&
                                !permissionManager.hasPermission(player, "drugcraft.bypass.police")) {
                            detectIllegalActivity(player, player.getLocation(), true);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }
}