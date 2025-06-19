package com.spence.drugcraft.town;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;

public class TownCitizen extends Trait {
    public TownCitizen() {
        super("towncitizen");
    }

    @Override
    public void onAttach() {
        // Initialize NPC pathfinding
        npc.getNavigator().getDefaultParameters().baseSpeed(1.0f);
    }

    // Placeholder: Add pathfinding logic
}