package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.CartelManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.PermissionManager;
import com.spence.drugcraft.utils.PoliceManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.logging.Logger;

public class PoliceListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final EconomyManager economyManager;
    private final PermissionManager permissionManager;
    private final CartelManager cartelManager;
    private final PoliceManager policeManager;
    private final Logger logger;

    public PoliceListener(DrugCraft plugin, DrugManager drugManager, EconomyManager economyManager, PermissionManager permissionManager, CartelManager cartelManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economyManager = economyManager;
        this.permissionManager = permissionManager;
        this.cartelManager = cartelManager;
        this.policeManager = new PoliceManager(plugin, drugManager, economyManager, permissionManager, cartelManager);
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (drugManager.isSeedItem(event.getItemInHand())) {
            logger.info("Detected seed planting by " + event.getPlayer().getName() + " at " + event.getBlock().getLocation());
            policeManager.detectIllegalActivity(event.getPlayer(), event.getBlock().getLocation(), false);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.WHEAT &&
                plugin.getCropManager().getCrop(event.getBlock().getLocation()) != null) {
            logger.info("Detected crop harvesting by " + event.getPlayer().getName() + " at " + event.getBlock().getLocation());
            policeManager.detectIllegalActivity(event.getPlayer(), event.getBlock().getLocation(), false);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getAction().isRightClick() && drugManager.isDrugItem(event.getItem())) {
            logger.info("Detected drug use by " + event.getPlayer().getName() + " at " + event.getPlayer().getLocation());
            policeManager.detectIllegalActivity(event.getPlayer(), event.getPlayer().getLocation(), false);
        }
    }
}