package rs.elfak.mosis.greenforce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class AddFriendsViaMapsActivity extends AppCompatActivity implements Serializable,IComponentInitializer, OnMapReadyCallback
{

    Spinner addFriendsSpinner;
    EditText radius;
    Toolbar toolbar;
    FusedLocationProviderClient fusedLocationProviderClient;
    GoogleMap map;
    Marker myMarker;
    LocationRequest locationRequest;
    Location lastLocation;
    LatLng latLng;
    String TAG="AddFriendsMapActivity";
    ArrayList<UserData> myFriends;
    ArrayList<UserData> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_via_maps);
        initializeComponents();
        setUpActionBar(R.string.maps);
        /*Bundle bundle=getIntent().getBundleExtra("Bundle");
        myFriends=(ArrayList<UserData>)bundle.getSerializable("MyFriends");*/
        myFriends=MyUserManager.getInstance().getMyFriends();

        /*for(UserData user:myFriends)
            Log.d("tag","#######________######"+user.surname);*/

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_maps_fragment_view_location);
        mapFragment.getMapAsync(this);

    }

    public class GetAllUsersCallback implements IGetAllUsersCallback
    {

        @Override
        public void onUsersReceived(ArrayList<UserData> users)
        {
            allUsers=users;

        }
    }

    LocationCallback locationCallback=new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations())
                lastLocation=location;

            drawMyMarker();
            MyUserManager.getInstance().saveUserCoordinates(lastLocation.getLatitude(),lastLocation.getLongitude());

        }
    };

    private void drawMyMarker() {
        if(myMarker!=null)
            myMarker.remove();
        latLng=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        myMarker=map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));
    }

    @Override
    public void initializeComponents()
    {
         addFriendsSpinner=findViewById(R.id.spinner);
         radius=findViewById(R.id.radius);
         toolbar=findViewById(R.id.addFriendsViaMapToolbar);
         fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_add_friends_map,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_friends_map,menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {

        map = googleMap;

       map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
       locationRequest=new LocationRequest();
       locationRequest.setInterval(7000);
       locationRequest.setFastestInterval(10000);
       locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);



       //requestLocationDialog(); nista za sad
       //permissionAndRequestLocation(); //na svakih 10 sekunde vraca trenutnu lokaciju
        getLastKnownLocation(); // samo 1 vrati lokaciju

    }

    private void permissionAndRequestLocation() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(true);
            }else{
                checkForLocationPermission();
            }
        }else{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
            map.setMyLocationEnabled(true);
        }
    }

    private void checkForLocationPermission() {
        if(!checkPermissions()){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }
    }

    private boolean checkPermissions() {
        if(Build.VERSION.SDK_INT>=23){
            int result=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
            if(result==PackageManager.PERMISSION_GRANTED){
                return true;
            }else
                return false;
        }
        return true;
    }

    private void requestLocationDialog() {
        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder();
        builder.addAllLocationRequests(Collections.singleton(locationRequest));
        builder.setAlwaysShow(true);
        LocationSettingsRequest locationSettingsRequest=builder.build();
        SettingsClient client=LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task=client.checkLocationSettings(locationSettingsRequest);
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void setUpActionBar(int rid)
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
    }
   //ne treba??
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    lastLocation=location;
                    drawMyMarker();
                    MyUserManager.getInstance().saveUserCoordinates(lastLocation.getLatitude(),lastLocation.getLongitude());
                    Log.d(TAG, "onComplete: latitude: " + location.getLatitude());
                    Log.d(TAG, "onComplete: longitude: " + location.getLongitude());
                   // MyUserManager.getInstance().saveUserCoordinates(lastLocation.getLatitude(),lastLocation.getLongitude());
                }
            }
        });

    }
}