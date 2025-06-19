package com.spence.drugcraft.drugs;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DrugManager {
    private final DrugCraft plugin;
    private final List<Drug> drugs = new ArrayList<>();

    public DrugManager(DrugCraft plugin) {
        this.plugin = plugin;
        loadDrugs();
    }

    private void loadDrugs() {
        ConfigurationSection drugsSection = plugin.getConfig("drugs.yml").getConfigurationSection("drugs");
        if (drugsSection != null) {
            for (String drugId : drugsSection.getKeys(false)) {
                ConfigurationSection drugConfig = drugsSection.getConfigurationSection(drugId);
                if (drugConfig != null) {
                    Drug drug = new Drug(drugId, drugConfig);
                    drugs.add(drug);
                }
            }
        }
    }

    public boolean isDrugItem(ItemStack item) {
        if (item == null) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("drug_id");
    }

    public boolean isSeedItem(ItemStack item) {
        if (item == null) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("drug_id") && item.getType() == Material.WHEAT_SEEDS;
    }

    public boolean hasDrugsInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isDrugItem(item)) return true;
        }
        return false;
    }

    public String getDrugIdFromItem(ItemStack item) {
        if (item == null) return null;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("drug_id") ? nbtItem.getString("drug_id") : null;
    }

    public String getQualityFromItem(ItemStack item) {
        if (item == null) return "Basic";
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("quality") ? nbtItem.getString("quality") : "Basic";
    }

    public Drug getDrug(String drugId) {
        return drugs.stream().filter(drug -> drug.getId().equalsIgnoreCase(drugId)).findFirst().orElse(null);
    }

    public boolean isTrimmer(ItemStack item) {
        if (item == null) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("trimmer") && nbtItem.getBoolean("trimmer");
    }

    public ItemStack getSeedItem(String drugId, String quality, Player player) {
        Drug drug = getDrug(drugId);
        if (drug == null || !drug.isGrowable()) return null;

        ItemStack item = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage("gui.dealer.seed-name", "drug_name", "<" + drug.getHexColor() + ">" + drug.getName() + "</" + drug.getHexColor() + ">"));
            meta.setLore(drug.getLore(quality));
            item.setItemMeta(meta);
        }

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("drug_id", drugId);
        nbtItem.setString("quality", quality);
        return nbtItem.getItem();
    }

    public ItemStack getDrugItem(String drugId, String quality, Player player) {
        Drug drug = getDrug(drugId);
        if (drug == null) return null;

        ItemStack item = new ItemStack(drug.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage("gui.dealer.drug-name", "drug_name", "<" + drug.getHexColor() + ">" + drug.getName() + "</" + drug.getHexColor() + ">"));
            meta.setLore(drug.getLore(quality));
            meta.setCustomModelData(drug.getCustomModelData());
            item.setItemMeta(meta);
        }

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("drug_id", drugId);
        nbtItem.setString("quality", quality);
        return nbtItem.getItem();
    }

    public List<Drug> getDrugs() {
        return new ArrayList<>(drugs);
    }

    public List<Drug> getSortedDrugs() {
        List<Drug> sortedDrugs = new ArrayList<>(drugs);
        sortedDrugs.sort((d1, d2) -> d1.getName().compareToIgnoreCase(d2.getName()));
        return sortedDrugs;
    }

    public double getBaseBuyPrice(String drugId) {
        Drug drug = getDrug(drugId);
        return drug != null ? drug.getBaseBuyPrice() : 0.0;
    }
}