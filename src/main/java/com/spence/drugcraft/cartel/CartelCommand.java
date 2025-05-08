package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CartelCommand implements CommandExecutor {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelCommand(DrugCraft plugin, CartelManager cartelManager) {
        this.plugin = plugin;
        this.cartelManager = cartelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.color("&cThis command can only be used by players."));
            return true;
        }
        Player player = (Player) sender;
        if (!plugin.getPermissionManager().hasPermission(player, "drugcraft.cartel")) {
            player.sendMessage(MessageUtils.color("&cYou do not have permission to use this command."));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(MessageUtils.color("&eUsage: /cartel <create|invite|join|leave|info|manage>"));
            return true;
        }
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.color("&cUsage: /cartel create <name>"));
                    return true;
                }
                cartelManager.createCartel(player, args[1]);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.color("&cUsage: /cartel invite <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(MessageUtils.color("&cPlayer not found."));
                    return true;
                }
                cartelManager.invitePlayer(player, target);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.color("&cUsage: /cartel join <cartel>"));
                    return true;
                }
                cartelManager.joinCartel(player, args[1]);
                break;
            case "leave":
                cartelManager.leaveCartel(player);
                break;
            case "info":
                cartelManager.showCartelInfo(player);
                break;
            case "manage":
                new CartelGUI(plugin, cartelManager).openGUI(player);
                break;
            default:
                player.sendMessage(MessageUtils.color("&eUsage: /cartel <create|invite|join|leave|info|manage>"));
        }
        return true;
    }
}