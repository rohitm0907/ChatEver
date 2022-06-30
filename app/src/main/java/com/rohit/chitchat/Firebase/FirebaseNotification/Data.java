package com.rohit.chitchat.Firebase.FirebaseNotification;

public class Data {
    private String Title;
    private String Message;
    private String Type;

    public Data(String title, String message, String type) {
        this.Title = title;
        this.Message = message;
        this.Type=type;
    }

    public Data() {
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

}
