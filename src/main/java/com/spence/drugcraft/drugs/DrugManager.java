package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DrugManager {
    private final DrugCraft plugin;
    private final FileConfiguration config;
    private final Logger logger;
    private final Map<String, Drug> drugs = new HashMap<>();
    private final Map<String, String> qualityColors = new HashMap<>();

    public DrugManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getDrugsConfig();
        this.logger = plugin.getLogger();
        qualityColors.put("Basic", "�FFFF");
        qualityColors.put("Standard", "�FF00");
        qualityColors.put("Exotic", "&#FF4500");
        qualityColors.put("Prime", "E90FF");
        qualityColors.put("Legendary", "&#FFD700");
        loadDrugs();
    }

    private void loadDrugs() {
        ConfigurationSection drugsSection = config.getConfigurationSection("drugs");
        if (drugsSection == null) {
            logger.warning("No drugs found in drugs.yml");
            return;
        }
        for (String drugId : drugsSection.getKeys(false)) {
            ConfigurationSection drugSection = drugsSection.getConfigurationSection(drugId);
            if (drugSection == null) continue;
            try {
                Material type = Material.valueOf(drugSection.getString("type"));
                String name = drugSection.getString("name");
                int customModelData = drugSection.getInt("custom_model_data", 0);
                List<String> lore = drugSection.getStringList("lore").stream()
                        .map(MessageUtils::color)
                        .collect(Collectors.toList());
                List<String> rawEffects = drugSection.getStringList("effects");
                List<String> coloredEffects = new ArrayList<>();
                for (String effect : rawEffects) {
                    String[] parts = effect.split(":");
                    String effectType = parts[0];
                    String coloredEffect = switch (effectType) {
                        case "SPEED" -> "�BFFF" + effect;
                        case "NIGHT_VISION" -> "&#FFD700" + effect;
                        case "PARTICLE" -> "&#FF00FF" + effect;
                        case "REGENERATION" -> " CD32" + effect;
                        case "SLOW" -> "󅒐" + effect;
                        case "HEALTH_BOOST" -> "&#FF4500" + effect;
                        case "HUNGER" -> "B4513" + effect;
                        case "INCREASE_DAMAGE" -> "&#FF0000" + effect;
                        case "SOUND" -> "ቊB4" + effect;
                        case "FAST_DIGGING" -> "&#FFFF00" + effect;
                        case "JUMP" -> "�FF00" + effect;
                        case "CONFUSION" -> "&#DA70D6" + effect;
                        default -> "&#D3D3D3" + effect;
                    };
                    coloredEffects.add(MessageUtils.color(coloredEffect));
                }
                lore.set(1, coloredEffects.get(0));
                if (coloredEffects.size() > 1) {
                    lore.addAll(coloredEffects.subList(1, coloredEffects.size()));
                }
                ItemStack seed = null;
                ConfigurationSection seedSection = drugSection.getConfigurationSection("seed");
                if (seedSection != null) {
                    Material seedType = Material.valueOf(seedSection.getString("type"));
                    String seedName = seedSection.getString("name");
                    int seedCustomModelData = seedSection.getInt("custom_model_data", 0);
                    List<String> seedLore = seedSection.getStringList("lore").stream()
                            .map(MessageUtils::color)
                            .collect(Collectors.toList());
                    seed = new ItemStack(seedType);
                    ItemMeta seedMeta = seed.getItemMeta();
                    seedMeta.setDisplayName(MessageUtils.color(seedName));
                    seedMeta.setLore(seedLore);
                    if (seedCustomModelData != 0) {
                        seedMeta.setCustomModelData(seedCustomModelData);
                    }
                    seed.setItemMeta(seedMeta);
                }
                int growthTime = drugSection.getInt("growth_time", 0);
                double buyPrice = drugSection.getDouble("buy_price", 0.0);
                double sellPrice = drugSection.getDouble("sell_price", 0.0);
                String quality = drugSection.getString("quality", "Basic");
                Drug drug = new Drug(drugId, type, name, customModelData, lore, seed, growthTime, buyPrice, sellPrice, quality);
                drugs.put(drugId, drug);
                logger.fine("Loaded drug: " + drugId + " (" + quality + ")");
            } catch (Exception e) {
                logger.severe("Failed to load drug " + drugId + ": " + e.getMessage());
            }
        }
        logger.info("Loaded " + drugs.size() + " drugs successfully");
    }

    public ItemStack getDrugItem(String drugId, String quality) {
        Drug drug = drugs.get(drugId);
        if (drug == null) return null;
        ItemStack item = drug.getItem(quality);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.add(MessageUtils.color(qualityColors.getOrDefault(quality, "&#D3D3D3") + "Quality: " + quality));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getSeedItem(String drugId, String quality) {
        Drug drug = drugs.get(drugId);
        if (drug == null || !drug.hasSeed()) return null;
        ItemStack seed = drug.getSeedItem(quality);
        ItemMeta meta = seed.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.add(MessageUtils.color(qualityColors.getOrDefault(quality, "&#D3D3D3") + "Quality: " + quality));
        meta.setLore(lore);
        seed.setItemMeta(meta);
        return seed;
    }

    public boolean isDrugItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.hasLore()) return false;
        String displayName = meta.getDisplayName();
        return drugs.values().stream().anyMatch(drug -> MessageUtils.color(drug.getName()).equals(displayName));
    }

    public boolean isSeedItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.hasLore()) return false;
        String displayName = meta.getDisplayName();
        return drugs.values().stream()
                .filter(Drug::hasSeed)
                .anyMatch(drug -> drug.getSeed() != null &&
                        MessageUtils.color(drug.getSeed().getItemMeta().getDisplayName()).equals(displayName));
    }

    public String getQualityFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return "Basic";
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return "Basic";
        for (String line : meta.getLore()) {
            for (String quality : qualityColors.keySet()) {
                if (line.contains(quality)) {
                    return quality;
                }
            }
        }
        return "Basic";
    }

    public Drug getDrug(String drugId) {
        return drugs.get(drugId);
    }

    public List<Drug> getSortedDrugs() {
        return drugs.values().stream()
                .sorted((d1, d2) -> d1.getName().compareTo(d2.getName()))
                .collect(Collectors.toList());
    }
}