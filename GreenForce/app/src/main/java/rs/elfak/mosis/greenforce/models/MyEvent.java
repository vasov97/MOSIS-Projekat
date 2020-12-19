package rs.elfak.mosis.greenforce.models;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.enums.EventStatus;
import rs.elfak.mosis.greenforce.enums.EventTypes;

public class MyEvent
{
    private String eventDescription;
    private String eventAddress;
    private ArrayList<EventTypes> eventTypes;
    private EventStatus eventStatus;
    private int eventPoints;
    private String createdByID;

    @Exclude
    private ArrayList<UserData> volunteers;
    @Exclude
    private ArrayList<Bitmap> eventPhotos;
    @Exclude
    private MyLatLong eventLocation;
    @Exclude
    private String eventID;

    public String getCreatedByID() {
        return createdByID;
    }

    public void setCreatedByID(String createdByID) {
        this.createdByID = createdByID;
    }


    public String getEventAddress() { return eventAddress; }

    public void setEventAddress(String eventAddress) { this.eventAddress = eventAddress; }

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

    @Exclude public MyLatLong getEventLocation() {
        return eventLocation;
    }

    @Exclude public void setEventLocation(MyLatLong eventLocation) { this.eventLocation = eventLocation; }

    @Exclude public String getEventID() {
        return eventID;
    }

    @Exclude public void setEventID(String eventID) {
        this.eventID = eventID;
    }


}
