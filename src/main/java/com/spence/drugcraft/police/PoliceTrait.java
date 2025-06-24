package com.spence.drugcraft.police;

import com.spence.drugcraft.DrugCraft;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;
import java.util.logging.Logger;

public class PoliceTrait extends Trait {
    public PoliceTrait() {
        super("police");
        Logger.getLogger(PoliceTrait.class.getName()).info("Initializing PoliceTrait");
    }

    public static class PoliceTraitListener implements Listener {
        private final DrugCraft plugin;

        public PoliceTraitListener(DrugCraft plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onNPCRightClick(NPCRightClickEvent event) {
            if (!event.getNPC().hasTrait(PoliceTrait.class)) return;
            Player player = event.getClicker();
            PoliceManager policeManager = plugin.getServer().getServicesManager()
                    .getRegistration(PoliceManager.class).getProvider();
            policeManager.requestSearch(player);

            if (event.getNPC().data().has("k9_id")) {
                Wolf k9 = (Wolf) plugin.getServer().getEntity((UUID) event.getNPC().data().get("k9_id"));
                if (k9 != null) {
                    policeManager.checkK9Detection(player, k9);
                }
            }
        }
    }
}