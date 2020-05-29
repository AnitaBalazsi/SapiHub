package com.example.sapihub.Model;

import java.io.Serializable;
import java.util.List;

public class Poll implements Serializable {
    private List<PollAnswer> answers;
    private int totalVotes;


    public Poll(List<PollAnswer> answers) {
        this.answers = answers;
        this.totalVotes = 0;
    }

    public Poll() {
    }

    public void setAnswers(List<PollAnswer> answers) {
        this.answers = answers;
    }

    public List<PollAnswer> getAnswers() {
        return answers;
    }

}
