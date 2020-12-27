package rs.elfak.mosis.greenforce.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.mosis.greenforce.enums.VolunteerType;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.MyEvent;

public interface IGetEventsCallback extends IGetDataCallback{
    void onEventsReceived(ArrayList<MyEvent> events);
    void onSingleEventReceived(MyEvent event);
    void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole);
}
