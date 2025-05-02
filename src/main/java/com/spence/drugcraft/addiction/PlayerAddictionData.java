package com.spence.drugcraft.addiction;

import java.util.HashMap;
import java.util.Map;

public class PlayerAddictionData {
    private final Map<String, Integer> uses = new HashMap<>();
    private final Map<String, Long> lastUse = new HashMap<>();

    public int getUses(String drugId) {
        return uses.getOrDefault(drugId, 0);
    }

    public void addUse(String drugId) {
        uses.put(drugId, uses.getOrDefault(drugId, 0) + 1);
        lastUse.put(drugId, System.currentTimeMillis());
    }

    public long getLastUse(String drugId) {
        return lastUse.getOrDefault(drugId, 0L);
    }

    public Map<String, Integer> getUsesMap() {
        return uses;
    }

    public Map<String, Long> getLastUseMap() {
        return lastUse;
    }
}