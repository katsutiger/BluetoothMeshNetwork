package com.example.nearlink;

import android.location.Location;

public class LocationMessage extends Message {
    private final Location location;
    private final String userId;
    private final long updateTime;

    public LocationMessage(String userId, Location location) {
        super("LOCATION_UPDATE", userId, location);
        this.userId = userId;
        this.location = location;
        this.updateTime = System.currentTimeMillis();
    }

    public Location getLocation() {
        return location;
    }

    public String getUserId() {
        return userId;
    }

    public long getUpdateTime() {
        return updateTime;
    }
}