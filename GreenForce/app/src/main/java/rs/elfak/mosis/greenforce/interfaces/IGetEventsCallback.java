package rs.elfak.mosis.greenforce.interfaces;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.models.MyEvent;

public interface IGetEventsCallback extends IGetDataCallback{
    void onEventsReceived(ArrayList<MyEvent> events);
}
