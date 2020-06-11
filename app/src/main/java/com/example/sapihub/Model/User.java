package com.example.sapihub.Model;

public class User {
    private String name;
    private Token userId;
    private String occupation;
    private String department;
    private String degree;
    private String studyYear;
    private String typingTo;
    private String status;

    public User() {
    }

    public User(String name, Token token) {
        this.name = name;
        this.userId = token;
    }

    public String getName() {
        return name;
    }

    public Token getUserId() {
        return userId;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getDepartment() {
        return department;
    }

    public String getDegree() {
        return degree;
    }

    public String getStudyYear() {
        return studyYear;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public void setStudyYear(String studyYear) {
        this.studyYear = studyYear;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
}