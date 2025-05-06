package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        ConfigurationSection drugsSection = plugin.getConfigManager().getDrugsConfig().getConfigurationSection("drugs");
        if (drugsSection == null) {
            logger.severe("No 'drugs' section found in drugs.yml or drugs.yml failed to load");
            return;
        }

        logger.info("Loading drugs from drugs.yml...");
        int drugCount = 0;
        for (String key : drugsSection.getKeys(false)) {
            try {
                String name = drugsSection.getString(key + ".name");
                String materialName = drugsSection.getString(key + ".material");
                Material material = materialName != null ? Material.getMaterial(materialName) : null;
                List<String> lore = drugsSection.getStringList(key + ".lore");
                List<String> effectStrings = drugsSection.getStringList(key + ".effects");
                String particleName = drugsSection.getString(key + ".particle");
                String soundName = drugsSection.getString(key + ".sound");
                String specialEffect = drugsSection.getString(key + ".special_effect");
                double price = drugsSection.getDouble(key + ".price", 0);
                boolean hasSeed = drugsSection.getBoolean(key + ".has_seed", false);
                String seedMaterialName = drugsSection.getString(key + ".seed_material");
                Material seedMaterial = hasSeed && seedMaterialName != null ? Material.getMaterial(seedMaterialName) : null;
                int growthTime = hasSeed ? drugsSection.getInt(key + ".growth_time", 3600) : 0;

                if (name == null || name.isEmpty()) {
                    logger.warning("Skipping drug '" + key + "': Missing or empty name");
                    continue;
                }
                if (material == null) {
                    logger.warning("Skipping drug '" + key + "': Invalid material '" + materialName + "'");
                    continue;
                }
                if (hasSeed && seedMaterial == null) {
                    logger.warning("Skipping seed for drug '" + key + "': Invalid seed material '" + seedMaterialName + "'");
                    hasSeed = false;
                }

                List<PotionEffect> effects = new ArrayList<>();
                for (String effect : effectStrings) {
                    String[] parts = effect.split(":");
                    if (parts.length == 3) {
                        PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                        if (type != null) {
                            int level = Integer.parseInt(parts[1]);
                            int duration = Integer.parseInt(parts[2]) * 20; // Convert seconds to ticks
                            effects.add(new PotionEffect(type, duration, level - 1));
                        } else {
                            logger.warning("Invalid potion effect type for drug '" + key + "': " + parts[0]);
                        }
                    } else {
                        logger.warning("Invalid effect format for drug '" + key + "': " + effect);
                    }
                }

                Drug drug = new Drug(key, name, material, lore, effects, particleName, soundName, specialEffect,
                        price, hasSeed, seedMaterial, growthTime, plugin, logger);
                drugs.put(key, drug);
                drugCount++;
                logger.info("Loaded drug: " + key + (hasSeed ? " with growth time " + drug.getGrowthTime() + " seconds (base: " + growthTime + ")" : ""));
            } catch (Exception e) {
                logger.severe("Failed to load drug '" + key + "': " + e.getMessage());
            }
        }
        logger.info("Registered " + drugCount + " drugs successfully.");
    }

    public boolean isDrugItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            logger.fine("isDrugItem: Null or air item");
            return false;
        }
        try {
            NBTItem nbtItem = new NBTItem(item);
            String drugId = nbtItem.getString("drug_id");
            boolean isDrug = nbtItem.hasKey("drug_id") && drugs.containsKey(drugId);
            if (!isDrug && nbtItem.hasKey("drug_id")) {
                logger.warning("Item has drug_id '" + drugId + "' but no matching drug found");
            } else if (isDrug) {
                logger.fine("Item is drug with ID: " + drugId);
            }
            return isDrug;
        } catch (Exception e) {
            logger.severe("Error checking drug item NBT: " + e.getMessage());
            return false;
        }
    }

    public boolean isSeedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            logger.fine("isSeedItem: Null or air item");
            return false;
        }
        try {
            NBTItem nbtItem = new NBTItem(item);
            String seedId = nbtItem.getString("seed_id");
            boolean isSeed = nbtItem.hasKey("seed_id") && drugs.containsKey(seedId != null ? seedId.replace("_seed", "") : "");
            if (!isSeed && nbtItem.hasKey("seed_id")) {
                logger.warning("Item has seed_id '" + seedId + "' but no matching drug found");
            } else if (isSeed) {
                logger.fine("Item is seed with ID: " + seedId);
            }
            return isSeed;
        } catch (Exception e) {
            logger.severe("Error checking seed item NBT: " + e.getMessage());
            return false;
        }
    }

    public void useDrug(Player player, ItemStack item) {
        if (!isDrugItem(item)) {
            logger.fine("useDrug: Item is not a drug item for player " + player.getName());
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
        } else {
            logger.warning("Drug not found for ID: " + drugId + " during use by " + player.getName());
        }
    }

    public Drug getDrug(String id) {
        Drug drug = drugs.get(id);
        if (drug == null) {
            logger.warning("Drug not found for ID: " + id);
        } else {
            logger.fine("Retrieved drug: " + id);
        }
        return drug;
    }

    public Map<String, Drug> getDrugs() {
        return drugs;
    }

    public String getDrugIdFromItem(ItemStack item) {
        if (isDrugItem(item)) {
            NBTItem nbtItem = new NBTItem(item);
            String drugId = nbtItem.getString("drug_id");
            logger.fine("Retrieved drug ID from item: " + drugId);
            return drugId;
        }
        logger.fine("No drug ID found for item");
        return null;
    }

    public String getDrugIdFromSeed(ItemStack item) {
        if (isSeedItem(item)) {
            NBTItem nbtItem = new NBTItem(item);
            String seedId = nbtItem.getString("seed_id");
            String drugId = seedId != null ? seedId.replace("_seed", "") : null;
            logger.fine("Retrieved drug ID from seed: " + drugId);
            return drugId;
        }
        logger.fine("No seed ID found for item");
        return null;
    }
}