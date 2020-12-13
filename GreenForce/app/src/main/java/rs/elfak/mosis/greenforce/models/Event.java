package rs.elfak.mosis.greenforce.models;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.enums.EventStatus;
import rs.elfak.mosis.greenforce.enums.EventTypes;

public class Event
{
    private String eventDescription;
    private EventTypes eventTypes;
    private ArrayList<UserData> volunteers;
    private EventStatus eventStatus;
    private int eventPoints;

    @Exclude
    private ArrayList<Bitmap> eventPhotos;
    @Exclude
    private MyLatLong eventLocation;
    @Exclude
    private String eventID;

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

    public EventTypes getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(EventTypes eventTypes) {
        this.eventTypes = eventTypes;
    }

    public ArrayList<UserData> getVolunteers() {
        return volunteers;
    }

    public void setVolunteers(ArrayList<UserData> volunteers) {
        this.volunteers = volunteers;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public ArrayList<Bitmap> getEventPhotos() {
        return eventPhotos;
    }

    public void setEventPhotos(ArrayList<Bitmap> eventPhotos) {
        this.eventPhotos = eventPhotos;
    }

    public MyLatLong getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(MyLatLong eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }


}
