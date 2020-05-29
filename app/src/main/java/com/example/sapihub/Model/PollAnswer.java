package com.example.sapihub.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PollAnswer implements Serializable {
    private List<String> users = new ArrayList<>();
    private String option;

    public PollAnswer(String option) {
        this.option = option;
    }


    public PollAnswer() {
    }

    public String getOption() {
        return option;
    }

    public List<String> getUsers() {
        return users;
    }

    public void addUserAnswer(String userId){
        users.add(userId);
    }

    public void removeUserAnswer(String userId){
        users.remove(userId);
    }

    public void setOption(String option) {
        this.option = option;
    }
}
