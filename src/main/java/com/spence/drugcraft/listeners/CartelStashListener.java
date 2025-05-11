package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.cartel.CartelManager;
import com.spence.drugcraft.cartel.CartelStash;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CartelStashListener implements Listener {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final Map<UUID, String> activeStashes = new HashMap<>();

    public CartelStashListener(DrugCraft plugin, CartelManager cartelManager, DataManager dataManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
        this.dataManager = dataManager;
        this.drugManager = plugin.getDrugManager();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (CartelStash.isCartelStash(item)) {
            Player player = event.getPlayer();
            String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
            if (cartelName == null) {
                player.sendMessage(MessageUtils.color("&#FF4040You are not in a cartel."));
                event.setCancelled(true);
                return;
            }
            if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.cartel.stash")) {
                player.sendMessage(MessageUtils.color("&#FF4040You do not have permission to place a cartel stash."));
                event.setCancelled(true);
                return;
            }
            CartelManager.Cartel cartel = cartelManager.getCartel(cartelName);
            if (cartel == null) {
                player.sendMessage(MessageUtils.color("&#FF4040Cartel not found."));
                event.setCancelled(true);
                return;
            }
            Location location = event.getBlock().getLocation();
            String locationKey = location.getWorld().getName() + "_" +
                    location.getBlockX() + "_" +
                    location.getBlockY() + "_" +
                    location.getBlockZ();
            cartel.getStash().put("location", locationKey);
            cartel.getStash().put("items", new ArrayList<ItemStack>());
            dataManager.saveStash(cartelName, cartel.getStash());
            player.sendMessage(MessageUtils.color("&#FF7F00Placed cartel stash for " + cartelName));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST) {
            Player player = event.getPlayer();
            String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
            if (cartelName == null) return;
            CartelManager.Cartel cartel = cartelManager.getCartel(cartelName);
            if (cartel == null) return;
            String locationKey = block.getLocation().getWorld().getName() + "_" +
                    block.getLocation().getBlockX() + "_" +
                    block.getLocation().getBlockY() + "_" +
                    block.getLocation().getBlockZ();
            if (cartel.getStash().containsKey("location") && cartel.getStash().get("location").equals(locationKey)) {
                if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.cartel.stash")) {
                    player.sendMessage(MessageUtils.color("&#FF4040You do not have permission to break this stash."));
                    event.setCancelled(true);
                    return;
                }
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), CartelStash.createCartelStash());
                cartel.getStash().remove("location");
                cartel.getStash().remove("items");
                dataManager.saveStash(cartelName, cartel.getStash());
                player.sendMessage(MessageUtils.color("&#FF7F00Removed cartel stash for " + cartelName));
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick() && event.hasBlock()) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.CHEST) {
                Player player = event.getPlayer();
                String cartelName = cartelManager.getPlayerCartel(player.getUniqueId());
                if (cartelName == null) return;
                CartelManager.Cartel cartel = cartelManager.getCartel(cartelName);
                if (cartel == null) return;
                String locationKey = block.getLocation().getWorld().getName() + "_" +
                        block.getLocation().getBlockX() + "_" +
                        block.getLocation().getBlockY() + "_" +
                        block.getLocation().getBlockZ();
                if (cartel.getStash().containsKey("location") && cartel.getStash().get("location").equals(locationKey)) {
                    event.setCancelled(true);
                    if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.cartel.stash")) {
                        player.sendMessage(MessageUtils.color("&#FF4040You do not have permission to access the stash."));
                        return;
                    }
                    Inventory stashInventory = Bukkit.createInventory(null, 27, MessageUtils.color("&#4682B4Stash Inventory: " + cartelName));
                    List<ItemStack> items = (List<ItemStack>) cartel.getStash().getOrDefault("items", new ArrayList<>());
                    for (int i = 0; i < Math.min(items.size(), 27); i++) {
                        stashInventory.setItem(i, items.get(i));
                    }
                    player.openInventory(stashInventory);
                    activeStashes.put(player.getUniqueId(), cartelName);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String cartelName = activeStashes.remove(player.getUniqueId());
        if (cartelName == null) return;
        CartelManager.Cartel cartel = cartelManager.getCartel(cartelName);
        if (cartel == null) return;
        Inventory inventory = event.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && (drugManager.isDrugItem(item) || drugManager.isSeedItem(item))) {
                items.add(item);
            }
        }
        cartel.getStash().put("items", items);
        dataManager.saveStash(cartelName, cartel.getStash());
        player.sendMessage(MessageUtils.color("&#FF7F00Updated cartel stash for " + cartelName));
    }
}