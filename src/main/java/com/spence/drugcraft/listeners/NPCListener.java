package com.spence.drugcraft.listeners;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.Drug;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.inventory.ItemStack;

public class NPCListener implements Listener {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final Economy economy;

    public NPCListener(DrugCraft plugin, DrugManager drugManager, Economy economy) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economy = economy;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!drugManager.isDrugItem(item)) {
            MessageUtils.sendMessage(player, "&cYou must hold a drug item to sell to this NPC.");
            return;
        }

        String drugId = drugManager.getDrugIdFromItem(item);
        Drug drug = drugManager.getDrug(drugId);
        if (drug != null) {
            economy.depositPlayer(player, drug.getPrice());
            item.setAmount(item.getAmount() - 1);
            String message = plugin.getConfigManager().getConfig().getString("npc.sell_message", "&aSold %drug% for $%price%!");
            MessageUtils.sendMessage(player, message.replace("%drug%", drug.getName()).replace("%price%", String.valueOf(drug.getPrice())));
            plugin.getLogger().info(player.getName() + " sold " + drug.getName() + " to NPC");
        }
    }
}