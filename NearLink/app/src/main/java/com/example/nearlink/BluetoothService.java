package com.example.nearlink;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import java.util.UUID;

public class BluetoothService {
    private final BluetoothAdapter bluetoothAdapter;
    private final MeshNode meshNode;
    private final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final Context context;
    private boolean isDiscovering = false;

    public BluetoothService(BluetoothAdapter adapter, MeshNode meshNode, Context context) {
        this.bluetoothAdapter = adapter;
        this.meshNode = meshNode;
        this.context = context;
    }

    public void startDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled() && !isDiscovering) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                } else {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                registerReceivers();
                bluetoothAdapter.startDiscovery();
                isDiscovering = true;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopDiscovery() {
        if (bluetoothAdapter != null && isDiscovering) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                bluetoothAdapter.cancelDiscovery();
                unregisterReceivers();
                isDiscovering = false;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerReceivers() {
        try {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(discoveryReceiver, filter);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void unregisterReceivers() {
        try {
            context.unregisterReceiver(discoveryReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent == null || intent.getAction() == null) return;

                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        BluetoothDevice device = intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                        if (device != null) {
                            handleNewDevice(device);
                        }
                    } else {
                        @SuppressWarnings("deprecation")
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null) {
                            handleNewDevice(device);
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isDiscovering) {
                            startDiscovery();
                        }
                    }, 1000);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    };

    private void handleNewDevice(BluetoothDevice device) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            String deviceName = device.getName();
            if (deviceName != null) {
                // デバイスとの接続処理を実装
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}