package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class AddictionManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final Map<UUID, PlayerAddictionData> playerData = new HashMap<>();
    private final Logger logger;

    public AddictionManager(DrugCraft plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.logger = plugin.getLogger();
        startWithdrawalTask();
    }

    public void addDrugUse(Player player, String drugId) {
        PlayerAddictionData data = getPlayerData(player.getUniqueId());
        data.addUse(drugId);
        dataManager.savePlayerData(player.getUniqueId(), data);
    }

    public PlayerAddictionData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerAddictionData());
    }

    public void loadPlayerData() {
        playerData.clear();
        dataManager.loadPlayerData(playerData);
    }

    private void startWithdrawalTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                checkWithdrawal(player);
            }
        }, 0L, 20L * 60); // Check every minute
    }

    private void checkWithdrawal(Player player) {
        PlayerAddictionData data = getPlayerData(player.getUniqueId());
        int threshold = plugin.getConfig().getInt("addiction.use_threshold", 5);
        long withdrawalTime = plugin.getConfig().getLong("addiction.withdrawal_time", 3600) * 1000;
        List<String> effectStrings = plugin.getConfig().getStringList("addiction.withdrawal_effects");

        for (String drugId : data.getUsesMap().keySet()) {
            int uses = data.getUses(drugId);
            long lastUse = data.getLastUse(drugId);
            if (uses >= threshold && (System.currentTimeMillis() - lastUse) > withdrawalTime) {
                for (String effect : effectStrings) {
                    String[] parts = effect.split(":");
                    if (parts.length == 3) {
                        PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                        if (type != null) {
                            int level = Integer.parseInt(parts[1]);
                            int duration = Integer.parseInt(parts[2]) * 20;
                            player.addPotionEffect(new PotionEffect(type, duration, level - 1));
                        }
                    }
                }
                player.sendMessage(ChatColor.RED + "You are experiencing withdrawal symptoms from " + drugId + ".");
            }
        }
    }
}