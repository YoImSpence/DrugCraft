package com.spence.drugcraft.addiction;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.police.PoliceManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddictionManager {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final PoliceManager policeManager;
    private final Map<UUID, Map<String, Addiction>> playerAddictions = new HashMap<>();

    public AddictionManager(DrugCraft plugin, DataManager dataManager, DrugManager drugManager, PoliceManager policeManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
        this.policeManager = policeManager;
        startWithdrawalChecker();
    }

    public void applyAddiction(Player player, String drugId, String quality) {
        UUID playerUUID = player.getUniqueId();
        Drug drug = drugManager.getDrug(drugId);
        if (drug == null) return;

        playerAddictions.computeIfAbsent(playerUUID, k -> new HashMap<>());
        Addiction addiction = playerAddictions.get(playerUUID).computeIfAbsent(drugId, k -> new Addiction(drugId, 0, System.currentTimeMillis()));

        double addictionRate = plugin.getConfig("drugs.yml").getDouble("drugs." + drugId + ".addiction.rate", 0.1);
        addiction.increaseLevel(addictionRate);
        addiction.updateLastUse();

        dataManager.saveAddiction(playerUUID, drugId, addiction.getLevel(), addiction.getLastUse());
        MessageUtils.sendMessage(player, "addiction.increased", "message", "Addiction level increased for " + drug.getName() + "!");

        applyDrugEffects(player, drug, quality);
    }

    public void checkWithdrawal(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<String, Addiction> addictions = playerAddictions.get(playerUUID);
        if (addictions == null) return;

        for (Addiction addiction : addictions.values()) {
            long lastUse = addiction.getLastUse();
            double withdrawalDelay = plugin.getConfig("drugs.yml").getDouble("drugs." + addiction.getDrugId() + ".addiction.withdrawalDelay", 600000);
            if (System.currentTimeMillis() - lastUse > withdrawalDelay) {
                applyWithdrawalEffects(player, addiction.getDrugId());
            }
        }
    }

    public void soberUp(Player player, String drugId) {
        UUID playerUUID = player.getUniqueId();
        Map<String, Addiction> addictions = playerAddictions.get(playerUUID);
        if (addictions == null || !addictions.containsKey(drugId)) return;

        addictions.remove(drugId);
        dataManager.removeAddiction(playerUUID, drugId);
        MessageUtils.sendMessage(player, "addiction.sobered", "message", "You’re clean from " + drugId + "!");
    }

    private void applyDrugEffects(Player player, Drug drug, String quality) {
        double effectMultiplier = quality.equals("high") ? 1.5 : quality.equals("low") ? 0.5 : 1.0;
        String effectType = plugin.getConfig("drugs.yml").getString("drugs." + drug.getId() + ".effect", "SPEED");
        int duration = (int) (plugin.getConfig("drugs.yml").getInt("drugs." + drug.getId() + ".effectDuration", 600) * effectMultiplier);
        int amplifier = (int) (plugin.getConfig("drugs.yml").getInt("drugs." + drug.getId() + ".effectAmplifier", 1) * effectMultiplier);

        PotionEffect effect = new PotionEffect(PotionEffectType.getByName(effectType), duration, amplifier);
        player.addPotionEffect(effect);
    }

    private void applyWithdrawalEffects(Player player, String drugId) {
        String effectType = plugin.getConfig("drugs.yml").getString("drugs." + drugId + ".addiction.withdrawalEffect", "SLOWNESS");
        int duration = plugin.getConfig("drugs.yml").getInt("drugs." + drugId + ".addiction.withdrawalDuration", 600);
        int amplifier = plugin.getConfig("drugs.yml").getInt("drugs." + drugId + ".addiction.withdrawalAmplifier", 1);

        PotionEffect effect = new PotionEffect(PotionEffectType.getByName(effectType), duration, amplifier);
        player.addPotionEffect(effect);
        MessageUtils.sendMessage(player, "addiction.withdrawal", "message", "Withdrawal symptoms from " + drugId + "!");
    }

    private void startWithdrawalChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    checkWithdrawal(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 1200L);
    }

    private static class Addiction {
        private final String drugId;
        private double level;
        private long lastUse;

        public Addiction(String drugId, double level, long lastUse) {
            this.drugId = drugId;
            this.level = level;
            this.lastUse = lastUse;
        }

        public String getDrugId() { return drugId; }
        public double getLevel() { return level; }
        public long getLastUse() { return lastUse; }

        public void increaseLevel(double amount) { this.level += amount; }
        public void updateLastUse() { this.lastUse = System.currentTimeMillis(); }
    }
}