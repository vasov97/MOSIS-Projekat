package rs.elfak.mosis.greenforce.models;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import rs.elfak.mosis.greenforce.models.UserData;

public class ClusterMarker implements ClusterItem
{
     private LatLng position;
     private String title;
     private String snippet;
     private UserData userData;

    public ClusterMarker(String snippet, UserData userData)
    {

        this.position=new LatLng(userData.getMyLatLong().getLatitude(),userData.getMyLatLong().getLongitude());
        this.title=userData.getUsername();
        this.snippet=snippet;
        this.userData=userData;
    }

    ClusterMarker(){}


    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}
