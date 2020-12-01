package rs.elfak.mosis.greenforce;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MyLatLong {
    double latitude;
    double longitude;
    @Exclude
    String uid;

    public MyLatLong(double lat, double lon) {
        this.latitude=lat;
        this.longitude=lon;
    }

    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}
    public void setLatitude(double latitude){this.latitude=latitude;}
    public void setLongitude(double longitude){this.longitude=longitude;}

    public void setUid(String uid){this.uid=uid;}
    public String getUid(){return uid;}
}
