package com.spence.drugcraft.houses;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class HouseManager {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;
    private final Logger logger;
    private final File housesFile;
    private final FileConfiguration housesConfig;
    private final Map<String, House> houses = new HashMap<>();
    private final Map<UUID, Set<String>> playerHouses = new HashMap<>();

    public HouseManager(DrugCraft plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.logger = plugin.getLogger();
        this.housesFile = new File(plugin.getDataFolder(), "houses.yml");
        if (!housesFile.exists()) {
            plugin.saveResource("houses.yml", false);
        }
        this.housesConfig = YamlConfiguration.loadConfiguration(housesFile);
        loadHouses();
    }

    private void loadHouses() {
        ConfigurationSection housesSection = housesConfig.getConfigurationSection("houses");
        if (housesSection == null) {
            logger.info("No houses found in houses.yml");
            return;
        }
        for (String houseId : housesSection.getKeys(false)) {
            ConfigurationSection houseSection = housesSection.getConfigurationSection(houseId);
            if (houseSection == null) continue;
            try {
                String regionId = houseSection.getString("region_id");
                String worldName = houseSection.getString("world");
                double price = houseSection.getDouble("price");
                UUID owner = houseSection.contains("owner") ? UUID.fromString(houseSection.getString("owner")) : null;
                House house = new House(houseId, regionId, worldName, price, owner);
                houses.put(houseId, house);
                if (owner != null) {
                    playerHouses.computeIfAbsent(owner, k -> new HashSet<>()).add(houseId);
                }
                logger.fine("Loaded house: " + houseId);
            } catch (Exception e) {
                logger.warning("Failed to load house " + houseId + ": " + e.getMessage());
            }
        }
        logger.info("Loaded " + houses.size() + " houses");
    }

    public void saveHouses() {
        for (House house : houses.values()) {
            String path = "houses." + house.getId();
            housesConfig.set(path + ".region_id", house.getRegionId());
            housesConfig.set(path + ".world", house.getWorldName());
            housesConfig.set(path + ".price", house.getPrice());
            housesConfig.set(path + ".owner", house.getOwner() != null ? house.getOwner().toString() : null);
        }
        try {
            housesConfig.save(housesFile);
        } catch (IOException e) {
            logger.severe("Failed to save houses.yml: " + e.getMessage());
        }
    }

    public void purchaseHouse(Player player, String houseId) {
        House house = houses.get(houseId);
        if (house == null) {
            player.sendMessage(MessageUtils.color("&#FF4040House not found."));
            return;
        }
        if (house.getOwner() != null) {
            player.sendMessage(MessageUtils.color("&#FF4040This house is already owned."));
            return;
        }
        double price = house.getPrice();
        if (!economyManager.getEconomy().has(player, price)) {
            player.sendMessage(MessageUtils.color("&#FF4040You do not have enough money to purchase this house ($" + price + ")."));
            return;
        }
        World world = Bukkit.getWorld(house.getWorldName());
        if (world == null) {
            player.sendMessage(MessageUtils.color("&#FF4040World not found: " + house.getWorldName()));
            return;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            player.sendMessage(MessageUtils.color("&#FF4040WorldGuard region manager not found for world: " + house.getWorldName()));
            return;
        }
        ProtectedRegion region = regionManager.getRegion(house.getRegionId());
        if (region == null) {
            player.sendMessage(MessageUtils.color("&#FF4040Region not found: " + house.getRegionId()));
            return;
        }
        economyManager.getEconomy().withdrawPlayer(player, price);
        house.setOwner(player.getUniqueId());
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(player.getUniqueId());
        region.setOwners(owners);
        playerHouses.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(houseId);
        saveHouses();
        player.sendMessage(MessageUtils.color("&#FF7F00Successfully purchased house " + houseId + " for $" + price + "!"));
        logger.info("Player " + player.getName() + " purchased house: " + houseId);
    }

    public boolean canUseDrugBlocks(Player player, Location location) {
        World world = location.getWorld();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) return false;
        BlockVector3 position = BlockVector3.at(location.getX(), location.getY(), location.getZ());
        ApplicableRegionSet regions = regionManager.getApplicableRegions(position);
        for (ProtectedRegion region : regions) {
            if (region.getOwners().contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getPlayerHouses(UUID playerId) {
        return playerHouses.getOrDefault(playerId, new HashSet<>());
    }

    public Map<String, House> getHouses() {
        return houses;
    }

    public static class House {
        private final String id;
        private final String regionId;
        private final String worldName;
        private final double price;
        private UUID owner;

        public House(String id, String regionId, String worldName, double price, UUID owner) {
            this.id = id;
            this.regionId = regionId;
            this.worldName = worldName;
            this.price = price;
            this.owner = owner;
        }

        public String getId() {
            return id;
        }

        public String getRegionId() {
            return regionId;
        }

        public String getWorldName() {
            return worldName;
        }

        public double getPrice() {
            return price;
        }

        public UUID getOwner() {
            return owner;
        }

        public void setOwner(UUID owner) {
            this.owner = owner;
        }
    }
}