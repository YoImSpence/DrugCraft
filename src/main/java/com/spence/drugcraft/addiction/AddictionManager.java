package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class AddictionManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final PoliceManager policeManager;

    public AddictionManager(DrugCraft plugin, DataManager dataManager, DrugManager drugManager, PoliceManager policeManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
        this.policeManager = policeManager;
        startAddictionCheck();
    }

    public void applyDrugEffect(Player player, String drugId) {
        Drug drug = drugManager.getDrug(drugId);
        if (drug == null) return;

        double severity = dataManager.getAddictionSeverity(player.getUniqueId(), drugId) + 0.1;
        long lastUse = System.currentTimeMillis();
        dataManager.saveAddiction(player.getUniqueId(), drugId, severity, lastUse);

        // Apply potion effects based on drug
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (drug.getDuration() * 20), (int) drug.getStrength()));
        MessageUtils.sendMessage(player, "addiction.applied", "drug_id", drugId, "severity", String.valueOf(severity));
    }

    public void checkAddiction(Player player) {
        // Addiction effects applied only on drug use
    }

    private void startAddictionCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Periodic checks for addiction effects (e.g., withdrawal)
                }
            }
        }.runTaskTimer(plugin, 0L, 1200L);
    }
}