package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DrugManager {
    private final DrugCraft plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DrugManager(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public boolean isDrugItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() >= 1 && meta.getCustomModelData() <= 110;
    }

    public String getDrugIdFromItem(ItemStack item) {
        if (!isDrugItem(item)) return null;
        ConfigurationSection drugs = plugin.getConfigManager().getConfig("drugs.yml").getConfigurationSection("drugs");
        if (drugs == null) return null;

        int customModelData = item.getItemMeta().getCustomModelData();
        for (String drugId : drugs.getKeys(false)) {
            int drugCustomModelData = drugs.getInt(drugId + ".customModelData", -1);
            if (drugCustomModelData == customModelData) {
                return drugId;
            }
        }
        return null;
    }

    public ItemStack createDrugItem(String drugId) {
        ConfigurationSection drugs = plugin.getConfigManager().getConfig("drugs.yml").getConfigurationSection("drugs." + drugId);
        if (drugs == null) return null;

        Material material = Material.matchMaterial(drugs.getString("material", "GREEN_DYE"));
        if (material == null) material = Material.GREEN_DYE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<yellow>" + drugs.getString("name", "Unknown Drug")));
            List<String> lore = drugs.getStringList("lore");
            meta.setLore(lore.isEmpty() ? null : lore.stream().map(s -> miniMessage.serialize(miniMessage.deserialize("<yellow>" + s))).toList());
            meta.setCustomModelData(drugs.getInt("customModelData", 1));
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSeedItem(String drugId) {
        ConfigurationSection seeds = plugin.getConfigManager().getConfig("drugs.yml").getConfigurationSection("seeds." + drugId);
        if (seeds == null) return null;

        Material material = Material.matchMaterial(seeds.getString("material", "WHEAT_SEEDS"));
        if (material == null) material = Material.WHEAT_SEEDS;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<yellow>Seed: " + drugId.replace("_", " ").toUpperCase()));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<yellow>Plant to grow " + drugId.replace("_", " ") + "."));
            meta.lore(lore);
            meta.setCustomModelData(seeds.getInt("customModelData", 1));
            item.setItemMeta(meta);
        }
        return item;
    }

    public Drug getDrug(String drugId) {
        ConfigurationSection drugs = plugin.getConfigManager().getConfig("drugs.yml").getConfigurationSection("drugs." + drugId);
        if (drugs == null) return null;

        return new Drug(
                drugId,
                drugs.getString("name", "Unknown Drug"),
                drugs.getDouble("price", 100.0),
                drugs.getBoolean("growable", false),
                drugs.getDouble("base_effects.strength", 1.0),
                drugs.getInt("base_effects.duration", 600)
        );
    }
}