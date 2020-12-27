package rs.elfak.mosis.greenforce.models;

import rs.elfak.mosis.greenforce.enums.VolunteerType;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
public class EventVolunteer {
    @Exclude
    private String id;
    private VolunteerType type;
    @Exclude
    public String getId() {
        return id;
    }
    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public VolunteerType getType() {
        return type;
    }

    public void setType(VolunteerType type) {
        this.type = type;
    }
}
