package com.spence.drugcraft.cartel;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.UUID;

public class CartelStash {
    private final UUID id;
    private final Location location;
    private final String cartelId;

    public CartelStash(String cartelId, Location location) {
        this.id = UUID.randomUUID();
        this.cartelId = cartelId;
        this.location = location;

        World world = location.getWorld();
        if (world != null) {
            world.getBlockAt(location).setType(Material.CHEST);
        }
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public String getCartelId() {
        return cartelId;
    }
}