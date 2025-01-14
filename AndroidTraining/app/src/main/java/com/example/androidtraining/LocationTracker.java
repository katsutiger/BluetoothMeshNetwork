package com.example.androidtraining;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationTracker {
    private static final long MIN_UPDATE_INTERVAL = 1000; // 1 second
    private static final float MIN_DISTANCE = 1.0f; // 1 meter

    private final Context context;
    private final MeshNode meshNode;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;
    private Location lastLocation;
    private boolean isTracking = false;

    public LocationTracker(Context context, MeshNode meshNode) {
        this.context = context.getApplicationContext();  // Fix: Use application context
        this.meshNode = meshNode;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    handleLocationUpdate(location);
                }
            }
        };
    }

    public void startTracking() {
        if (isTracking) {
            return;
        }

        try {
            if (checkLocationPermission()) {
                LocationRequest locationRequest = createLocationRequest();
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback, Looper.getMainLooper());
                isTracking = true;

                // Get initial location
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this::handleLocationUpdate);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void stopTracking() {
        if (!isTracking) {
            return;
        }

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
                    .addOnCompleteListener(task -> isTracking = false);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest.Builder(MIN_UPDATE_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(MIN_DISTANCE)
                .setWaitForAccurateLocation(true)
                .build();
    }

    private void handleLocationUpdate(Location location) {
        if (!isValidLocation(location)) {
            return;
        }

        lastLocation = location;
        if (meshNode != null) {
            meshNode.updateLocation(location);
        }
    }

    private boolean isValidLocation(Location location) {
        if (location == null) {
            return false;
        }

        // Check if location is too old
        long locationAge = System.currentTimeMillis() - location.getTime();
        if (locationAge > 30000) { // 30 seconds
            return false;
        }

        // Check if accuracy is reasonable
        if (location.hasAccuracy() && location.getAccuracy() > 100) { // 100 meters
            return false;
        }

        return true;
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public Location getLastLocation() {
        return lastLocation;
    }
}