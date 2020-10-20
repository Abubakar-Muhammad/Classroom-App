package com.example.projectapplication.models;

public class LectureNotes {
    String lecture_note_id;
    String lecture_note_name;
    String lecture_note_description;
    String lecture_note_url;
    String lecture_note_uri;
    String lecture_note_session_uri;
    String lecture_note_upload_status;
    String lecture_note_tutor_file_path;
    String lecture_note_student_file_path;
    String lecture_note_upload_time;

    public LectureNotes(String lecture_note_id, String lecture_note_name, String lecture_note_description, String lecture_note_url, String lecture_note_uri, String lecture_note_session_uri, String lecture_note_upload_status, String lecture_note_tutor_file_path, String lecture_note_student_file_path, String lecture_note_upload_time) {
        this.lecture_note_id = lecture_note_id;
        this.lecture_note_name = lecture_note_name;
        this.lecture_note_description = lecture_note_description;
        this.lecture_note_url = lecture_note_url;
        this.lecture_note_uri = lecture_note_uri;
        this.lecture_note_session_uri = lecture_note_session_uri;
        this.lecture_note_upload_status = lecture_note_upload_status;
        this.lecture_note_tutor_file_path = lecture_note_tutor_file_path;
        this.lecture_note_student_file_path = lecture_note_student_file_path;
        this.lecture_note_upload_time = lecture_note_upload_time;
    }

    public LectureNotes() {
    }

    public String getLecture_note_uri() {
        return lecture_note_uri;
    }

    public void setLecture_note_uri(String lecture_note_uri) {
        this.lecture_note_uri = lecture_note_uri;
    }

    public String getLecture_note_session_uri() {
        return lecture_note_session_uri;
    }

    public void setLecture_note_session_uri(String lecture_note_session_uri) {
        this.lecture_note_session_uri = lecture_note_session_uri;
    }

    public String getLecture_note_upload_status() {
        return lecture_note_upload_status;
    }

    public void setLecture_note_upload_status(String lecture_note_upload_status) {
        this.lecture_note_upload_status = lecture_note_upload_status;
    }

    public String getLecture_note_id() {
        return lecture_note_id;
    }

    public void setLecture_note_id(String lecture_note_id) {
        this.lecture_note_id = lecture_note_id;
    }

    public String getLecture_note_name() {
        return lecture_note_name;
    }

    public void setLecture_note_name(String lecture_note_name) {
        this.lecture_note_name = lecture_note_name;
    }

    public String getLecture_note_description() {
        return lecture_note_description;
    }

    public void setLecture_note_description(String lecture_note_description) {
        this.lecture_note_description = lecture_note_description;
    }

    public String getLecture_note_url() {
        return lecture_note_url;
    }

    public void setLecture_note_url(String lecture_note_url) {
        this.lecture_note_url = lecture_note_url;
    }

    public String getLecture_note_tutor_file_path() {
        return lecture_note_tutor_file_path;
    }

    public void setLecture_note_tutor_file_path(String lecture_note_tutor_file_path) {
        this.lecture_note_tutor_file_path = lecture_note_tutor_file_path;
    }

    public String getLecture_note_student_file_path() {
        return lecture_note_student_file_path;
    }

    public void setLecture_note_student_file_path(String lecture_note_student_file_path) {
        this.lecture_note_student_file_path = lecture_note_student_file_path;
    }

    public String getLecture_note_upload_time() {
        return lecture_note_upload_time;
    }

    public void setLecture_note_upload_time(String lecture_note_upload_time) {
        this.lecture_note_upload_time = lecture_note_upload_time;
    }

    @Override
    public String toString() {
        return "LectureNotes{" +
                "lecture_note_id='" + lecture_note_id + '\'' +
                ", lecture_note_name='" + lecture_note_name + '\'' +
                ", lecture_note_description='" + lecture_note_description + '\'' +
                ", lecture_note_url='" + lecture_note_url + '\'' +
                ", lecture_note_uri='" + lecture_note_uri + '\'' +
                ", lecture_note_session_uri='" + lecture_note_session_uri + '\'' +
                ", lecture_note_upload_status='" + lecture_note_upload_status + '\'' +
                ", lecture_note_tutor_file_path='" + lecture_note_tutor_file_path + '\'' +
                ", lecture_note_student_file_path='" + lecture_note_student_file_path + '\'' +
                ", lecture_note_upload_time='" + lecture_note_upload_time + '\'' +
                '}';
    }
}
