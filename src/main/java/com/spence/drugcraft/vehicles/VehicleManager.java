package com.spence.drugcraft.vehicles;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VehicleManager {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;
    private final Map<UUID, Steed> playerSteeds = new HashMap<>();
    private final Map<UUID, Horse> activeSteeds = new HashMap<>();

    public VehicleManager(DrugCraft plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public boolean isSteedItem(ItemStack item) {
        if (item == null) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("steed_id");
    }

    public boolean canSummonSteed(Player player) {
        return !activeSteeds.containsKey(player.getUniqueId());
    }

    public Horse summonSteed(Player player, ItemStack item) {
        if (!canSummonSteed(player)) return null;

        NBTItem nbtItem = new NBTItem(item);
        String steedId = nbtItem.getString("steed_id");
        FileConfiguration config = plugin.getConfig("vehicles.yml");
        double speed = config.getDouble("steeds." + steedId + ".speed", 0.2);
        double health = config.getDouble("steeds." + steedId + ".health", 20.0);

        Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);
        horse.setOwner(player);
        horse.setMaxHealth(health);
        horse.setHealth(health);
        horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
        horse.getInventory().setSaddle(new ItemStack(org.bukkit.Material.SADDLE));
        horse.setCustomName(MessageUtils.getMessage("vehicle.steed-name", "player_name", player.getName()));
        activeSteeds.put(player.getUniqueId(), horse);

        Steed steed = new Steed(steedId, speed, health);
        playerSteeds.put(player.getUniqueId(), steed);
        saveSteedData(player.getUniqueId(), steed);

        return horse;
    }

    public void despawnSteed(Player player) {
        Horse horse = activeSteeds.remove(player.getUniqueId());
        if (horse != null) {
            horse.remove();
            MessageUtils.sendMessage(player, "vehicle.despawned");
        }
    }

    public Steed getPlayerSteed(Player player) {
        return playerSteeds.get(player.getUniqueId());
    }

    private void saveSteedData(UUID playerUUID, Steed steed) {
        FileConfiguration data = plugin.getConfig("data.yml");
        data.set("players." + playerUUID + ".steed.id", steed.getId());
        data.set("players." + playerUUID + ".steed.speed", steed.getSpeed());
        data.set("players." + playerUUID + ".steed.health", steed.getHealth());
        plugin.saveConfig();
    }
}