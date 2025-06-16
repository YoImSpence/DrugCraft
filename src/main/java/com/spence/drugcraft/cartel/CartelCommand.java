package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CartelCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;
    private final CartelGUI cartelGUI;
    private final EconomyManager economyManager;

    public CartelCommand(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
        this.economyManager = new EconomyManager(plugin);
        this.cartelGUI = new CartelGUI(plugin, cartelManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            plugin.getLogger().info("Cartel command attempted by non-player: " + sender.getName());
            return true;
        }
        Player player = (Player) sender;

        plugin.getLogger().info("Player " + player.getName() + " executed cartel command with args: " + String.join(" ", args));

        if (args.length == 0) {
            MessageUtils.sendMessage(player, "cartel.usage");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        try {
            switch (subCommand) {
                case "create":
                    if (args.length < 2) {
                        MessageUtils.sendMessage(player, "cartel.create-usage");
                        return true;
                    }
                    String cartelName = args[1];
                    if (!economyManager.isEconomyAvailable()) {
                        MessageUtils.sendMessage(player, "general.no-economy");
                        plugin.getLogger().warning("Economy unavailable for cartel creation by player " + player.getName());
                        return true;
                    }
                    String createResult = cartelManager.createCartel(player, cartelName);
                    switch (createResult) {
                        case "success":
                            MessageUtils.sendMessage(player, "cartel.created", "cartel_name", cartelName);
                            break;
                        case "already_in_cartel":
                            MessageUtils.sendMessage(player, "cartel.create-failed", "reason", "You are already in a cartel");
                            break;
                        case "name_taken":
                            MessageUtils.sendMessage(player, "cartel.create-failed", "reason", "Cartel name is taken");
                            break;
                        case "insufficient_funds":
                            MessageUtils.sendMessage(player, "cartel.create-insufficient-funds", "amount", "5000.0");
                            break;
                        default:
                            MessageUtils.sendMessage(player, "cartel.create-failed", "reason", "Unknown error");
                            plugin.getLogger().warning("Unknown error in cartel creation for player " + player.getName());
                    }
                    break;
                case "join":
                    if (args.length < 2) {
                        MessageUtils.sendMessage(player, "cartel.join-usage");
                        return true;
                    }
                    cartelName = args[1];
                    if (cartelManager.joinCartel(player, cartelName)) {
                        MessageUtils.sendMessage(player, "cartel.joined", "cartel_name", cartelName);
                    } else {
                        MessageUtils.sendMessage(player, "cartel.join-failed");
                    }
                    break;
                case "leave":
                    if (cartelManager.leaveCartel(player)) {
                        MessageUtils.sendMessage(player, "cartel.left");
                    } else {
                        MessageUtils.sendMessage(player, "cartel.leave-failed");
                    }
                    break;
                case "info":
                    if (args.length < 2) {
                        MessageUtils.sendMessage(player, "cartel.info-usage");
                        return true;
                    }
                    cartelName = args[1];
                    UUID leaderUUID = cartelManager.getCartelLeader(cartelName);
                    if (leaderUUID == null) {
                        MessageUtils.sendMessage(player, "cartel.not-found", "cartel_name", cartelName);
                        return true;
                    }
                    String leaderName = Bukkit.getOfflinePlayer(leaderUUID).getName();
                    List<String> memberNames = cartelManager.getCartelMembers(cartelName).stream()
                            .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                            .collect(Collectors.toList());
                    int level = cartelManager.getCartelLevel(cartelName);
                    MessageUtils.sendMessage(player, "cartel.info", "cartel_name", cartelName,
                            "leader", leaderName, "members", String.join(", ", memberNames), "level", String.valueOf(level));
                    break;
                case "gui":
                    cartelGUI.openMainMenu(player);
                    plugin.getLogger().info("Opened Cartel GUI for player " + player.getName());
                    break;
                default:
                    MessageUtils.sendMessage(player, "cartel.usage");
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error handling cartel command for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            MessageUtils.sendMessage(player, "general.error");
        }
        return true;
    }
}