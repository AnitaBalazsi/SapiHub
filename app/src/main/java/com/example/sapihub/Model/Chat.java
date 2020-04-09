package com.example.sapihub.Model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private List<String> users;
    private List<Message> messages = new ArrayList<>();

    public Chat(List<String> users) {
        this.users = users;
    }

    public Chat(List<String> users, List<Message> messageList) {
        this.users = users;
        this.messages = messageList;
    }

    public Chat() {
    }

    public List<String> getUsers() {
        return users;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message){
        messages.add(message);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Chat chat = (Chat) obj;
        if (this.getClass() != obj.getClass()) return false;
        for (String user : users){
            if (!chat.getUsers().contains(user)){
                return false;
            }
        }
        return true;
    }
}
