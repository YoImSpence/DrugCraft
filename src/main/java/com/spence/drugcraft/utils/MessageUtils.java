package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class MessageUtils {
    private static DrugCraft plugin;
    private static FileConfiguration messagesConfig;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void init(DrugCraft pluginInstance) {
        plugin = pluginInstance;
        messagesConfig = plugin.getConfig("messages.yml");
    }

    public static String getMessage(String key, String... placeholders) {
        String message = messagesConfig.getString(key, "<#FF5555>Message not found: " + key);
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }

        if (key.startsWith("gui.") || key.contains(".quality")) {
            return "<bold>" + message + "</bold>";
        }
        return message;
    }

    public static void sendMessage(Player player, String key, String... placeholders) {
        String message = getMessage(key, placeholders);
        Component component = miniMessage.deserialize(message);
        player.sendMessage(component);
    }

    public static String stripColor(String input) {
        return miniMessage.stripTags(input);
    }
}