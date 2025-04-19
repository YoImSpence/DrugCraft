package com.spence.drugcraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class InputListener implements Listener {
    private final DrugCraft plugin;
    private final Map<UUID, Consumer<String>> inputCallbacks;
    private final ReentrantLock lock;

    public InputListener(DrugCraft plugin) {
        this.plugin = plugin;
        this.inputCallbacks = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public void requestInput(Player player, String prompt, Consumer<String> callback) {
        lock.lock();
        try {
            player.sendMessage("§e" + prompt);
            inputCallbacks.put(player.getUniqueId(), callback);
        } finally {
            lock.unlock();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Consumer<String> callback;
        lock.lock();
        try {
            callback = inputCallbacks.remove(uuid);
            if (callback == null) return;
        } finally {
            lock.unlock();
        }

        event.setCancelled(true);
        String input = event.getMessage();
        callback.accept(input);
    }
}