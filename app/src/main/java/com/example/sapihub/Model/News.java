package com.example.sapihub.Model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

public class News implements Serializable {
    private String title;
    private String date;
    private String content;
    private String author;
    private String lastComment;
    private List<String> images;
    private List<String> files;
    private List<Poll> polls;
    private List<String> captions;

    public News() {
    }

    public News(String title, String date, String content, String author, String lastComment, List<String> images, List<String> files, List<Poll> polls, List<String> captions) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.author = author;
        this.lastComment = lastComment;
        this.images = images;
        this.files = files;
        this.polls = polls;
        this.captions = captions;
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

    public List<String> getFiles() {
        return files;
    }

    public List<Poll> getPolls() {
        return polls;
    }

    public List<String> getCaptions() {
        return captions;
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