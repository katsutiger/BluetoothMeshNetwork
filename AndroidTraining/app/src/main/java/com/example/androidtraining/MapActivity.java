package com.example.androidtraining;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int LOCATION_UPDATE_INTERVAL = 1000; // 1 second

    private GoogleMap mMap;
    private MeshNode meshNode;
    private LocationTracker locationTracker;
    private Handler updateHandler;
    private Map<String, Marker> userMarkers;
    private Circle rangeCircle;
    private boolean isFirstLocationUpdate = true;
    private Marker selectedLocationMarker;
    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");

        try {
            setContentView(R.layout.activity_map);

            // Initialize components
            updateHandler = new Handler(Looper.getMainLooper());
            userMarkers = new HashMap<>();

            // Get MeshNode from intent or create new
            String nodeId = getIntent().getStringExtra("NODE_ID");
            if (nodeId == null) {
                Log.w(TAG, "No NODE_ID provided, generating new one");
                nodeId = java.util.UUID.randomUUID().toString();
            }
            meshNode = new MeshNode(nodeId);
            Log.d(TAG, "Using NODE_ID: " + nodeId);

            // Initialize LocationTracker
            locationTracker = new LocationTracker(this, meshNode);

            // Initialize UI
            initializeUI();

            // Check permissions and initialize map
            checkPermissionsAndInitializeMap();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "地図の初期化に失敗しました", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeUI() {
        try {
            Button shareButton = findViewById(R.id.centerButton);
            shareButton.setText("位置共有");
            shareButton.setOnClickListener(v -> shareLocation());

            Button returnButton = findViewById(R.id.mapReturnButton);
            returnButton.setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UI", e);
        }
    }

    private void shareLocation() {
        try {
            String locationMessage;
            if (selectedLocation != null) {
                // 選択したロケーションを共有
                locationMessage = String.format(Locale.getDefault(),
                        "選択した位置: %.6f, %.6f",
                        selectedLocation.latitude,
                        selectedLocation.longitude);
            } else {
                // 現在位置を共有
                Location location = locationTracker.getLastLocation();
                if (location != null) {
                    locationMessage = String.format(Locale.getDefault(),
                            "現在位置: %.6f, %.6f",
                            location.getLatitude(),
                            location.getLongitude());
                } else {
                    Toast.makeText(this, "位置情報を取得できません", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("LOCATION_MESSAGE", locationMessage);
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error sharing location", e);
            Toast.makeText(this, "位置情報の共有に失敗しました", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPermissionsAndInitializeMap() {
        Log.d(TAG, "Checking permissions");
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (!hasPermissions(permissions)) {
            Log.d(TAG, "Requesting permissions");
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
        } else {
            Log.d(TAG, "Permissions already granted");
            initializeMap();
        }
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initializeMap() {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            } else {
                Log.e(TAG, "Map fragment is null");
                Toast.makeText(this, "地図の初期化に失敗しました", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing map", e);
            Toast.makeText(this, "地図の初期化に失敗しました", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady called");
        try {
            mMap = googleMap;

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
            }

            mMap.setOnMapClickListener(this::handleMapClick);

            LatLng defaultLocation = new LatLng(35.6762, 139.6503);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));

            setupMap();
            startLocationUpdates();
        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady", e);
            runOnUiThread(() -> {
                Toast.makeText(this, "地図の初期化に失敗しました: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void handleMapClick(LatLng latLng) {

        selectedLocation = latLng;


        if (selectedLocationMarker != null) {
            selectedLocationMarker.remove();
        }


        selectedLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("選択した位置")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        Toast.makeText(this, "位置が選択されました", Toast.LENGTH_SHORT).show();
    }

    private void setupMap() {
        if (mMap == null) return;

        try {
            // Add range circle
            CircleOptions circleOptions = new CircleOptions()
                    .fillColor(Color.argb(70, 100, 149, 237))
                    .strokeColor(Color.BLUE)
                    .strokeWidth(2)
                    .radius(1000) // 1km radius
                    .center(new LatLng(35.6762, 139.6503)); // 默认位置
            rangeCircle = mMap.addCircle(circleOptions);
        } catch (Exception e) {
            Log.e(TAG, "Error in setupMap", e);
        }
    }

    private void startLocationUpdates() {
        if (mMap == null) return;

        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isFinishing()) {
                        updateLocations();
                        updateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in location updates", e);
                }
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void updateLocations() {
        if (mMap == null || isFinishing()) return;

        try {
            Location myLocation = locationTracker.getLastLocation();
            if (myLocation != null) {
                LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                rangeCircle.setCenter(myLatLng);

                if (isFirstLocationUpdate) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, DEFAULT_ZOOM));
                    isFirstLocationUpdate = false;
                }

                Map<String, Location> nearbyLocations = meshNode.getNearbyUserLocations();
                updateUserMarkers(nearbyLocations);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating locations", e);
        }
    }

    private void updateUserMarkers(Map<String, Location> nearbyLocations) {
        // Remove markers for users no longer nearby
        userMarkers.entrySet().removeIf(entry -> !nearbyLocations.containsKey(entry.getKey()));

        // Update or add markers for nearby users
        for (Map.Entry<String, Location> entry : nearbyLocations.entrySet()) {
            String userId = entry.getKey();
            Location userLocation = entry.getValue();
            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

            Marker marker = userMarkers.get(userId);
            if (marker != null) {
                marker.setPosition(userLatLng);
            } else {
                marker = mMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("User: " + userId.substring(0, 8))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                userMarkers.put(userId, marker);
            }
        }
    }

    private void centerOnMyLocation() {
        Location location = locationTracker.getLastLocation();
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeMap();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationTracker.startTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationTracker.stopTracking();
    }

    @Override
    protected void onDestroy() {
        try {
            updateHandler.removeCallbacksAndMessages(null);
            Log.d(TAG, "MapActivity destroyed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
        super.onDestroy();
    }
}