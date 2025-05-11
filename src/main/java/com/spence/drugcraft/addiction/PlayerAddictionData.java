package com.spence.drugcraft.addiction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerAddictionData {
    private final UUID playerUUID;
    private final Map<String, Integer> addictionLevels;

    public PlayerAddictionData(UUID playerUUID, Map<String, Integer> addictionLevels) {
        this.playerUUID = playerUUID;
        this.addictionLevels = new HashMap<>(addictionLevels);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Map<String, Integer> getAddictionLevels() {
        return new HashMap<>(addictionLevels);
    }

    public int getAddictionLevel(String drugId) {
        return addictionLevels.getOrDefault(drugId, 0);
    }

    public void setAddictionLevel(String drugId, int level) {
        addictionLevels.put(drugId, level);
    }
}