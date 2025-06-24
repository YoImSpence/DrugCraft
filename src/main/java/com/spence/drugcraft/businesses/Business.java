package com.spence.drugcraft.businesses;

import java.util.UUID;

public class Business {
    private final UUID ownerUUID;
    private final String id;
    private final String name;
    private final String type;
    private final String location;
    private final int level;

    public Business(UUID ownerUUID, String id, String name, String type, String location, int level) {
        this.ownerUUID = ownerUUID;
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.level = level;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public int getLevel() {
        return level;
    }
}