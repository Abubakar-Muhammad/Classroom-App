package com.example.student.models;

import java.util.List;

public class Course {
    private String course_name;
    private String course_code;
    private String creator_id;
    private String course_id;
    private String date_created;

    public Course(String course_name, String course_code, String creator_id, String course_id, String date_created, List<User> users) {
        this.course_name = course_name;
        this.course_code = course_code;
        this.creator_id = creator_id;
        this.course_id = course_id;
        this.date_created = date_created;
    }
    public Course() { }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getCourse_code() {
        return course_code;
    }

    public void setCourse_code(String course_code) {
        this.course_code = course_code;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getCourse_id() {
        return course_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Course{" +
                "course_name='" + course_name + '\'' +
                ", course_code='" + course_code + '\'' +
                ", creator_id='" + creator_id + '\'' +
                ", course_id='" + course_id + '\'' +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}
