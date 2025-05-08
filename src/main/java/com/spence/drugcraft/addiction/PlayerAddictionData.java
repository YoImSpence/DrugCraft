package com.spence.drugcraft.addiction;

import java.util.HashMap;
import java.util.Map;

public class PlayerAddictionData {
    private final Map<String, Integer> drugUses = new HashMap<>();

    public void incrementDrugUse(String drugId) {
        drugUses.put(drugId, drugUses.getOrDefault(drugId, 0) + 1);
    }

    public Map<String, Integer> getDrugUses() {
        return drugUses;
    }

    public void setDrugUses(String drugId, int uses) {
        drugUses.put(drugId, uses);
    }
}