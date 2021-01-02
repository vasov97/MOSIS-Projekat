package rs.elfak.mosis.greenforce.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.MyLatLong;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.UserData;


public class LocationService extends Service  {


    private static final String TAG = "LocationService";
    long pushNotificationCounter=0;
    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */
    private final static long NEARBY_DISPLAY_INTERVAL=60*1000;
    LocationCallback locationCallback;
    GetNearbyObjects nearbyObjects;
    String CHANNEL_1_ID = "my_channel_01";
    String CHANNEL_2_ID = "my_channel_02";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        nearbyObjects=new GetNearbyObjects(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {

            NotificationChannel channel1 = new NotificationChannel(CHANNEL_1_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel1);
            NotificationChannel channel2 = new NotificationChannel(CHANNEL_2_ID,
                    "My Channel_2",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel2);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);

        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                    Log.d(TAG, "onLocationResult: got location result.");

                    Location location = locationResult.getLastLocation();

                    if (location != null && MyUserManager.getInstance().getUser()!=null) {
                        MyLatLong myLatLong=new MyLatLong(location.getLatitude(),location.getLongitude());
                        LatLng userLatLng = new LatLng(myLatLong.getLatitude(),myLatLong.getLongitude());
                        saveUserLocation(myLatLong);
                        pushNotificationCounter+=UPDATE_INTERVAL;
                        if(pushNotificationCounter==NEARBY_DISPLAY_INTERVAL){
                            pushNotificationCounter=0;
                            nearbyObjects.getNearbyObjects(myLatLong);
                        }

                    }
                }

        };

    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation() {

        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");

        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy,locationCallback,Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }

    private void saveUserLocation(MyLatLong myLatLong){
        MyUserManager.getInstance().saveUserCoordinates(myLatLong);
    }

    public String getCHANNEL_2_ID() {
        return CHANNEL_2_ID;
    }



    @Override
    public void onDestroy() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }
}