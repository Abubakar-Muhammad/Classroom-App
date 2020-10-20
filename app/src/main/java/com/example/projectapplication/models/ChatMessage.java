package com.example.projectapplication.models;

public class ChatMessage {
    private String message_id;
    private String message_sender_id;
    private String message_timestamp;
    private String message;


    public ChatMessage(String message_id, String message_sender_id, String message_timestamp, String message) {
        this.message_id = message_id;
        this.message_sender_id = message_sender_id;
        this.message_timestamp = message_timestamp;
        this.message = message;
    }

    public ChatMessage() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_sender_id() {
        return message_sender_id;
    }

    public void setMessage_sender_id(String message_sender_id) {
        this.message_sender_id = message_sender_id;
    }

    public String getMessage_timestamp() {
        return message_timestamp;
    }

    public void setMessage_timestamp(String message_timestamp) {
        this.message_timestamp = message_timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "message_id='" + message_id + '\'' +
                ", message_sender_id='" + message_sender_id + '\'' +
                ", message_timestamp='" + message_timestamp + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
