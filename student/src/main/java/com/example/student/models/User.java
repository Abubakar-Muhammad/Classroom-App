package com.example.student.models;

import java.util.List;

public class User {
    private String name;
    private String email;
    private String profile_image;
    private String phone;
    private String type;
    private String id;
    private String student_id;
    private List<Course> courses;

    public User(String name, String email, String profile_image, String phone, String type, String id, List<Course> courses) {
        this.name = name;
        this.email = email;
        this.profile_image = profile_image;
        this.phone = phone;
        this.type = type;
        this.id = id;
        this.courses = courses;
    }

    public User(String name, String email, String profile_image, String phone, String type, String id, String student_id, List<Course> courses) {
        this.name = name;
        this.email = email;
        this.profile_image = profile_image;
        this.phone = phone;
        this.type = type;
        this.id = id;
        this.student_id = student_id;
        this.courses = courses;
    }

    public User() {
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", phone='" + phone + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", student_id='" + student_id + '\'' +
                ", courses=" + courses +
                '}';
    }
}
