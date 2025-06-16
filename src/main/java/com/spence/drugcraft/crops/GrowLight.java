package com.spence.drugcraft.crops;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GrowLight {
    private final DrugCraft plugin;

    public GrowLight(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public ItemStack createGrowLightItem(String quality) {
        ItemStack growLight = new ItemStack(Material.LANTERN);
        ItemMeta meta = growLight.getItemMeta();
        meta.displayName(Component.text("Grow Light (" + quality + ")", TextColor.fromHexString("#FFD700")));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Quality: " + quality, TextColor.fromHexString("#D3D3D3")));
        meta.lore(lore);
        growLight.setItemMeta(meta);
        return growLight;
    }

    public boolean isGrowLightBlock(Block block) {
        NBTBlock nbtBlock = new NBTBlock(block);
        return "grow_light".equals(nbtBlock.getData().getString("drugcraft_type"));
    }

    public String getQualityFromBlock(Block block) {
        NBTBlock nbtBlock = new NBTBlock(block);
        return nbtBlock.getData().getString("quality");
    }

    public boolean isGrowLightItem(ItemStack item) {
        if (item == null || (item.getType() != Material.LANTERN && item.getType() != Material.SEA_LANTERN)) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasLore() && meta.lore() != null && meta.lore().stream()
                .anyMatch(lore -> MessageUtils.stripColor(lore).startsWith("Quality: "));
    }
}