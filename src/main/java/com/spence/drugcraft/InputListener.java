package com.spence.drugcraft;

import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class InputListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final EconomyManager economyManager;

    public InputListener(DrugCraft plugin) {
        this.plugin = plugin;
        this.drugManager = plugin.getDrugManager();
        this.economyManager = plugin.getEconomyManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        DrugCraft.ActionState state = plugin.getActionState(player.getUniqueId());
        if (state == null) {
            return;
        }

        try {
            int amount = Integer.parseInt(message);
            if (amount <= 0) {
                player.sendMessage("§cPlease enter a positive number!");
                event.setCancelled(true);
                return;
            }

            String itemName = state.getItemName();
            String action = state.getAction();
            boolean isSeed = state.isSeed();
            Drug drug = drugManager.getDrug(itemName);
            if (drug == null) {
                player.sendMessage("§cInvalid item: " + itemName);
                event.setCancelled(true);
                return;
            }

            ItemStack item = isSeed ? drug.getSeedItem() : drug.getItem();
            if (item == null) {
                player.sendMessage("§cInvalid item: " + itemName);
                event.setCancelled(true);
                return;
            }

            if (action.equalsIgnoreCase("buy")) {
                if (economyManager == null) {
                    player.sendMessage("§cEconomy system unavailable!");
                    event.setCancelled(true);
                    return;
                }
                double totalPrice = (isSeed ? 10.0 : drug.getPrice()) * amount;
                if (economyManager.hasBalance(player, totalPrice)) {
                    economyManager.withdrawMoney(player, totalPrice);
                    ItemStack toAdd = item.clone();
                    toAdd.setAmount(amount);
                    player.getInventory().addItem(toAdd);
                    player.sendMessage("§aBought " + amount + " " + (isSeed ? itemName.replace("cannabis_", "") + " Seed" : drug.getName()) + " for $" + totalPrice);
                } else {
                    player.sendMessage("§cYou don't have enough money!");
                }
            } else if (action.equalsIgnoreCase("sell")) {
                if (economyManager == null) {
                    player.sendMessage("§cEconomy system unavailable!");
                    event.setCancelled(true);
                    return;
                }
                double sellPrice = (isSeed ? 8.0 : drug.getSellPrice()) * amount;
                if (drugManager.removeDrugItem(player, item, amount)) {
                    economyManager.depositMoney(player, sellPrice);
                    player.sendMessage("§aSold " + amount + " " + (isSeed ? itemName.replace("cannabis_", "") + " Seed" : drug.getName()) + " for $" + sellPrice);
                } else {
                    player.sendMessage("§cYou don't have enough items!");
                }
            } else if (action.equalsIgnoreCase("give")) {
                ItemStack toAdd = item.clone();
                toAdd.setAmount(amount);
                player.getInventory().addItem(toAdd);
                player.sendMessage("§aGave yourself " + amount + " " + (isSeed ? itemName.replace("cannabis_", "") + " Seed" : drug.getName()));
            }

            plugin.clearActionState(player.getUniqueId());
            event.setCancelled(true);
        } catch (NumberFormatException e) {
            player.sendMessage("§cPlease enter a valid number!");
            event.setCancelled(true);
        }
    }
}