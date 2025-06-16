package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CartelStash {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelStash(DrugCraft plugin) {
        this.plugin = plugin;
        this.cartelManager = plugin.getCartelManager();
    }

    public ItemStack createCartelStashItem(String cartelName) {
        ItemStack stash = new ItemStack(Material.CHEST);
        ItemMeta meta = stash.getItemMeta();
        meta.displayName(MessageUtils.color(MessageUtils.getMessage("cartel.stash-item-name")));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtils.color(MessageUtils.getMessage("cartel.stash-item-lore")));
        meta.lore(lore);
        stash.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(stash);
        if (cartelName != null) {
            nbtItem.setString("cartel_name", cartelName);
        }
        return nbtItem.getItem();
    }

    public ItemStack createDebugGrowLightItem(String cartelName) {
        ItemStack growLight = new ItemStack(Material.SEA_LANTERN);
        ItemMeta meta = growLight.getItemMeta();
        meta.displayName(MessageUtils.color("Debug Grow Light").color(TextColor.fromHexString("#FFD700")));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtils.color("Used for debugging crop growth"));
        meta.lore(lore);
        growLight.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(growLight);
        if (cartelName != null) {
            nbtItem.setString("cartel_name", cartelName);
        }
        nbtItem.setString("drugcraft_type", "grow_light");
        return nbtItem.getItem();
    }

    public boolean isCartelStash(ItemStack item) {
        if (item == null || item.getType() != Material.CHEST) return false;
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("cartel_name");
    }

    public void addCartelStash(Location location, String cartelName) {
        placeStash(location.getBlock(), cartelName);
    }

    public void placeStash(Block block, String cartelName) {
        ItemStack stashItem = createCartelStashItem(cartelName);
        block.setType(Material.CHEST);

        ItemMeta meta = stashItem.getItemMeta();
        meta.displayName(MessageUtils.color(MessageUtils.getMessage("cartel.stash-placed-name")));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtils.color(MessageUtils.getMessage("cartel.stash-placed-lore")));
        meta.lore(lore);
        stashItem.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(stashItem);
        nbtItem.setString("cartel_name", cartelName);
        block.getWorld().dropItemNaturally(block.getLocation(), nbtItem.getItem());

        Location location = block.getLocation();
        cartelManager.setStashLocation(cartelName, location);
        plugin.getLogger().info("Placed cartel stash for " + cartelName + " at " + location);
    }
}