package com.spence.drugcraft.listeners;

import com.spence.drugcraft.handlers.GUIHandler;

import java.util.Map;
import java.util.UUID;

public interface AdminGUIListener extends GUIHandler {
    Map<UUID, String> getActivePlayers();
}