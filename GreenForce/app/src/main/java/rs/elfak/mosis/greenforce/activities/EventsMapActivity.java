package rs.elfak.mosis.greenforce.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

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
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.dialogs.DisplayEventInformationOnMapDialog;
import rs.elfak.mosis.greenforce.dialogs.EventFiltersDialog;
import rs.elfak.mosis.greenforce.enums.DataRetriveAction;
import rs.elfak.mosis.greenforce.enums.EventStatus;
import rs.elfak.mosis.greenforce.interfaces.IApplyEventFilters;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetUsersCallback;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.LikeDislike;
import rs.elfak.mosis.greenforce.models.MyEvent;
import rs.elfak.mosis.greenforce.models.MyLatLong;
import rs.elfak.mosis.greenforce.models.UserData;

public class EventsMapActivity extends AppCompatActivity implements IComponentInitializer, OnMapReadyCallback, IApplyEventFilters,GoogleMap.OnMarkerClickListener {

    String zoom;
    MyLatLong zoomLatLong;
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
    DisplayEventInformationOnMapDialog eventDialog;



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
            MyEvent event=dataSnapshot.getValue(MyEvent.class);
            if(event!=null) {
                event.setEventID(dataSnapshot.getKey());
                for (MyEvent e : myEvents) {
                    if(e.getEventID()!=null && event.getEventID()!=null) {
                        if (e.getEventID().equals(event.getEventID())) {
                            if (e.getEventStatus() != event.getEventStatus()) {
                                e.setEventStatus(event.getEventStatus());
                                if (event.getEventStatus() == EventStatus.PENDING)
                                    e.setImagesAfterCount(event.getImagesAfterCount());
                                updateMarkerTag(e);
                            }
                        }
                    }
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        MyEvent eventToView=(MyEvent) marker.getTag();
        UserData createdBy=eventCreatedByMap.get(eventToView.getEventID());
        if(eventToView!=null && createdBy!=null){
            if(eventDialog==null)
            {
                eventDialog=new DisplayEventInformationOnMapDialog(this,createdBy,eventToView);

            }

            else{
                eventDialog.setEventToView(eventToView);
                eventDialog.setCreatedByUser(createdBy);
            }
            eventDialog.showDialog();
        }
        return false;
    }

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

        @Override
        public void onSingleEventReceived(MyEvent event) {

        }

        @Override
        public void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {

        }

        @Override
        public void onCompletedEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {

        }

        @Override
        public void onEventImagesReceived(ArrayList<Bitmap> images) {

        }

        @Override
        public void onEventVolunteersReceived(ArrayList<EventVolunteer> volunteers) {

        }

        @Override
        public void onLikeDislikeReceived(ArrayList<LikeDislike> list) {

        }
    }

    public class GetUsersCallback implements IGetUsersCallback{

        @Override
        public void onUsersReceived(ArrayList<UserData> allUsers) {

        }

        @Override
        public void onUserReceived(UserData user) {
            for(MyEvent e : getAllEventByUser(user.getUserUUID(),myEvents)){
                eventCreatedByMap.put(e.getEventID(),user);
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_map);
        initializeComponents();
        zoom=getIntent().getStringExtra("Zoom");
        if(zoom!=null){
            double lat=Double.parseDouble(getIntent().getStringExtra("Lat"));
            double lon=Double.parseDouble(getIntent().getStringExtra("Lon"));
            zoomLatLong=new MyLatLong(lat,lon);
        }
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
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public void initializeComponents() {
        radius=findViewById(R.id.events_map_radius);
        toolbar=findViewById(R.id.eventsMapToolbar);
        eventMarkers= new HashMap<>();
        eventCreatedByMap= new HashMap<>();
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
            if(event.getEventStatus()!= EventStatus.COMPLETED)
                drawEventMarker(event);
        }
        if(zoom!=null){
            zoom=null;
            setCameraView(zoomLatLong);
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
        ArrayList<String> uniqueIDs=new ArrayList<String>();
        for(MyEvent e : myEvents){
           if(!uniqueIDs.contains(e.getCreatedByID()))
               uniqueIDs.add(e.getCreatedByID());
        }
        for(String key : uniqueIDs){
            usersClb=new GetUsersCallback();
            MyUserManager.getInstance().getSingleUser(DataRetriveAction.GET_USER,key,usersClb);
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
    private ArrayList<MyEvent> getAllEventByUser(String id,ArrayList<MyEvent> myEvents){
        ArrayList<MyEvent> eventToReturn=new ArrayList<MyEvent>();
        for(MyEvent e : myEvents){
            if(e.getCreatedByID().equals(id)){
                eventToReturn.add(e);
            }
        }
        return eventToReturn;
    }

    private void updateMarkerTag(MyEvent event) {
        Marker marker=eventMarkers.get(event.getEventID());
        if(marker!=null)
            marker.setTag(event);
        if(eventDialog!=null){
            if(eventDialog.getOnScreen()){
                eventDialog.setEventToView(event);
                eventDialog.refreshDialogInformation();
            }
        }
    }
    private void setCameraView(MyLatLong location){

        double bottomBoundary=location.getLatitude()-.1;
        double leftBoundary=location.getLongitude()-.1;
        double topBoundary=location.getLatitude()+.1;
        double rightBoundary=location.getLongitude()+.1;

        LatLngBounds myMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(myMapBoundary,0));
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