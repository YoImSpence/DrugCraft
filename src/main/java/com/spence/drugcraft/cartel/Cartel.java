package com.spence.drugcraft.cartel;

import java.util.*;

public class Cartel {
    private final String name;
    private final UUID owner;
    private final Set<UUID> members;
    private int stashLevel;
    private int growthLevel;
    private final Map<UUID, Set<String>> permissions;

    public Cartel(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new HashSet<>();
        this.members.add(owner);
        this.stashLevel = 1;
        this.growthLevel = 1;
        this.permissions = new HashMap<>();
        this.permissions.put(owner, new HashSet<>(Arrays.asList("stash_access", "manage_members", "purchase_upgrades", "manage_crops", "access_chests")));
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean isLeader(UUID playerUUID) {
        return owner.equals(playerUUID);
    }

    public void addMember(UUID playerUUID) {
        members.add(playerUUID);
    }

    public void removeMember(UUID playerUUID) {
        members.remove(playerUUID);
        permissions.remove(playerUUID);
    }

    public int getStashLevel() {
        return stashLevel;
    }

    public void upgradeStashLevel() {
        stashLevel++;
    }

    public int getGrowthLevel() {
        return growthLevel;
    }

    public void upgradeGrowthLevel() {
        growthLevel++;
    }

    public void addPermission(UUID playerUUID, String permission) {
        permissions.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(permission);
    }

    public void removePermission(UUID playerUUID, String permission) {
        Set<String> playerPermissions = permissions.get(playerUUID);
        if (playerPermissions != null) {
            playerPermissions.remove(permission);
            if (playerPermissions.isEmpty()) {
                permissions.remove(playerUUID);
            }
        }
    }

    public boolean hasPermission(UUID playerUUID, String permission) {
        Set<String> playerPermissions = permissions.get(playerUUID);
        return playerPermissions != null && playerPermissions.contains(permission);
    }
}