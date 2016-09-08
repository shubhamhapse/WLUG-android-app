package com.wlug.wlug.model;

/**
 * Created by Inspiron on 18-06-2016.
 */

import java.io.Serializable;
public class ChatRoom implements Serializable {
    String name, lastMessage, timestamp;
    int unreadCount;

    public ChatRoom() {
    }

    public ChatRoom(String name, String lastMessage, String timestamp, int unreadCount) {

        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
