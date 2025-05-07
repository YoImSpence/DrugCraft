package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DrugManager {
    private final DrugCraft plugin;
    private final Logger logger;
    private final Map<String, Drug> drugs = new HashMap<>();

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
        FileConfiguration drugsConfig = YamlConfiguration.loadConfiguration(drugsFile);
        ConfigurationSection drugsSection = drugsConfig.getConfigurationSection("drugs");
        if (drugsSection == null) {
            logger.warning("No drugs defined in drugs.yml");
            return;
        }
        for (String drugId : drugsSection.getKeys(false)) {
            ConfigurationSection drugSection = drugsSection.getConfigurationSection(drugId);
            if (drugSection == null) {
                logger.warning("Invalid drug configuration for ID: " + drugId);
                continue;
            }
            String type = drugSection.getString("type");
            String name = MessageUtils.color(drugSection.getString("name", drugId));
            List<String> lore = drugSection.getStringList("lore").stream()
                    .map(MessageUtils::color)
                    .collect(Collectors.toList());
            List<String> effects = drugSection.getStringList("effects");
            double buyPrice = drugSection.getDouble("buy_price", 0.0);
            double sellPrice = drugSection.getDouble("sell_price", 0.0);
            long growthTime = drugSection.getLong("growth_time", 0);
            String quality = drugSection.getString("quality", "Basic");
            ItemStack item;
            try {
                Material material = Material.valueOf(type);
                item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(lore);
                if (drugSection.contains("custom_model_data")) {
                    meta.setCustomModelData(drugSection.getInt("custom_model_data"));
                }
                item.setItemMeta(meta);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material type for drug " + drugId + ": " + type);
                continue;
            }
            ItemStack seedItem = null;
            ConfigurationSection seedSection = drugSection.getConfigurationSection("seed");
            if (seedSection != null) {
                String seedType = seedSection.getString("type");
                String seedName = MessageUtils.color(seedSection.getString("name", name + " Seed"));
                List<String> seedLore = seedSection.getStringList("lore").stream()
                        .map(MessageUtils::color)
                        .collect(Collectors.toList());
                try {
                    Material seedMaterial = Material.valueOf(seedType);
                    seedItem = new ItemStack(seedMaterial);
                    ItemMeta seedMeta = seedItem.getItemMeta();
                    seedMeta.setDisplayName(seedName);
                    seedMeta.setLore(seedLore);
                    if (seedSection.contains("custom_model_data")) {
                        seedMeta.setCustomModelData(seedSection.getInt("custom_model_data"));
                    }
                    seedItem.setItemMeta(seedMeta);
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid seed material type for drug " + drugId + ": " + seedType);
                }
            }
            Drug drug = new Drug(drugId, item, effects, seedItem, growthTime, buyPrice, sellPrice, quality);
            drugs.put(drugId, drug);
            logger.fine("Loaded drug: " + drugId);
        }
        logger.info("Loaded " + drugs.size() + " drugs successfully");
    }

    public Drug getDrug(String id) {
        return drugs.get(id);
    }

    public Map<String, Drug> getDrugs() {
        return drugs;
    }

    public List<Drug> getSortedDrugs() {
        List<Drug> sorted = new ArrayList<>(drugs.values());
        sorted.sort((a, b) -> {
            int aRank = switch (a.getQuality()) {
                case "Basic" -> 1;
                case "Standard" -> 2;
                case "Exotic" -> 3;
                case "Prime" -> 4;
                case "Legendary" -> 5;
                default -> 0;
            };
            int bRank = switch (b.getQuality()) {
                case "Basic" -> 1;
                case "Standard" -> 2;
                case "Exotic" -> 3;
                case "Prime" -> 4;
                case "Legendary" -> 5;
                default -> 0;
            };
            if (aRank != bRank) {
                return aRank - bRank; // Ascending quality
            }
            String aName = a.getName().replaceAll("&[0-9a-fk-or]", "");
            String bName = b.getName().replaceAll("&[0-9a-fk-or]", "");
            return aName.compareTo(bName); // Alphabetical within quality
        });
        // Log sorting order for debugging
        logger.fine("Sorted drugs: " + sorted.stream().map(d -> d.getName() + " (" + d.getQuality() + ")").collect(Collectors.joining(", ")));
        return sorted;
    }

    public boolean isDrugItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        for (Drug drug : drugs.values()) {
            ItemStack drugItem = drug.getItem(null);
            if (item.getType() == drugItem.getType() && item.getItemMeta().getDisplayName().equals(drugItem.getItemMeta().getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSeedItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        for (Drug drug : drugs.values()) {
            if (drug.hasSeed()) {
                ItemStack seedItem = drug.getSeedItem(null);
                if (item.getType() == seedItem.getType() && item.getItemMeta().getDisplayName().equals(seedItem.getItemMeta().getDisplayName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getDrugIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        for (Map.Entry<String, Drug> entry : drugs.entrySet()) {
            ItemStack drugItem = entry.getValue().getItem(null);
            if (item.getType() == drugItem.getType() && item.getItemMeta().getDisplayName().equals(drugItem.getItemMeta().getDisplayName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getDrugIdFromSeed(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        for (Map.Entry<String, Drug> entry : drugs.entrySet()) {
            if (entry.getValue().hasSeed()) {
                ItemStack seedItem = entry.getValue().getSeedItem(null);
                if (item.getType() == seedItem.getType() && item.getItemMeta().getDisplayName().equals(seedItem.getItemMeta().getDisplayName())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public String getQualityFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return "Basic";
        }
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("Quality: ")) {
                return line.substring(line.indexOf("Quality: ") + 9);
            }
        }
        return "Basic";
    }

    public void useDrug(Player player, ItemStack item) {
        String drugId = getDrugIdFromItem(item);
        if (drugId == null) {
            logger.warning("Attempted to use invalid drug item by " + player.getName());
            return;
        }
        Drug drug = getDrug(drugId);
        if (drug == null) {
            logger.warning("Drug not found for ID: " + drugId);
            return;
        }
        String quality = getQualityFromItem(item);
        boolean appliedEffect = false;
        for (String effect : drug.getEffects(quality)) {
            String[] parts = effect.split(":");
            try {
                if (parts[0].startsWith("PARTICLE")) {
                    String[] particleParts = parts[1].split(";");
                    if (particleParts.length == 5) {
                        player.getWorld().spawnParticle(
                                org.bukkit.Particle.valueOf(particleParts[0]),
                                player.getLocation().add(0, 1, 0),
                                (int) Float.parseFloat(particleParts[4]),
                                Float.parseFloat(particleParts[1]),
                                Float.parseFloat(particleParts[2]),
                                Float.parseFloat(particleParts[3]),
                                0.1
                        );
                        appliedEffect = true;
                    }
                } else if (parts[0].startsWith("SOUND")) {
                    player.getWorld().playSound(player.getLocation(), parts[1], 1.0f, 1.0f);
                    appliedEffect = true;
                } else {
                    PotionEffectType type = PotionEffectType.getByName(parts[0]);
                    if (type == null) {
                        logger.warning("Invalid potion effect type for drug " + drugId + ": " + parts[0]);
                        continue;
                    }
                    int amplifier = Integer.parseInt(parts[1]);
                    int duration = Integer.parseInt(parts[2]) * 20; // Convert seconds to ticks
                    player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                    logger.info("Applied effect " + type.getName() + " to " + player.getName() + " for drug " + drugId);
                    appliedEffect = true;
                }
            } catch (IllegalArgumentException e) {
                logger.warning("Failed to apply effect for drug " + drugId + ": " + effect + " (" + e.getMessage() + ")");
            }
        }
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
        }
        if (appliedEffect) {
            player.sendMessage(MessageUtils.color("&aUsed " + drug.getName() + " (" + quality + ")"));
        } else {
            player.sendMessage(MessageUtils.color("&cFailed to use " + drug.getName() + " due to invalid effects!"));
        }
    }
}