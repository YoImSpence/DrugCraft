package com.spence.drugcraft.cartel;

import org.bukkit.Location;

import java.util.*;

public class Cartel {
    private final String id;
    private final String name;
    private final int level;
    private final List<UUID> members;
    private final Map<UUID, String> ranks;
    private final List<Location> stashes;
    private UUID owner;
    private Map<UUID, List<String>> permissions;

    public Cartel(String id, String name, int level, List<UUID> members, Map<UUID, String> ranks, List<Location> stashes) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.members = new ArrayList<>(members);
        this.ranks = new HashMap<>(ranks);
        this.stashes = new ArrayList<>(stashes);
        this.permissions = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public Map<UUID, String> getRanks() {
        return new HashMap<>(ranks);
    }

    public List<Location> getStashes() {
        return new ArrayList<>(stashes);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Map<UUID, List<String>> getPermissions() {
        return new HashMap<>(permissions);
    }

    public void setPermissions(Map<UUID, List<String>> permissions) {
        this.permissions = new HashMap<>(permissions);
    }
}