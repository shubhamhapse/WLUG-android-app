package com.wlug.wlug.model;

/**
 * Created by Inspiron on 18-06-2016.
 */

import java.io.Serializable;

public class Message implements Serializable {
    String message, createdAt;
    User user;

    public Message() {
    }

    public Message(String message, String createdAt, User user) {

        this.message = message;
        this.createdAt = createdAt;
        this.user = user;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
