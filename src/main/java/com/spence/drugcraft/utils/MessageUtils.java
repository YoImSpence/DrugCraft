package com.spence.drugcraft.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&#([0-9a-f]{6})");
    private static final Logger LOGGER = Logger.getLogger("DrugCraft");

    public static String color(String message) {
        if (message == null) {
            return null;
        }
        // Handle HEX color codes in the format &#RRGGBB
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            if (hexCode != null && hexCode.length() == 6) {
                matcher.appendReplacement(result, convertHexToMinecraftColor(hexCode));
            } else {
                LOGGER.warning("Malformed HEX code in message: " + matcher.group() + " (full message: " + message + ")");
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        message = result.toString();
        // Then, handle legacy color codes with & (in case any remain)
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendMessage(Player player, String message) {
        if (player != null && message != null) {
            player.sendMessage(color(message));
        }
    }

    private static String convertHexToMinecraftColor(String hex) {
        StringBuilder result = new StringBuilder("\u00A7x");
        for (char c : hex.toUpperCase().toCharArray()) {
            result.append("\u00A7").append(c);
        }
        return result.toString();
    }
}