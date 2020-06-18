package com.example.sapihub.Model;

import androidx.annotation.Nullable;

public class Message {
    private String sender;
    private String receiver;
    private String content;
    private String date;
    private String type;
    private boolean seen;

    public Message() {
    }

    public Message(String sender, String receiver, String content, String date, String type, boolean seen) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.date = date;
        this.type = type;
        this.seen = seen;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public boolean isSeen() { return seen; }

    @Override
    public boolean equals(@Nullable Object obj) {
        Message message = (Message) obj;
        if (this.getClass() != obj.getClass()) return false;
        if (this.getContent().equals(message.getContent()) &&
                this.getType().equals(message.getType()) &&
                this.getDate().equals(message.getDate()) &&
                this.getSender().equals(message.getSender()) &&
                this.getReceiver().equals(message.getReceiver())){
            return true;
        } else {
            return false;
        }
    }
}
