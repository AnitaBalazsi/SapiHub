package com.example.sapihub.Model.Notifications;

public class NotificationSender {
    private NotificationData notification;
    private String to;

    public NotificationSender() {
    }

    public NotificationSender(NotificationData notification, String to) {
        this.notification = notification;
        this.to = to;
    }

    public NotificationData getNotification() {
        return notification;
    }

    public String getTo() {
        return to;
    }
}
