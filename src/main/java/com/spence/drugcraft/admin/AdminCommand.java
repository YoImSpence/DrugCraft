package com.spence.drugcraft.admin;

import com.spence.drugcraft.gui.AdminGUIHandler;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AdminCommand implements CommandExecutor {
    private final AdminGUIHandler adminGUIHandler;

    public AdminCommand(AdminGUIHandler adminGUIHandler) {
        this.adminGUIHandler = adminGUIHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, "general.player-only");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("drugcraft.admin")) {
            MessageUtils.sendMessage(player, "general.no-permission");
            return true;
        }

        if (args.length == 0) {
            adminGUIHandler.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setlevel":
                handleSetLevel(player, args);
                break;
            case "givexp":
                handleGiveXP(player, args);
                break;
            case "resetxp":
                handleResetXP(player, args);
                break;
            case "stats":
                handleViewStats(player, args);
                break;
            case "inv":
                handleViewInventory(player, args);
                break;
            default:
                MessageUtils.sendMessage(player, "general.invalid-input");
                break;
        }

        return true;
    }

    private void handleSetLevel(Player player, String[] args) {
        if (args.length != 3) {
            MessageUtils.sendMessage(player, "general.invalid-input");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(player, "general.player-offline");
            return;
        }
        try {
            int level = Integer.parseInt(args[2]);
            adminGUIHandler.setLevel(target, level);
            MessageUtils.sendMessage(player, "admin.level-set", "level", String.valueOf(level), "player_name", target.getName());
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "general.invalid-input");
        }
    }

    private void handleGiveXP(Player player, String[] args) {
        if (args.length != 4) {
            MessageUtils.sendMessage(player, "general.invalid-input");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(player, "general.player-offline");
            return;
        }
        try {
            long xp = Long.parseLong(args[3]);
            String skill = args[2];
            adminGUIHandler.giveXP(target, skill, xp);
            MessageUtils.sendMessage(player, "admin.xp-given", "xp", String.valueOf(xp), "skill", skill, "player_name", target.getName());
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "general.invalid-input");
        }
    }

    private void handleResetXP(Player player, String[] args) {
        if (args.length != 3) {
            MessageUtils.sendMessage(player, "general.invalid-input");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(player, "general.player-offline");
            return;
        }
        String skill = args[2];
        adminGUIHandler.resetXP(target, skill);
        MessageUtils.sendMessage(player, "admin.xp-reset", "player_name", target.getName(), "skill", skill);
    }

    private void handleViewStats(Player player, String[] args) {
        if (args.length != 2) {
            MessageUtils.sendMessage(player, "general.invalid-input");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(player, "general.player-offline");
            return;
        }
        adminGUIHandler.viewStats(player, target);
        MessageUtils.sendMessage(player, "admin.stats-viewed", "player_name", target.getName());
    }

    private void handleViewInventory(Player player, String[] args) {
        if (args.length != 2) {
            MessageUtils.sendMessage(player, "general.invalid-input");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(player, "general.player-offline");
            return;
        }
        adminGUIHandler.viewInventory(player, target);
        MessageUtils.sendMessage(player, "admin.inventory-viewed", "player_name", target.getName());
    }
}
