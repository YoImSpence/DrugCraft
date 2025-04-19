package com.spence.drugcraft.economy;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class EconomyManager {
    private final DrugCraft plugin;
    private final Economy economy;

    public EconomyManager(DrugCraft plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        if (economy != null) {
            plugin.getLogger().info("Economy provider hooked: " + economy.getName());
        } else {
            plugin.getLogger().warning("No economy provider found!");
        }
    }

    public void sellDrug(Player player, String drugName, int amount) {
        if (economy == null) {
            player.sendMessage("§cEconomy system is unavailable.");
            return;
        }

        Drug drug = plugin.getDrugManager().getDrugByName(drugName);
        if (drug == null) {
            player.sendMessage("§cUnknown drug: " + drugName);
            return;
        }

        if (amount <= 0) {
            player.sendMessage("§cAmount must be greater than 0!");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerInventory inventory = player.getInventory();
                ItemStack drugItem = drug.getItem().clone();
                int itemsRemoved = 0;

                for (ItemStack item : inventory.getContents()) {
                    if (item != null && isMatchingDrugItem(item, drug)) {
                        itemsRemoved += item.getAmount();
                    }
                }

                if (itemsRemoved < amount) {
                    player.sendMessage("§cYou don't have enough " + drug.getName() + " to sell! Have: " + itemsRemoved);
                    return;
                }

                itemsRemoved = 0;
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && isMatchingDrugItem(item, drug)) {
                        int itemAmount = item.getAmount();
                        if (itemsRemoved + itemAmount <= amount) {
                            inventory.remove(item);
                            itemsRemoved += itemAmount;
                        } else {
                            item.setAmount(itemAmount - (amount - itemsRemoved));
                            itemsRemoved = amount;
                            break;
                        }
                    }
                }

                double totalPrice = drug.getSellPrice() * amount;
                economy.depositPlayer(player, totalPrice);
                player.sendMessage("§aSold " + amount + " " + drug.getName() + " for $" + String.format("%.2f", totalPrice));
            }
        }.runTask(plugin);
    }

    public void buyDrug(Player player, String drugName, int amount) {
        if (economy == null) {
            player.sendMessage("§cEconomy system is unavailable.");
            return;
        }

        Drug drug = plugin.getDrugManager().getDrugByName(drugName);
        if (drug == null) {
            player.sendMessage("§cUnknown drug: " + drugName);
            return;
        }

        if (amount <= 0) {
            player.sendMessage("§cAmount must be greater than 0!");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                double buyPrice = drug.getSellPrice() * 1.5;
                double totalCost = buyPrice * amount;
                double balance = economy.getBalance(player);

                if (balance < totalCost) {
                    player.sendMessage("§cYou don't have enough money! Need $" + String.format("%.2f", totalCost));
                    return;
                }

                economy.withdrawPlayer(player, totalCost);
                ItemStack item = drug.getItem().clone();
                item.setAmount(amount);
                player.getInventory().addItem(item);
                player.sendMessage("§aBought " + amount + " " + drug.getName() + " for $" + String.format("%.2f", totalCost));
            }
        }.runTask(plugin);
    }

    private boolean isMatchingDrugItem(ItemStack item, Drug drug) {
        ItemStack drugItem = drug.getItem();
        if (item.getType() != drugItem.getType()) return false;
        if (!item.hasItemMeta() || !drugItem.hasItemMeta()) return false;
        return item.getItemMeta().getDisplayName().equals(drugItem.getItemMeta().getDisplayName());
    }
}