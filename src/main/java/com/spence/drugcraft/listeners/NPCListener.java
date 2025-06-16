package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.town.TownCitizenManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NPCListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final EconomyManager economyManager;
    private final TownCitizenManager townCitizenManager;

    public NPCListener(DrugCraft plugin, DrugManager drugManager, EconomyManager economyManager, TownCitizenManager townCitizenManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economyManager = economyManager;
        this.townCitizenManager = townCitizenManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        NPC npc = event.getNPC();
        ItemStack selectedDrug = player.getInventory().getItemInMainHand();

        if (selectedDrug == null || !drugManager.isDrugItem(selectedDrug)) {
            MessageUtils.sendMessage(player, "deal.no-drugs");
            plugin.getLogger().warning("Player " + player.getName() + " attempted deal with NPC ID " + npc.getId() + " without a drug item");
            return;
        }

        int quantity = selectedDrug.getAmount();
        if (quantity <= 0) {
            MessageUtils.sendMessage(player, "general.quantity-positive");
            plugin.getLogger().warning("Player " + player.getName() + " attempted deal with NPC ID " + npc.getId() + " with invalid quantity");
            return;
        }

        String drugId = drugManager.getDrugIdFromItem(selectedDrug);
        String quality = drugManager.getQualityFromItem(selectedDrug);
        Drug drug = drugManager.getDrug(drugId);
        if (drug == null) {
            MessageUtils.sendMessage(player, "general.error");
            plugin.getLogger().warning("Invalid drug ID: " + drugId + " for player " + player.getName());
            return;
        }

        double basePrice = drug.getPrice() * quantity;
        double qualityMultiplier = switch (quality.toLowerCase()) {
            case "prime" -> 1.3;
            case "exotic" -> 1.5;
            case "legendary" -> 2.0;
            case "cosmic" -> 3.0;
            default -> 1.0;
        };
        double price = basePrice * qualityMultiplier;

        List<Location> meetupSpots = townCitizenManager.getMeetupSpots();
        if (meetupSpots.isEmpty()) {
            MessageUtils.sendMessage(player, "general.error");
            plugin.getLogger().warning("No meetup spots available for deal with NPC ID " + npc.getId());
            return;
        }

        townCitizenManager.initiateDeal(player, npc.getId(), selectedDrug, quantity, price, meetupSpots);
        MessageUtils.sendMessage(player, "deal.offer", "npc_name", npc.getName(), "quantity", String.valueOf(quantity), "drug", drugId, "price", String.format("%.2f", price));
        plugin.getLogger().info("Player " + player.getName() + " initiated deal with NPC ID " + npc.getId() + " for " + quantity + " " + drugId + " at $" + price);
    }
}