package com.spence.drugcraft.dealer;

import com.spence.drugcraft.DrugCraft;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DealerNPC implements Listener {
    private final DrugCraft plugin;
    private final NPCRegistry npcRegistry;

    public DealerNPC(DrugCraft plugin) {
        this.plugin = plugin;
        this.npcRegistry = CitizensAPI.getNPCRegistry();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration config = plugin.getConfig("dealers.yml");
                ConfigurationSection dealersSection = config.getConfigurationSection("dealers");
                if (dealersSection == null) {
                    plugin.getLogger().warning("No dealers defined in dealers.yml");
                    return;
                }

                for (String dealerId : dealersSection.getKeys(false)) {
                    String path = "dealers." + dealerId;
                    String worldName = config.getString(path + ".world");
                    double x = config.getDouble(path + ".x");
                    double y = config.getDouble(path + ".y");
                    double z = config.getDouble(path + ".z");
                    float yaw = (float) config.getDouble(path + ".yaw");
                    float pitch = (float) config.getDouble(path + ".pitch");
                    String name = config.getString(path + ".name", "Dealer");

                    Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
                    spawnNPC(location, name, dealerId);
                }
            }
        }.runTaskLater(plugin, 20L); // Delay by 1 second (20 ticks) to ensure worlds are loaded
    }

    public void spawnNPC(Location location, String name, String dealerId) {
        if (location.getWorld() == null) {
            plugin.getLogger().severe("Cannot spawn dealer NPC '" + name + "' (ID: " + dealerId + ") because world '" + (location.getWorld() != null ? location.getWorld().getName() : "null") + "' is not loaded. Check dealers.yml configuration.");
            return;
        }

        NPC npc = npcRegistry.createNPC(EntityType.PLAYER, name);
        npc.addTrait(DealerTrait.class);
        npc.spawn(location);
        plugin.getLogger().info("Spawned dealer NPC '" + name + "' (ID: " + dealerId + ") at " + location.toString());
    }
}