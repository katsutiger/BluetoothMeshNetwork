package com.example.androidtraining;

import android.location.Location;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MeshNode {
    private String nodeId;
    private Location location;
    private List<Message> messageQueue;
    private Set<String> processedMessageIds;
    private final LocationCache locationCache;
    private MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(Message message);
    }

    public MeshNode(String nodeId) {
        this.nodeId = nodeId;
        this.messageQueue = new ArrayList<>();
        this.processedMessageIds = new HashSet<>();
        this.locationCache = new LocationCache();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void updateLocation(Location location) {
        this.location = location;
        LocationMessage locationMessage = new LocationMessage(nodeId, location);
        broadcastMessage(locationMessage);
    }

    public void broadcastMessage(Message message) {
        if (!processedMessageIds.contains(message.getId())) {
            messageQueue.add(message);
            processedMessageIds.add(message.getId());

            if (messageListener != null && !(message instanceof LocationMessage)) {
                messageListener.onMessageReceived(message);
            }

            if (message instanceof LocationMessage) {
                LocationMessage locMessage = (LocationMessage) message;
                locationCache.updateLocation(
                        locMessage.getUserId(),
                        locMessage.getLocation(),
                        locMessage.getUpdateTime()
                );
            }

            relayMessageToNearbyNodes(message);
        }
    }

    private void relayMessageToNearbyNodes(Message message) {
        if (!message.canBeRelayed()) {
            return;
        }
        message.incrementHopCount();

        // 近くのノードを検索して、メッセージを転送
        // この実装は BluetoothService によって行われます
    }

    public Map<String, Location> getNearbyUserLocations() {
        return locationCache.getActiveUserLocations();
    }

    public void receiveMessage(Message message) {
        if (!processedMessageIds.contains(message.getId())) {
            broadcastMessage(message);
        }
    }
}


