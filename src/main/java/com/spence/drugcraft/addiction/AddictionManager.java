package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class AddictionManager {
    private final DrugCraft plugin;
    private final Map<UUID, PlayerAddictionData> playerData = new HashMap<>();
    private final Logger logger;

    public AddictionManager(DrugCraft plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        startWithdrawalTask();
    }

    public void addDrugUse(Player player, String drugId) {
        PlayerAddictionData data = getPlayerData(player.getUniqueId());
        data.addUse(drugId);
    }

    public PlayerAddictionData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerAddictionData());
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
        long currentTime