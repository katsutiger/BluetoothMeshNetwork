//package com.example.androidtraining;
//
//import android.Manifest;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothServerSocket;
//import android.bluetooth.BluetoothSocket;
//import android.content.Context;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//public class MessageRelay {
//    private static final String TAG = "MessageRelay";
//    private static final String APP_NAME = "MeshApp";
//    private static final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//
//    private final BluetoothAdapter bluetoothAdapter;
//    private final Context context;
//    private final Handler handler;
//    private AcceptThread acceptThread;
//    private final Set<String> processedMessages;
//    private MessageListener messageListener;
//    private boolean isRunning = false;
//
//    public MessageRelay(Context context) {
//        this.context = context;
//        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        this.handler = new Handler(Looper.getMainLooper());
//        this.processedMessages = new HashSet<>();
//    }
//
//    public interface MessageListener {
//        void onMessageReceived(String message, String sourceDevice);
//        void onMessageRelayed(String message, String targetDevice);
//        void onConnectionStateChanged(boolean isConnected);  // 添加连接状态回调
//    }
//
//    public void setMessageListener(MessageListener listener) {
//        this.messageListener = listener;
//    }
//
//    public void start() {
//        if (!isRunning && bluetoothAdapter != null) {
//            acceptThread = new AcceptThread();
//            acceptThread.start();
//            isRunning = true;
//            if (messageListener != null) {
//                messageListener.onConnectionStateChanged(true);
//            }
//        }
//    }
//
//    public void stop() {
//        if (isRunning) {
//            if (acceptThread != null) {
//                acceptThread.cancel();
//                acceptThread = null;
//            }
//            isRunning = false;
//            if (messageListener != null) {
//                messageListener.onConnectionStateChanged(false);
//            }
//        }
//    }
//
//    // メッセージを送信および中継する
//    public void sendMessage(String message) {
//        String messageId = generateMessageId(message);
//        if (processedMessages.contains(messageId)) {
//            return;
//        }
//        processedMessages.add(messageId);
//
//        // ペアリング済みの全デバイスにメッセージを送信
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        for (BluetoothDevice device : pairedDevices) {
//            new ConnectThread(device, message).start();
//        }
//    }
//
//    private String generateMessageId(String message) {
//        return bluetoothAdapter.getAddress() + "_" + System.currentTimeMillis() + "_" + message.hashCode();
//    }
//
//    // 接続を受け付けるスレッド
//    private class AcceptThread extends Thread {
//        private final BluetoothServerSocket serverSocket;
//
//        public AcceptThread() {
//            BluetoothServerSocket tmp = null;
//            try {
//                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);
//            } catch (IOException e) {
//                Log.e(TAG, "Socket listen() failed", e);
//            }
//            serverSocket = tmp;
//        }
//
//        public void run() {
//            BluetoothSocket socket;
//            while (true) {
//                try {
//                    socket = serverSocket.accept();
//                } catch (IOException e) {
//                    Log.e(TAG, "Socket accept() failed", e);
//                    break;
//                }
//
//                if (socket != null) {
//                    manageConnectedSocket(socket);
//                }
//            }
//        }
//
//        public void cancel() {
//            try {
//                serverSocket.close();
//            } catch (IOException e) {
//                Log.e(TAG, "Could not close the connect socket", e);
//            }
//        }
//    }
//
//    // 接続を確立するスレッド
//    private class ConnectThread extends Thread {
//        private final BluetoothSocket socket;
//        private final String message;
//
//        public ConnectThread(BluetoothDevice device, String message) {
//            BluetoothSocket tmp = null;
//            this.message = message;
//
//            try {
//                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
//            } catch (IOException e) {
//                Log.e(TAG, "Socket create() failed", e);
//            }
//            socket = tmp;
//        }
//
//        public void run() {
//            bluetoothAdapter.cancelDiscovery();
//
//            try {
//                socket.connect();
//                sendMessageThroughSocket(socket, message);
//            } catch (IOException connectException) {
//                try {
//                    socket.close();
//                } catch (IOException closeException) {
//                    Log.e(TAG, "Could not close the client socket", closeException);
//                }
//                return;
//            }
//        }
//    }
//
//    // 接続されたソケットの管理
//    private void manageConnectedSocket(BluetoothSocket socket) {
//        new Thread(() -> {
//            byte[] buffer = new byte[1024];
//            int bytes;
//
//            try {
//                InputStream inputStream = socket.getInputStream();
//                while (true) {
//                    try {
//                        bytes = inputStream.read(buffer);
//                        String message = new String(buffer, 0, bytes);
//                        handleReceivedMessage(message, socket.getRemoteDevice());
//                    } catch (IOException e) {
//                        Log.e(TAG, "Input stream was disconnected", e);
//                        break;
//                    }
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "Error occurred when creating input stream", e);
//            }
//        }).start();
//    }
//
//    // メッセージの送信処理
//    private void sendMessageThroughSocket(BluetoothSocket socket, String message) {
//        try {
//            OutputStream outputStream = socket.getOutputStream();
//            outputStream.write(message.getBytes());
//
//            if (messageListener != null) {
//                handler.post(() -> messageListener.onMessageRelayed(message, socket.getRemoteDevice().getName()));
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Error occurred when sending data", e);
//        }
//    }
//
//    // 受信したメッセージの処理
//    private void handleReceivedMessage(String message, BluetoothDevice sourceDevice) {
//        String messageId = generateMessageId(message);
//        if (processedMessages.contains(messageId)) {
//            return;
//        }
//        processedMessages.add(messageId);
//
//        // リスナーに通知
//        if (messageListener != null) {
//            handler.post(() -> messageListener.onMessageReceived(message, sourceDevice.getName()));
//        }
//
//        // メッセージを他のデバイスに中継
//        sendMessage(message);
//    }
//}
