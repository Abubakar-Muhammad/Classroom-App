package com.example.projectapplication.models;

public class AssignmentSubmission {
    String assignment_id;
    String submission_status;
    String submission_time;
    String student_id;
    String submission_url;
    String submission_name;
    String submission_location;
    String user_id;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public AssignmentSubmission() {
    }

    public String getAssignment_id() {
        return assignment_id;
    }

    public void setAssignment_id(String assignment_id) {
        this.assignment_id = assignment_id;
    }

    public String getSubmission_status() {
        return submission_status;
    }

    public void setSubmission_status(String submission_status) {
        this.submission_status = submission_status;
    }

    public String getSubmission_time() {
        return submission_time;
    }

    public void setSubmission_time(String submission_time) {
        this.submission_time = submission_time;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getSubmission_url() {
        return submission_url;
    }

    public void setSubmission_url(String submission_url) {
        this.submission_url = submission_url;
    }

    public String getSubmission_name() {
        return submission_name;
    }

    public void setSubmission_name(String submission_name) {
        this.submission_name = submission_name;
    }

    public String getSubmission_location() {
        return submission_location;
    }

    public void setSubmission_location(String submission_location) {
        this.submission_location = submission_location;
    }

    @Override
    public String toString() {
        return "AssignmentSubmission{" +
                "assignment_id='" + assignment_id + '\'' +
                ", submission_status='" + submission_status + '\'' +
                ", submission_time='" + submission_time + '\'' +
                ", student_id='" + student_id + '\'' +
                ", submission_url='" + submission_url + '\'' +
                ", submission_name='" + submission_name + '\'' +
                ", submission_location='" + submission_location + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
