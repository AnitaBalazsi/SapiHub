package com.example.sapihub.Model;

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
}
