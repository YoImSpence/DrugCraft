package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DrugManager {
    private final DrugCraft plugin;
    private final Map<String, Drug> drugs = new HashMap<>();
    private final Logger logger;

    public DrugManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadDrugs();
    }

    private void loadDrugs() {
        File drugsFile = new File(plugin.getDataFolder(), "drugs.yml");
        if (!drugsFile.exists()) {
            plugin.saveResource("drugs.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(drugsFile);
        ConfigurationSection drugsSection = config.getConfigurationSection("drugs");
        if (drugsSection != null) {
            for (String key : drugsSection.getKeys(false)) {
                String name = drugsSection.getString(key + ".name");
                Material material = Material.getMaterial(drugsSection.getString(key + ".material"));
                List<String> lore = drugsSection.getStringList(key + ".lore");
                List<String> effectStrings = drugsSection.getStringList(key + ".effects");
                double price = drugsSection.getDouble(key + ".price", 0);
                boolean hasSeed = drugsSection.getBoolean(key + ".has_seed", false);
                Material seedMaterial = hasSeed ? Material.getMaterial(drugsSection.getString(key + ".seed_material")) : null;
                int growthTime = hasSeed ? drugsSection.getInt(key + ".growth_time", 3600) : 0;

                List<PotionEffect> effects = new ArrayList<>();
                for (String effect : effectStrings) {
                    String[] parts = effect.split(":");
                    if (parts.length == 3) {
                        PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                        if (type != null) {
                            int level = Integer.parseInt(parts[1]);
                            int duration = Integer.parseInt(parts[2]) * 20; // Convert seconds to ticks
                            effects.add(new PotionEffect(type, duration, level - 1));
                        }
                    }
                }

                Drug drug = new Drug(key, name, material, lore, effects, price, hasSeed, seedMaterial, growthTime, logger);
                drugs.put(key, drug);
            }
            logger.info("Registered " + drugs.size() + " drugs successfully.");
        }
    }

    public boolean isDrugItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        try {
            NBTItem nbtItem = new NBTItem(item);
            return nbtItem.hasKey("drug_id") && drugs.containsKey(nbtItem.getString("drug_id"));
        } catch (Exception e) {
            logger.severe("Error checking drug item NBT: " + e.getMessage());
            return false;
        }
    }

    public boolean isSeedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            logger.warning("isSeedItem: Null item or seedItem");
            return false;
        }
        try {
            NBTItem nbtItem = new NBTItem(item);
            return nbtItem.hasKey("seed_id") && drugs.containsKey(nbtItem.getString("seed_id").replace("_seed", ""));
        } catch (Exception e) {
            logger.severe("Error checking seed item NBT: " + e.getMessage());
            return false;
        }
    }

    public void useDrug(Player player, ItemStack item) {
        if (!isDrugItem(item)) {
            return;
        }
        NBTItem nbtItem = new NBTItem(item);
        String drugId = nbtItem.getString("drug_id");
        Drug drug = drugs.get(drugId);
        if (drug != null) {
            drug.use(player);
            item.setAmount(item.getAmount() - 1);
            logger.info("Player " + player.getName() + " used drug: " + drugId);
            plugin.getAddictionManager().addDrugUse(player, drugId);
        }
    }

    public Drug getDrug(String id) {
        return drugs.get(id);
    }

    public Map<String, Drug> getDrugs() {
        return drugs;
    }

    public String getDrugIdFromItem(ItemStack item) {
        if (isDrugItem(item)) {
            NBTItem nbtItem = new NBTItem(item);
            return nbtItem.getString("drug_id");
        }
        return null;
    }

    public String getDrugIdFromSeed(ItemStack item) {
        if (isSeedItem(item)) {
            NBTItem nbtItem = new NBTItem(item);
            return nbtItem.getString("seed_id").replace("_seed", "");
        }
        return null;
    }
}