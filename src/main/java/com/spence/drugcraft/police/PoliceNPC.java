package com.spence.drugcraft.police;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PoliceNPC implements Listener {
    private final DrugCraft plugin;

    public PoliceNPC(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void createPoliceNPC(NPC npc, boolean hasK9) {
        if (hasK9) {
            Wolf k9 = npc.getEntity().getWorld().spawn(npc.getEntity().getLocation(), Wolf.class);
            k9.setOwner((Player) npc.getEntity());
            // Placeholder: Store K9 association
        }
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        Player player = event.getClicker();

        if (!npc.hasTrait(PoliceTrait.class)) return;

        if (plugin.getDrugManager().hasDrugsInInventory(player)) {
            MessageUtils.sendMessage(player, "police.detected-drugs");
            plugin.getPoliceManager().requestSearch(player);
        }
    }
}