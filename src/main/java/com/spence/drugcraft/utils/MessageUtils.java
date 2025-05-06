package com.spence.drugcraft.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("\\{#([A-Fa-f0-9]{6})\\}");

    public static String color(String message) {
        if (message == null) {
            return null;
        }
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = "";
            for (char c : hex.toCharArray()) {
                replacement += ChatColor.COLOR_CHAR + "" + Character.toLowerCase(c);
            }
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}