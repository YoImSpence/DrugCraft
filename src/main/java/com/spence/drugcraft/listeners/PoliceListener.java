package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.police.PoliceManager;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PoliceListener implements Listener {
    private final DrugCraft plugin;
    private final PoliceManager policeManager;

    public PoliceListener(DrugCraft plugin, PoliceManager policeManager) {
        this.plugin = plugin;
        this.policeManager = policeManager;
    }

    @EventHandler
    public void onNPCDamage(NPCDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        policeManager.handlePlayerAttack(player, event.getNPC());
    }
}