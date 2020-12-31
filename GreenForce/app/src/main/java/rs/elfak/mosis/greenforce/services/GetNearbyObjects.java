package rs.elfak.mosis.greenforce.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;

import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;

import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.activities.AddFriendsViaMapsActivity;
import rs.elfak.mosis.greenforce.activities.EventActivity;
import rs.elfak.mosis.greenforce.activities.EventsMapActivity;
import rs.elfak.mosis.greenforce.activities.MyProfileActivity;
import rs.elfak.mosis.greenforce.activities.RankingsActivity;
import rs.elfak.mosis.greenforce.enums.DataRetriveAction;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetUsersCallback;
import rs.elfak.mosis.greenforce.models.ClusterMarker;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.MyEvent;
import rs.elfak.mosis.greenforce.models.MyLatLong;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.UserData;


public class GetNearbyObjects {

    
    LocationService locationServiceContext;
    ArrayList<UserData> users;
    ArrayList<MyEvent> allEvents;
    HashMap<String, MyLatLong> tmpChildAddedLatLng;
    GetUsers getUsers;
    GetEventsCallback getEvents;
    String CHANNEL_ID="my_channel_2";
    ArrayList<String> displayedUsers;
    ArrayList<String> displayedEvents;
    double radius;
    public GetNearbyObjects(LocationService locationService)
    {
        radius=5;
        locationServiceContext=locationService;
        displayedUsers=new ArrayList<>();
        displayedEvents=new ArrayList<>();
        getEvents=new GetEventsCallback();
        getUsers=new GetUsers();
        MyUserManager.getInstance().getAllEvents(getEvents);
        MyUserManager.getInstance().getAllUsers(getUsers,DataRetriveAction.GET_USERS);
        MyUserManager.getInstance().getDatabaseEventsReference().addChildEventListener(eventsChildEventListener);
        MyUserManager.getInstance().getDatabaseCoordinatesReference().addChildEventListener(locationsChildEventListener);
        
    }
    ChildEventListener locationsChildEventListener=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String uid=dataSnapshot.getKey();
            
            if(tmpChildAddedLatLng==null)
                tmpChildAddedLatLng=new HashMap<String,MyLatLong>();
            if(users!=null){
                UserData tmpData=new UserData();
                tmpData.setUserUUID(uid);
                if(!users.contains(tmpData)){
                    tmpChildAddedLatLng.put(uid,dataSnapshot.getValue(MyLatLong.class));
                    MyUserManager.getInstance().getSingleUser(DataRetriveAction.GET_USER,uid,getUsers);
                }
            }
        }
        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if(dataSnapshot.exists()){
                String uuid=dataSnapshot.getKey();
                MyLatLong myLatLong=dataSnapshot.getValue(MyLatLong.class);
                myLatLong.setUid(uuid);
                if(!uuid.equals(MyUserManager.getInstance().getCurrentUserUid())){
                    {
                        changeUsersLocation(uuid,myLatLong);
                    }
                }}
            }
        
        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };

    ChildEventListener eventsChildEventListener=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            MyEvent event=dataSnapshot.getValue(MyEvent.class);
            if(allEvents!=null){
                if(!allEvents.contains(event)){
                    allEvents.add(event);
                }
            }
        }
        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };
    private void changeUsersLocation(String uuid, MyLatLong myLatLong) {
        if(users!=null){
            for(UserData u : users){
                if(u.getUserUUID().equals(uuid))
                    u.setMyLatLong(myLatLong);
            }
        }
    }



    public class GetUsers implements IGetUsersCallback {

        @Override
        public void onUsersReceived(ArrayList<UserData> allUsers)
        {

            checkForInvalidLocations(allUsers);
            
            users = allUsers;
        }

        @Override
        public void onUserReceived(UserData user) {

            if (tmpChildAddedLatLng.containsKey(user.getUserUUID())) {
                user.setMyLatLong(tmpChildAddedLatLng.get(user.getUserUUID()));
                if (users != null)
                    users.add(user);
            }

        }
    }
    
    
    public class GetEventsCallback implements IGetEventsCallback {

        @Override
        public void onEventsReceived(ArrayList<MyEvent> events) {
            allEvents = events;
        }

        @Override
        public void onSingleEventReceived(MyEvent event) {
        }

        @Override
        public void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {
        }

        @Override
        public void onEventImagesReceived(ArrayList<Bitmap> images) {
        }

        @Override
        public void onEventVolunteersReceived(ArrayList<EventVolunteer> volunteers) {
        }
    }

    public void getNearbyObjects(MyLatLong userLocation)
    {
        ArrayList<Object> nearbyObjects=new ArrayList<>();
        if(allEvents!=null){
            if(allEvents.size()!=0) {
                for (MyEvent event : allEvents) {
                    if(calculateDistance(event.getEventLocation(),userLocation)<=radius*1000)
                        nearbyObjects.add(event);
                }
            }
        }
        if(users!=null && users.size()!=0){
            if(users!=null)
            {
                if(users.size()!=0)
                {
                    for(UserData userData: users)
                    {
                        if(!userData.getUserUUID().equals(MyUserManager.getInstance().getCurrentUserUid())){
                            if(calculateDistance(userData.getMyLatLong(),userLocation)<=radius*1000)
                                nearbyObjects.add(userData);
                        }
                    }
                }

            }

        }
        if(nearbyObjects.size()!=0){
            Random rand=new Random();
            int upperBound=nearbyObjects.size();
            int getRandom = rand.nextInt(upperBound);
            Object o=nearbyObjects.get(getRandom);
            if(!checkIfRecentlyDisplayed(o))
                     showPushNotification(o);
        }
    }

    private void showPushNotification(Object o) 
    {
        if(o instanceof UserData)
        {
            showPushNotificationForUser((UserData)o);
        }
        else if(o instanceof MyEvent)
        {
            showPushNotificationForEvent((MyEvent)o);
        }
    }

    /*public void test(Service service)
    {
        NotificationManager notificationManager = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }*/


    private void showPushNotificationForEvent(MyEvent myEvent)
    {

        Intent intent = new Intent(locationServiceContext, EventsMapActivity.class);
        intent.putExtra("Zoom","zoom");
        intent.putExtra("Lat",myEvent.getEventLocation().getLatitude()+"");
        intent.putExtra("Lon",myEvent.getEventLocation().getLongitude()+"");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(locationServiceContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(locationServiceContext,locationServiceContext.getCHANNEL_2_ID())
                .setSmallIcon(R.drawable.push_notification)
                .setContentTitle("Event info")
                .setContentText("There is an"+myEvent.getEventStatus().toString()+"event nearby")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(locationServiceContext);
        notificationManagerCompat.notify(2,builder.build());

    }

    private void showPushNotificationForUser(UserData userData)
    {

        Intent intent = new Intent(locationServiceContext, AddFriendsViaMapsActivity.class);
        intent.putExtra("Zoom", "zoom");
        intent.putExtra("Lat",userData.getMyLatLong().getLatitude()+"");
        intent.putExtra("Lon",userData.getMyLatLong().getLongitude()+"");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(locationServiceContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(locationServiceContext,locationServiceContext.getCHANNEL_2_ID())
                .setSmallIcon(R.drawable.push_notification)
                .setContentTitle("User info")
                .setContentText("User: "+userData.getUsername() + "is nearby")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(locationServiceContext);
        notificationManagerCompat.notify(2,builder.build());
    }


    private void checkForInvalidLocations(ArrayList<UserData> userList){
        if(users==null)
            users=new ArrayList<UserData>();
        else{
            users.clear();
        }
        for(UserData user: userList){
            if(user.getMyLatLong()!=null){
                users.add(user);
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

    private boolean checkIfRecentlyDisplayed(Object o){
        if(o instanceof UserData)
        {
            UserData user=(UserData)o;
            if(displayedUsers.contains(user.getUserUUID()))
                return true;
            else{
                displayedUsers.add(user.getUserUUID());
                return false;
            }
        }
        else if(o instanceof MyEvent)
        {
            MyEvent event=(MyEvent)o;
            if(displayedEvents.contains(event.getEventID()))
                return true;
            else{
                displayedEvents.add(event.getEventID());
                return false;
            }
        }
        return false;
    }



}