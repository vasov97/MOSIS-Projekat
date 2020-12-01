package rs.elfak.mosis.greenforce;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MyLatLong
{
    double latitude;
    double longitude;
    @Exclude
    String uid;

    public MyLatLong(){}
    public MyLatLong(double latitude, double longitude) {

        this.latitude=latitude;
        this.longitude=longitude;
    }

    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}
    public void setLatitude(double latitude){this.latitude=latitude;}
    public void setLongitude(double longitude){this.longitude=longitude;}
    @Exclude
    public void setUid(String uid){this.uid=uid;}
    @Exclude
    public String getUid(){return uid;}
}
