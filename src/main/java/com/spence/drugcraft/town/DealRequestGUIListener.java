package com.spence.drugcraft.town;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.gui.ChatInputHandler;
import com.spence.drugcraft.gui.GUIHandler;
import com.spence.drugcraft.listeners.GUIListener;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import de.tr7zw.nbtapi.NBTItem;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.Random;

public class DealRequestGUIListener implements Listener, ChatInputHandler, GUIHandler {
    private final DrugCraft plugin;
    private final DealRequestGUI dealRequestGUI;
    private final GUIListener guiListener;
    private final TownCitizenManager townCitizenManager;
    private final Random random = new Random();

    public DealRequestGUIListener(DrugCraft plugin, DealRequestGUI dealRequestGUI, GUIListener guiListener, TownCitizenManager townCitizenManager) {
        this.plugin = plugin;
        this.dealRequestGUI = dealRequestGUI;
        this.guiListener = guiListener;
        this.townCitizenManager = townCitizenManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onClick(Player player, ItemStack clickedItem, int slot, Inventory inventory) {
        if (clickedItem == null) return;

        ActiveGUI activeGUI = plugin.getActiveMenus().get(player.getUniqueId());
        if (activeGUI == null || !activeGUI.getGuiType().equals("DEAL_REQUEST")) return;

        String displayName = MessageUtils.stripColor(clickedItem.getItemMeta().displayName());
        DealRequestGUI.DealRequest deal = dealRequestGUI.getPendingDeal(player);
        if (deal == null) {
            MessageUtils.sendMessage(player, "general.error");
            player.closeInventory();
            return;
        }
        NPC npc = CitizensAPI.getNPCRegistry().getById(deal.npcId());
        if (npc == null || !townCitizenManager.isTownCitizen(npc)) {
            MessageUtils.sendMessage(player, "deal.npc-unavailable");
            dealRequestGUI.removePendingDeal(player);
            player.closeInventory();
            return;
        }

        if (displayName.equals(MessageUtils.stripColor(MessageUtils.color("Accept Deal")))) {
            dealRequestGUI.openDealRequest(player, deal.npcId(), deal.item(), deal.quantity(), deal.price(), deal.meetupSpots());
        } else if (displayName.equals(MessageUtils.stripColor(MessageUtils.color("Deny Deal")))) {
            dealRequestGUI.removePendingDeal(player);
            MessageUtils.sendMessage(player, "deal.denied");
            player.closeInventory();
        } else if (displayName.equals(MessageUtils.stripColor(MessageUtils.color("Confirm Deal")))) {
            ItemStack requiredItem = deal.item().clone();
            requiredItem.setAmount(deal.quantity());
            if (!hasItem(player, requiredItem)) {
                MessageUtils.sendMessage(player, "deal.no-drugs");
                dealRequestGUI.removePendingDeal(player);
                player.closeInventory();
                return;
            }
            EconomyManager economyManager = plugin.getEconomyManager();
            if (!economyManager.isEconomyAvailable()) {
                MessageUtils.sendMessage(player, "general.no-economy");
                dealRequestGUI.removePendingDeal(player);
                player.closeInventory();
                return;
            }
            removeItem(player, requiredItem);
            economyManager.getEconomy().depositPlayer(player, deal.price());
            String drugId = plugin.getDrugManager().getDrugIdFromItem(deal.item());
            String quality = plugin.getDrugManager().getQualityFromItem(deal.item());
            String drugName = plugin.getDrugManager().getDrug(drugId).getName();
            Location meetupSpot = deal.meetupSpots().get(random.nextInt(deal.meetupSpots().size()));
            townCitizenManager.addAcceptedDeal(player, deal.npcId(), drugId, drugName, quality, deal.quantity(), deal.price(), meetupSpot);
            MessageUtils.sendMessage(player, "deal.confirmed", "npc_name", npc.getName());
            try {
                npc.getNavigator().setTarget(meetupSpot);
                npc.getNavigator().getDefaultParameters().distanceMargin(1.0).pathDistanceMargin(2.0);
                plugin.getLogger().info("NPC " + npc.getName() + " navigating to meetup spot: " + meetupSpot);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to set navigation target for NPC " + npc.getName() + ": " + e.getMessage());
            }
            dealRequestGUI.removePendingDeal(player);
            player.closeInventory();
        } else if (displayName.equals(MessageUtils.stripColor(MessageUtils.color("Negotiate Price")))) {
            guiListener.setAwaitingChatInput(player.getUniqueId(), "negotiate_price", deal);
            MessageUtils.sendMessage(player, "deal.request-negotiate");
            player.closeInventory();
        }
    }

    private boolean hasItem(Player player, ItemStack requiredItem) {
        int requiredAmount = requiredItem.getAmount();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isSameItem(item, requiredItem)) {
                requiredAmount -= item.getAmount();
                if (requiredAmount <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeItem(Player player, ItemStack requiredItem) {
        int requiredAmount = requiredItem.getAmount();
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && isSameItem(item, requiredItem)) {
                int amount = item.getAmount();
                if (amount <= requiredAmount) {
                    player.getInventory().setItem(i, null);
                    requiredAmount -= amount;
                } else {
                    item.setAmount(amount - requiredAmount);
                    requiredAmount = 0;
                }
                if (requiredAmount == 0) {
                    break;
                }
            }
        }
        player.updateInventory();
    }

    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1.getType() != item2.getType()) return false;
        NBTItem nbtItem1 = new NBTItem(item1);
        NBTItem nbtItem2 = new NBTItem(item2);
        String drugId1 = nbtItem1.getString("drug_id");
        String drugId2 = nbtItem2.getString("drug_id");
        String quality1 = nbtItem1.getString("quality");
        String quality2 = nbtItem2.getString("quality");
        return Objects.equals(drugId1, drugId2) && Objects.equals(quality1, quality2);
    }

    @Override
    public void handleChatInput(Player player, String action, String message, Object context) {
        if (!action.equals("negotiate_price")) return;
        DealRequestGUI.DealRequest deal = (DealRequestGUI.DealRequest) context;
        if (deal == null) {
            MessageUtils.sendMessage(player, "general.error");
            return;
        }
        double newPrice;
        try {
            newPrice = Double.parseDouble(message.trim());
            if (newPrice <= deal.price()) {
                MessageUtils.sendMessage(player, "deal.price-too-low");
                return;
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "deal.invalid-price");
            return;
        }
        if (random.nextDouble() < 0.5) {
            MessageUtils.sendMessage(player, "deal.price-accepted", "price", String.format("%.2f", newPrice));
            dealRequestGUI.openDealRequest(player, deal.npcId(), deal.item(), deal.quantity(), newPrice, deal.meetupSpots());
        } else {
            MessageUtils.sendMessage(player, "deal.price-rejected");
            dealRequestGUI.openDealRequest(player, deal.npcId(), deal.item(), deal.quantity(), deal.price(), deal.meetupSpots());
        }
    }
}