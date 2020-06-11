package com.example.sapihub.Model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String author;
    private String date;
    private String content;
    private List<String> likes = new ArrayList<>();
    private List<String> images = new ArrayList<>();

    public Comment() {
    }

    public Comment(String author, String date, String content, List<String> images) {
        this.author = author;
        this.date = date;
        this.content = content;
        this.images = images;
    }

    public Comment(String author, String date, String content, List<String> likes, List<String> images) {
        this.author = author;
        this.date = date;
        this.content = content;
        this.likes = likes;
        this.images = images;
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

    public List<String> getLikes() {
        return likes;
    }

    public void addLike(String user){
        likes.add(user);
    }

    public void removeLike(String user){
        likes.remove(user);
    }

    public List<String> getImages() {
        return images;
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
