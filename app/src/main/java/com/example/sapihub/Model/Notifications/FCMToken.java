package com.example.sapihub.Model.Notifications;

public class FCMToken {
    String token;

    public FCMToken() {
    }

    public FCMToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
