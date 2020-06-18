package com.example.sapihub.Model.Notifications;

import androidx.annotation.Nullable;

public class NotificationData {
    private String user;
    private String title;
    private String body;
    private String date;

    public NotificationData() {

    }

    public NotificationData(String title, String body, String date) {
        this.title = title;
        this.body = body;
        this.date = date;
    }

    public NotificationData(String user, String title, String body, String date) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getUser() {
        return user;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        NotificationData notificationData = (NotificationData) obj;
        if (this.getClass() != obj.getClass()) return false;
        if (this.getBody().equals(notificationData.getBody()) &&
                this.getTitle().equals(notificationData.getTitle()) &&
                this.getDate().equals(notificationData.getDate())){
            return true;
        } else {
            return false;
        }
    }
}
