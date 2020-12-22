package rs.elfak.mosis.greenforce.interfaces;

import rs.elfak.mosis.greenforce.models.MyEvent;

public interface IApplyEventFilters {
    void disableMarker(MyEvent e);
    void enableMarker(MyEvent e);
    boolean checkIfPostedBy(String username,String eventID);
    void appliedFilters();
    void clearedFilters();
}
