package com.spence.drugcraft.drugs;

import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Drug {
    private final String id;
    private final ItemStack item;
    private final List<String> effects;
    private final ItemStack seed;
    private final long growthTime;
    private final double buyPrice;
    private final double sellPrice;
    private final String quality;

    public Drug(String id, ItemStack item, List<String> effects, ItemStack seed, long growthTime, double buyPrice, double sellPrice, String quality) {
        this.id = id;
        this.item = item;
        this.effects = effects;
        this.seed = seed;
        this.growthTime = growthTime;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.quality = quality;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItem(String quality) {
        ItemStack result = item.clone();
        if (quality != null) {
            ItemMeta meta = result.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            String color = switch (quality) {
                case "Legendary" -> "&d"; // Magenta
                case "Prime" -> "&9"; // Blue
                case "Exotic" -> "&e"; // Yellow
                case "Standard" -> "&a"; // Green
                default -> "&b"; // Cyan (Basic)
            };
            lore.add(MessageUtils.color(color + "Quality: " + quality));
            meta.setLore(lore);
            result.setItemMeta(meta);
        }
        return result;
    }

    public List<String> getEffects(String quality) {
        List<String> modifiedEffects = new ArrayList<>();
        double multiplier = switch (quality == null ? "Basic" : quality) {
            case "Legendary" -> 2.0;
            case "Prime" -> 1.5;
            case "Exotic" -> 1.2;
            case "Standard" -> 1.0;
            default -> 0.5; // Basic
        };
        for (String effect : effects) {
            String[] parts = effect.split(":");
            if (parts.length >= 3) {
                try {
                    int duration = (int) (Integer.parseInt(parts[2]) * multiplier);
                    modifiedEffects.add(parts[0] + ":" + parts[1] + ":" + duration);
                } catch (NumberFormatException e) {
                    modifiedEffects.add(effect);
                }
            } else if (parts.length == 2 && parts[0].startsWith("PARTICLE")) {
                try {
                    float count = (float) (Float.parseFloat(parts[1].split(";")[4]) * multiplier);
                    String[] particleParts = parts[1].split(";");
                    modifiedEffects.add(parts[0] + ":" + particleParts[0] + ";" + particleParts[1] + ";" + particleParts[2] + ";" + particleParts[3] + ";" + count);
                } catch (NumberFormatException e) {
                    modifiedEffects.add(effect);
                }
            } else {
                modifiedEffects.add(effect);
            }
        }
        return modifiedEffects;
    }

    public ItemStack getSeedItem(String quality) {
        if (seed == null) return null;
        ItemStack result = seed.clone();
        if (quality != null) {
            ItemMeta meta = result.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            String color = switch (quality) {
                case "Legendary" -> "&d"; // Magenta
                case "Prime" -> "&9"; // Blue
                case "Exotic" -> "&e"; // Yellow
                case "Standard" -> "&a"; // Green
                default -> "&b"; // Cyan (Basic)
            };
            lore.add(MessageUtils.color(color + "Quality: " + quality));
            meta.setLore(lore);
            result.setItemMeta(meta);
        }
        return result;
    }

    public boolean hasSeed() {
        return seed != null;
    }

    public long getGrowthTime() {
        return growthTime;
    }

    public String getName() {
        return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : id;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double sellPrice() {
        return sellPrice;
    }

    public String getQuality() {
        return quality;
    }
}