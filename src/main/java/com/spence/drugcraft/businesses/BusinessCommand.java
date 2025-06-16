package com.spence.drugcraft.businesses;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class BusinessCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final BusinessManager businessManager;
    private final BusinessGUI businessGUI;
    private final EconomyManager economyManager;

    public BusinessCommand(DrugCraft plugin, BusinessManager businessManager) {
        this.plugin = plugin;
        this.businessManager = businessManager;
        this.economyManager = new EconomyManager(plugin);
        this.businessGUI = new BusinessGUI(plugin, businessManager, plugin.getDataManager());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            plugin.getLogger().info("Business command attempted by non-player: " + sender.getName());
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("drugcraft.business")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            plugin.getLogger().info("Player " + player.getName() + " attempted business command without permission");
            return true;
        }

        plugin.getLogger().info("Player " + player.getName() + " executed business command with args: " + String.join(" ", args));

        if (args.length == 0) {
            businessGUI.openMainMenu(player);
            plugin.getLogger().info("Opened Business GUI for player " + player.getName());
            return true;
        }

        String subCommand = args[0].toLowerCase();
        try {
            switch (subCommand) {
                case "list":
                    Map<String, Business> businesses = businessManager.getBusinesses();
                    if (businesses.isEmpty()) {
                        MessageUtils.sendMessage(player, "business.list-empty");
                        return true;
                    }
                    MessageUtils.sendMessage(player, "business.list-header");
                    for (Business business : businesses.values()) {
                        String ownerName = business.getOwnerUUID() != null ? Bukkit.getOfflinePlayer(business.getOwnerUUID()).getName() : "None";
                        MessageUtils.sendMessage(player, "business.list-entry",
                                "id", business.getId(),
                                "name", business.getName(),
                                "owner", ownerName,
                                "price", String.format("%.2f", business.getPrice()),
                                "revenue", String.format("%.2f", business.getRevenue()));
                    }
                    break;
                case "buy":
                    if (args.length != 2) {
                        MessageUtils.sendMessage(player, "business.usage-buy");
                        return true;
                    }
                    String businessId = args[1];
                    if (!economyManager.isEconomyAvailable()) {
                        MessageUtils.sendMessage(player, "general.no-economy");
                        plugin.getLogger().warning("Economy unavailable for business purchase by player " + player.getName());
                        return true;
                    }
                    if (businessManager.purchaseBusiness(player, businessId)) {
                        MessageUtils.sendMessage(player, "business.purchase-success",
                                "id", businessId,
                                "name", businessManager.getBusiness(businessId).getName());
                    } else {
                        MessageUtils.sendMessage(player, "business.purchase-failed", "reason", "Purchase failed");
                    }
                    break;
                default:
                    MessageUtils.sendMessage(player, "business.usage");
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error handling business command for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            MessageUtils.sendMessage(player, "business.error");
        }

        return true;
    }
}