package com.wlug.wlug.model;

/**
 * Created by Inspiron on 18-06-2016.
 */
import java.io.Serializable;

public class User implements Serializable {
   String name, email;

    public User() {
    }

    public User( String name, String email) {

        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}