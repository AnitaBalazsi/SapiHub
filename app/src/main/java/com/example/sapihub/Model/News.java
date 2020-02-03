package com.example.sapihub.Model;

import java.io.Serializable;

public class News implements Serializable {
    private String title;
    private String date;
    private String content;
    private String imageName;

    public News() {
    }

    public News(String title, String date, String content, String imageName) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.imageName = imageName;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
