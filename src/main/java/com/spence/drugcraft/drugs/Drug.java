package com.spence.drugcraft.drugs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Drug {
    private final String id;
    private final String name;
    private final Material material;
    private final List<String> lore;
    private final int customModelData;
    private final double baseBuyPrice;
    private final int level;
    private final double price;
    private final Map<String, Effect> effects;
    private final String hexColor;
    private final boolean isGrowable;

    public Drug(String id, ConfigurationSection config) {
        this.id = id;
        this.name = config.getString("name", id);
        this.material = Material.valueOf(config.getString("material", "WHITE_DYE"));
        this.lore = config.getStringList("lore");
        this.customModelData = config.getInt("customModelData", 0);
        this.baseBuyPrice = config.getDouble("baseBuyPrice", 0.0);
        this.level = config.getInt("level", 0);
        this.price = config.getDouble("price", 100.0);
        this.hexColor = config.getString("hex_color", "#FFFFFF");
        this.isGrowable = config.getBoolean("growable", false);
        this.effects = new HashMap<>();

        String[] qualities = {"Basic", "Standard", "Prime", "Exotic", "Legendary", "Cosmic"};
        for (String quality : qualities) {
            String path = "effects." + quality.toLowerCase();
            if (config.contains(path)) {
                double strength = config.getDouble(path + ".strength", 1.0);
                long duration = config.getLong(path + ".duration", 600L);
                effects.put(quality, new Effect(strength, duration));
            } else {
                Effect base = effects.getOrDefault("Basic", new Effect(1.0, 600L));
                double scale = switch (quality) {
                    case "Standard" -> 1.2;
                    case "Prime" -> 1.5;
                    case "Exotic" -> 2.0;
                    case "Legendary" -> 2.5;
                    case "Cosmic" -> 3.0;
                    default -> 1.0;
                };
                effects.put(quality, new Effect(base.strength * scale, (long) (base.duration * scale)));
            }
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Material getMaterial() { return material; }
    public List<String> getLore(String quality) {
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add("<" + hexColor + ">" + line + "</" + hexColor + ">");
        }
        Effect effect = effects.getOrDefault(quality, effects.get("Basic"));
        coloredLore.add("<gradient:#FF5555:#5555FF><bold>Quality: " + quality + "</bold></gradient>");
        coloredLore.add("<" + hexColor + ">Strength: " + effect.strength + "</" + hexColor + ">");
        coloredLore.add("<" + hexColor + ">Duration: " + (effect.duration / 20) + "s</" + hexColor + ">");
        return coloredLore;
    }
    public int getCustomModelData() { return customModelData; }
    public double getBaseBuyPrice() { return baseBuyPrice; }
    public int getLevel() { return level; }
    public double getPrice() { return price; }
    public Effect getEffect(String quality) { return effects.getOrDefault(quality, effects.get("Basic")); }
    public String getHexColor() { return hexColor; }
    public boolean isGrowable() { return isGrowable; }

    public static class Effect {
        private final double strength;
        private final long duration;

        public Effect(double strength, long duration) {
            this.strength = strength;
            this.duration = duration;
        }

        public double getStrength() { return strength; }
        public long getDuration() { return duration; }
    }
}