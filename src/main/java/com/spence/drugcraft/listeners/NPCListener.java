package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.dealer.DealerNPC;
import com.spence.drugcraft.gui.DealRequestGUI;
import com.spence.drugcraft.town.TownCitizenManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCListener implements Listener {
    private final DrugCraft plugin;

    public NPCListener(DrugCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        if (event.getNPC().hasTrait(DealerNPC.DealerTrait.class)) {
            plugin.getDealerGUIHandler().openMainMenu(event.getClicker());
        } else if (event.getNPC().hasTrait(TownCitizenManager.TownCitizenTrait.class)) {
            plugin.getServer().getServicesManager()
                    .getRegistration(DealRequestGUI.class).getProvider()
                    .openDealRequestGUI(event.getClicker());
        }
    }
}