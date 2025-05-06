package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class Drug {
    private final String id;
    private final String name;
    private final Material material;
    private final List<String> lore;
    private final List<PotionEffect> effects;
    private final Particle particle;
    private final Sound sound;
    private final String specialEffect;
    private final double price;
    private final boolean hasSeed;
    private final Material seedMaterial;
    private final int baseGrowthTime; // In seconds
    private final DrugCraft plugin;
    private final Logger logger;

    // Static map for sound names to Sound enums
    private static final Map<String, Sound> SOUND_MAP = new HashMap<>();
    static {
        SOUND_MAP.put("ENTITY_BLAZE_SHOOT", Sound.ENTITY_BLAZE_SHOOT);
        SOUND_MAP.put("BLOCK_GRASS_BREAK", Sound.BLOCK_GRASS_BREAK);
        SOUND_MAP.put("BLOCK_BEACON_ACTIVATE", Sound.BLOCK_BEACON_ACTIVATE);
        SOUND_MAP.put("ENTITY_ILLUSIONER_CAST_SPELL", Sound.ENTITY_ILLUSIONER_CAST_SPELL);
        SOUND_MAP.put("ENTITY_ENDERMAN_TELEPORT", Sound.ENTITY_ENDERMAN_TELEPORT);
        SOUND_MAP.put("ITEM_HONEY_BOTTLE_DRINK", Sound.ITEM_HONEY_BOTTLE_DRINK);
    }

    public Drug(String id, String name, Material material, List<String> lore, List<PotionEffect> effects,
                String particleName, String soundName, String specialEffect, double price,
                boolean hasSeed, Material seedMaterial, int growthTime, Logger logger) {
        this.id = id;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.material = material;
        this.lore = new ArrayList<>();
        for (String line : lore) {
            this.lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        this.effects = effects;
        this.particle = parseParticle(particleName);
        this.sound = parseSound(soundName);
        this.specialEffect = specialEffect != null ? specialEffect : "NONE";
        this.price = price;
        this.hasSeed = hasSeed;
        this.seedMaterial = seedMaterial;
        this.baseGrowthTime = growthTime;
        this.plugin = null;
        this.logger = logger;
    }

    public Drug(String id, String name, Material material, List<String> lore, List<PotionEffect> effects,
                String particleName, String soundName, String specialEffect, double price,
                boolean hasSeed, Material seedMaterial, int growthTime, DrugCraft plugin, Logger logger) {
        this.id = id;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.material = material;
        this.lore = new ArrayList<>();
        for (String line : lore) {
            this.lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        this.effects = effects;
        this.particle = parseParticle(particleName);
        this.sound = parseSound(soundName);
        this.specialEffect = specialEffect != null ? specialEffect : "NONE";
        this.price = price;
        this.hasSeed = hasSeed;
        this.seedMaterial = seedMaterial;
        this.baseGrowthTime = growthTime;
        this.plugin = plugin;
        this.logger = logger;
    }

    private Particle parseParticle(String particleName) {
        if (particleName == null) return null;
        try {
            return Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid particle name for drug '" + id + "': " + particleName);
            return null;
        }
    }

    private Sound parseSound(String soundName) {
        if (soundName == null) return null;
        Sound sound = SOUND_MAP.get(soundName);
        if (sound == null) {
            logger.warning("Invalid sound name for drug '" + id + "': " + soundName);
        }
        return sound;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        try {
            NBTItem nbtItem = new NBTItem(item);
            nbtItem.setString("drug_id", id);
            ItemStack result = nbtItem.getItem();
            logger.fine("Created drug item for ID: " + id + " with NBT drug_id");
            return result;
        } catch (Exception e) {
            logger.severe("Failed to apply NBT to drug item '" + id + "': " + e.getMessage());
            return item;
        }
    }

    public ItemStack getSeedItem() {
        if (!hasSeed) return null;
        ItemStack item = new ItemStack(seedMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + name + " Seed");
            List<String> seedLore = new ArrayList<>();
            seedLore.add(ChatColor.GRAY + "Plant on farmland to grow " + name);
            meta.setLore(seedLore);
            item.setItemMeta(meta);
        }
        try {
            NBTItem nbtItem = new NBTItem(item);
            nbtItem.setString("seed_id", id + "_seed");
            ItemStack result = nbtItem.getItem();
            logger.fine("Created seed item for ID: " + id + " with NBT seed_id");
            return result;
        } catch (Exception e) {
            logger.severe("Failed to apply NBT to seed item '" + id + "_seed': " + e.getMessage());
            return item;
        }
    }

    public void use(Player player) {
        for (PotionEffect effect : effects) {
            player.addPotionEffect(effect);
        }
        // Play particles
        if (particle != null) {
            Location loc = player.getLocation().add(0, 1, 0);
            player.getWorld().spawnParticle(particle, loc, 20, 0.5, 0.5, 0.5, 0.05);
        }
        // Play sound
        if (sound != null) {
            player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        // Apply special effect
        if (specialEffect.equals("RANDOM_TELEPORT")) {
            Random rand = new Random();
            Location loc = player.getLocation();
            double x = loc.getX() + (rand.nextDouble() * 10 - 5);
            double z = loc.getZ() + (rand.nextDouble() * 10 - 5);
            double y = loc.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;
            Location newLoc = new Location(loc.getWorld(), x, y, z);
            player.teleport(newLoc);
        }
        logger.info("Player " + player.getName() + " used drug " + id + " with effects");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean hasSeed() {
        return hasSeed;
    }

    public int getBaseGrowthTime() {
        return baseGrowthTime;
    }

    public int getGrowthTime() {
        if (plugin == null) {
            logger.warning("DrugCraft instance is null for drug '" + id + "'. Using base growth time: " + baseGrowthTime);
            return baseGrowthTime;
        }
        double multiplier = plugin.getConfigManager().getGrowthMultiplier();
        int adjustedTime = (int) (baseGrowthTime / multiplier); // Higher multiplier = faster growth
        return Math.max(1, adjustedTime); // Ensure at least 1 second
    }
}