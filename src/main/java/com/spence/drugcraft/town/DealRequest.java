package com.spence.drugcraft.town;

import org.bukkit.Location;

import java.util.UUID;

public class DealRequest {
    private final UUID playerUUID;
    private final UUID citizenUUID;
    private final Location meetupLocation;

    public DealRequest(UUID playerUUID, UUID citizenUUID, Location meetupLocation) {
        this.playerUUID = playerUUID;
        this.citizenUUID = citizenUUID;
        this.meetupLocation = meetupLocation;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getCitizenUUID() {
        return citizenUUID;
    }

    public Location getMeetupLocation() {
        return meetupLocation;
    }
}