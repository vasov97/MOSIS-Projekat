package rs.elfak.mosis.greenforce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AddFriendsViaMapsActivity extends AppCompatActivity implements Serializable,IComponentInitializer, OnMapReadyCallback
{

    Spinner addFriendsSpinner;
    EditText radius;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    GoogleMap googleMap;

   // Marker myMarker;
    Location lastLocation;
    LatLng latLng;
    LatLngBounds myMapBoundary;

    String TAG="AddFriendsMapActivity";
    ArrayList<UserData> myFriends;
    ArrayList<UserData> allUsers;
    Map<String,Object> userMarkers;
    IGetAllUsersCallback clb;

    private ClusterManager clusterManager;
    private MyClusterManagerRenderer myClusterManagerRenderer;
    private ArrayList<ClusterMarker> clusterMarkers = new ArrayList<ClusterMarker>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_via_maps);
        initializeComponents();
        setUpActionBar(R.string.maps);
        myFriends=MyUserManager.getInstance().getMyFriends();

        clb=new GetAllUsersCallback();
        MyUserManager.getInstance().getAllUsers(clb);
       // allUsers=MyUserManager.getInstance().getAllUsers();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_maps_fragment_view_location);
        mapFragment.getMapAsync(this);
        loadAllUsers();

    }

    public class GetAllUsersCallback implements IGetAllUsersCallback {
        @Override
        public void onUsersReceived(ArrayList<UserData> users) {
            allUsers=users;
            progressDialog.dismiss();
            drawAllMarkers();//zbog testiranja tu

        }
    }

    LocationCallback locationCallback=new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations())
                lastLocation=location;

          //  drawMyMarker();
            //MyUserManager.getInstance().saveUserCoordinates(lastLocation.getLatitude(),lastLocation.getLongitude());

        }
    };
    @Override
    public void initializeComponents()
    {
         addFriendsSpinner=findViewById(R.id.spinner);
         radius=findViewById(R.id.radius);
         toolbar=findViewById(R.id.addFriendsViaMapToolbar);
         fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        userMarkers=new HashMap<String,Object>();
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

        this.googleMap = googleMap;

       this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
       locationRequest=new LocationRequest();
       locationRequest.setInterval(7000);
       locationRequest.setFastestInterval(10000);
       locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);



       //requestLocationDialog(); nista za sad
       //permissionAndRequestLocation(); //na svakih 10 sekunde vraca trenutnu lokaciju
        getLastKnownLocation(); // samo 1 vrati lokaciju
       // drawMyMarker();

    }

    private void permissionAndRequestLocation() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                googleMap.setMyLocationEnabled(true);
            }else{
                checkForLocationPermission();
            }
        }else{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
            googleMap.setMyLocationEnabled(true);
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
    private void loadAllUsers()
    {
        progressDialog=new ProgressDialog(AddFriendsViaMapsActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

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
                    setCameraView();
                    MyUserManager.getInstance().saveUserCoordinates(lastLocation.getLatitude(),lastLocation.getLongitude());
                    Log.d(TAG, "onComplete: latitude: " + location.getLatitude());
                    Log.d(TAG, "onComplete: longitude: " + location.getLongitude());
                   //MyUserManager.getInstance().saveUserCoordinates(lastLocation.getLatitude(),lastLocation.getLongitude());
                }
            }
        });

    }

    private void setCameraView(){
        double bottomBoundary=lastLocation.getLatitude()-.1;
        double leftBoundary=lastLocation.getLongitude()-.1;
        double topBoundary=lastLocation.getLatitude()+.1;
        double rightBoundary=lastLocation.getLongitude()+.1;

        myMapBoundary=new LatLngBounds(new LatLng(bottomBoundary,leftBoundary),new LatLng(topBoundary,rightBoundary));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(myMapBoundary,0));
    }

    private void drawAllMarkers(){
        if (googleMap != null) {
            if (clusterManager == null)
                clusterManager = new ClusterManager<ClusterMarker>(this, googleMap);

            if (myClusterManagerRenderer == null) {
                myClusterManagerRenderer = new MyClusterManagerRenderer(this, googleMap, clusterManager);
                clusterManager.setRenderer(myClusterManagerRenderer);
            }
        }
            for (UserData user : allUsers) {
                if (myFriends.contains(user))
                    drawFriendMarker(user);
                else
                    Log.d(TAG, "onComplete: User: " + user.getUserUUID() + "is NOT your friend");
            }
        clusterManager.cluster();
    }
    private void drawMyMarker()
    {
        String myUUid = MyUserManager.getInstance().getCurrentUserUid();
        Marker myMarker = (Marker)userMarkers.get(myUUid);

            if (myMarker != null)
                myMarker.remove();

           latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            myMarker = googleMap.addMarker(markerOptions);
            userMarkers.put(myUUid, myMarker);
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));

    }

    private void drawFriendMarker(UserData user){
                Log.d(TAG, "addMapMarkers: location: " + user.getMyLatLong().getLatitude()+","+user.getMyLatLong().getLongitude());
                try{
                    String snippet = "";
                    if(user.getUserUUID().equals(MyUserManager.getInstance().getCurrentUserUid()))
                        snippet="This is you";
                    else
                        snippet="Determine route to"+user.getUsername();

//                    try{
//                        avatar = Integer.parseInt(String.valueOf(user.getUserImage()));
//                    }catch (NumberFormatException e){
//                        Log.d(TAG, "addMapMarkers: no avatar for " + user.getUsername() + ", setting default.");
//                    }
                    ClusterMarker newClusterMarker = new ClusterMarker(snippet, user);
                    clusterManager.addItem(newClusterMarker);
                    clusterMarkers.add(newClusterMarker);

                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                }
    }

    private UserData getUserFromList(ArrayList<UserData> allUsers, String uuid) {
        for(UserData u: allUsers){
            if(u.getUserUUID().equals(uuid))
                return u;
        }
        return null;
    }


}