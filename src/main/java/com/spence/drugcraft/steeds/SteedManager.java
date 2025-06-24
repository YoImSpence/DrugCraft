package com.spence.drugcraft.steeds;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SteedManager {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;
    private final Map<UUID, List<Steed>> playerSteeds = new HashMap<>();
    private final Map<UUID, Long> steedCooldowns = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SteedManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.cartelManager = plugin.getCartelManager();
        startFollowTask();
    }

    public void purchaseSteed(Player player, String steedType) {
        UUID playerUUID = player.getUniqueId();
        FileConfiguration config = plugin.getConfigManager().getConfig("data.yml");
        int level = plugin.getDataManager().getPlayerLevel(playerUUID);
        int maxSteeds = level / 5 + 1; // 1 steed at level 1, 2 at level 5, etc.
        List<String> ownedSteeds = config.getStringList("steeds." + playerUUID + ".steeds");
        if (ownedSteeds.size() >= maxSteeds) {
            MessageUtils.sendMessage(player, "vehicle.insufficient-funds"); // Placeholder message
            return;
        }
        double price = switch (steedType) {
            case "Horse" -> 2000.0;
            case "Donkey" -> 3000.0;
            case "Mule" -> 4000.0;
            case "Warhorse" -> 6000.0;
            default -> 500.0;
        };
        if (plugin.getEconomyManager().withdrawPlayer(player, price)) {
            ownedSteeds.add(steedType);
            config.set("steeds." + playerUUID + ".steeds", ownedSteeds);
            ItemStack saddle = new ItemStack(Material.SADDLE);
            ItemMeta meta = saddle.getItemMeta();
            if (meta != null) {
                meta.displayName(miniMessage.deserialize("<yellow>" + steedType));
                meta.setLore(List.of("<yellow>Type: " + steedType));
                saddle.setItemMeta(meta);
            }
            player.getInventory().addItem(saddle);
            plugin.getConfigManager().saveConfig("data.yml");
            MessageUtils.sendMessage(player, "vehicle.purchased", "steed_id", steedType, "price", String.valueOf(price));
        } else {
            MessageUtils.sendMessage(player, "vehicle.insufficient-funds");
        }
    }

    public void summonSteed(Player player, ItemStack saddle) {
        UUID playerUUID = player.getUniqueId();
        ItemMeta meta = saddle.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        String steedType = miniMessage.serialize(meta.lore().get(0)).split(": ")[1];
        long currentTime = System.currentTimeMillis();
        if (steedCooldowns.containsKey(playerUUID) && currentTime < steedCooldowns.get(playerUUID)) {
            MessageUtils.sendMessage(player, "vehicle.no-steed-item"); // Placeholder
            return;
        }
        List<Steed> steeds = playerSteeds.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        FileConfiguration config = plugin.getConfigManager().getConfig("data.yml");
        int maxSteeds = config.getInt("players." + playerUUID + ".level", 1) / 5 + 1;
        if (steeds.size() >= maxSteeds) {
            MessageUtils.sendMessage(player, "vehicle.already-summoned");
            return;
        }
        Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);
        horse.setOwner(player);
        switch (steedType) {
            case "Horse" -> horse.setMaxHealth(15.0);
            case "Donkey" -> horse.setMaxHealth(18.0);
            case "Mule" -> horse.setMaxHealth(20.0);
            case "Warhorse" -> horse.setMaxHealth(25.0);
        }
        horse.setTamed(true);
        steeds.add(new Steed(horse, steedType));
        MessageUtils.sendMessage(player, "vehicle.summoned", "steed_id", steedType);
    }

    public void despawnSteed(Player player, ItemStack saddle) {
        UUID playerUUID = player.getUniqueId();
        List<Steed> steeds = playerSteeds.getOrDefault(playerUUID, new ArrayList<>());
        ItemMeta meta = saddle.getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        String steedType = miniMessage.serialize(meta.lore().get(0)).split(": ")[1];
        steeds.removeIf(steed -> {
            if (steed.type.equals(steedType)) {
                steed.horse.remove();
                return true;
            }
            return false;
        });
        MessageUtils.sendMessage(player, "vehicle.despawned");
    }

    public void onSteedDeath(Horse horse, Player owner) {
        UUID playerUUID = owner.getUniqueId();
        List<Steed> steeds = playerSteeds.getOrDefault(playerUUID, new ArrayList<>());
        steeds.removeIf(steed -> steed.horse.equals(horse));
        steedCooldowns.put(playerUUID, System.currentTimeMillis() + 300000); // 5-minute cooldown
    }

    private void startFollowTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, List<Steed>> entry : playerSteeds.entrySet()) {
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player == null) continue;
                    List<Steed> steeds = entry.getValue();
                    Horse ridden = null;
                    for (Steed steed : steeds) {
                        if (steed.horse.getPassenger() != null && steed.horse.getPassenger().equals(player)) {
                            ridden = steed.horse;
                            break;
                        }
                    }
                    for (Steed steed : steeds) {
                        if (steed.horse.equals(ridden)) continue;
                        if (ridden != null) {
                            steed.horse.getPathfinder().moveTo(ridden.getLocation());
                        } else {
                            steed.horse.getPathfinder().moveTo(player.getLocation());
                        }
                        if (steed.horse.getLocation().distance(player.getLocation()) > 20) {
                            steed.horse.remove();
                            steeds.remove(steed);
                            MessageUtils.sendMessage(player, "vehicle.despawned");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public boolean isFriendlyDamage(Player attacker, Horse horse) {
        if (horse.getOwner() == null) return false;
        Player owner = (Player) horse.getOwner();
        if (attacker.equals(owner)) return true;
        return cartelManager.getCartelByPlayer(attacker.getUniqueId()) != null &&
                cartelManager.getCartelByPlayer(attacker.getUniqueId()).equals(cartelManager.getCartelByPlayer(owner.getUniqueId()));
    }

    private static class Steed {
        Horse horse;
        String type;

        Steed(Horse horse, String type) {
            this.horse = horse;
            this.type = type;
        }
    }
}