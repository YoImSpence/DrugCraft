package com.spence.drugcraft.town;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.drugs.DrugManager;
import com.spence.drugcraft.utils.EconomyManager;
import com.spence.drugcraft.utils.MessageUtils;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TownCitizenManager {
    private final DrugCraft plugin;
    private final DrugManager drugManager;
    private final EconomyManager economyManager;
    private final List<AcceptedDeal> acceptedDeals = new ArrayList<>();
    private final Random random = new Random();

    public TownCitizenManager(DrugCraft plugin, DrugManager drugManager) {
        this.plugin = plugin;
        this.drugManager = drugManager;
        this.economyManager = new EconomyManager(plugin);
    }

    public boolean isTownCitizen(NPC npc) {
        return npc != null && npc.hasTrait(TownCitizen.class);
    }

    public void addAcceptedDeal(Player player, int npcId, String npcName, String drugId, String quality, int quantity, double price, Location location) {
        AcceptedDeal deal = new AcceptedDeal(npcId, npcName, drugId, quality, quantity, price, location);
        acceptedDeals.add(deal);
        startDealTimer(player, deal);
    }

    public void initiateDeal(Player player, NPC npc) {
        if (!isTownCitizen(npc)) {
            MessageUtils.sendMessage(player, "deal.invalid-npc");
            return;
        }
        FileConfiguration config = plugin.getConfig("town.yml");
        List<String> drugIds = config.getStringList("deals.drugs");
        if (drugIds.isEmpty()) {
            MessageUtils.sendMessage(player, "deal.no-drugs-configured");
            return;
        }
        String drugId = drugIds.get(random.nextInt(drugIds.size()));
        String[] qualities = config.getStringList("deals.qualities").toArray(new String[0]);
        String quality = qualities[random.nextInt(qualities.length)];
        int quantity = random.nextInt(config.getInt("deals.max_quantity", 5)) + 1;
        double basePrice = drugManager.getDrug(drugId).getPrice();
        double price = basePrice * quantity * (quality.equals("high") ? 1.5 : quality.equals("low") ? 0.8 : 1.0);

        plugin.getDealRequestGUI().openDealRequestMenu(player, npc.getId(), npc.getName(), drugId, quality, quantity, price);
    }

    public List<Location> getMeetupSpots() {
        FileConfiguration config = plugin.getConfig("town.yml");
        List<Location> spots = new ArrayList<>();
        for (String key : config.getConfigurationSection("meetup_spots").getKeys(false)) {
            String path = "meetup_spots." + key;
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            String world = config.getString(path + ".world");
            if (world != null) {
                spots.add(new Location(plugin.getServer().getWorld(world), x, y, z));
            }
        }
        return spots;
    }

    private void startDealTimer(Player player, AcceptedDeal deal) {
        new BukkitRunnable() {
            int timeLeft = 300; // 5 minutes, configurable in town.yml
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    acceptedDeals.remove(deal);
                    MessageUtils.sendMessage(player, "deal.failed-timeout", "npc_name", deal.getNpcName());
                    cancel();
                    return;
                }
                if (player.getLocation().distance(deal.getMeetupLocation()) < 5.0) {
                    if (completeDeal(player, deal)) {
                        acceptedDeals.remove(deal);
                        economyManager.depositPlayer(player, deal.getPrice());
                        MessageUtils.sendMessage(player, "deal.completed", "npc_name", deal.getNpcName(), "price", String.valueOf(deal.getPrice()));
                        cancel();
                    } else {
                        acceptedDeals.remove(deal);
                        MessageUtils.sendMessage(player, "deal.failed-wrong-items", "npc_name", deal.getNpcName());
                        cancel();
                    }
                }
                timeLeft -= 20;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean completeDeal(Player player, AcceptedDeal deal) {
        int requiredQuantity = deal.getQuantity();
        String drugId = deal.getDrugId();
        String quality = deal.getQuality();

        for (ItemStack item : player.getInventory().getContents()) {
            if (drugManager.isDrugItem(item) && drugManager.getDrugIdFromItem(item).equals(drugId) && drugManager.getQualityFromItem(item).equals(quality)) {
                requiredQuantity -= item.getAmount();
                if (requiredQuantity <= 0) {
                    player.getInventory().remove(item);
                    return true;
                }
            }
        }
        return false;
    }
}