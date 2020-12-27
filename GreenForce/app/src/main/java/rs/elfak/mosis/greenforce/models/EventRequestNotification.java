package rs.elfak.mosis.greenforce.models;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.Exclude;

import java.time.LocalDateTime;

public class EventRequestNotification {
    boolean isSeen;
    String username;
    String dateTime;
    @Exclude
    String senderUid;
    @Exclude
    String eventID;

    public EventRequestNotification(){}
    @RequiresApi(api = Build.VERSION_CODES.O)
    public EventRequestNotification(String username, boolean isSeen) {

        this.isSeen=isSeen;
        this.username=username;
        LocalDateTime sentDateTime=LocalDateTime.now();
        dateTime=sentDateTime.toString();

    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public boolean getSeen() {
        return isSeen;
    }
    @Exclude
    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }
    @Exclude
    public String getSenderUid() {
        return senderUid;
    }
    @Exclude
    public void setEventID(String senderUid) {
        this.eventID = senderUid;
    }
    @Exclude
    public String getEvenID() {
        return eventID;
    }
}
