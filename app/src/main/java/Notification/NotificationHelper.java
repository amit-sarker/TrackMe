package Notification;

/**
 * Created by amit on 10/25/17.
 */

public class NotificationHelper {


    public String Title, Body, Image, type;

    //Constructors
    public NotificationHelper(String Title, String Body, String Image, String type) {
        this.Title = Title;
        this.Body = Body;
        this.Image = Image;
        this.type = type;
    }

    public NotificationHelper() {
    }

    ////////////////

    //Getters and Setters//
    public String getNotificationBody() {
        return Body;
    }

    public String getNotificationTitle() {
        return Title;
    }

    public String getNotificationType() {
        return type;
    }

    //////////////////////

}
