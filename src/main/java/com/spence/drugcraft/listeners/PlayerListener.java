package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.CartelManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

public class PlayerListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final CartelManager cartelManager;
    private final Logger logger;
    private final Random random = new Random();

    public PlayerListener(DrugCraft plugin, DrugManager drugManager, CartelManager cartelManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.cartelManager = cartelManager;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || !event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (drugManager.isDrugItem(item)) {
            event.setCancelled(true);
            drugManager.useDrug(player, item);
            logger.info("Player " + player.getName() + " used drug item: " + item.getItemMeta().getDisplayName());
        } else if (item.getType() == Material.SHEARS && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            // Check for trimmer crafting (combine shears with rare items)
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand != null && (offHand.getType() == Material.DIAMOND || offHand.getType() == Material.EMERALD)) {
                event.setCancelled(true);
                String rarity = offHand.getType() == Material.DIAMOND ? "Epic" : "Rare";
                ItemStack trimmer = new ItemStack(Material.SHEARS);
                ItemMeta meta = trimmer.getItemMeta();
                meta.setDisplayName(MessageUtils.color("&6&l" + rarity + " Trimmer"));
                meta.setLore(Arrays.asList(MessageUtils.color("&7Trimmer Rarity: " + rarity)));
                trimmer.setItemMeta(meta);
                player.getInventory().setItemInMainHand(trimmer);
                offHand.setAmount(offHand.getAmount() - 1);
                player.sendMessage(MessageUtils.color("&aCrafted " + rarity + " Trimmer!"));
                logger.info("Player " + player.getName() + " crafted " + rarity + " trimmer");
            }
        }
    }
}