package com.spence.drugcraft.utils;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.addiction.PlayerAddictionData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderEx extends PlaceholderExpansion {
    private final DrugCraft plugin;

    public PlaceholderEx(DrugCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "drugcraft";
    }

    @Override
    public String getAuthor() {
        return "YoImSpence";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return null;

        if (identifier.startsWith("addiction_uses_")) {
            String drugId = identifier.replace("addiction_uses_", "");
            PlayerAddictionData data = plugin.getAddictionManager().getPlayerData(player.getUniqueId());
            return String.valueOf(data.getUses(drugId));
        }

        return null;
    }
}