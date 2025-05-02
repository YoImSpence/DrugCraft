package com.spence.drugcraft;

import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Level;

/**
 * Listens for player interactions to handle drug usage and seed planting.
 */
public class PlayerListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final CropGrowthTask cropGrowthTask;
    private static final NamespacedKey TYPE_KEY = new NamespacedKey("drugcraft", "type");

    public PlayerListener(DrugCraft plugin) {
        this.plugin = plugin;
        this.drugManager = plugin.getDrugManager();
        this.cropGrowthTask = plugin.getCropGrowthTask();
    }

    /**
     * Handles right-click interactions for drug usage and seed planting.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        // Handle drug usage
        for (Drug drug : drugManager.getDrugs().values()) {
            if (drugManager.isDrugItem(item, drug.getItem())) {
                try {
                    plugin.getLogger().fine("Checking NBT for drug usage: " + item.getType());
                    NBTItem nbtItem = new NBTItem(item);
                    String drugKey = nbtItem.getString("drugcraft:type");
                    if (drugKey != null) {
                        drugManager.useDrug(event.getPlayer(), drugKey);
                        event.setCancelled(true);
                        plugin.getLogger().fine("Drug used via NBT: " + drugKey);
                    } else {
                        plugin.getLogger().fine("No drug type found in NBT");
                    }
                } catch (Throwable e) {
                    plugin.getLogger().log(Level.SEVERE, "Error checking drug NBT for usage: " + e.getMessage(), e);
                    // Fallback to PersistentDataContainer
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        String drugKey = meta.getPersistentDataContainer().get(TYPE_KEY, PersistentDataType.STRING);
                        if (drugKey != null && drugKey.equals(drug.getKey())) {
                            drugManager.useDrug(event.getPlayer(), drugKey);
                            event.setCancelled(true);
                            plugin.getLogger().fine("Drug used via PDC: " + drugKey);
                        } else {
                            plugin.getLogger().fine("No drug type found in PDC");
                        }
                    }
                }
                return;
            }
        }

        // Handle seed planting
        for (Drug drug : drugManager.getDrugs().values()) {
            ItemStack seedItem = drug.getSeedItem();
            if (drugManager.isSeedItem(item, seedItem)) {
                if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.FARMLAND) {
                    try {
                        plugin.getLogger().fine("Checking NBT for seed planting: " + item.getType());
                        NBTItem nbtItem = new NBTItem(item);
                        String strain = nbtItem.getString("drugcraft:type");
                        if (strain != null) {
                            cropGrowthTask.addCrop(event.getClickedBlock().getLocation().add(0, 1, 0), strain);
                            item.setAmount(item.getAmount() - 1);
                            event.setCancelled(true);
                            plugin.getLogger().fine("Seed planted via NBT: " + strain);
                        } else {
                            plugin.getLogger().fine("No seed type found in NBT");
                        }
                    } catch (Throwable e) {
                        plugin.getLogger().log(Level.SEVERE, "Error checking seed NBT for planting: " + e.getMessage(), e);
                        // Fallback to PersistentDataContainer
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            String strain = meta.getPersistentDataContainer().get(TYPE_KEY, PersistentDataType.STRING);
                            if (strain != null && strain.equals(drug.getKey())) {
                                cropGrowthTask.addCrop(event.getClickedBlock().getLocation().add(0, 1, 0), strain);
                                item.setAmount(item.getAmount() - 1);
                                event.setCancelled(true);
                                plugin.getLogger().fine("Seed planted via PDC: " + strain);
                            } else {
                                plugin.getLogger().fine("No seed type found in PDC");
                            }
                        }
                    }
                }
                return;
            }
        }
    }
}