package com.spence.drugcraft.houses;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class HouseCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final HouseManager houseManager;

    public HouseCommand(DrugCraft plugin, HouseManager houseManager) {
        this.plugin = plugin;
        this.houseManager = houseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.color("&#FF4040This command can only be used by players."));
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(MessageUtils.color("&#FFFF00House Commands:"));
            player.sendMessage(MessageUtils.color("&#D3D3D3/house buy <id> - Purchase a house"));
            player.sendMessage(MessageUtils.color("&#D3D3D3/house list - List your owned houses"));
            player.sendMessage(MessageUtils.color("&#D3D3D3/house available - List available houses"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "buy":
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.color("&#FF4040Usage: /house buy <id>"));
                    return true;
                }
                String houseId = args[1];
                houseManager.purchaseHouse(player, houseId);
                break;
            case "list":
                Set<String> ownedHouses = houseManager.getPlayerHouses(player.getUniqueId());
                if (ownedHouses.isEmpty()) {
                    player.sendMessage(MessageUtils.color("&#FF4040You do not own any houses."));
                    return true;
                }
                player.sendMessage(MessageUtils.color("&#FFFF00Your Houses:"));
                for (String id : ownedHouses) {
                    HouseManager.House house = houseManager.getHouses().get(id);
                    player.sendMessage(MessageUtils.color("&#D3D3D3- " + id + " (World: " + house.getWorldName() + ", Region: " + house.getRegionId() + ")"));
                }
                break;
            case "available":
                player.sendMessage(MessageUtils.color("&#FFFF00Available Houses:"));
                boolean found = false;
                for (HouseManager.House house : houseManager.getHouses().values()) {
                    if (house.getOwner() == null) {
                        found = true;
                        player.sendMessage(MessageUtils.color("&#D3D3D3- " + house.getId() + " (World: " + house.getWorldName() + ", Region: " + house.getRegionId() + ", Price: $" + house.getPrice() + ")"));
                    }
                }
                if (!found) {
                    player.sendMessage(MessageUtils.color("&#FF4040No houses are available for purchase."));
                }
                break;
            default:
                player.sendMessage(MessageUtils.color("&#FF4040Unknown subcommand. Usage: /house [buy|list|available]"));
                break;
        }
        return true;
    }
}