package com.spence.drugcraft.addiction;

import java.util.HashMap;
import java.util.Map;

public class PlayerAddictionData {
    private final Map<String, Integer> usesMap = new HashMap<>();

    public void incrementUses(String drugId) {
        usesMap.put(drugId, usesMap.getOrDefault(drugId, 0) + 1);
    }

    public void setUses(String drugId, int uses) {
        usesMap.put(drugId, uses);
    }

    public int getUses(String drugId) {
        return usesMap.getOrDefault(drugId, 0);
    }

    public Map<String, Integer> getUsesMap() {
        return usesMap;
    }
}