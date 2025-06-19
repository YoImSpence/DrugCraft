package com.spence.drugcraft.cartel;

import com.spence.drugcraft.DrugCraft;
import com.spence.drugcraft.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartelManager {
    private final DrugCraft plugin;
    private final List<Cartel> cartels = new ArrayList<>();

    public CartelManager(DrugCraft plugin) {
        this.plugin = plugin;
    }

    public void createCartel(Player player, String name) {
        if (getCartelByPlayer(player.getUniqueId()) != null) {
            MessageUtils.sendMessage(player, "cartel.already-in-cartel");
            return;
        }
        Cartel cartel = new Cartel(name, player.getUniqueId());
        cartels.add(cartel);
        MessageUtils.sendMessage(player, "cartel.created", "name", name);
    }

    public boolean joinCartel(Player player, String cartelName) {
        Cartel cartel = getCartel(cartelName);
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-found", "name", cartelName);
            return false;
        }
        if (getCartelByPlayer(player.getUniqueId()) != null) {
            MessageUtils.sendMessage(player, "cartel.already-in-cartel");
            return false;
        }
        cartel.addMember(player.getUniqueId());
        MessageUtils.sendMessage(player, "cartel.joined", "name", cartelName);
        return true;
    }

    public boolean leaveCartel(Player player) {
        Cartel cartel = getCartelByPlayer(player.getUniqueId());
        if (cartel == null) {
            MessageUtils.sendMessage(player, "cartel.not-in-cartel");
            return false;
        }
        cartel.removeMember(player.getUniqueId());
        MessageUtils.sendMessage(player, "cartel.left", "name", cartel.getName());
        return true;
    }

    public UUID getCartelLeader(String cartelName) {
        Cartel cartel = getCartel(cartelName);
        return cartel != null ? cartel.getLeader() : null;
    }

    public List<UUID> getCartelMembers(String cartelName) {
        Cartel cartel = getCartel(cartelName);
        return cartel != null ? new ArrayList<>(cartel.getMembers()) : new ArrayList<>();
    }

    public int getCartelLevel(String cartelName) {
        Cartel cartel = getCartel(cartelName);
        return cartel != null ? cartel.getLevel() : 0;
    }

    public Cartel getPlayerCartel(UUID playerUUID) {
        return getCartelByPlayer(playerUUID);
    }

    public Cartel getCartelByPlayer(UUID playerUUID) {
        return cartels.stream().filter(c -> c.getMembers().contains(playerUUID)).findFirst().orElse(null);
    }

    public Cartel getCartel(String name) {
        return cartels.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Cartel getCartelByStashLocation(Location location) {
        // Placeholder: Implement stash location lookup
        return null;
    }

    public void setStashLocation(String cartelName, Location location) {
        Cartel cartel = getCartel(cartelName);
        if (cartel != null) {
            // Placeholder: Store location
        }
    }
}