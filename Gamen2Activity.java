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
import androidx.appcompat.app.AppCompatActivity;
import java.util.Map;
import java.util.UUID;
import android.content.Intent;

public class Gamen2Activity extends AppCompatActivity {
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
        returnButton.setOnClickListener(v -> finish());

        locationButton.setOnClickListener(v -> {
            Location location = locationTracker.getLastLocation();
            if (location != null) {
                // Create location message string
                String locationMessage = String.format("現在地: %.6f, %.6f",
                        location.getLatitude(),
                        location.getLongitude());

                // Start MapActivity
                Intent intent = new Intent(Gamen2Activity.this, MapActivity.class);
                intent.putExtra("LOCATION_LAT", location.getLatitude());
                intent.putExtra("LOCATION_LNG", location.getLongitude());
                startActivity(intent);

                // Set the location message in the input field
                messageInput.setText(locationMessage);
            }
        });

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