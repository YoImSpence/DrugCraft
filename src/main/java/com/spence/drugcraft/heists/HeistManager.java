package com.spence.drugcraft.heists;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeistManager {
    private final DrugCraft plugin;
    private final EconomyManager economyManager;
    private final List<Heist> activeHeists = new ArrayList<>();

    public HeistManager(DrugCraft plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public void startHeist(Player leader, String heistId) {
        // Placeholder: Implement heist logic
        Heist heist = new Heist(heistId, leader.getUniqueId());
        activeHeists.add(heist);
        MessageUtils.sendMessage(leader, "heist.started", "<gradient:#FF0000:#FFFFFF>Heist " + heistId + " started!</gradient>");
    }

    public void completeHeist(Heist heist) {
        Player leader = plugin.getServer().getPlayer(heist.getLeader());
        if (leader != null) {
            double reward = plugin.getConfig("heists.yml").getDouble("heists." + heist.getId() + ".reward", 1000.0);
            economyManager.depositPlayer(leader, reward);
            MessageUtils.sendMessage(leader, "heist.completed", "<gradient:#00FF00:#FFFFFF>Heist completed! Reward: $" + reward + "</gradient>");
        }
        activeHeists.remove(heist);
    }

    private static class Heist {
        private final String id;
        private final UUID leader;

        public Heist(String id, UUID leader) {
            this.id = id;
            this.leader = leader;
        }

        public String getId() { return id; }
        public UUID getLeader() { return leader; }
    }
}