package com.example.sapihub.Model;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

public class News implements Serializable {
    private String title;
    private String date;
    private String content;
    private String author;
    private List<String> images;

    public News() {
    }

    public News(String title, String date, String content, String author, List<String> images) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.author = author;
        this.images = images;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getImages() {
        return images;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        News news = (News) obj;
        if (this.getClass() != obj.getClass()) return false;
        if (this.getAuthor().equals(news.getAuthor()) &&
                this.getTitle().equals(news.getTitle()) &&
                this.getDate().equals(news.getDate())){
            return true;
        } else {
            return false;
        }
    }
}