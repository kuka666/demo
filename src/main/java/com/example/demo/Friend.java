package com.example.demo;

public class Friend {
    private String friend_email;
    private String locationId;
    private String accessLevel;

    public Friend() {
    }

    public Friend(String friend_email, String locationId, String accessLevel) {
        this.friend_email = friend_email;
        this.locationId = locationId;
        this.accessLevel = accessLevel;
    }

    // Getters and setters
    public String getFriend_email() {
        return friend_email;
    }

    public void setFriend_email(String friend_email) {
        this.friend_email = friend_email;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}