package com.spence.drugcraft.town;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class TownCitizenManager {
    private final DrugCraft plugin;
    private final Map<UUID, AcceptedDeal> acceptedDeals = new HashMap<>();
    private final Map<Integer, NPC> townCitizens = new HashMap<>();
    private final Random random = new Random();

    public TownCitizenManager(DrugCraft plugin) {
        this.plugin = plugin;
        loadCitizens();
        startPatrolTask();
        startDealInitiationTask();
    }

    private void loadCitizens() {
        File configFile = new File(plugin.getDataFolder(), "town.yml");
        if (!configFile.exists()) {
            plugin.saveResource("town.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<String> spawnLocations = config.getStringList("spawn_locations");
        List<String> citizenNames = config.getStringList("citizen_names");
        List<String> citizenSkins = config.getStringList("citizen_skins");

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        for (String locStr : spawnLocations) {
            String[] parts = locStr.split(",");
            if (parts.length != 5) continue;
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;
            Location location = new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), 0);
            String name = citizenNames.get(random.nextInt(citizenNames.size()));
            NPC npc = registry.createNPC(EntityType.PLAYER, name);
            npc.spawn(location);
            String skin = citizenSkins.get(random.nextInt(citizenSkins.size()));
            npc.getOrAddTrait(SkinTrait.class).setSkinName(skin);
            townCitizens.put(npc.getId(), npc);
            plugin.getLogger().info("Spawned citizen " + name + " (ID: " + npc.getId() + ") at " + location);
        }
    }

    public void cleanupCitizens() {
        for (NPC npc : townCitizens.values()) {
            if (npc.getName().equals("Dealer John")) {
                npc.destroy();
            }
        }
        townCitizens.clear();
        plugin.getLogger().info("Cleaned up town citizens");
    }

    private void startPatrolTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NPC npc : townCitizens.values()) {
                    if (!npc.isSpawned()) continue;
                    Location currentLocation = npc.getStoredLocation();
                    if (currentLocation == null) continue;
                    List<Location> nearbyLocations = getNearbyLocations(currentLocation, 10, 3);
                    if (nearbyLocations.isEmpty()) continue;
                    Location target = nearbyLocations.get(random.nextInt(nearbyLocations.size()));
                    npc.getNavigator().setTarget(target);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 30);
    }

    private List<Location> getNearbyLocations(Location center, int horizontalRange, int verticalRange) {
        List<Location> locations = new ArrayList<>();
        World world = center.getWorld();
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
        World world = start.getWorld();
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

    public void completeDeal(Player player, int citizenId) {
        AcceptedDeal deal = acceptedDeals.remove(player.getUniqueId());
        if (deal != null && deal.getCitizenId() == citizenId) {
            plugin.getLogger().info("Completed deal for player " + player.getName() + " with citizen ID " + citizenId);
            plugin.getDataManager().addXP(player.getUniqueId(), deal.getDrugId(), 100);
            MessageUtils.sendMessage(player, "town-citizen.deal-completed-xp", "xp", "100", "drug_id", deal.getDrugId());
        }
    }

    private void startDealInitiationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, AcceptedDeal> entry : acceptedDeals.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null) continue;
                    AcceptedDeal deal = entry.getValue();
                    NPC npc = townCitizens.get(deal.getCitizenId());
                    if (npc == null || !npc.isSpawned()) {
                        MessageUtils.sendMessage(player, "deal.npc-unavailable");
                        acceptedDeals.remove(entry.getKey());
                        continue;
                    }
                    Location meetupSpot = deal.getMeetupSpots().get(random.nextInt(deal.getMeetupSpots().size()));
                    npc.getNavigator().setTarget(meetupSpot);
                    plugin.getDealRequestGUI().openAcceptDenyMenu(player, deal.getCitizenId(), deal.getItem(), deal.getQuantity(), deal.getPrice(), deal.getMeetupSpots());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 60);
    }

    public void initiateDeal(Player player, int npcId, ItemStack item, int quantity, double price, List<Location> meetupSpots) {
        acceptedDeals.put(player.getUniqueId(), new AcceptedDeal(npcId, item, quantity, price, meetupSpots, drugManager.getDrugIdFromItem(item)));
    }

    public List<Location> getMeetupSpots() {
        File configFile = new File(plugin.getDataFolder(), "town.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<String> meetupSpots = config.getStringList("meetup_spots");
        List<Location> locations = new ArrayList<>();
        for (String locStr : meetupSpots) {
            String[] parts = locStr.split(",");
            if (parts.length != 5) continue;
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;
            Location location = new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), 0);
            locations.add(location);
        }
        return locations;
    }

    private record AcceptedDeal(int citizenId, ItemStack item, int quantity, double price, List<Location> meetupSpots, String drugId) {
        public int getCitizenId() { return citizenId; }
        public ItemStack getItem() { return item; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public List<Location> getMeetupSpots() { return meetupSpots; }
        public String getDrugId() { return drugId; }
    }
}