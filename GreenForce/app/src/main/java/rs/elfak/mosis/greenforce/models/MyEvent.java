package rs.elfak.mosis.greenforce.models;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.enums.EventStatus;
import rs.elfak.mosis.greenforce.enums.EventTypes;
import rs.elfak.mosis.greenforce.enums.VolunteerType;

public class MyEvent
{
    private String eventDescription;
    private ArrayList<EventTypes> eventTypes;
    private EventStatus eventStatus;
    private int eventPoints;
    private String createdByID;

    public int getImagesBeforeCount() {
        return imagesBeforeCount;
    }

    public void setImagesBeforeCount(int imagesBeforeCount) {
        this.imagesBeforeCount = imagesBeforeCount;
    }

    private int imagesBeforeCount;
    //String dateTime;
    String date;
    String time;





    @Exclude
    private ArrayList<UserData> volunteers;
    @Exclude
    private ArrayList<Bitmap> eventPhotos;
    private MyLatLong eventLocation;
    @Exclude
    private String eventID;

    public String getDate() {
        return date;
    }

    public void setDate(String dateTime) {
        this.date = dateTime;
    }

    public String getTime(){return time;}

    public void setTime(String time){this.time=time;}

    public String getCreatedByID() {
        return createdByID;
    }

    public void setCreatedByID(String createdByID) {
        this.createdByID = createdByID;
    }


    public int getEventPoints() {
        return eventPoints;
    }

    public void setEventPoints(int eventPoints) {
        this.eventPoints = eventPoints;
    }

    public String getDescription() {
        return eventDescription;
    }

    public void setDescription(String description) {
        this.eventDescription = description;
    }

    public ArrayList<EventTypes> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(ArrayList<EventTypes> eventTypes) {
        this.eventTypes = eventTypes;
    }

    @Exclude public ArrayList<UserData> getVolunteers() {
        return volunteers;
    }

    @Exclude  public void setVolunteers(ArrayList<UserData> volunteers) { this.volunteers = volunteers; }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    @Exclude public ArrayList<Bitmap> getEventPhotos() {
        return eventPhotos;
    }

    @Exclude public void setEventPhotos(ArrayList<Bitmap> eventPhotos) { this.eventPhotos = eventPhotos; }

    public MyLatLong getEventLocation() {
        return eventLocation;
    }

   public void setEventLocation(MyLatLong eventLocation) { this.eventLocation = eventLocation; }

    @Exclude public String getEventID() {
        return eventID;
    }

    @Exclude public void setEventID(String eventID) {
        this.eventID = eventID;
    }



    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof UserData)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        MyEvent c = (MyEvent) o;

        // Compare the data members and return accordingly
        return this.eventID.equals(c.getEventID());
    }

}
