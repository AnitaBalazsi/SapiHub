package com.example.sapihub.Model.Notifications;

public class NotificationData {
    private String user;
    private String title;
    private String body;
    private int icon;
    private boolean isScheduled;
    private String scheduledTime;

    public NotificationData() {

    }

    public NotificationData(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public NotificationData(String user, String title, String body, int icon) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.icon = icon;
    }

    public NotificationData(String user, String title, String body, int icon, boolean isScheduled, String scheduledTime) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.icon = icon;
        this.isScheduled = isScheduled;
        this.scheduledTime = scheduledTime;
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

    public int getIcon() {
        return icon;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }
}
