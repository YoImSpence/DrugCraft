package com.spence.drugcraft.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {
    public static String color(String message) {
        if (message == null) {
            return null;
        }
        // Map hex colors to legacy codes
        String formatted = message
                .replaceAll("\\{#FF5555\\}", "&c") // Red
                .replaceAll("\\{#55FF55\\}", "&a") // Green
                .replaceAll("\\{#FFFF55\\}", "&e") // Yellow
                .replaceAll("\\{#55FFFF\\}", "&b") // Cyan
                .replaceAll("\\{#FF55FF\\}", "&d") // Magenta
                .replaceAll("\\{#5555FF\\}", "&9") // Blue
                .replaceAll("\\{#AAAAAA\\}", "&7") // Gray
                .replaceAll("\\{#FFFFFF\\}", "&f"); // White
        return ChatColor.translateAlternateColorCodes('&', formatted);
    }

    public static void sendMessage(Player player, String message) {
        if (player != null && message != null) {
            player.sendMessage(color(message));
        }
    }
}