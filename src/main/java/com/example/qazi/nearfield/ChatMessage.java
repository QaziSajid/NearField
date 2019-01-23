package com.example.qazi.nearfield;

import java.util.Date;

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private long messageTime;
    private double latitude;
    private double longitude;

    public ChatMessage(String messageText, String messageUser, double latitude, double longitude) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.latitude = latitude;
        this.longitude = longitude;
        // Initialize to current time
        messageTime = new Date().getTime();
    }
    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }
    /*
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public void setLongitude(double latitude) { this.latitude = latitude; }
    */
}