package com.rohit.chatever.Firebase.FirebaseNotification;

public class Data {
    private String Title;
    private String Message;
    private String Type;
    private String chatId;

    public Data(String title, String message, String type,String chatId) {
        this.Title = title;
        this.Message = message;
        this.Type=type;
        this.chatId=chatId;
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


    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        chatId = chatId;
    }
    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

}
