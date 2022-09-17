package com.sudhirtheindian4.whatsappnewchattingapp.Models;

public class Status {
    private  String imageUrl;
    private  long timeStamp;



    // this is empty consturucor for firevase user
    public Status() {
    }

    public Status(String imageUrl, long timeStamp) {
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
