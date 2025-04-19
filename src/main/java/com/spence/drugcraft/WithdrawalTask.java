package com.spence.drugcraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WithdrawalTask extends BukkitRunnable {
    private final AddictionManager addictionManager;

    public WithdrawalTask(AddictionManager addictionManager) {
        this.addictionManager = addictionManager;
    }

    @Override
    public void run() {
        List<Player> addictedPlayers = addictionManager.getAddictionLevels().keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline() && addictionManager.getAddictionLevel(player) > 0)
                .collect(Collectors.toList());

        for (Player player : addictedPlayers) {
            int level = addictionManager.getAddictionLevel(player);
            if (level > 10) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0)); // 10s Nausea
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 0)); // 10s Weakness
                player.sendMessage("§cYou feel withdrawal symptoms...");
            }
            addictionManager.addAddiction(player, -1);
        }
    }
}