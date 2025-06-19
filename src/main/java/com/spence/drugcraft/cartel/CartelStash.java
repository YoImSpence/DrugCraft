package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.handlers.ActiveGUI;
import com.spence.drugcraft.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.Bukkit;

public class CartelStash {
    private final DrugCraft plugin;
    private final CartelManager cartelManager;

    public CartelStash(DrugCraft plugin) {
        this.plugin = plugin;
        this.cartelManager = plugin.getCartelManager();
    }

    public void openStash(Player player, Block block) {
        Cartel cartel = cartelManager.getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-in-cartel");
            return;
        }
        if (!cartel.getRank(player.getUniqueId()).equals("leader") && !cartel.getRank(player.getUniqueId()).equals("admin")) {
            MessageUtils.sendMessage(player, "cartel.no-permission");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, MiniMessage.miniMessage().deserialize(MessageUtils.getMessage("<gradient:#FF0000:#FFFFFF>Cartel Stash</gradient>")));
        ActiveGUI activeGUI = new ActiveGUI("CARTEL_STASH", inv);
        plugin.getActiveMenus().put(player.getUniqueId(), activeGUI);

        player.openInventory(inv);
    }

    public void placeStash(Player player, Block block) {
        Cartel cartel = cartelManager.getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-in-cartel");
            return;
        }
        if (!cartel.getRank(player.getUniqueId()).equals("leader")) {
            MessageUtils.sendMessage(player, "cartel.no-permission");
            return;
        }
        if (block.getType() != Material.CHEST) {
            MessageUtils.sendMessage(player, "cartel.invalid-block");
            return;
        }

        cartelManager.setStashLocation(cartel.getName(), block.getLocation());
        MessageUtils.sendMessage(player, "cartel.stash-placed");
    }
}