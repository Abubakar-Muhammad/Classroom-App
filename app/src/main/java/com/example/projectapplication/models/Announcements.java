package com.example.projectapplication.models;

public class Announcements {
    private String announcement_title;
    private String announcement_description;
    private String announcement_time;

    public boolean isNewDate() {
        return isNewDate;
    }

    public void setNewDate(boolean newDate) {
        isNewDate = newDate;
    }

    private boolean isNewDate;

    public Announcements(String announcement_title, String announcement_description, String announcement_time) {
        this.announcement_title = announcement_title;
        this.announcement_description = announcement_description;
        this.announcement_time = announcement_time;
    }
    public Announcements() {

    }

    public String getAnnouncement_title() {
        return announcement_title;
    }

    public void setAnnouncement_title(String announcement_title) {
        this.announcement_title = announcement_title;
    }

    public String getAnnouncement_description() {
        return announcement_description;
    }

    public void setAnnouncement_description(String announcement_description) {
        this.announcement_description = announcement_description;
    }

    public String getAnnouncement_time() {
        return announcement_time;
    }

    public void setAnnouncement_time(String announcement_time) {
        this.announcement_time = announcement_time;
    }

    @Override
    public String toString() {
        return "Announcements{" +
                "announcement_title='" + announcement_title + '\'' +
                ", announcement_description='" + announcement_description + '\'' +
                ", announcement_time='" + announcement_time + '\'' +
                '}';
    }
}
