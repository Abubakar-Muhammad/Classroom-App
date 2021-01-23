package com.example.projectapplication.models;

import java.util.List;

public class Assignments {
    private String assignment_id;
    private String assignment_name;
    private String assignment_description;
    private String assignment_due_date;
    private String assignment_uri;
    private String assignment_url;
    private String assignment_session_uri;
    private String assignment_tutor_path;
    private String assignment_student_path;
    private String assignment_upload_status;
    private List<String> assignment_submissions;

    public Assignments(String assignment_id, String assignment_name, String assignment_description, String assignment_due_date, String assignment_uri, String assignment_url, String assignment_session_uri, String assignment_tutor_path, String assignment_student_path, String assignment_upload_status, List<String> assignment_submissions) {
        this.assignment_id = assignment_id;
        this.assignment_name = assignment_name;
        this.assignment_description = assignment_description;
        this.assignment_due_date = assignment_due_date;
        this.assignment_uri = assignment_uri;
        this.assignment_url = assignment_url;
        this.assignment_session_uri = assignment_session_uri;
        this.assignment_tutor_path = assignment_tutor_path;
        this.assignment_student_path = assignment_student_path;
        this.assignment_upload_status = assignment_upload_status;
        this.assignment_submissions = assignment_submissions;
    }

    public Assignments() {
    }

    public String getAssignment_id() {
        return assignment_id;
    }

    public void setAssignment_id(String assignment_id) {
        this.assignment_id = assignment_id;
    }

    public String getAssignment_name() {
        return assignment_name;
    }

    public void setAssignment_name(String assignment_name) {
        this.assignment_name = assignment_name;
    }

    public String getAssignment_description() {
        return assignment_description;
    }

    public void setAssignment_description(String assignment_description) {
        this.assignment_description = assignment_description;
    }

    public String getAssignment_due_date() {
        return assignment_due_date;
    }

    public void setAssignment_due_date(String assignment_due_date) {
        this.assignment_due_date = assignment_due_date;
    }

    public String getAssignment_uri() {
        return assignment_uri;
    }

    public void setAssignment_uri(String assignment_uri) {
        this.assignment_uri = assignment_uri;
    }

    public String getAssignment_url() {
        return assignment_url;
    }

    public void setAssignment_url(String assignment_url) {
        this.assignment_url = assignment_url;
    }

    public String getAssignment_session_uri() {
        return assignment_session_uri;
    }

    public void setAssignment_session_uri(String assignment_session_uri) {
        this.assignment_session_uri = assignment_session_uri;
    }

    public String getAssignment_tutor_path() {
        return assignment_tutor_path;
    }

    public void setAssignment_tutor_path(String assignment_tutor_path) {
        this.assignment_tutor_path = assignment_tutor_path;
    }

    public String getAssignment_student_path() {
        return assignment_student_path;
    }

    public void setAssignment_student_path(String assignment_student_path) {
        this.assignment_student_path = assignment_student_path;
    }

    public String getAssignment_upload_status() {
        return assignment_upload_status;
    }

    public void setAssignment_upload_status(String assignment_upload_status) {
        this.assignment_upload_status = assignment_upload_status;
    }

    public List<String> getAssignment_submissions() {
        return assignment_submissions;
    }

    public void setAssignment_submissions(List<String> assignment_submissions) {
        this.assignment_submissions = assignment_submissions;
    }

    @Override
    public String toString() {
        return "Assignments{" +
                "assignmentId='" + assignment_id + '\'' +
                ", assignment_name='" + assignment_name + '\'' +
                ", assignment_description='" + assignment_description + '\'' +
                ", assignment_due_date='" + assignment_due_date + '\'' +
                ", assignemnt__uri='" + assignment_uri + '\'' +
                ", assignment_url='" + assignment_url + '\'' +
                ", assignement_session_uri='" + assignment_session_uri + '\'' +
                ", assignment_tutor_path='" + assignment_tutor_path + '\'' +
                ", assignment_student_path='" + assignment_student_path + '\'' +
                ", assignment_upload_status='" + assignment_upload_status + '\'' +
                ", assignement_submissions=" + assignment_submissions +
                '}';
    }
}
