package com.spence.drugcraft.npc;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;

public class DealerListener implements Listener {

    private final DealerManager dealerManager;
    private final Economy economy;
    private final DrugCraft plugin;
    private final Random random = new Random();

    public DealerListener(DrugCraft plugin, DealerManager dealerManager) {
        this.plugin = plugin;
        this.dealerManager = dealerManager;
        this.economy = plugin.getEconomy(); // assuming you have getEconomy() in your main class
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        int npcId = event.getNPC().getId();

        DealerManager.DealerData dealer = dealerManager.getDealerData(npcId);
        if (dealer == null) return;

        for (Drug drug : plugin.getDrugs()) {
            if (player.getInventory().contains(drug.getItem()) && dealer.getAcceptedDrugs().contains(drug.getName())) {

                double chance = random.nextDouble();
                if (chance <= dealer.getSuccessChance()) {
                    // Success
                    player.getInventory().removeItem(drug.getItem());
                    double payout = dealer.getPayoutMap().getOrDefault(drug.getName(), 0.0);
                    economy.depositPlayer(player, payout);
                    player.sendMessage(ChatColor.GREEN + "Sold " + drug.getName() + " for $" + payout + "!");
                } else {
                    // Failure
                    player.sendMessage(ChatColor.RED + "The dealer rejected your " + drug.getName() + ".");
                }

                break; // Only sell one drug per click
            }
        }
    }
}
