package com.spence.drugcraft;

import com.spence.drugcraft.addiction.AddictionManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodically applies withdrawal effects to addicted players.
 */
public class WithdrawalTask extends BukkitRunnable {
    private final DrugCraft plugin;
    private final AddictionManager addictionManager;

    public WithdrawalTask(DrugCraft plugin) {
        this.plugin = plugin;
        this.addictionManager = plugin.getAddictionManager();
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (addictionManager.getAddictionLevel(player) > 0) {
                addictionManager.applyWithdrawalEffects(player);
                plugin.getLogger().info("Applied withdrawal effects to " + player.getName());
            }
        }
    }
}