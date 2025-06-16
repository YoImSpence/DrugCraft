package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.crops.CropManager;
import com.spence.drugcraft.crops.GrowLight;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final CropManager cropManager;
    private final GrowLight growLight;
    private final DataManager dataManager;

    public BlockPlaceListener(DrugCraft plugin, DrugManager drugManager, CropManager cropManager, GrowLight growLight, DataManager dataManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.cropManager = cropManager;
        this.growLight = growLight;
        this.dataManager = dataManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Block block = event.getBlock();
        Block placedAgainst = event.getBlockAgainst();
        Location location = block.getLocation();

        if (!block.getType().equals(Material.FARMLAND) && drugManager.isSeedItem(item)) {
            MessageUtils.sendMessage(player, "crop-listener.invalid-soil");
            event.setCancelled(true);
            return;
        }

        if (drugManager.isSeedItem(item)) {
            String drugId = drugManager.getDrugIdFromItem(item);
            String quality = drugManager.getQualityFromItem(item);
            int requiredLevel = switch (drugId.toLowerCase()) {
                case "cannabis" -> 1;
                case "cannabis_blue_dream" -> 2;
                case "cannabis_og_kush" -> 4;
                case "cannabis_sour_diesel" -> 6;
                default -> Integer.MAX_VALUE;
            };
            if (requiredLevel == Integer.MAX_VALUE) {
                MessageUtils.sendMessage(player, "general.error");
                event.setCancelled(true);
                plugin.getLogger().warning("Invalid seed drug ID: " + drugId + " at " + location);
                return;
            }
            if (dataManager.getPlayerLevel(player.getUniqueId(), drugId) < requiredLevel) {
                MessageUtils.sendMessage(player, "crop-listener.level-required", "level", String.valueOf(requiredLevel));
                event.setCancelled(true);
                return;
            }
            if (!placedAgainst.getType().equals(Material.FARMLAND)) {
                MessageUtils.sendMessage(player, "crop-listener.invalid-soil");
                event.setCancelled(true);
                return;
            }
            cropManager.plantCrop(block.getLocation(), item);
            MessageUtils.sendMessage(player, "crop.planted", "drug", drugId);
            item.setAmount(item.getAmount() - 1);
            plugin.getLogger().info("Player " + player.getName() + " planted " + drugId + " at " + location);
        } else if (growLight.isGrowLightItem(item)) {
            if (block.getRelative(BlockFace.DOWN).getType().isAir()) {
                MessageUtils.sendMessage(player, "general.error");
                event.setCancelled(true);
                return;
            }
            NBTBlock nbtBlock = new NBTBlock(block);
            nbtBlock.getData().setString("drugcraft_type", "grow_light");
            nbtBlock.getData().setString("quality", growLight.getQualityFromItem(item));
            MessageUtils.sendMessage(player, "grow-light.placed", "quality", growLight.getQualityFromItem(item));
            plugin.getLogger().info("Player " + player.getName() + " placed grow light at " + location);
        }
    }
}