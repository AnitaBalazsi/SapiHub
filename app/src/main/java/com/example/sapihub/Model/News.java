package com.example.sapihub.Model;

public class News {
    private String title;
    private String content;
    private String imageName;

    public News() {
    }

    public News(String title, String content, String imageName) {
        this.title = title;
        this.content = content;
        this.imageName = imageName;
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
