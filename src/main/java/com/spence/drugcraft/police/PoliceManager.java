package com.spence.drugcraft.police;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.utils.PermissionManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class PoliceManager implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Economy economy;
    private final PermissionManager permissionManager;
    private final CartelManager cartelManager;
    private final File configFile;
    private FileConfiguration config;
    private final Map<Integer, PoliceNPC> policeNPCs = new HashMap<>();
    private final Random random = new Random();

    public PoliceManager(DrugCraft plugin, DrugManager drugManager, Economy economy, PermissionManager permissionManager, CartelManager cartelManager, PoliceConfig policeConfig) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economy = economy;
        this.permissionManager = permissionManager;
        this.cartelManager = cartelManager;
        this.configFile = policeConfig.getConfigFile();
        this.config = policeConfig.getConfig();
        loadPoliceNPCs();
        startPatrolTask();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadPoliceNPCs() {
        ConfigurationSection policeSection = config.getConfigurationSection("police_npcs");
        if (policeSection == null) {
            plugin.getLogger().warning("No 'police_npcs' section found in police.yml; spawning default police NPCs");
            spawnDefaultPoliceNPCs();
            return;
        }
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        for (String key : policeSection.getKeys(false)) {
            ConfigurationSection npcSection = policeSection.getConfigurationSection(key);
            if (npcSection == null) continue;
            int npcId;
            try {
                npcId = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid NPC ID format: " + key + "; skipping");
                continue;
            }
            String name = npcSection.getString("name", "Police Officer");
            ConfigurationSection locSection = npcSection.getConfigurationSection("location");
            if (locSection == null) continue;
            String worldName = locSection.getString("world");
            if (worldName == null) continue;
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world == null) continue;
            double x = locSection.getDouble("x");
            double y = locSection.getDouble("y");
            double z = locSection.getDouble("z");
            float yaw = (float) locSection.getDouble("yaw");
            float pitch = (float) locSection.getDouble("pitch");
            Location location = new Location(world, x, y, z, yaw, pitch);
            NPC npc = registry.getById(npcId);
            if (npc == null) {
                npc = registry.createNPC(EntityType.PLAYER, name);
                if (npc.getId() != npcId) {
                    plugin.getLogger().warning("NPC ID mismatch for " + name + "; expected " + npcId + ", got " + npc.getId() + "; updating ID");
                    policeSection.set(String.valueOf(npcId), null);
                    npcId = npc.getId();
                }
            }
            if (!npc.isSpawned()) {
                npc.spawn(location);
                plugin.getLogger().info("Spawned police NPC " + name + " (ID: " + npcId + ") at " + location);
            } else {
                npc.teleport(location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
                plugin.getLogger().info("Teleported police NPC " + name + " (ID: " + npcId + ") to " + location);
            }
            PoliceNPC policeNPC = new PoliceNPC(npc);
            policeNPCs.put(npcId, policeNPC);
        }
        plugin.getLogger().info("Loaded " + policeNPCs.size() + " police NPCs");
        if (policeNPCs.isEmpty()) {
            spawnDefaultPoliceNPCs();
        }
    }

    private void spawnDefaultPoliceNPCs() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        org.bukkit.World world = org.bukkit.Bukkit.getWorld("Greenfield");
        if (world == null) {
            plugin.getLogger().warning("World 'Greenfield' not found; cannot spawn default police NPCs");
            return;
        }
        List<Location> defaultLocations = Arrays.asList(
                new Location(world, 2200, 70, 900, 0, 0),
                new Location(world, 2205, 70, 905, 90, 0),
                new Location(world, 2195, 70, 895, 180, 0),
                new Location(world, 2210, 70, 910, 270, 0)
        );
        for (Location location : defaultLocations) {
            NPC npc = registry.createNPC(EntityType.PLAYER, "Police Officer");
            npc.spawn(location);
            PoliceNPC policeNPC = new PoliceNPC(npc);
            policeNPCs.put(npc.getId(), policeNPC);
            plugin.getLogger().info("Spawned default police NPC " + npc.getName() + " (ID: " + npc.getId() + ") at " + location);
        }
        savePoliceNPCs();
    }

    public void savePoliceNPCs() {
        ConfigurationSection policeSection = config.createSection("police_npcs");
        for (Map.Entry<Integer, PoliceNPC> entry : policeNPCs.entrySet()) {
            PoliceNPC policeNPC = entry.getValue();
            NPC npc = policeNPC.getNPC();
            ConfigurationSection npcSection = policeSection.createSection(String.valueOf(npc.getId()));
            npcSection.set("name", npc.getName());
            Location location = npc.getStoredLocation();
            if (location != null) {
                ConfigurationSection locSection = npcSection.createSection("location");
                locSection.set("world", location.getWorld().getName());
                locSection.set("x", location.getX());
                locSection.set("y", location.getY());
                locSection.set("z", location.getZ());
                locSection.set("yaw", location.getYaw());
                locSection.set("pitch", location.getPitch());
            }
        }
        try {
            config.save(configFile);
            plugin.getLogger().info("Saved " + policeNPCs.size() + " police NPCs");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save police.yml: " + e.getMessage());
        }
    }

    private void startPatrolTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (PoliceNPC policeNPC : policeNPCs.values()) {
                    if (!policeNPC.getNPC().isSpawned()) {
                        plugin.getLogger().warning("Police NPC " + policeNPC.getNPC().getName() + " (ID: " + policeNPC.getNPC().getId() + ") is not spawned; skipping patrol");
                        continue;
                    }
                    Location currentLocation = policeNPC.getNPC().getStoredLocation();
                    if (currentLocation == null) {
                        plugin.getLogger().warning("Police NPC " + policeNPC.getNPC().getName() + " (ID: " + policeNPC.getNPC().getId() + ") has no stored location; skipping patrol");
                        continue;
                    }
                    List<Location> nearbyLocations = getNearbyLocations(currentLocation, 10, 3);
                    if (nearbyLocations.isEmpty()) {
                        plugin.getLogger().info("No nearby locations found for Police NPC " + policeNPC.getNPC().getName() + " (ID: " + policeNPC.getNPC().getId() + ") at " + currentLocation);
                        continue;
                    }
                    Location target = nearbyLocations.get(random.nextInt(nearbyLocations.size()));
                    try {
                        policeNPC.getNPC().getNavigator().setTarget(target);
                        plugin.getLogger().info("Police NPC " + policeNPC.getNPC().getName() + " (ID: " + policeNPC.getNPC().getId() + ") is patrolling to " + target);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to set patrol target for Police NPC " + policeNPC.getNPC().getName() + " (ID: " + policeNPC.getNPC().getId() + ") to " + target + ": " + e.getMessage());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 30);
    }

    private List<Location> getNearbyLocations(Location center, int horizontalRange, int verticalRange) {
        List<Location> locations = new ArrayList<>();
        org.bukkit.World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = centerX - horizontalRange; x <= centerX + horizontalRange; x++) {
            for (int z = centerZ - horizontalRange; z <= centerZ + horizontalRange; z++) {
                for (int y = centerY - verticalRange; y <= centerY + verticalRange; y++) {
                    org.bukkit.block.Block block = world.getBlockAt(x, y, z);
                    if (block.isPassable() && block.getRelative(org.bukkit.block.BlockFace.UP).isPassable() && !block.getRelative(org.bukkit.block.BlockFace.DOWN).isPassable()) {
                        Location location = new Location(world, x + 0.5, y, z + 0.5);
                        if (isPathable(center, location)) {
                            locations.add(location);
                        }
                    }
                }
            }
        }
        return locations;
    }

    private boolean isPathable(Location start, Location end) {
        double distance = start.distance(end);
        if (distance > 10) return false;
        org.bukkit.World world = start.getWorld();
        double step = 0.5;
        double dx = (end.getX() - start.getX()) / distance;
        double dy = (end.getY() - start.getY()) / distance;
        double dz = (end.getZ() - start.getZ()) / distance;
        for (double d = 0; d <= distance; d += step) {
            double x = start.getX() + dx * d;
            double y = start.getY() + dy * d;
            double z = start.getZ() + dz * d;
            org.bukkit.block.Block block = world.getBlockAt((int) x, (int) y, (int) z);
            if (!block.isPassable()) return false;
        }
        return true;
    }

    public void cleanupNPCs() {
        for (PoliceNPC policeNPC : policeNPCs.values()) {
            policeNPC.getNPC().destroy();
        }
        policeNPCs.clear();
        plugin.getLogger().info("Cleaned up police NPCs");
    }

    public void notifyPolice(Player player, String reason) {
        double fine = calculateFine(reason);
        if (economy != null && economy.isEnabled() && fine > 0) {
            if (economy.has(player, fine)) {
                economy.withdrawPlayer(player, fine);
                MessageUtils.sendMessage(player, "police.fined", "amount", String.format("%.2f", fine));
                plugin.getLogger().info("Player " + player.getName() + " fined $" + fine + " for " + reason);
            } else {
                plugin.getLogger().info("Player " + player.getName() + " does not have enough money for fine $" + fine + " for " + reason);
            }
        }
        String message = "&#FF4040Player " + player.getName() + " detected for " + reason + " at " + player.getLocation();
        String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
        if (cartelName != null) {
            message += " (Cartel: " + cartelName + ")";
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (permissionManager.isPolice(onlinePlayer)) {
                MessageUtils.sendMessage(onlinePlayer, message);
            }
        }
        plugin.getLogger().info("Notified police: " + message);
    }

    private double calculateFine(String reason) {
        return switch (reason.toLowerCase()) {
            case "drug_deal" -> 500.0;
            case "removing grow light" -> 200.0;
            case "attempted crop break" -> 100.0;
            case "addiction increase" -> 50.0;
            default -> 0.0;
        };
    }

    public void addPolice(Player player) {
        permissionManager.addPermission(player, "drugcraft.police");
        plugin.getLogger().info("Added police permission to player " + player.getName());
    }

    public void removePolice(Player player) {
        permissionManager.removePermission(player, "drugcraft.police");
        plugin.getLogger().info("Removed police permission from player " + player.getName());
    }

    public boolean isPolice(Player player) {
        return permissionManager.isPolice(player);
    }

    public static class PoliceNPC {
        private final NPC npc;

        public PoliceNPC(NPC npc) {
            this.npc = npc;
        }

        public NPC getNPC() {
            return npc;
        }
    }

    public static class PoliceConfig {
        private final File configFile;
        private final FileConfiguration config;

        public PoliceConfig(File configFile) {
            this.configFile = configFile;
            if (!configFile.exists()) {
                try {
                    configFile.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.config = YamlConfiguration.loadConfiguration(configFile);
        }

        public File getConfigFile() {
            return configFile;
        }

        public FileConfiguration getConfig() {
            return config;
        }
    }
}