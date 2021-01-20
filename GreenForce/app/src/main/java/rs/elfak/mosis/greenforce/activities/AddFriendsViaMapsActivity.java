package rs.elfak.mosis.greenforce.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rs.elfak.mosis.greenforce.dialogs.DisplayUserInformationOnMapDialog;
import rs.elfak.mosis.greenforce.dialogs.FindUserOnMapDialog;
import rs.elfak.mosis.greenforce.enums.DataRetriveAction;
import rs.elfak.mosis.greenforce.interfaces.IFindUserOnMapDialogListener;
import rs.elfak.mosis.greenforce.interfaces.IGetFriendsCallback;
import rs.elfak.mosis.greenforce.interfaces.IOnClickNewIntent;
import rs.elfak.mosis.greenforce.interfaces.IRemoveUserFromFriends;
import rs.elfak.mosis.greenforce.models.ClusterMarker;
import rs.elfak.mosis.greenforce.managers.MyClusterManagerRenderer;
import rs.elfak.mosis.greenforce.models.MyLatLong;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.UserData;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetUsersCallback;

public class AddFriendsViaMapsActivity extends AppCompatActivity implements Serializable, IComponentInitializer,
        OnMapReadyCallback, AdapterView.OnItemSelectedListener, IFindUserOnMapDialogListener, GoogleMap.OnMarkerClickListener, IRemoveUserFromFriends
{
    String zoom;
    MyLatLong zoomLatLong;
    Spinner mySpinner;
    EditText radius;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    DisplayUserInformationOnMapDialog myDialog;
    FusedLocationProviderClient fusedLocationProviderClient;
    GoogleMap googleMap;
    Circle myCircle;
    LatLngBounds myMapBoundary;
    String TAG="AddFriendsMapActivity";
    ArrayList<UserData> myFriends;
    ArrayList<UserData> allUsers;
    Map<String,Object> userMarkers;
    Map<String,MyLatLong> tmpChildAddedLatLng;
    IGetUsersCallback clb;
    IGetFriendsCallback friendsCallback;
    private ClusterManager clusterManager;
    private MyClusterManagerRenderer myClusterManagerRenderer;
    private ArrayList<ClusterMarker> clusterMarkers = new ArrayList<ClusterMarker>();

        ChildEventListener locationsChildEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String uid=dataSnapshot.getKey();
                if(tmpChildAddedLatLng==null)
                    tmpChildAddedLatLng=new HashMap<String,MyLatLong>();
                if(allUsers!=null){
                    if(!checkIfUserExists(uid,allUsers)){
                        tmpChildAddedLatLng.put(uid,dataSnapshot.getValue(MyLatLong.class));
                        MyUserManager.getInstance().getSingleUser(DataRetriveAction.GET_USER,uid,clb);
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    String uuid=dataSnapshot.getKey();
                    MyLatLong myLatLong=dataSnapshot.getValue(MyLatLong.class);
                    if(uuid.equals(MyUserManager.getInstance().getCurrentUserUid())){
                        MyUserManager.getInstance().getUser().setMyLatLong(myLatLong);
                        drawMyMarker();
                    }else
                        updateMarker(uuid,myLatLong);
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        ChildEventListener friendsChildEventListener= new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(allUsers!=null && myFriends!=null) {
                    UserData user = getUserFromList(allUsers, dataSnapshot.getKey());
                    if (user != null) {
                        if (!myFriends.contains(user)) {
                            if (myDialog != null && myDialog.getUser().getUserUUID().equals(user.getUserUUID()))
                                myDialog.dismissDialog();
                            removeUserMarker(user);
                            drawFriendMarker(user);
                            setUpClusterManager();
                            clusterManager.cluster();
                            myFriends.add(user);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    String key=dataSnapshot.getKey();
                    UserData user=getUserFromList(allUsers,key);
                    if(user!=null) {
                        myFriends.remove(user);
                        if (userMarkers.get(user.getUserUUID()) instanceof ClusterMarker) {
                            ClusterMarker marker = (ClusterMarker) userMarkers.get(user.getUserUUID());
                            if (marker != null) {
                                myClusterManagerRenderer.removeClusterMarker(marker);
                                userMarkers.remove(key);
                            }

                            drawUserMarker(user);
                        }
                    }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
       TextWatcher myTextWatcher=new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) { }

           @Override
           public void afterTextChanged(Editable s) {
               String text=radius.getText().toString();
               removeMyCircle();
               drawMyCircle(text);
           }
       };
    public class GetFriendsCallback implements IGetFriendsCallback {
        @Override
        public void onFriendsReceived(ArrayList<UserData> friends) {
            myFriends=friends;
            MyUserManager.getInstance().getAllUsers(clb,DataRetriveAction.GET_USERS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_via_maps);
        initializeComponents();
        setUpActionBar(R.string.maps);
        zoom=getIntent().getStringExtra("Zoom");
        if(zoom!=null){
            double lat=Double.parseDouble(getIntent().getStringExtra("Lat"));
            double lon=Double.parseDouble(getIntent().getStringExtra("Lon"));
            zoomLatLong=new MyLatLong(lat,lon);
        }


        clb=new GetUsersCallback();
        friendsCallback=new GetFriendsCallback();
        loadAllUsers();
        MyUserManager.getInstance().getFriends(MyUserManager.getInstance().getCurrentUserUid(),friendsCallback,DataRetriveAction.GET_FRIENDS,true);
//        if(MyUserManager.getInstance().getMyFriends()==null){
//            MyUserManager.getInstance().getFriends(MyUserManager.getInstance().getCurrentUserUid(),friendsCallback,DataRetriveAction.GET_FRIENDS,true);
//        }else{
//            myFriends= MyUserManager.getInstance().getMyFriends();
//            MyUserManager.getInstance().getAllUsers(clb,DataRetriveAction.GET_USERS);
//
//        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_maps_fragment_view_location);
        mapFragment.getMapAsync(this);
        MyUserManager.getInstance().getDatabaseCoordinatesReference().addChildEventListener(locationsChildEventListener);
        MyUserManager.getInstance().getDatabaseFriendsReference().child(MyUserManager.getInstance().getCurrentUserUid()).addChildEventListener(friendsChildEventListener);
        radius.addTextChangedListener(myTextWatcher);

    }


    @Override
    public void onUserRemoved(UserData user)
    {
        if(myDialog!=null)
            myDialog.dismissDialog();
        ClusterMarker clusterMarker=(ClusterMarker)userMarkers.get(user.getUserUUID());
        userMarkers.remove(user.getUserUUID());
        myFriends.remove(user);
        myClusterManagerRenderer.removeClusterMarker(clusterMarker);
        MyUserManager.getInstance().removeFriend(user.getUserUUID());
        drawUserMarker(user);
    }


    public class GetUsersCallback implements IGetUsersCallback {
        @Override
        public void onUsersReceived(ArrayList<UserData> users) {
            checkForInvalidLocations(users);
            progressDialog.dismiss();
        }
        @Override
        public void onUserReceived(UserData user) {
            user.setMyLatLong(tmpChildAddedLatLng.get(user.getUserUUID()));
            tmpChildAddedLatLng.remove(user.getUserUUID());
            if(myFriends.contains(user)){
                if(mySpinner.getSelectedItem().toString().equals(getString(R.string.all_users))){
                    setUpClusterManager();
                    allUsers.add(user);
                    drawFriendMarker(user);
                    clusterManager.cluster();
                }else{
                    allUsers.add(user);
                }
            }else{
                if(mySpinner.getSelectedItem().toString().equals(getString(R.string.all_users))){
                    allUsers.add(user);
                    drawUserMarker(user);
                }else{
                    allUsers.add(user);
                }
            }
        }}


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(mySpinner.getSelectedItem().toString().equals(getString(R.string.all_users))) {
            drawAllMarkers();
            radius.setEnabled(true);
        }
        else if(mySpinner.getSelectedItem().toString().equals(getString(R.string.me))) {
            removeAllMarkers();
            removeMyCircle();
            radius.setEnabled(false);
            radius.setText("");
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
    @Override
    public void onUsernameReceived(String username) {
        if(username!="" && allUsers!=null){
            if(checkIfUserExists(username,allUsers)){
                for (UserData user : allUsers) {
                    if(!user.getUsername().equals(username)){
                        if (myFriends.contains(user)){
                            ClusterMarker clusterMarker=(ClusterMarker)userMarkers.get(user.getUserUUID());
                            if(clusterMarker!=null)
                                myClusterManagerRenderer.setMarkerVisibility(clusterMarker,false);
                        }
                        else{
                            Marker marker=(Marker)userMarkers.get(user.getUserUUID());
                            if(marker!=null)
                                marker.setVisible(false);
                        }
                    }else{//zbog ponovnog trazenja
                        if (myFriends.contains(user)){
                            ClusterMarker clusterMarker=(ClusterMarker)userMarkers.get(user.getUserUUID());
                            if(clusterMarker!=null)
                                myClusterManagerRenderer.setMarkerVisibility(clusterMarker,true);
                        }
                        else{
                            Marker marker=(Marker)userMarkers.get(user.getUserUUID());
                            if(marker!=null)
                                marker.setVisible(true);
                        }
                    }
                }
            }else{
                Toast.makeText(this, "User "+username+" was not found.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void initializeComponents() {
        mySpinner=findViewById(R.id.spinner);
        radius=findViewById(R.id.radius);
        toolbar=findViewById(R.id.addFriendsViaMapToolbar);

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        userMarkers=new HashMap<String,Object>();

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,R.array.spinner_data,R.layout.support_simple_spinner_dropdown_item);
        mySpinner.setAdapter(spinnerAdapter);
        mySpinner.setOnItemSelectedListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_friends_map,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_friends_map,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.search_user_on_map) {
            openSearchUserDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        getLastKnownLocation();// samo 1 vrati lokaciju
        googleMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(final Marker marker)
    {
        UserData user = (UserData)marker.getTag();
        if(!user.equals(MyUserManager.getInstance().getUser())){
            if(myFriends.contains(user))
                showUser(user,true);
            else
                showUser(user,false);
        }
        return false;
    }

    private void removeAllMarkers() {
        if(allUsers!=null) {
            for (UserData user : allUsers) {
                if (!myFriends.contains(user))
                   removeUserMarker(user);
          }
        }

        if(clusterManager!=null) {
            clusterManager.clearItems();
            clusterManager.cluster();
        }
    }

    private void removeUserMarker(UserData user) {
        if(userMarkers.get(user.getUserUUID())!=null){
            if(userMarkers.get(user.getUserUUID()) instanceof Marker) {
                Marker marker = (Marker)userMarkers.get(user.getUserUUID());
                marker.remove();
                userMarkers.remove(user.getUserUUID());
            }
        }
    }

    private boolean checkIfUserExists(String username, ArrayList<UserData> list) {
        boolean exists=false;
        for(UserData user : list){
            if(user.getUsername().equals(username)){
                exists=true;
                break;
            }
        }
        return exists;
    }


    private void loadAllUsers() {
        progressDialog=new ProgressDialog(AddFriendsViaMapsActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void setUpActionBar(int rid) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

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
                    MyLatLong myLatLong=new MyLatLong(location.getLatitude(),location.getLongitude());
                    //MyUserManager.getInstance().getUser().setMyLatLong(myLatLong);

                    //setCameraView(myLatLong);
                    MyUserManager.getInstance().saveUserCoordinates(myLatLong);
                    drawMyMarker();
                    Log.d(TAG, "onComplete: latitude: " + location.getLatitude());
                    Log.d(TAG, "onComplete: longitude: " + location.getLongitude());
                }
            }
        });
    }

    private void setCameraView(MyLatLong location){

        double bottomBoundary=location.getLatitude()-.1;
        double leftBoundary=location.getLongitude()-.1;
        double topBoundary=location.getLatitude()+.1;
        double rightBoundary=location.getLongitude()+.1;

        myMapBoundary=new LatLngBounds(new LatLng(bottomBoundary,leftBoundary),new LatLng(topBoundary,rightBoundary));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(myMapBoundary,0));
    }
    private void setUpClusterManager(){
        if (googleMap != null) {
            if (clusterManager == null)
                clusterManager = new ClusterManager<ClusterMarker>(this, googleMap);

            if (myClusterManagerRenderer == null) {
                myClusterManagerRenderer = new MyClusterManagerRenderer(this, googleMap, clusterManager);
                clusterManager.setRenderer(myClusterManagerRenderer);
            }


        }
    }

    private void drawAllMarkers(){
        setUpClusterManager();
        if(allUsers!=null) {
            for (UserData user : allUsers) {
                if (myFriends.contains(user))
                    drawFriendMarker(user);
                else
                    drawUserMarker(user);
            }
            clusterManager.cluster();
        }
    }
    private void drawMyMarker() {
        UserData currentUser = MyUserManager.getInstance().getUser();
        if(userMarkers.get(currentUser.getUserUUID()) instanceof Marker || userMarkers.size()==0) {
            Marker myMarker = (Marker) userMarkers.get(currentUser.getUserUUID());

            if (myMarker != null)
                myMarker.remove();

            LatLng latLng = new LatLng(currentUser.getMyLatLong().getLatitude(), currentUser.getMyLatLong().getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            myMarker = googleMap.addMarker(markerOptions);
            myMarker.setTag(currentUser);
            userMarkers.put(currentUser.getUserUUID(), myMarker);
            //removeAllMarkers();

        }
    }

    private void drawFriendMarker(UserData user){
       // if(userMarkers.get(user.getUserUUID())==null) {
            Log.d(TAG, "addMapMarkers: location: " + user.getMyLatLong().getLatitude() + "," + user.getMyLatLong().getLongitude());
            try {
                String snippet = "";
//                if (user.getUserUUID().equals(MyUserManager.getInstance().getCurrentUserUid()))
//                    snippet = "This is you";
//                else
//                    snippet = "Determine route to " + user.getUsername();

//                    try{
//                        avatar = Integer.parseInt(String.valueOf(user.getUserImage()));
//                    }catch (NumberFormatException e){
//                        Log.d(TAG, "addMapMarkers: no avatar for " + user.getUsername() + ", setting default.");
//                    }
                ClusterMarker newClusterMarker = new ClusterMarker(snippet, user);
                userMarkers.put(user.getUserUUID(), newClusterMarker);
                clusterManager.addItem(newClusterMarker);
                clusterMarkers.add(newClusterMarker);
                //myClusterManagerRenderer.setTag(newClusterMarker);

            } catch (NullPointerException e) {
                Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
            }
      //  }
    }
    private void drawUserMarker(UserData user){
        Marker userMarker=(Marker)userMarkers.get(user.getUserUUID());
        LatLng userLatLng = new LatLng(user.getMyLatLong().getLatitude(),user.getMyLatLong().getLongitude());
        if(userMarker!=null)
            userMarker.setPosition(userLatLng);
        else{
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(userLatLng);
            markerOptions.title("User Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            userMarker = googleMap.addMarker(markerOptions);
            userMarker.setTag(user);
            userMarkers.put(user.getUserUUID(), userMarker);
        }
    }
    private UserData getUserFromList(ArrayList<UserData> allUsers, String uuid) {
        if(allUsers!=null) {
            for (UserData u : allUsers) {
                if (u.getUserUUID().equals(uuid))
                    return u;
            }
        }
        return null;
    }
    private void updateMarker(String uuid, MyLatLong myLatLong) {
        UserData user=getUserFromList(myFriends,uuid);
        if(user!=null){
            user.setMyLatLong(myLatLong);
            updateFriendMarker(user);
        }else{
            user=getUserFromList(allUsers,uuid);
            if(user!=null){
                user.setMyLatLong(myLatLong);
                drawUserMarker(user);
            }
        }
    }
    private void updateFriendMarker(UserData user){
        try{
            ClusterMarker clusterMarker=(ClusterMarker) userMarkers.get(user.getUserUUID());
            LatLng userLatLng = new LatLng(user.getMyLatLong().getLatitude(),user.getMyLatLong().getLongitude());
            if(clusterMarker!=null){
                myClusterManagerRenderer.setUpdateMarker(clusterMarker,userLatLng);
            }
        }catch (IllegalStateException e){
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }

    }
    private void checkForInvalidLocations(ArrayList<UserData> userList){
        if(allUsers==null)
            allUsers=new ArrayList<UserData>();
        else{
            allUsers.clear();
        }
        for(UserData user: userList){
            if(user.getMyLatLong()!=null){
                allUsers.add(user);
            }
        }
        if(mySpinner!=null && zoom!=null){
            zoom=null;
            mySpinner.setSelection(1);
            drawAllMarkers();
            radius.setEnabled(true);
            setCameraView(zoomLatLong);
        }
    }
    private void drawMyCircle(String text){
        if(!text.equals("")){
            double value=Double.parseDouble(radius.getText().toString());
            value*=1000;//unosimo kilometre a radius() uzima metre
            MyLatLong myLatLong=MyUserManager.getInstance().getUser().getMyLatLong();
            LatLng latLng = new LatLng(myLatLong.getLatitude(),myLatLong.getLongitude());

            CircleOptions myCircleOptions = new CircleOptions().center(latLng).radius(value);

            myCircle = googleMap.addCircle(myCircleOptions);
            myCircle.setStrokeColor(Color.BLUE);
            toggleOutOfRangeMarkers(value);
        }else{
            removeMyCircle();
            double earthsRadius=6371*1000;
            toggleOutOfRangeMarkers(earthsRadius);
        }
    }

    private void toggleOutOfRangeMarkers(double value) {
        if(allUsers!=null){
            for (UserData user : allUsers) {
                MyLatLong start=MyUserManager.getInstance().getUser().getMyLatLong();
                MyLatLong end=user.getMyLatLong();
                if (myFriends.contains(user)){
                    ClusterMarker clusterMarker=(ClusterMarker)userMarkers.get(user.getUserUUID());
                    if(clusterMarker!=null){
                        if(value<calculateDistance(start,end))
                            myClusterManagerRenderer.setMarkerVisibility(clusterMarker,false);
                        else
                            myClusterManagerRenderer.setMarkerVisibility(clusterMarker,true);
                    }
                }
                else{
                    Marker marker=(Marker)userMarkers.get(user.getUserUUID());
                    if(marker!=null){
                        if(value<calculateDistance(start,end))
                            marker.setVisible(false);
                        else
                            marker.setVisible(true);
                    }
                }
            }
        }
    }

    private void removeMyCircle(){
        if(myCircle!=null)
            myCircle.remove();
    }
    //vraca vrednost u metrima
    private double calculateDistance(MyLatLong start,MyLatLong end){
        float[] results = new float[1];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude(),
                results);
        return results[0];
    }
    private void openSearchUserDialog(){
        if(mySpinner.getSelectedItem().toString().equals(getString(R.string.me))){
            Toast.makeText(this, "All users must be selected in order to search for a user.", Toast.LENGTH_SHORT).show();
        }else{
            FindUserOnMapDialog dialog=new FindUserOnMapDialog();
            dialog.show(getSupportFragmentManager(),"Find user dialog");
        }

    }

    public void showUser(UserData user,boolean isFriend){
        myDialog=new DisplayUserInformationOnMapDialog(this);
        myDialog.showDialog(user,isFriend);

    }

    @Override
    public void onBackPressed() {
        if(zoomLatLong!=null)
        {
            Intent i=new Intent(this,HomePageActivity.class);
            startActivity(i);
            finish();

        }else{
            super.onBackPressed();
        }

    }
}