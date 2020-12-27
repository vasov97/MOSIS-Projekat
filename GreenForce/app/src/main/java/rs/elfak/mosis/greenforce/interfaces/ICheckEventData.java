package rs.elfak.mosis.greenforce.interfaces;

import rs.elfak.mosis.greenforce.enums.VolunteerType;

public interface ICheckEventData {
    void onCheckIfVolunteer(boolean volunteer, VolunteerType type);
    void onCheckIfRequestSent(boolean sent);
}
