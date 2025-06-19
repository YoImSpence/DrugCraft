package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.dealer.DealerTrait;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.town.TownCitizenManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Random;

public class NPCListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final EconomyManager economyManager;
    private final TownCitizenManager townCitizenManager;
    private final Random random = new Random();

    public NPCListener(DrugCraft plugin, DrugManager drugManager, EconomyManager economyManager, TownCitizenManager townCitizenManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economyManager = economyManager;
        this.townCitizenManager = townCitizenManager;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        Player player = event.getClicker();

        if (npc.hasTrait(DealerTrait.class)) {
            if (!player.hasPermission("drugcraft.dealer")) {
                MessageUtils.sendMessage(player, "general.no-permission");
                return;
            }
            plugin.getDealerGUIHandler().openMainMenu(player);
        } else if (npc.hasTrait(com.spence.drugcraft.police.PoliceTrait.class)) {
            if (!player.hasPermission("drugcraft.police")) {
                MessageUtils.sendMessage(player, "general.no-permission");
                return;
            }
            plugin.getPoliceNPC().createPoliceNPC(npc, random.nextBoolean());
        } else if (townCitizenManager.isTownCitizen(npc)) {
            if (!player.hasPermission("drugcraft.town")) {
                MessageUtils.sendMessage(player, "general.no-permission");
                return;
            }
            List<Drug> drugs = drugManager.getDrugs();
            if (drugs.isEmpty()) return;
            Drug drug = drugs.get(random.nextInt(drugs.size()));
            String[] qualities = {"low", "standard", "high"};
            String quality = qualities[random.nextInt(qualities.length)];
            int quantity = random.nextInt(5) + 1;
            double price = drug.getPrice() * quantity * (quality.equals("high") ? 1.5 : quality.equals("low") ? 0.8 : 1.0);
            townCitizenManager.initiateDeal(player, npc);
        }
    }
}