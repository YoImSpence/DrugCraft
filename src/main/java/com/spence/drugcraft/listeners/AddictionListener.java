package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.AddictionManager;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class AddictionListener implements Listener {
    private final DrugCraft plugin;
    private final DataManager dataManager;
    private final DrugManager drugManager;
    private final AddictionManager addictionManager;

    public AddictionListener(DrugCraft plugin, DataManager dataManager, DrugManager drugManager, AddictionManager addictionManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
        this.addictionManager = addictionManager;
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!drugManager.isDrugItem(item)) return;

        String drugId = null;
        Drug consumedDrug = null;
        for (Drug drug : drugManager.getSortedDrugs()) {
            if (item.getType() == drug.getItem(drug.getQuality()).getType() &&
                    item.getItemMeta().getDisplayName().equals(drug.getItem(drug.getQuality()).getItemMeta().getDisplayName())) {
                drugId = drug.getDrugId();
                consumedDrug = drug;
                break;
            }
        }
        if (drugId == null || consumedDrug == null) return;

        // Apply drug effects
        applyDrugEffects(player, consumedDrug);

        // Manage addiction
        AddictionManager.PlayerAddictionData addictionData = addictionManager.getPlayerAddictionData(player.getUniqueId());
        int addictionLevel = addictionData.getAddictionLevel(drugId);
        addictionLevel++;
        addictionData.setAddictionLevel(drugId, addictionLevel);
        addictionManager.saveAddictionData();

        if (addictionLevel >= plugin.getConfigManager().getConfig().getInt("addiction.threshold", 5)) {
            applyWithdrawalEffects(player);
            player.sendMessage(MessageUtils.color("&#FF4040You are experiencing withdrawal symptoms from " + drugId + "!"));
        }
    }

    private void applyDrugEffects(Player player, Drug drug) {
        List<String> effects = plugin.getConfigManager().getDrugsConfig().getStringList("drugs." + drug.getDrugId() + ".effects");
        Location playerLoc = player.getLocation();
        for (String effect : effects) {
            String[] parts = effect.split(":");
            String effectType = parts[0].toUpperCase();
            if (effectType.equals("PARTICLE")) {
                if (parts.length < 2) continue;
                String particleName = parts[1].toUpperCase();
                try {
                    Particle particle = Particle.valueOf(particleName);
                    player.getWorld().spawnParticle(particle, playerLoc, 30, 0.5, 0.5, 0.5, 0.1);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid particle type for drug " + drug.getDrugId() + ": " + particleName);
                }
            } else if (effectType.equals("SOUND")) {
                if (parts.length < 2) continue;
                String soundName = parts[1].toUpperCase();
                try {
                    Sound sound = Sound.valueOf(soundName);
                    player.getWorld().playSound(playerLoc, sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound type for drug " + drug.getDrugId() + ": " + soundName);
                }
            } else {
                if (parts.length < 3) continue;
                int level = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]) * 20; // Convert seconds to ticks
                PotionEffectType potionEffectType = PotionEffectType.getByName(effectType);
                if (potionEffectType != null) {
                    player.addPotionEffect(new PotionEffect(potionEffectType, duration, level - 1));
                } else {
                    plugin.getLogger().warning("Unknown potion effect type for drug " + drug.getDrugId() + ": " + effectType);
                }
            }
        }
        player.sendMessage(MessageUtils.color("&#FF7F00You consumed " + drug.getName() + " and feel its effects!"));
    }

    private void applyWithdrawalEffects(Player player) {
        List<String> effects = plugin.getConfigManager().getConfig().getStringList("addiction.withdrawal_effects");
        for (String effect : effects) {
            String[] parts = effect.split(":");
            PotionEffectType effectType = PotionEffectType.getByName(parts[0]);
            if (effectType != null) {
                int level = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]) * 20;
                player.addPotionEffect(new PotionEffect(effectType, duration, level - 1));
            }
        }
    }
}