package com.spence.drugcraft.cartel;

import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class CartelStash {
    public static ItemStack createCartelStash() {
        ItemStack stash = new ItemStack(Material.CHEST);
        ItemMeta meta = stash.getItemMeta();
        meta.setDisplayName(MessageUtils.color("&#DAA520Cartel Stash"));
        meta.setLore(Arrays.asList(
                MessageUtils.color("&#D3D3D3Place to store cartel items"),
                MessageUtils.color("&#D3D3D3Only accessible with permission")
        ));
        meta.setCustomModelData(3001);
        stash.setItemMeta(meta);
        return stash;
    }

    public static boolean isCartelStash(ItemStack item) {
        if (item == null || item.getType() != Material.CHEST || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() && meta.getDisplayName().equals(MessageUtils.color("&#DAA520Cartel Stash"));
    }
}