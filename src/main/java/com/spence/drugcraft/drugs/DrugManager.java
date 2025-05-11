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
    private final Map<String, String> effectColors = new HashMap<>();
    private final Map<String, List<String>> uniqueLore = new HashMap<>();

    public DrugManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getDrugsConfig();
        this.logger = plugin.getLogger();
        // Use proper &#RRGGBB format
        qualityColors.put("Basic", "&#00FFFF"); // Cyan
        qualityColors.put("Standard", "&#00FF00"); // Lime
        qualityColors.put("Exotic", "&#FF4500"); // Orange Red
        qualityColors.put("Prime", "&#1E90FF"); // Dodger Blue
        qualityColors.put("Legendary", "&#FFD700"); // Gold
        effectColors.put("SPEED", "&#00BFFF"); // Deep Sky Blue
        effectColors.put("NIGHT_VISION", "&#FFD700"); // Gold
        effectColors.put("PARTICLE", "&#FF00FF"); // Magenta
        effectColors.put("REGENERATION", "&#32CD32"); // Lime Green
        effectColors.put("SLOW", "&#808080"); // Gray
        effectColors.put("HEALTH_BOOST", "&#FF4500"); // Orange Red
        effectColors.put("HUNGER", "&#8B4513"); // Saddle Brown
        effectColors.put("INCREASE_DAMAGE", "&#FF0000"); // Red
        effectColors.put("SOUND", "&#4682B4"); // Steel Blue
        effectColors.put("FAST_DIGGING", "&#FFFF00"); // Yellow
        effectColors.put("JUMP", "&#00FF00"); // Lime
        effectColors.put("CONFUSION", "&#DA70D6"); // Orchid
        // Unique lore descriptions for each drug
        uniqueLore.put("cannabis_blue_dream", Arrays.asList(
                "&#00FFFFA dreamy strain with a sweet berry aroma",
                "&#00FFFFRelaxes the mind and body"
        ));
        uniqueLore.put("cannabis_dosidos", Arrays.asList(
                "&#00FF00Nutty and earthy with a hint of lime",
                "&#00FF00Perfect for a calming evening"
        ));
        uniqueLore.put("cannabis_granddaddy_purp", Arrays.asList(
                "&#FF4500Deep purple buds with a grape flavor",
                "&#FF4500Induces a heavy, soothing high"
        ));
        uniqueLore.put("cannabis_og_kush", Arrays.asList(
                "&#1E90FFClassic strain with a piney scent",
                "&#1E90FFDelivers a balanced, euphoric buzz"
        ));
        uniqueLore.put("cannabis_sour_diesel", Arrays.asList(
                "&#32CD32Pungent diesel aroma with a citrus kick",
                "&#32CD32Energizes and uplifts the spirit"
        ));
        uniqueLore.put("lsd", Arrays.asList(
                "&#FF00FFMind-bending visuals and thoughts",
                "&#FF00FFPrepare for a cosmic journey"
        ));
        uniqueLore.put("mystic_shroom", Arrays.asList(
                "&#DA70D6Glowing mushrooms from enchanted forests",
                "&#DA70D6Unleashes mystical hallucinations"
        ));
        uniqueLore.put("cocaine", Arrays.asList(
                "&#FFFFFFPure white powder for a quick rush",
                "&#FFFFFFHeightens alertness, use with caution"
        ));
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
            if (drugSection == null) {
                logger.warning("Skipping drug " + drugId + ": Invalid configuration section");
                continue;
            }
            try {
                Material type = Material.valueOf(drugSection.getString("type", "STONE"));
                String name = drugSection.getString("name", drugId);
                int customModelData = drugSection.getInt("custom_model_data", 0);
                List<String> lore = drugSection.getStringList("lore").stream()
                        .map(MessageUtils::color)
                        .collect(Collectors.toList());
                List<String> uniqueDrugLore = uniqueLore.getOrDefault(drugId, lore);
                lore = new ArrayList<>(uniqueDrugLore);
                List<String> rawEffects = drugSection.getStringList("effects");
                List<String> formattedEffects = new ArrayList<>();
                for (String effect : rawEffects) {
                    try {
                        String[] parts = effect.split(":");
                        String effectType = parts[0].toUpperCase();
                        String formattedEffect;
                        if (effectType.equals("PARTICLE")) {
                            if (parts.length < 2) {
                                logger.warning("Invalid PARTICLE effect format for drug " + drugId + ": " + effect + " (expected PARTICLE:NAME)");
                                continue;
                            }
                            String particleName = parts[1].toUpperCase();
                            String displayName = switch (particleName) {
                                case "WATER_SPLASH" -> "Water Splash Burst";
                                case "VILLAGER_HAPPY" -> "Villager Happiness Glow";
                                case "SMOKE_LARGE" -> "Large Smoke Cloud";
                                case "PORTAL" -> "Nether Portal Swirl";
                                case "SMOKE_NORMAL" -> "Gentle Smoke Puff";
                                default -> particleName;
                            };
                            formattedEffect = effectColors.getOrDefault(effectType, "&#FFFFFF") + "Particle: " + displayName;
                        } else if (effectType.equals("SOUND")) {
                            if (parts.length < 2) {
                                logger.warning("Invalid SOUND effect format for drug " + drugId + ": " + effect + " (expected SOUND:NAME)");
                                continue;
                            }
                            String soundName = parts[1].toUpperCase();
                            String displayName = switch (soundName) {
                                case "ENTITY_CAT_PURR" -> "Cat Purring Echo";
                                case "ENTITY_WOLF_HOWL" -> "Wolf Howling Cry";
                                case "ENTITY_GHAST_SCREAM" -> "Ghastly Wail";
                                default -> soundName;
                            };
                            formattedEffect = effectColors.getOrDefault(effectType, "&#FFFFFF") + "Sound: " + displayName;
                        } else {
                            if (parts.length < 3) {
                                logger.warning("Invalid potion effect format for drug " + drugId + ": " + effect + " (expected EFFECT:LEVEL:DURATION)");
                                continue;
                            }
                            int level = Integer.parseInt(parts[1]);
                            int duration = Integer.parseInt(parts[2]);
                            String effectName = switch (effectType) {
                                case "SPEED" -> "Speed Boost";
                                case "NIGHT_VISION" -> "Night Vision";
                                case "REGENERATION" -> "Healing Surge";
                                case "SLOW" -> "Sluggishness";
                                case "HEALTH_BOOST" -> "Vitality Increase";
                                case "HUNGER" -> "Ravenous Craving";
                                case "INCREASE_DAMAGE" -> "Strength Surge";
                                case "FAST_DIGGING" -> "Mining Haste";
                                case "JUMP" -> "Leap Enhancement";
                                case "CONFUSION" -> "Mind Fog";
                                default -> effectType;
                            };
                            formattedEffect = effectColors.getOrDefault(effectType, "&#FFFFFF") + effectName + " (Level " + level + ", " + duration + "s)";
                        }
                        formattedEffects.add(MessageUtils.color(formattedEffect));
                    } catch (Exception e) {
                        logger.warning("Failed to parse effect for drug " + drugId + ": " + effect + " (" + e.getMessage() + ")");
                    }
                }
                lore.addAll(formattedEffects);
                ItemStack seed = null;
                ConfigurationSection seedSection = drugSection.getConfigurationSection("seed");
                if (seedSection != null) {
                    Material seedType = Material.valueOf(seedSection.getString("type", "WHEAT_SEEDS"));
                    String seedName = seedSection.getString("name", drugId + " Seed");
                    int seedCustomModelData = seedSection.getInt("custom_model_data", 0);
                    List<String> seedLore = seedSection.getStringList("lore").stream()
                            .map(MessageUtils::color)
                            .collect(Collectors.toList());
                    List<String> uniqueSeedLore = uniqueLore.getOrDefault(drugId + "_seed", seedLore);
                    seedLore = new ArrayList<>(uniqueSeedLore);
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
        lore.add(MessageUtils.color(qualityColors.getOrDefault(quality, "&#FFFFFF") + "Quality: " + quality));
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
        lore.add(MessageUtils.color(qualityColors.getOrDefault(quality, "&#FFFFFF") + "Quality: " + quality));
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
                .sorted((d1, d2) -> {
                    int nameCompare = d1.getName().compareTo(d2.getName());
                    if (nameCompare != 0) return nameCompare;
                    return compareQuality(d1.getQuality(), d2.getQuality());
                })
                .collect(Collectors.toList());
    }

    private int compareQuality(String q1, String q2) {
        List<String> qualityOrder = Arrays.asList("Basic", "Standard", "Exotic", "Prime", "Legendary");
        return Integer.compare(qualityOrder.indexOf(q1), qualityOrder.indexOf(q2));
    }
}