package com.spence.drugcraft.gui;

import org.bukkit.entity.Player;

public interface ChatInputHandler {
    void handleChatInput(Player player, String action, String message, Object context);
}