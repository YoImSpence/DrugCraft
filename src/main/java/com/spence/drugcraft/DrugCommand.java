package com.spence.drugcraft;

import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the /drug command for using, giving, and opening the GUI.
 */
public class DrugCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final DrugGUI drugGUI;

    public DrugCommand(DrugCraft plugin) {
        this.plugin = plugin;
        this.drugManager = plugin.getDrugManager();
        this.drugGUI = plugin.getDrugGUI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            drugGUI.openMainGUI(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("use") && args.length == 2) {
            String inputKey = args[1].toLowerCase().replace("_seed", "");
            String drugKey = drugManager.getDrugs().keySet().stream()
                    .filter(key -> key.equalsIgnoreCase(inputKey) || ("cannabis_" + inputKey).equalsIgnoreCase(key))
                    .findFirst()
                    .orElse(inputKey);
            drugManager.useDrug(player, drugKey);
            return true;
        }

        if (args[0].equalsIgnoreCase("give") && args.length == 2 && player.hasPermission("drugcraft.admin.give")) {
            String inputKey = args[1].toLowerCase().replace("_seed", "");
            String drugKey = drugManager.getDrugs().keySet().stream()
                    .filter(key -> key.equalsIgnoreCase(inputKey) || ("cannabis_" + inputKey).equalsIgnoreCase(key))
                    .findFirst()
                    .orElse(inputKey);
            Drug drug = drugManager.getDrug(drugKey);
            if (drug != null) {
                ItemStack item = args[1].toLowerCase().endsWith("_seed") ? drug.getSeedItem() : drug.getItem();
                if (item != null) {
                    player.getInventory().addItem(item);
                    player.sendMessage("§aGave yourself 1 " + (args[1].toLowerCase().endsWith("_seed") ? inputKey + " Seed" : drug.getName()));
                } else {
                    player.sendMessage("§cInvalid item: " + args[1]);
                }
            } else {
                player.sendMessage("§cInvalid drug or seed: " + args[1]);
                plugin.getLogger().warning("Invalid drug/seed key: " + drugKey);
            }
            return true;
        }

        player.sendMessage("§cUsage: /drug [use <drug>|give <drug|seed>]");
        return true;
    }
}