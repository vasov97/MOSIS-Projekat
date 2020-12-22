package rs.elfak.mosis.greenforce;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rs.elfak.mosis.greenforce.activities.AddFriendsViaMapsActivity;
import rs.elfak.mosis.greenforce.dialogs.EventFiltersDialog;
import rs.elfak.mosis.greenforce.enums.DataRetriveAction;
import rs.elfak.mosis.greenforce.interfaces.IApplyEventFilters;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetUsersCallback;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.ClusterMarker;
import rs.elfak.mosis.greenforce.models.MyEvent;
import rs.elfak.mosis.greenforce.models.MyLatLong;
import rs.elfak.mosis.greenforce.models.UserData;

public class EventsMapActivity extends AppCompatActivity implements IComponentInitializer, OnMapReadyCallback, IApplyEventFilters {

    EditText radius;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    GoogleMap googleMap;
    ArrayList<MyEvent> myEvents;
    Map<String,Marker> eventMarkers;
    Map<String,UserData> eventCreatedByMap;
    Circle myCircle;
    GetEventsCallback clb;
    GetUsersCallback usersClb;
    boolean dataLoaded=false,appliedFilters=false;
    EventFiltersDialog filtersDialog;

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
    ChildEventListener eventsChildEventListener=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            MyEvent event=dataSnapshot.getValue(MyEvent.class);
            if(myEvents!=null){
                if(!myEvents.contains(event)){
                    myEvents.add(event);
                    drawEventMarker(event);
                    if(appliedFilters)
                        setMarkerVisibility(event,false);
                }
            }
        }
        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            //samo se status eventa azurira?
            MyEvent event=dataSnapshot.getValue(MyEvent.class);
            for(MyEvent e : myEvents){
                if(e.getEventID().equals(event.getEventID())){
                    e.setEventStatus(event.getEventStatus());
                }
            }
        }
        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };




    public class GetEventsCallback implements IGetEventsCallback{
        @Override
        public void onEventsReceived(ArrayList<MyEvent> events) {
              progressDialog.dismiss();
              myEvents=events;
              if(myEvents!=null){
                  getEventCreators();
                  drawAllMarkers();
              }

        }
    }

    public class GetUsersCallback implements IGetUsersCallback{

        @Override
        public void onUsersReceived(ArrayList<UserData> allUsers) {

        }

        @Override
        public void onUserReceived(UserData user) {
            MyEvent e=getEventFromListByCreatorID(user.getUserUUID(),myEvents);
            if(e!=null){
                eventCreatedByMap.put(e.getEventID(),user);
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_map);
        initializeComponents();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_maps_fragment_events_map);
        mapFragment.getMapAsync(this);
        loadAllEvents();
        setUpActionBar(R.string.events);
        radius.addTextChangedListener(myTextWatcher);
        MyUserManager.getInstance().getDatabaseEventsReference().addChildEventListener(eventsChildEventListener);


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        clb=new GetEventsCallback();
        MyUserManager.getInstance().getAllEvents(clb);
    }

    @Override
    public void initializeComponents() {
        radius=findViewById(R.id.events_map_radius);
        toolbar=findViewById(R.id.eventsMapToolbar);
        eventMarkers=new HashMap<String,Marker>();
        eventCreatedByMap=new HashMap<String,UserData>();
    }
    @Override
    public void disableMarker(MyEvent e) {
        setMarkerVisibility(e,false);
    }

    @Override
    public void enableMarker(MyEvent e) {
        setMarkerVisibility(e,true);
    }

    @Override
    public boolean checkIfPostedBy(String username, String eventID) {
        if(eventCreatedByMap.containsKey(eventID)){
            if(eventCreatedByMap.get(eventID).getUsername().equals(username))
                return true;
        }
        return false;
    }

    @Override
    public void appliedFilters() {
        appliedFilters=true;
    }

    @Override
    public void clearedFilters() {
        appliedFilters=false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.search_user_on_map) {
            if(!dataLoaded){
                dataLoaded=checkIfDataLoaded();
            }
            if(dataLoaded){
                if(filtersDialog==null)
                    filtersDialog=new EventFiltersDialog(this,myEvents);
                filtersDialog.showDialog();
            }else
            {
                Toast.makeText(this,"Data is being loaded please try again in a few seconds.",Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
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
    private void setUpActionBar(int rid) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //da bi postavio back strelicu na menu
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        // toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
    }
    private void loadAllEvents() {
        progressDialog=new ProgressDialog(EventsMapActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void drawEventMarker(MyEvent event){
        Marker eventMarker=(Marker)eventMarkers.get(event.getEventID());
        LatLng eventLatLng = new LatLng(event.getEventLocation().getLatitude(),event.getEventLocation().getLongitude());
        if(eventMarker!=null)
            eventMarker.setPosition(eventLatLng);
        else{
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(eventLatLng);
            markerOptions.title("Event");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            eventMarker = googleMap.addMarker(markerOptions);
            eventMarker.setTag(event);
            eventMarkers.put(event.getEventID(), eventMarker);
        }
    }
    private void drawAllMarkers(){
        for (MyEvent event : myEvents) {
            drawEventMarker(event);
        }
    }
    private void removeMyCircle(){
        if(myCircle!=null)
            myCircle.remove();
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
        if(myEvents!=null){
            for (MyEvent event : myEvents) {
                MyLatLong start=MyUserManager.getInstance().getUser().getMyLatLong();
                MyLatLong end=event.getEventLocation();
                Marker marker=(Marker)eventMarkers.get(event.getEventID());
                if(marker!=null){
                    if(value<calculateDistance(start,end))
                        marker.setVisible(false);
                    else
                        marker.setVisible(true);
                }
            }
        }
    }
    private double calculateDistance(MyLatLong start,MyLatLong end){
        float[] results = new float[1];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude(),
                results);
        return results[0];
    }

    private void getEventCreators() {
        for(MyEvent e : myEvents){
            usersClb=new GetUsersCallback();
            MyUserManager.getInstance().getSingleUser(DataRetriveAction.GET_USER,e.getCreatedByID(),usersClb);
        }
    }

    private MyEvent getEventFromListByCreatorID(String id,ArrayList<MyEvent> myEvents){
        for(MyEvent e : myEvents){
            if(e.getCreatedByID().equals(id)){
                return e;
            }
        }
        return null;
    }

    private boolean checkIfDataLoaded(){
        for(MyEvent e : myEvents){
            if(!eventCreatedByMap.containsKey(e.getEventID()))
                return false;
        }
        return true;
    }
    public void setMarkerVisibility(MyEvent event,boolean visibility){
        Marker marker=eventMarkers.get(event.getEventID());
        if(marker!=null)
            marker.setVisible(visibility);
    }


}