package com.example.androidtraining;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Map;
import java.util.UUID;

public class Gamen1Activity extends AppCompatActivity {
    private TextView broadcastText;
    private Button chatButton;
    private MeshNode meshNode;
    private BluetoothService bluetoothService;
    private LocationTracker locationTracker;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamen1);

        // Handlerの初期化
        handler = new Handler(Looper.getMainLooper());

        // UI要素の初期化
        broadcastText = findViewById(R.id.broadcastText);
        chatButton = findViewById(R.id.chatButton);

        // MeshNodeの初期化
        String nodeId = UUID.randomUUID().toString();
        meshNode = new MeshNode(nodeId);

        // BluetoothServiceの初期化
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothService = new BluetoothService(bluetoothAdapter, meshNode, this);

        // LocationTrackerの初期化
        locationTracker = new LocationTracker(this, meshNode);

        // チャットボタンのクリックリスナー
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(Gamen1Activity.this, Gamen2Activity.class);
            intent.putExtra("NODE_ID", nodeId);
            startActivity(intent);
        });

        // 近くのユーザー情報の更新
        startNearbyUsersUpdate();

    }

    private void startNearbyUsersUpdate() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateNearbyUsersDisplay();
                handler.postDelayed(this, 1000); // 1秒ごとに更新
            }
        });
    }

    private void updateNearbyUsersDisplay() {
        Map<String, Location> nearbyLocations = meshNode.getNearbyUserLocations();
        StringBuilder displayText = new StringBuilder();
        Location myLocation = locationTracker.getLastLocation();

        if (myLocation != null) {
            for (Map.Entry<String, Location> entry : nearbyLocations.entrySet()) {
                String userId = entry.getKey();
                Location userLocation = entry.getValue();

                // 距離の計算
                float[] results = new float[1];
                Location.distanceBetween(
                        myLocation.getLatitude(),
                        myLocation.getLongitude(),
                        userLocation.getLatitude(),
                        userLocation.getLongitude(),
                        results
                );
                float distance = results[0];

                // 表示テキストの作成
                String userInfo = String.format(
                        "ユーザー: %s\n距離: %.0fm\n\n",
                        userId.substring(0, 8),
                        distance
                );
                displayText.append(userInfo);
            }
        }

        if (displayText.length() == 0) {
            displayText.append("近くにユーザーはいません");
        }

        broadcastText.setText(displayText.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothService.startDiscovery();
        locationTracker.startTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothService.stopDiscovery();
        locationTracker.stopTracking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

}