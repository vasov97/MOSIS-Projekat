package rs.elfak.mosis.greenforce.interfaces;

import android.graphics.Bitmap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.mosis.greenforce.enums.VolunteerType;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.LikeDislike;
import rs.elfak.mosis.greenforce.models.MyEvent;

public interface IGetEventsCallback extends IGetDataCallback{
    void onEventsReceived(ArrayList<MyEvent> events);
    void onSingleEventReceived(MyEvent event);
    void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole);
    void onEventImagesReceived(ArrayList<Bitmap> images);
    void onEventVolunteersReceived(ArrayList<EventVolunteer> volunteers);
    void onLikeDislikeReceived(ArrayList<LikeDislike> list);
}
