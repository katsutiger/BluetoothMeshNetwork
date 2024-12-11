package com.example.androidtraining;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int LOCATION_UPDATE_INTERVAL = 1000; // 1 second

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private MeshNode meshNode;
    private LocationTracker locationTracker;
    private Handler updateHandler;
    private Map<String, Marker> userMarkers;
    private Circle rangeCircle;
    private Button centerButton;
    private boolean isFirstLocationUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize components
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        updateHandler = new Handler(Looper.getMainLooper());
        userMarkers = new HashMap<>();

        // Get MeshNode from intent or create new
        String nodeId = getIntent().getStringExtra("NODE_ID");
        meshNode = new MeshNode(nodeId != null ? nodeId : java.util.UUID.randomUUID().toString());

        // Initialize LocationTracker
        locationTracker = new LocationTracker(this, meshNode);

        // Initialize UI
        initializeUI();

        // Check permissions and initialize map
        checkPermissionsAndInitializeMap();
    }

    private void initializeUI() {
        centerButton = findViewById(R.id.centerButton);
        centerButton.setOnClickListener(v -> centerOnMyLocation());

        Button returnButton = findViewById(R.id.mapReturnButton);
        returnButton.setOnClickListener(v -> finish());
    }

    private void checkPermissionsAndInitializeMap() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
        } else {
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
        startLocationUpdates();
    }

    private void setupMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Add range circle
        CircleOptions circleOptions = new CircleOptions()
                .fillColor(Color.argb(70, 100, 149, 237))
                .strokeColor(Color.BLUE)
                .strokeWidth(2)
                .radius(1000); // 1km radius
        rangeCircle = mMap.addCircle(circleOptions);
    }

    private void startLocationUpdates() {
        updateHandler.post(new Runnable() {
            @Override
            public void run() {
                updateLocations();
                updateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        });
    }

    private void updateLocations() {
        Location myLocation = locationTracker.getLastLocation();
        if (myLocation == null) return;

        LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

        // Update range circle position
        rangeCircle.setCenter(myLatLng);

        // Center map on first location update
        if (isFirstLocationUpdate) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, DEFAULT_ZOOM));
            isFirstLocationUpdate = false;
        }

        // Update nearby users
        Map<String, Location> nearbyLocations = meshNode.getNearbyUserLocations();
        updateUserMarkers(nearbyLocations);
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
        super.onDestroy();
        updateHandler.removeCallbacksAndMessages(null);
    }
}