package com.spence.drugcraft.police;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import com.spence.drugcraft.utils.PermissionManager;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class PoliceManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Economy economy;
    private final PermissionManager permissionManager;
    private final CartelManager cartelManager;
    private final PoliceConfig policeConfig;
    private final Map<UUID, Integer> wantedLevels = new HashMap<>();
    private final Random random = new Random();

    public PoliceManager(DrugCraft plugin, DrugManager drugManager, Economy economy, PermissionManager permissionManager, CartelManager cartelManager, File configFile) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economy = economy;
        this.permissionManager = permissionManager;
        this.cartelManager = cartelManager;
        this.policeConfig = new PoliceConfig(plugin);
    }

    public void spawnNPCsFromConfig() {
        FileConfiguration config = plugin.getConfigManager().getConfig("police.yml");
        ConfigurationSection npcs = config.getConfigurationSection("k9.npcs");
        if (npcs != null) {
            for (String id : npcs.getKeys(false)) {
                String worldName = npcs.getString(id + ".location.world");
                double x = npcs.getDouble(id + ".location.x");
                double y = npcs.getDouble(id + ".location.y");
                double z = npcs.getDouble(id + ".location.z");
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    Location location = new Location(world, x, y, z);
                    new PoliceNPC(plugin).spawnNPCs();
                }
            }
        }
    }

    public void requestSearch(Player player) {
        if (random.nextDouble() < policeConfig.getDrugUseCheckFrequency()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && drugManager.isDrugItem(item)) {
                    applyFine(player, policeConfig.getDrugUseFine());
                    increaseWantedLevel(player, 1);
                    MessageUtils.sendMessage(player, "police.search-found-drugs");
                    return;
                }
            }
        }
        MessageUtils.sendMessage(player, "police.search-clear");
    }

    public void checkK9Detection(Player player, Wolf k9) {
        if (random.nextDouble() < policeConfig.getK9DetectionChance()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && drugManager.isDrugItem(item)) {
                    applyFine(player, policeConfig.getDrugUseFine());
                    increaseWantedLevel(player, 2);
                    MessageUtils.sendMessage(player, "police.k9-detected");
                    k9.setTarget(player);
                    return;
                }
            }
        }
    }

    private void applyFine(Player player, double amount) {
        if (economy != null && economy.has(player, amount)) {
            economy.withdrawPlayer(player, amount);
            MessageUtils.sendMessage(player, "police.fined", "amount", String.valueOf(amount));
        } else {
            arrestPlayer(player);
        }
    }

    private void arrestPlayer(Player player) {
        int jailTime = policeConfig.getDrugUseJailTime();
        Location jailLocation = policeConfig.getJailLocation();
        if (jailLocation != null) {
            player.teleport(jailLocation);
            plugin.getLockupManager().storeItems(player, Arrays.asList(player.getInventory().getContents()));
            player.getInventory().clear();
            MessageUtils.sendMessage(player, "police.arrested", "time", String.valueOf(jailTime));
            Bukkit.getScheduler().runTaskLater(plugin, () -> releasePlayer(player), jailTime * 20L);
        }
    }

    private void releasePlayer(Player player) {
        Location releaseLocation = policeConfig.getReleaseLocation();
        if (releaseLocation != null) {
            player.teleport(releaseLocation);
            plugin.getLockupManager().retrieveItems(player, releaseLocation);
            MessageUtils.sendMessage(player, "police.released");
        }
    }

    public void handlePlayerAttack(Player player, NPC npc) {
        if (npc.hasTrait(PoliceTrait.class)) {
            applyFine(player, policeConfig.getNPCAttackFine());
            increaseWantedLevel(player, 3);
            MessageUtils.sendMessage(player, "police.npc-attacked");
        }
    }

    private void increaseWantedLevel(Player player, int increment) {
        UUID playerUUID = player.getUniqueId();
        int currentLevel = wantedLevels.getOrDefault(playerUUID, 0);
        int newLevel = Math.min(currentLevel + increment, policeConfig.getMaxWantedLevel());
        wantedLevels.put(playerUUID, newLevel);
        MessageUtils.sendMessage(player, "police.wanted-level", "level", String.valueOf(newLevel));
        if (newLevel >= 3) {
            alertNearbyPolice(player);
        }
    }

    private void alertNearbyPolice(Player player) {
        Location location = player.getLocation();
        FileConfiguration config = plugin.getConfigManager().getConfig("police.yml");
        double detectionRange = config.getDouble("k9.detectionRange", 10.0);
        for (Entity entity : location.getWorld().getNearbyEntities(location, detectionRange, detectionRange, detectionRange)) {
            if (entity instanceof Wolf wolf && wolf.getOwner() == null) {
                wolf.setTarget(player);
            }
        }
    }

    public class PoliceConfig {
        private final DrugCraft plugin;

        public PoliceConfig(DrugCraft plugin) {
            this.plugin = plugin;
        }

        public double getDrugUseCheckFrequency() {
            return plugin.getConfigManager().getConfig("police.yml").getDouble("checks.drugUse.frequency", 0.05);
        }

        public double getDrugUseFine() {
            return plugin.getConfigManager().getConfig("police.yml").getDouble("checks.drugUse.fine", 500.0);
        }

        public int getDrugUseJailTime() {
            return plugin.getConfigManager().getConfig("police.yml").getInt("checks.drugUse.jailTime", 300);
        }

        public double getK9DetectionChance() {
            return plugin.getConfigManager().getConfig("police.yml").getDouble("k9.detectionChance", 0.1);
        }

        public double getNPCAttackFine() {
            return plugin.getConfigManager().getConfig("police.yml").getDouble("checks.npcAttack.fine", 1000.0);
        }

        public int getMaxWantedLevel() {
            return plugin.getConfigManager().getConfig("police.yml").getInt("wanted.maxLevel", 5);
        }

        public Location getJailLocation() {
            FileConfiguration config = plugin.getConfigManager().getConfig("police.yml");
            ConfigurationSection jails = config.getConfigurationSection("jail.regions");
            if (jails != null) {
                String jailId = jails.getKeys(false).stream().findFirst().orElse(null);
                if (jailId != null) {
                    String worldName = jails.getString(jailId + ".world");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        return new Location(world, jails.getDouble(jailId + ".x"), jails.getDouble(jailId + ".y"), jails.getDouble(jailId + ".z"));
                    }
                }
            }
            return null;
        }

        public Location getReleaseLocation() {
            return Bukkit.getWorlds().get(0).getSpawnLocation(); // Placeholder
        }
    }
}