package rs.elfak.mosis.greenforce.models;

import android.os.Build;
import android.text.format.DateFormat;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;


import java.time.LocalDateTime;
import java.util.Date;

@IgnoreExtraProperties
public class FriendsRequestNotification
{
    boolean isSeen;
    String username;
    String dateTime;
    @Exclude
    String senderUid;

    public FriendsRequestNotification(){}
    @RequiresApi(api = Build.VERSION_CODES.O)
    public FriendsRequestNotification(String username, boolean isSeen) {

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
}
