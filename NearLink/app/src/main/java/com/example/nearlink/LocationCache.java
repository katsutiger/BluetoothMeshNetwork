package com.example.nearlink;

import android.location.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocationCache {
    private final Map<String, LocationInfo> userLocations;
    private static final long LOCATION_EXPIRY_TIME = 5 * 60 * 1000; // 5分

    public LocationCache() {
        this.userLocations = new ConcurrentHashMap<>();
    }

    public void updateLocation(String userId, Location location, long updateTime) {
        userLocations.put(userId, new LocationInfo(location, updateTime));
    }

    public Map<String, Location> getActiveUserLocations() {
        long now = System.currentTimeMillis();
        Map<String, Location> activeLocations = new HashMap<>();

        userLocations.entrySet().removeIf(entry ->
                (now - entry.getValue().updateTime) > LOCATION_EXPIRY_TIME
        );

        for (Map.Entry<String, LocationInfo> entry : userLocations.entrySet()) {
            activeLocations.put(entry.getKey(), entry.getValue().location);
        }

        return activeLocations;
    }

    private static class LocationInfo {
        final Location location;
        final long updateTime;

        LocationInfo(Location location, long updateTime) {
            this.location = location;
            this.updateTime = updateTime;
        }
    }
}