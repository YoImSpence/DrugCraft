package com.spence.drugcraft.dealer;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.MobType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DealerNPC implements Listener {
    private final DrugCraft plugin;
    private final DealerGUI dealerGUI;
    private NPC dealerNPC;
    private final Random random = new Random();

    public DealerNPC(DrugCraft plugin, DealerGUI dealerGUI) {
        this.plugin = plugin;
        this.dealerGUI = dealerGUI;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        spawn();
    }

    public void spawn() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        // Remove existing dealer NPC to prevent duplication
        for (NPC npc : registry) {
            if (npc.getName().equals("Dealer John")) {
                npc.destroy();
                plugin.getLogger().info("Removed existing Dealer NPC (ID: " + npc.getId() + ")");
            }
        }

        File configFile = new File(plugin.getDataFolder(), "town.yml");
        if (!configFile.exists()) {
            plugin.saveResource("town.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<String> spawnLocations = config.getStringList("spawn_locations");
        String dealerSpawn = spawnLocations.stream()
                .filter(loc -> loc.contains("2122,69,715"))
                .findFirst()
                .orElse("Greenfield,2122,69,715,90");
        String[] parts = dealerSpawn.split(",");
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            plugin.getLogger().warning("World 'Greenfield' not found; cannot spawn Dealer NPC");
            return;
        }
        Location location = new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), 0);

        dealerNPC = registry.createNPC(EntityType.VILLAGER, "Dealer John");
        dealerNPC.addTrait(MobType.class);
        dealerNPC.getTrait(MobType.class).setType(EntityType.VILLAGER);
        dealerNPC.spawn(location);
        plugin.getLogger().info("Spawned Dealer NPC at " + location);
        startPatrolTask();
    }

    public void despawn() {
        if (dealerNPC != null) {
            dealerNPC.destroy();
            dealerNPC = null;
            plugin.getLogger().info("Despawned Dealer NPC");
        }
    }

    private void startPatrolTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (dealerNPC == null || !dealerNPC.isSpawned()) return;
                Location currentLocation = dealerNPC.getStoredLocation();
                if (currentLocation == null) return;
                List<Location> waypoints = new ArrayList<>();
                World world = currentLocation.getWorld();
                waypoints.add(new Location(world, 2132, 69, 725));
                waypoints.add(new Location(world, 2112, 69, 705));
                Location target = waypoints.get(random.nextInt(waypoints.size()));
                try {
                    dealerNPC.getNavigator().setTarget(target);
                    dealerNPC.getNavigator().getDefaultParameters().distanceMargin(1.0).pathDistanceMargin(2.0);
                    plugin.getLogger().info("Dealer NPC is patrolling to " + target);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to set patrol target for Dealer NPC: " + e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 30); // Every 30 seconds
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        if (dealerNPC != null && event.getNPC().equals(dealerNPC)) {
            dealerGUI.openMainMenu(event.getClicker());
            MessageUtils.sendMessage(event.getClicker(), "gui.dealer.title-main");
            plugin.getLogger().info("Player " + event.getClicker().getName() + " interacted with Dealer NPC");
        }
    }
}