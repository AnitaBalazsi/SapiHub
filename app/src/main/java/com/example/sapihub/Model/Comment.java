package com.example.sapihub.Model;

import androidx.annotation.Nullable;

public class Comment {
    private String author;
    private String date;
    private String content;

    public Comment() {
    }

    public Comment(String author, String date, String content) {
        this.author = author;
        this.date = date;
        this.content = content;
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

    @Override
    public boolean equals(@Nullable Object obj) {
        Comment comment = (Comment) obj;
        if (this.getClass() != obj.getClass()) return false;
        if (this.getAuthor().equals(comment.getAuthor()) &&
                this.getContent().equals(comment.getContent()) &&
                this.getDate().equals(comment.getDate())){
            return true;
        } else {
            return false;
        }
    }
}
