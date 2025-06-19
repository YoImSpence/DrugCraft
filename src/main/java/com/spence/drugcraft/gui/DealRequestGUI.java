package com.spence.drugcraft.gui;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.town.DealRequest;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DealRequestGUI {
    private final DrugCraft plugin;
    private final Map<UUID, DealRequest> activeDeals = new HashMap<>();

    public DealRequestGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openDealRequestMenu(Player player, int npcId, String npcName, String drugId, String quality, int quantity, double price) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("<gradient:#FFFF00:#FFFFFF>Deal Request</gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("DEAL_REQUEST", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        DealRequest dealRequest = new DealRequest(npcId, npcName, drugId, quality, quantity, price, null);
        activeDeals.put(player.getUniqueId(), dealRequest);
        activeGUI.setDealRequest(dealRequest);

        ItemStack dealInfo = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = dealInfo.getItemMeta();
        infoMeta.setDisplayName(MessageUtils.getMessage("deal.info"));
        infoMeta.setLore(Arrays.asList(
                MessageUtils.getMessage("deal.npc", "npc_name", npcName),
                MessageUtils.getMessage("deal.drug", "drug_id", drugId),
                MessageUtils.getMessage("deal.quality", "quality", quality),
                MessageUtils.getMessage("deal.quantity", "quantity", String.valueOf(quantity)),
                MessageUtils.getMessage("deal.price", "price", String.valueOf(price))
        ));
        dealInfo.setItemMeta(infoMeta);

        ItemStack accept = new ItemStack(Material.GREEN_WOOL);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.setDisplayName(MessageUtils.getMessage("deal.accept"));
        accept.setItemMeta(acceptMeta);

        ItemStack deny = new ItemStack(Material.RED_WOOL);
        ItemMeta denyMeta = deny.getItemMeta();
        denyMeta.setDisplayName(MessageUtils.getMessage("deal.deny"));
        deny.setItemMeta(denyMeta);

        inv.setItem(11, dealInfo);
        inv.setItem(13, accept);
        inv.setItem(15, deny);

        player.openInventory(inv);
    }

    public void openMeetupMenu(Player player, int npcId, String npcName, String drugId, String quality, int quantity, double price) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("<gradient:#FFFF00:#FFFFFF>Select Meetup</gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("DEAL_REQUEST", inv, "meetup");
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        ItemStack alley = new ItemStack(Material.COBBLESTONE);
        ItemMeta alleyMeta = alley.getItemMeta();
        alleyMeta.setDisplayName(MessageUtils.getMessage("deal.meetup-alley"));
        alley.setItemMeta(alleyMeta);

        ItemStack dock = new ItemStack(Material.OAK_PLANKS);
        ItemMeta dockMeta = dock.getItemMeta();
        dockMeta.setDisplayName(MessageUtils.getMessage("deal.meetup-dock"));
        dock.setItemMeta(dockMeta);

        inv.setItem(11, alley);
        inv.setItem(15, dock);

        player.openInventory(inv);
    }

    public DealRequest getCurrentDeal(Player player) {
        return activeDeals.get(player.getUniqueId());
    }
}