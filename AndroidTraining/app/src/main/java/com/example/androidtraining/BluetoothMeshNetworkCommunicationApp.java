package com.example.androidtraining;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothMeshNetworkCommunicationApp extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private LocationManager locationManager;
    private LocationTracker locationTracker;
    private Handler updateHandler;
    private MeshNode meshNode;
    private BluetoothService bluetoothService;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int LOCATION_UPDATE_INTERVAL = 1000; // 1秒ごとに更新

    private final ActivityResultLauncher<Intent> bluetoothEnableLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Bluetooth有効化成功
                    startServices();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamen2);

        // Handlerの初期化
        updateHandler = new Handler(Looper.getMainLooper());

        // MeshNodeの初期化
        String nodeId = UUID.randomUUID().toString();
        meshNode = new MeshNode(nodeId);

        // Bluetooth初期化
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // BluetoothServiceの初期化
        bluetoothService = new BluetoothService(bluetoothAdapter, meshNode, this);

        // 位置情報マネージャー初期化
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // LocationTrackerの初期化
        locationTracker = new LocationTracker(this, meshNode);

        // 必要な権限の確認と要求
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_LOCATION_PERMISSION
            );
        }
    }

    private void startServices() {
        if (checkPermissions()) {
            locationTracker.startTracking();
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateLocationDisplay();
                updateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        };
        updateHandler.post(updateRunnable);
    }

    private void updateLocationDisplay() {
        Map<String, Location> nearbyLocations = meshNode.getNearbyUserLocations();
        // UIの更新処理
    }

    private boolean checkPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_LOCATION_PERMISSION
            );
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        } else {
            startServices();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }
        bluetoothService.stopDiscovery();
        locationTracker.stopTracking();
    }
}