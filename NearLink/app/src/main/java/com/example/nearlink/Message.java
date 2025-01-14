package com.example.nearlink;

import android.location.Location;
import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    private String id;
    private String senderId;
    private String content;
    private Location senderLocation;
    private long timestamp;
    private int hopCount;
    private static final int MAX_HOPS = 10;

    public Message(String content, String senderId, Location senderLocation) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.senderId = senderId;
        this.senderLocation = senderLocation;
        this.timestamp = System.currentTimeMillis();
        this.hopCount = 0;
    }

    public boolean canBeRelayed() {
        return hopCount < MAX_HOPS;
    }

    public void incrementHopCount() {
        this.hopCount++;
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public String getSenderId() { return senderId; }
    public Location getSenderLocation() { return senderLocation; }
    public long getTimestamp() { return timestamp; }
}