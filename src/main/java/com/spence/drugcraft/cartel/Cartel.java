package com.spence.drugcraft.cartel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Cartel {
    private final String name;
    private final UUID leader;
    private final List<UUID> members;
    private final Map<UUID, String> ranks;
    private int level;

    public Cartel(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.ranks = new HashMap<>();
        this.level = 1;
        members.add(leader);
        ranks.put(leader, "leader");
    }

    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public List<UUID> getMembers() { return new ArrayList<>(members); }
    public String getRank(UUID playerUUID) { return ranks.getOrDefault(playerUUID, "member"); }
    public int getLevel() { return level; }

    public void addMember(UUID playerUUID) {
        members.add(playerUUID);
        ranks.put(playerUUID, "member");
    }

    public void removeMember(UUID playerUUID) {
        members.remove(playerUUID);
        ranks.remove(playerUUID);
    }

    public boolean hasPermission(UUID playerUUID, String permission) {
        String rank = getRank(playerUUID);
        return rank.equals("leader") || rank.equals("admin");
    }
}