package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DrugManager {
    private final DrugCraft plugin;
    private final Map<String, Drug> drugs = new HashMap<>();

    public DrugManager(DrugCraft plugin) {
        this.plugin = plugin;
        loadDrugs();
    }

    private void loadDrugs() {
        FileConfiguration drugsConfig = plugin.getDrugsConfig();
        ConfigurationSection drugsSection = drugsConfig.getConfigurationSection("drugs");
        if (drugsSection == null) {
            plugin.getLogger().warning("No 'drugs' section found in drugs.yml");
            return;
        }
        for (String drugId : drugsSection.getKeys(false)) {
            ConfigurationSection drugSection = drugsSection.getConfigurationSection(drugId);
            if (drugSection != null) {
                drugs.put(drugId, new Drug(drugId, drugSection));
            }
        }
        plugin.getLogger().info("Loaded " + drugs.size() + " drugs");
    }

    public boolean isTrimmer(ItemStack item) {
        if (item == null || item.getType() != Material.SHEARS) return false;
        NBTItem nbtItem = new NBTItem(item);
        return "trimmer".equals(nbtItem.getString("drugcraft_type"));
    }

    public double getSeedPrice(String drugId, String quality) {
        Drug drug = drugs.get(drugId);
        if (drug == null) return 0.0;
        double multiplier = switch (quality.toLowerCase()) {
            case "prime" -> 1.3;
            case "exotic" -> 1.5;
            case "legendary" -> 2.0;
            case "cosmic" -> 3.0;
            default -> 1.0;
        };
        return drug.getPrice() * multiplier * 0.5;
    }

    public boolean isDrugItem(ItemStack item) {
        if (item == null) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("drug_id");
    }

    public String getDrugIdFromItem(ItemStack item) {
        if (item == null) return null;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.getString("drug_id");
    }

    public String getQualityFromItem(ItemStack item) {
        if (item == null) return "Standard";
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.getString("quality", "Standard");
    }

    public Drug getDrug(String drugId) {
        return drugs.get(drugId);
    }

    public boolean isSeedItem(ItemStack item) {
        if (item == null) return false;
        NBTItem nbtItem = new NBTItem(item);
        return "seed".equals(nbtItem.getString("drugcraft_type"));
    }
}