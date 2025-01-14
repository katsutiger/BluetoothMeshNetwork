package com.example.androidtraining;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import java.util.Map;
import java.util.UUID;

public class Gamen2Activity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int MAP_ACTIVITY_REQUEST_CODE = 1001;
    private static final String TAG = "Gamen2Activity";

    private Button returnButton;
    private Button locationButton;
    private Button sendButton;
    private EditText messageInput;
    private TextView messageDisplay;
    private TextView statusText;
    private MeshNode meshNode;
    private LocationTracker locationTracker;
    private BluetoothService bluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamen2);

        // UI要素の初期化
        initializeViews();

        // MeshNodeの取得または作成
        String nodeId = getIntent().getStringExtra("NODE_ID");
        if (nodeId == null) {
            nodeId = UUID.randomUUID().toString();
        }
        meshNode = new MeshNode(nodeId);

        // BluetoothServiceの初期化
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothService = new BluetoothService(bluetoothAdapter, meshNode, this);

        // LocationTrackerの初期化
        locationTracker = new LocationTracker(this, meshNode);

        // ボタンのクリックリスナーを設定
        setupButtonListeners();

        // メッセージ受信のリスナーを設定
        setupMessageListener();
    }

    private void initializeViews() {
        returnButton = findViewById(R.id.returnButton);
        locationButton = findViewById(R.id.locationButton);
        sendButton = findViewById(R.id.sendButton);
        messageInput = findViewById(R.id.messageInput);
        messageDisplay = findViewById(R.id.messageDisplay);
        statusText = findViewById(R.id.statusText);
    }

    private void setupButtonListeners() {
        // 戻るボタン
        returnButton.setOnClickListener(v -> finish());

        // 位置ボタン
        locationButton.setOnClickListener(v -> {
            Log.d(TAG, "Location button clicked");

            if (!checkLocationPermission()) {
                Log.d(TAG, "Location permission not granted, requesting...");
                requestLocationPermission();
                return;
            }

            try {
                if (!isActivityRegistered("com.example.androidtraining.MapActivity")) {
                    Log.e(TAG, "MapActivity is not registered in AndroidManifest.xml");
                    Toast.makeText(this, "地図画面の設定が不正です", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Creating intent for MapActivity");
                // MapActivityへ遷移
                Intent intent = new Intent(Gamen2Activity.this, MapActivity.class);

                // 現在位置を取得
                Location location = locationTracker.getLastLocation();
                // 必要に応じて位置情報をIntentに追加
                if (location != null) {
                    Log.d(TAG, "Location found: " + location.getLatitude() + ", " + location.getLongitude());
                    intent.putExtra("LATITUDE", location.getLatitude());
                    intent.putExtra("LONGITUDE", location.getLongitude());
                }else {
                    Log.d(TAG, "No location available");
                }

                // 現在のノードIDを渡す
                intent.putExtra("NODE_ID", meshNode.getNodeId());
                Log.d(TAG, "Starting MapActivity for result");

                // MapActivityを起動（結果を待つ）
                startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE);
            } catch (Exception e) {
                Log.e(TAG, "Error starting MapActivity", e);
                Toast.makeText(this, "地図画面の起動に失敗しました: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        // 送信ボタン
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString();
            if (!messageText.isEmpty()) {
                Location location = locationTracker.getLastLocation();
                Message message = new Message(messageText, meshNode.getNodeId(), location);
                meshNode.broadcastMessage(message);
                messageInput.setText("");
                appendMessage("自分", messageText);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult called: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == MAP_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String locationMessage = data.getStringExtra("LOCATION_MESSAGE");
            if (locationMessage != null) {
                Log.d(TAG, "Received location message: " + locationMessage);
                messageInput.setText(locationMessage);
            } else {
                Log.w(TAG, "Received null location message");
            }
        }
    }

    private boolean isActivityRegistered(String activityName) {
        try {
            Class.forName(activityName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void setupMessageListener() {
        meshNode.setMessageListener(message -> {
            runOnUiThread(() -> {
                String senderId = message.getSenderId().substring(0, 8);
                appendMessage(senderId, message.getContent());
            });
        });
    }

    private void appendMessage(String sender, String content) {
        String currentText = messageDisplay.getText().toString();
        String newMessage = String.format("%s: %s\n", sender, content);
        messageDisplay.setText(currentText + newMessage);
    }

    private void updateConnectionStatus(boolean isConnected) {
        statusText.setText(isConnected ? "接続中" : "未接続");
        statusText.setTextColor(isConnected ?
                Color.parseColor("#53D558") : Color.parseColor("#666666"));
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationButton.performClick();
            } else {
                Toast.makeText(this, "位置情報の権限が必要です", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothService.startDiscovery();
        locationTracker.startTracking();
        updateConnectionStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothService.stopDiscovery();
        locationTracker.stopTracking();
        updateConnectionStatus(false);
    }
}