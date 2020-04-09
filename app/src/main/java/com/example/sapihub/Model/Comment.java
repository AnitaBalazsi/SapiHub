package com.example.sapihub.Model;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String author;
    private String date;
    private String content;
    private List<Comment> replies = new ArrayList<>();


    public Comment() {
    }

    public Comment(String author, String date, String content) {
        this.author = author;
        this.date = date;
        this.content = content;
    }

    public Comment(String author, String date, String content, List<Comment> replies) {
        this.author = author;
        this.date = date;
        this.content = content;
        this.replies = replies;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public void addReply(Comment reply){
        replies.add(reply);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Comment comment = (Comment) obj;
        if (this.getClass() != obj.getClass()) return false;
        if (this.getAuthor().equals(comment.getAuthor()) &&
                this.getDate().equals(comment.getDate())){
            return true;
        } else {
            return false;
        }
    }
}
