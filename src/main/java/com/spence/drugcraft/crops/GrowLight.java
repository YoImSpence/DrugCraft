package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GrowLight {
    private final DrugCraft plugin;

    public GrowLight(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public String getQualityFromItem(ItemStack item) {
        if (item == null) return "standard";
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("quality") ? nbtItem.getString("quality") : "standard";
    }

    public ItemStack createGrowLight(String quality) {
        ItemStack item = new ItemStack(Material.REDSTONE_LAMP);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.getMessage("growlight.name"));
            item.setItemMeta(meta);
        }
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("quality", quality);
        return nbtItem.getItem();
    }

    public boolean isGrowLightBlock(Block block) {
        return block.getType() == Material.REDSTONE_LAMP;
    }

    public String getQualityFromBlock(Block block) {
        // Placeholder: Block metadata not directly supported, assume standard
        return "standard";
    }

    public boolean isGrowLightItem(ItemStack item) {
        return item != null && item.getType() == Material.REDSTONE_LAMP && new NBTItem(item).hasKey("quality");
    }
}