package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
    private static DrugCraft plugin;
    private static final Map<String, String> messages = new HashMap<>();

    public static void init(DrugCraft pluginInstance) {
        plugin = pluginInstance;
        loadMessages();
    }

    private static void loadMessages() {
        FileConfiguration config = plugin.getConfigManager().getConfig("messages.yml");
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                messages.put(key, config.getString(key));
            }
        }
    }

    public static String getMessage(String key, String... placeholders) {
        String message = messages.getOrDefault(key, key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }
        return message;
    }

    public static void sendMessage(CommandSender sender, String key, String... placeholders) {
        String message = getMessage(key, placeholders);
        Component component = MiniMessage.miniMessage().deserialize(message);
        sender.sendMessage(component);
    }
}