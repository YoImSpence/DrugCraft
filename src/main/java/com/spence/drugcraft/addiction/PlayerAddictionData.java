package com.spence.drugcraft.addiction;

import java.util.HashMap;
import java.util.Map;

public class PlayerAddictionData {
    private final Map<String, Integer> uses = new HashMap<>();

    public void addUse(String drugId) {
        uses.put(drugId, uses.getOrDefault(drugId, 0) + 1);
    }

    public void setUses(String drugId, int count) {
        if (count <= 0) {
            uses.remove(drugId);
        } else {
            uses.put(drugId, count);
        }
    }

    public int getUses(String drugId) {
        return uses.getOrDefault(drugId, 0);
    }

    public Map<String, Integer> getUsesMap() {
        return uses;
    }
}