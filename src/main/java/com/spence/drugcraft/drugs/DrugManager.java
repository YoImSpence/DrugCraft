package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DrugManager {
    private final DrugCraft plugin;

    public DrugManager(DrugCraft plugin) {
        this.plugin = plugin;
        loadDrugs();
    }

    private void loadDrugs() {
        ConfigurationSection drugsSection = plugin.getConfig("drugs.yml").getConfigurationSection("drugs");
        if (drugsSection != null) {
            for (String drugId : drugsSection.getKeys(false)) {
                // Load drug configurations
            }
        }
    }

    public ConfigurationSection getDrugsConfig() {
        return plugin.getConfig("drugs.yml").getConfigurationSection("drugs");
    }

    public ItemStack getDrugItem(String drugId, String quality, Player player) {
        ConfigurationSection drugConfig = getDrugsConfig().getConfigurationSection(drugId);
        if (drugConfig == null) return null;

        ItemStack item = new ItemStack(Material.valueOf(drugConfig.getString("material", "WHITE_DYE")));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(drugConfig.getString("name"));
            meta.setLore(drugConfig.getStringList("lore"));
            meta.setCustomModelData(drugConfig.getInt("customModelData", 0));
            item.setItemMeta(meta);
        }

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("drug_id", drugId);
        nbtItem.setString("quality", quality);
        return nbtItem.getItem();
    }

    public ItemStack getSeedItem(String drugId, String quality, Player player) {
        ConfigurationSection drugConfig = getDrugsConfig().getConfigurationSection(drugId);
        if (drugConfig == null) return null;

        ItemStack item = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Seed: " + drugConfig.getString("name"));
            meta.setLore(drugConfig.getStringList("lore"));
            item.setItemMeta(meta);
        }

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("drug_id", drugId);
        nbtItem.setString("quality", quality);
        return nbtItem.getItem();
    }

    public List<String> getSortedDrugs() {
        ConfigurationSection drugsSection = getDrugsConfig();
        if (drugsSection == null) return new ArrayList<>();
        Set<String> drugs = drugsSection.getKeys(false);
        List<String> sortedDrugs = new ArrayList<>(drugs);
        sortedDrugs.sort(String::compareToIgnoreCase);
        return sortedDrugs;
    }

    public List<String> getDrugs() {
        ConfigurationSection drugsSection = getDrugsConfig();
        if (drugsSection == null) return new ArrayList<>();
        return new ArrayList<>(drugsSection.getKeys(false));
    }

    public double getBaseBuyPrice(String drugId) {
        ConfigurationSection drugConfig = getDrugsConfig().getConfigurationSection(drugId);
        return drugConfig != null ? drugConfig.getDouble("baseBuyPrice", 0.0) : 0.0;
    }
}