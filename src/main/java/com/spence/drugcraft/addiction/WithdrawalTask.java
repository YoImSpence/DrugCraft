package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class WithdrawalTask implements Runnable {
    private final DrugCraft plugin;
    private final AddictionManager addictionManager;

    public WithdrawalTask(DrugCraft plugin, AddictionManager addictionManager) {
        this.plugin = plugin;
        this.addictionManager = addictionManager;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerAddictionData data = addictionManager.getPlayerData(player.getUniqueId());
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
}