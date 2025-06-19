package com.spence.drugcraft.businesses;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.gui.BusinessGUI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class BusinessNPCManager implements Listener {
    private final DrugCraft plugin;
    private final BusinessGUI businessGUI;
    private final NPCRegistry npcRegistry;
    private NPC businessNPC;

    public BusinessNPCManager(DrugCraft plugin, BusinessGUI businessGUI) {
        this.plugin = plugin;
        this.businessGUI = businessGUI;
        this.npcRegistry = CitizensAPI.getNPCRegistry();
        spawnBusinessNPC();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void spawnBusinessNPC() {
        Location spawnLocation = new Location(Bukkit.getWorld("Greenfield"), 2122, 69, 715, 90, 0);
        businessNPC = npcRegistry.createNPC(EntityType.PLAYER, "Business Broker");
        businessNPC.spawn(spawnLocation);
        try {
            SkinTrait skinTrait = businessNPC.getOrAddTrait(SkinTrait.class);
            skinTrait.setSkinName("Broker", true);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply SkinTrait to Business Broker NPC: " + e.getMessage());
        }
        plugin.getLogger().info("Spawned Business Broker NPC at " + spawnLocation);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getUniqueId().equals(businessNPC.getUniqueId())) {
            event.setCancelled(true);
            businessGUI.openMainMenu(event.getPlayer());
            plugin.getLogger().info("Player " + event.getPlayer().getName() + " interacted with Business Broker NPC");
        }
    }

    public void cleanup() {
        if (businessNPC != null) {
            businessNPC.destroy();
            plugin.getLogger().info("Removed Business Broker NPC");
        }
    }
}