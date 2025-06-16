package com.spence.drugcraft.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class MessageUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static Logger logger;
    private static FileConfiguration messagesConfig;

    // Initialize the logger and load messages.yml
    public static void setLogger(Logger pluginLogger) {
        logger = pluginLogger;
        if (logger != null) {
            logger.info("MessageUtils logger initialized successfully");
        }
    }

    public static void loadMessages(JavaPlugin plugin) {
        try {
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            if (!messagesFile.exists()) {
                plugin.saveResource("messages.yml", false);
            }
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            if (logger != null) {
                logger.info("Loaded messages.yml");
            }
        } catch (Exception e) {
            if (logger != null) {
                logger.severe("Failed to load messages.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Get a message from messages.yml with placeholder replacement
    public static String getMessage(String key, Object... placeholders) {
        String message;
        if (messagesConfig == null) {
            if (logger != null) {
                logger.warning("messagesConfig is null; failed to retrieve message for key: " + key);
            }
            message = "<red>Error: Message configuration not loaded (key: " + key + ")";
        } else {
            message = messagesConfig.getString(key, "<red>Missing message: " + key);
            if (message == null) {
                if (logger != null) {
                    logger.warning("Message key '" + key + "' not found in messages.yml");
                }
                message = "<red>Missing message: " + key;
            }
        }

        // Replace placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = "{" + placeholders[i].toString() + "}";
            String value = placeholders[i + 1].toString();
            message = message.replace(placeholder, value);
        }

        return message;
    }

    // Parse a string with MiniMessage to create a Component with colors and formatting
    public static Component color(String message) {
        if (message == null) return Component.empty();

        // Convert legacy & codes to MiniMessage format for backward compatibility
        message = message.replace("&", "§");
        message = message.replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§r", "<reset>");

        // Convert custom hex codes (&#FF4040) to MiniMessage format (<#FF4040>)
        message = message.replaceAll("(?i)&#([0-9A-F]{6})", "<#$1>");

        // Parse the message with MiniMessage
        Component component = MINI_MESSAGE.deserialize(message);

        // Log the translated message for debugging
        if (logger != null) {
            logger.info("Translated message: " + MINI_MESSAGE.serialize(component));
        }

        return component;
    }

    // Send a message to a player
    public static void sendMessage(Player player, String key, Object... placeholders) {
        String message = getMessage(key, placeholders);
        Component component = color(message);
        player.sendMessage(component);
    }

    // Send a message to a command sender
    public static void sendMessage(CommandSender sender, String key, Object... placeholders) {
        String message = getMessage(key, placeholders);
        Component component = color(message);
        sender.sendMessage(component);
    }

    // Broadcast a message to all players and console
    public static void broadcastMessage(String key, Object... placeholders) {
        String message = getMessage(key, placeholders);
        Component component = color(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }
        // Also send to console
        Bukkit.getConsoleSender().sendMessage(component);
    }

    // Strip all color and formatting from a message
    public static String stripColor(String message) {
        if (message == null) return null;
        Component component = color(message);
        return MINI_MESSAGE.stripTags(MINI_MESSAGE.serialize(component));
    }

    // Strip color from a Component
    public static String stripColor(Component component) {
        if (component == null) return null;
        return MINI_MESSAGE.stripTags(MINI_MESSAGE.serialize(component));
    }
}