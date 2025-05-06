package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
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

    public PoliceManager(DrugCraft plugin, DrugManager drugManager, EconomyManager economyManager, PermissionManager permissionManager, CartelManager cartelManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economyManager = economyManager;
        this.permissionManager = permissionManager;
        this.cartelManager = cartelManager;
        this.logger = plugin.getLogger();
        this.npcRegistry = CitizensAPI.getNPCRegistry();
        startPatrolTask();
    }

    public void spawnPoliceNPC(Location location) {
        for (NPC existing : policeNPCs) {
            if (existing.isSpawned() && existing.getEntity().getLocation().distanceSquared(location) < 1) {
                logger.warning("NPC already exists at " + location + ", skipping spawn");
                return;
            }
        }
        NPC npc = npcRegistry.createNPC(EntityType.VILLAGER, "Police Officer");
        npc.spawn(location);
        policeNPCs.add(npc);
        logger.info("Spawned police NPC at " + location + " with ID " + npc.getId());
    }

    public void cleanupNPCs() {
        for (NPC npc : policeNPCs) {
            if (npc.isSpawned()) {
                npc.despawn();
                npc.destroy();
                logger.info("Cleaned up police NPC with ID " + npc.getId());
            }
        }
        policeNPCs.clear();
    }

    public void detectIllegalActivity(Player player, Location location, boolean isInventoryCheck) {
        if (!permissionManager.hasPermission(player, "drugcraft.bypass.police")) {
            String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
            double policeReduction = cartelName != null ? cartelManager.getCartel(cartelName).getPoliceReduction() : 0.0;
            if (Math.random() < policeReduction) {
                logger.info("Police detection skipped for " + player.getName() + " due to cartel upgrade (reduction: " + policeReduction + ")");
                return;
            }
            for (NPC npc : policeNPCs) {
                if (npc.isSpawned() && npc.getEntity().getLocation().getWorld().equals(location.getWorld()) &&
                        npc.getEntity().getLocation().distanceSquared(location) < 400) { // ~20 blocks
                    logger.info("Police NPC " + npc.getId() + " detected " + (isInventoryCheck ? "inventory" : "activity") + " by " + player.getName() + " at " + location);
                    applyConsequence(player, isInventoryCheck);
                    break;
                }
            }
        } else {
            logger.fine("Player " + player.getName() + " bypassed police detection at " + location);
        }
    }

    private void applyConsequence(Player player, boolean isInventoryCheck) {
        if (!economyManager.isEconomyAvailable()) {
            player.sendMessage(MessageUtils.color("{#FF5555}Economy system is not available, police cannot fine you!"));
            logger.warning("Economy not available for police consequence to " + player.getName());
            return;
        }
        Economy economy = economyManager.getEconomy();
        if (isInventoryCheck) {
            boolean hasDrugs = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && drugManager.isDrugItem(item)) {
                    hasDrugs = true;
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().remove(item);
                    }
                    player.sendMessage(MessageUtils.color("{#FF5555}Police confiscated one drug item!"));
                    logger.info("Confiscated one drug item from " + player.getName());
                    break;
                }
            }
            if (!hasDrugs) {
                logger.fine("No drugs found in " + player.getName() + "'s inventory");
                return;
            }
        }
        double fine = 500.0;
        if (economy.has(player, fine)) {
            economy.withdrawPlayer(player, fine);
            player.sendMessage(MessageUtils.color("{#FF5555}You were fined $500 by police for illegal activity!"));
            logger.info("Fined player " + player.getName() + " $500 for illegal activity");
        } else {
            player.teleport(new Location(player.getWorld(), 0, 64, 0));
            player.sendMessage(MessageUtils.color("{#FF5555}You were jailed for illegal activity due to insufficient funds!"));
            logger.info("Jailed player " + player.getName() + " for illegal activity");
        }
    }

    private void startPatrolTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc : policeNPCs) {
                    if (npc.isSpawned()) {
                        Location current = npc.getEntity().getLocation();
                        Location target = current.clone().add(
                                (Math.random() - 0.5) * 20,
                                0,
                                (Math.random() - 0.5) * 20
                        );
                        npc.getNavigator().setTarget(target);
                        for (Player player : current.getWorld().getPlayers()) {
                            if (player.getLocation().distanceSquared(current) < 400 &&
                                    !permissionManager.hasPermission(player, "drugcraft.bypass.police")) {
                                detectIllegalActivity(player, player.getLocation(), true);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }
}