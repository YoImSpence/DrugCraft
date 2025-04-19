package com.spence.drugcraft;

import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.economy.EconomyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DrugCommand implements CommandExecutor {
    private final DrugCraft plugin;

    public DrugCommand(DrugCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            plugin.getDrugGUI().openMainMenu(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "gui":
                plugin.getDrugGUI().openMainMenu(player);
                return true;

            case "give":
                if (!player.hasPermission("drugcraft.give")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /drug give <drug> <amount>");
                    return true;
                }
                String drugName = args[1];
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cAmount must be a number!");
                    return true;
                }
                Drug drug = plugin.getDrugManager().getDrugByName(drugName);
                if (drug == null) {
                    player.sendMessage("§cUnknown drug: " + drugName);
                    return true;
                }
                if (amount <= 0) {
                    player.sendMessage("§cAmount must be greater than 0!");
                    return true;
                }
                ItemStack item = drug.getItem().clone();
                item.setAmount(amount);
                player.getInventory().addItem(item);
                player.sendMessage("§aGave " + amount + " " + drug.getName());
                return true;

            case "sell":
                if (args.length != 3) {
                    player.sendMessage("§cUsage: /drug sell <drug> <amount>");
                    return true;
                }
                drugName = args[1];
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cAmount must be a number!");
                    return true;
                }
                plugin.getEconomyManager().sellDrug(player, drugName, amount);
                return true;

            case "info":
                int addictionLevel = plugin.getAddictionManager().getAddictionLevel(player);
                player.sendMessage("§aYour addiction level: " + addictionLevel);
                return true;

            case "reset":
                if (!player.hasPermission("drugcraft.admin")) {
                    player.sendMessage("§cYou don't have permission!");
                    return true;
                }
                plugin.getAddictionManager().resetAddiction(player);
                player.sendMessage("§aAddiction reset!");
                return true;

            default:
                player.sendMessage("§cUnknown subcommand: " + subcommand);
                return true;
        }
    }
}