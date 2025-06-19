package com.spence.drugcraft.town;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.DealRequestGUI;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.gui.GUIListener;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class DealRequestGUIListener implements Listener {
    private final DrugCraft plugin;
    private final DealRequestGUI dealRequestGUI;
    private final GUIListener guiListener;
    private final TownCitizenManager townCitizenManager;

    public DealRequestGUIListener(DrugCraft plugin, DealRequestGUI dealRequestGUI, GUIListener guiListener, TownCitizenManager townCitizenManager) {
        this.plugin = plugin;
        this.dealRequestGUI = dealRequestGUI;
        this.guiListener = guiListener;
        this.townCitizenManager = townCitizenManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("DEAL_REQUEST")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null) return;
        String displayName = MessageUtils.stripColor(MiniMessage.miniMessage().serialize(meta.displayName()));

        if (displayName.equals(MessageUtils.getMessage("deal.accept"))) {
            DealRequest deal = dealRequestGUI.getCurrentDeal(player);
            if (deal != null) {
                dealRequestGUI.openMeetupMenu(player, deal.getNpcId(), deal.getNpcName(), deal.getDrugId(), deal.getQuality(), deal.getQuantity(), deal.getPrice());
            }
        } else if (displayName.equals(MessageUtils.getMessage("deal.deny"))) {
            MessageUtils.sendMessage(player, "deal.denied");
            player.closeInventory();
        } else if (activeGUI.getMenuSubType().equals("meetup")) {
            if (displayName.equals(MessageUtils.getMessage("deal.meetup-alley"))) {
                DealRequest deal = dealRequestGUI.getCurrentDeal(player);
                if (deal != null) {
                    townCitizenManager.addAcceptedDeal(player, deal.getNpcId(), deal.getNpcName(), deal.getDrugId(), deal.getQuality(), deal.getQuantity(), deal.getPrice(), player.getLocation());
                    MessageUtils.sendMessage(player, "deal.accepted");
                    player.closeInventory();
                }
            }
        }
    }
}