package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Logger;

public class CartelManager {
    private final DrugCraft plugin;
    private final Map<String, Cartel> cartels = new HashMap<>();
    private final Map<UUID, String> playerCartels = new HashMap<>();
    private final Map<Location, String> stashLocations = new HashMap<>();
    private final Logger logger = Logger.getLogger(CartelManager.class.getName());

    public CartelManager(DrugCraft plugin) {
        this.plugin = plugin;
        loadCartels();
    }

    private void loadCartels() {
        FileConfiguration config = plugin.getConfigManager().getConfig("cartels.yml");
        ConfigurationSection cartelsSection = config.getConfigurationSection("cartels");
        if (cartelsSection != null) {
            for (String id : cartelsSection.getKeys(false)) {
                String name = cartelsSection.getString(id + ".name");
                if (name == null || name.isEmpty()) {
                    logger.warning("Invalid or missing name for cartel ID: " + id + " in cartels.yml. Skipping entry.");
                    continue;
                }
                String ownerStr = cartelsSection.getString(id + ".owner");
                if (ownerStr == null || ownerStr.isEmpty()) {
                    logger.warning("Missing owner for cartel ID: " + id + " in cartels.yml. Skipping entry.");
                    continue;
                }
                UUID owner;
                try {
                    owner = UUID.fromString(ownerStr);
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid owner UUID for cartel ID: " + id + " in cartels.yml: " + ownerStr + ". Skipping entry.");
                    continue;
                }
                int level = cartelsSection.getInt(id + ".level", 1);
                List<UUID> members = cartelsSection.getStringList(id + ".members").stream()
                        .map(UUID::fromString).toList();
                Map<UUID, String> ranks = new HashMap<>();
                ConfigurationSection ranksSec = cartelsSection.getConfigurationSection(id + ".ranks");
                if (ranksSec != null) {
                    for (String uuid : ranksSec.getKeys(false)) {
                        ranks.put(UUID.fromString(uuid), ranksSec.getString(uuid));
                    }
                }
                List<Location> stashes = new ArrayList<>();
                ConfigurationSection stashesSec = cartelsSection.getConfigurationSection(id + ".stashes");
                if (stashesSec != null) {
                    for (String locKey : stashesSec.getKeys(false)) {
                        String[] locParts;
                        try {
                            locParts = locKey.split(",");
                            if (locParts.length != 3) {
                                logger.warning("Invalid stash location format for cartel ID: " + id + ", key: " + locKey);
                                continue;
                            }
                            World world = Bukkit.getWorld(stashesSec.getString(locKey + ".world"));
                            if (world == null) {
                                logger.warning("Invalid world for stash location in cartel ID: " + id + ", key: " + locKey);
                                continue;
                            }
                            Location loc = new Location(world,
                                    Double.parseDouble(locParts[0]),
                                    Double.parseDouble(locParts[1]),
                                    Double.parseDouble(locParts[2]));
                            stashes.add(loc);
                            stashLocations.put(loc, id);
                        } catch (NumberFormatException e) {
                            logger.warning("Invalid coordinates for stash location in cartel ID: " + id + ", key: " + locKey);
                            continue;
                        }
                    }
                }
                Cartel cartel = new Cartel(id, name, level, members, ranks, stashes);
                cartel.setOwner(owner);
                cartels.put(id, cartel);
                for (UUID member : members) {
                    playerCartels.put(member, id);
                }
            }
        } else {
            logger.info("No cartels found in cartels.yml");
        }
    }

    public Cartel getCartelByPlayer(UUID playerUUID) {
        String cartelId = playerCartels.get(playerUUID);
        return cartelId != null ? cartels.get(cartelId) : null;
    }

    public Map<String, Cartel> getCartels() {
        return new HashMap<>(cartels);
    }

    public Cartel getCartelById(String id) {
        return cartels.get(id);
    }

    public void disbandCartel(String id) {
        Cartel cartel = cartels.remove(id);
        if (cartel != null) {
            for (UUID member : cartel.getMembers()) {
                playerCartels.remove(member);
            }
            FileConfiguration config = plugin.getConfigManager().getConfig("cartels.yml");
            config.set("cartels." + id, null);
            plugin.getConfigManager().saveConfig("cartels.yml");
        }
    }

    public void setPermission(UUID playerUUID, String permission, boolean value) {
        String cartelId = playerCartels.get(playerUUID);
        if (cartelId != null) {
            Cartel cartel = cartels.get(cartelId);
            Map<UUID, List<String>> permissions = cartel.getPermissions();
            List<String> perms = permissions.computeIfAbsent(playerUUID, k -> new ArrayList<>());
            if (value && !perms.contains(permission)) {
                perms.add(permission);
            } else if (!value) {
                perms.remove(permission);
            }
            permissions.put(playerUUID, perms);
            cartel.setPermissions(permissions);
            saveCartel(cartel);
        }
    }

    public void kickMember(UUID playerUUID) {
        String cartelId = playerCartels.remove(playerUUID);
        if (cartelId != null) {
            Cartel cartel = cartels.get(cartelId);
            List<UUID> members = new ArrayList<>(cartel.getMembers());
            members.remove(playerUUID);
            Map<UUID, List<String>> permissions = cartel.getPermissions();
            permissions.remove(playerUUID);
            cartel.setPermissions(permissions);
            saveCartel(cartel);
        }
    }

    public boolean isNameTaken(String name) {
        return cartels.values().stream().anyMatch(cartel -> cartel.getName().equalsIgnoreCase(name));
    }

    public void createCartel(UUID ownerUUID, String name) {
        String id = UUID.randomUUID().toString();
        Cartel cartel = new Cartel(id, name, 1, List.of(ownerUUID), new HashMap<>(), new ArrayList<>());
        cartel.setOwner(ownerUUID);
        Map<UUID, List<String>> perms = new HashMap<>();
        perms.put(ownerUUID, List.of("build", "interact", "manage"));
        cartel.setPermissions(perms);
        cartels.put(id, cartel);
        playerCartels.put(ownerUUID, id);
        saveCartel(cartel);
        MessageUtils.sendMessage(Bukkit.getPlayer(ownerUUID), "cartel.created", "name", name);
    }

    public boolean isPlayerInCartel(UUID playerUUID, String cartelId) {
        return cartelId.equals(playerCartels.get(playerUUID));
    }

    public boolean hasPermission(UUID playerUUID, String permission) {
        String cartelId = playerCartels.get(playerUUID);
        if (cartelId != null) {
            Cartel cartel = cartels.get(cartelId);
            return cartel.getPermissions().getOrDefault(playerUUID, new ArrayList<>()).contains(permission);
        }
        return false;
    }

    public String getCartelIdByStashLocation(Location location) {
        return stashLocations.get(location);
    }

    private void saveCartel(Cartel cartel) {
        FileConfiguration config = plugin.getConfigManager().getConfig("cartels.yml");
        String id = cartel.getId();
        config.set("cartels." + id + ".name", cartel.getName());
        config.set("cartels." + id + ".owner", cartel.getOwner().toString());
        config.set("cartels." + id + ".level", cartel.getLevel());
        config.set("cartels." + id + ".members", cartel.getMembers().stream().map(UUID::toString).toList());
        config.set("cartels." + id + ".ranks", null);
        for (Map.Entry<UUID, String> entry : cartel.getRanks().entrySet()) {
            config.set("cartels." + id + ".ranks." + entry.getKey(), entry.getValue());
        }
        config.set("cartels." + id + ".stashes", null);
        for (Location loc : cartel.getStashes()) {
            String locKey = loc.getX() + "," + loc.getY() + "," + loc.getZ();
            config.set("cartels." + id + ".stashes." + locKey + ".world", loc.getWorld().getName());
        }
        for (Map.Entry<UUID, List<String>> entry : cartel.getPermissions().entrySet()) {
            config.set("cartels." + id + ".permissions." + entry.getKey(), entry.getValue());
        }
        plugin.getConfigManager().saveConfig("cartels.yml");
    }
}