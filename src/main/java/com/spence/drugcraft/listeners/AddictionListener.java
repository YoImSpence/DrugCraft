package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.data.DataManager;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
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

    public AddictionListener(DrugCraft plugin, DataManager dataManager, DrugManager drugManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.drugManager = drugManager;
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!drugManager.isDrugItem(item)) return;

        String drugId = null;
        for (Drug drug : drugManager.getSortedDrugs()) {
            if (item.getType() == drug.getItem(drug.getQuality()).getType() &&
                    item.getItemMeta().getDisplayName().equals(drug.getItem(drug.getQuality()).getItemMeta().getDisplayName())) {
                drugId = drug.getDrugId();
                break;
            }
        }
        if (drugId == null) return;

        int addictionLevel = dataManager.getAddictionLevel(player, drugId);
        addictionLevel++;
        dataManager.setAddictionLevel(player, drugId, addictionLevel);

        if (addictionLevel >= plugin.getConfigManager().getConfig().getInt("addiction.threshold", 5)) {
            applyWithdrawalEffects(player);
            player.sendMessage(MessageUtils.color("&#FF4040You are experiencing withdrawal symptoms from " + drugId + "!"));
        }
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