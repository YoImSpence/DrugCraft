package com.spence.drugcraft.town;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DealRequestGUI {
    private final DrugCraft plugin;
    private final Map<UUID, DealRequest> pendingDeals = new HashMap<>();

    public DealRequestGUI(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void openAcceptDenyMenu(Player player, int npcId, ItemStack item, int quantity, double price, List<Location> meetupSpots) {
        Component title = MessageUtils.color("Drug Deal Offer").color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 27, title);

        ItemStack dealItem = item.clone();
        ItemMeta dealMeta = dealItem.getItemMeta();
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtils.color("Quantity: " + quantity));
        lore.add(MessageUtils.color("Price: $" + String.format("%.2f", price)));
        dealMeta.lore(lore);
        dealItem.setItemMeta(dealMeta);
        inventory.setItem(13, dealItem);

        ItemStack accept = new ItemStack(Material.LIME_DYE);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.displayName(MessageUtils.color("Accept Deal").color(TextColor.fromHexString("#55FF55")));
        accept.setItemMeta(acceptMeta);
        inventory.setItem(11, accept);

        ItemStack deny = new ItemStack(Material.RED_DYE);
        ItemMeta denyMeta = deny.getItemMeta();
        denyMeta.displayName(MessageUtils.color("Deny Deal").color(TextColor.fromHexString("#FF5555")));
        deny.setItemMeta(denyMeta);
        inventory.setItem(15, deny);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 27; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        pendingDeals.put(player.getUniqueId(), new DealRequest(npcId, item, quantity, price, meetupSpots));
        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("DEAL_REQUEST", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened accept/deny deal menu for player " + player.getName());
    }

    public void openDealRequest(Player player, int npcId, ItemStack item, int quantity, double price, List<Location> meetupSpots) {
        Component title = MessageUtils.color(MessageUtils.getMessage("deal.confirmed")).color(TextColor.fromHexString("#FF5555"));
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        ItemStack dealItem = item.clone();
        ItemMeta dealMeta = dealItem.getItemMeta();
        List<Component> dealLore = new ArrayList<>();
        dealLore.add(MessageUtils.color("Quantity: " + quantity));
        dealLore.add(MessageUtils.color("Price: $" + String.format("%.2f", price)));
        dealMeta.lore(dealLore);
        dealItem.setItemMeta(dealMeta);
        inventory.setItem(22, dealItem);

        ItemStack confirm = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(MessageUtils.color("Confirm Deal").color(TextColor.fromHexString("#00FF00")));
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(39, confirm);

        ItemStack negotiate = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta negotiateMeta = negotiate.getItemMeta();
        negotiateMeta.displayName(MessageUtils.color("Negotiate Price").color(TextColor.fromHexString("#FFD700")));
        negotiate.setItemMeta(negotiateMeta);
        inventory.setItem(41, negotiate);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, border);
            }
        }

        pendingDeals.put(player.getUniqueId(), new DealRequest(npcId, item, quantity, price, meetupSpots));
        Map<UUID, ActiveGUI> activeMenus = plugin.getActiveMenus();
        activeMenus.put(player.getUniqueId(), new ActiveGUI("DEAL_REQUEST", inventory));
        player.openInventory(inventory);
        plugin.getLogger().info("Opened deal request menu for player " + player.getName());
    }

    public DealRequest getPendingDeal(Player player) {
        return pendingDeals.get(player.getUniqueId());
    }

    public void removePendingDeal(Player player) {
        pendingDeals.remove(player.getUniqueId());
    }

    public record DealRequest(int npcId, ItemStack item, int quantity, double price, List<Location> meetupSpots) {
    }
}