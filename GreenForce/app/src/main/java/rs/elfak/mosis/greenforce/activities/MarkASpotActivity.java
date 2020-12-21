package rs.elfak.mosis.greenforce.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.xeoh.android.checkboxgroup.CheckBoxGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.enums.EventTypes;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IOnClickNewIntent;
import rs.elfak.mosis.greenforce.interfaces.IViewFlipperHandler;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.MyEvent;
import rs.elfak.mosis.greenforce.models.MyLatLong;

public class MarkASpotActivity extends AppCompatActivity implements IComponentInitializer, IViewFlipperHandler ,
        View.OnClickListener, IOnClickNewIntent
{
    private Toolbar toolbar;
    private ViewFlipper viewFlipper;
    private TabLayout tabLayout;
    private CheckBox waterPollution,landPollution,reforestation,other;
    private EditText eventPointsSum,description;
    private TextView eventLocationCity;
    private View markASpotInfoView;
    private View markASportAboutView;
    private Button uploadEventPhoto;
    private CheckBoxGroup<String> checkBoxGroup;
    MyEvent myEvent;
    ArrayList<String> eventValues;
    HashMap<String,EventTypes> pointsToTypesMap;

    CheckBoxGroup.CheckedChangeListener<String> myCheckBoxListener = new CheckBoxGroup.CheckedChangeListener<String>() {
        @Override
        public void onCheckedChange(ArrayList<String> values)
        {
            eventValues=values;
            int sum=0;
            for(String i:values)
                sum+=Integer.parseInt(i);
            eventPointsSum.setText(sum+"");
        }
    };
    TabLayout.OnTabSelectedListener myTabSelectedListener=new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition())
            {
                case 0:viewFlipper.setDisplayedChild(0);break;
                case 1:viewFlipper.setDisplayedChild(1);break;
                default:break;
            }
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) { }
        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_a_spot_acitvity);
        initializeComponents();
        initializePointsMap();
        setUpActionBar(R.string.mark_a_spot);
        viewFlipperSetup();
        checkBoxesSetup();
        getLastKnownLocation();

        tabLayout.addOnTabSelectedListener(myTabSelectedListener);
        uploadEventPhoto.setOnClickListener(this);

    }
    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    MyLatLong myLatLong=new MyLatLong(location.getLatitude(),location.getLongitude());
                    myEvent.setEventLocation(myLatLong);
                    try {
                        getEventAddress(myLatLong.getLatitude(),myLatLong.getLongitude());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void getEventAddress(double latitude,double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 3); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        eventLocationCity.setText(address + ", " + city + ", "+ country);
        //myEvent.setEventAddress(address + ", " + city + ", "+ country);

    }
    private void checkBoxesSetup()
    {
        HashMap<CheckBox,String> checkBoxes = new HashMap<>();
        checkBoxes.put(landPollution,"50");
        checkBoxes.put(waterPollution,"100");
        checkBoxes.put(reforestation,"200");
        checkBoxes.put(other,"30");
        checkBoxGroup= new CheckBoxGroup<>(checkBoxes,myCheckBoxListener);
    }
    public void viewFlipperSetup()
    {
        markASpotInfoView = getLayoutInflater().inflate(R.layout.activity_mark_a_spot_acitvity,viewFlipper,false);
        markASportAboutView = getLayoutInflater().inflate(R.layout.activity_mark_a_spot_acitvity,viewFlipper,false);
        viewFlipper.addView(markASpotInfoView);
        viewFlipper.addView(markASportAboutView);
    }

    private void setUpActionBar(int rid)
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public void initializeComponents()
    {
        toolbar=findViewById(R.id.events_toolbar_mark_a_spot);
        tabLayout=findViewById(R.id.tabMarkASpotLayout);
        viewFlipper=findViewById(R.id.view_flipper_mark_a_spot);
        landPollution=findViewById(R.id.land_pollution);
        waterPollution=findViewById(R.id.water_pollution);
        reforestation=findViewById(R.id.reforestation);
        other=findViewById(R.id.other);
        uploadEventPhoto=findViewById(R.id.upload_event_photo);
        eventPointsSum=findViewById(R.id.event_points_sum);
        description=findViewById(R.id.event_description_text);
        eventLocationCity=findViewById(R.id.event_location_city);

        myEvent = new MyEvent();

    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.upload_event_photo){
            if(other.isChecked()){
                if(!description.getText().toString().equals("")){
                    startUpload();
                }else{
                    Toast.makeText(this,"You must enter a description for Other",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                startUpload();
            }
        }
    }

    private void startUpload() {
        myEvent.setDescription(description.getText().toString());
        addEventTypes();

    }

    private void initializePointsMap(){
        pointsToTypesMap=new HashMap<>();
        pointsToTypesMap.put("50",EventTypes.LAND_POLLUTION);
        pointsToTypesMap.put("100",EventTypes.WATER_POLLUTION);
        pointsToTypesMap.put("200",EventTypes.REFORESTATION);
        pointsToTypesMap.put("30",EventTypes.OTHER);
    }

    private void addEventTypes() {
        if(eventValues!=null && eventValues.size()!=0){
            ArrayList<EventTypes> types=new ArrayList<EventTypes>();
            for(String s : eventValues){
                 types.add(pointsToTypesMap.get(s));
            }
            myEvent.setEventTypes(types);
            myEvent.setEventPoints(Integer.parseInt(eventPointsSum.getText().toString()));
            MyUserManager.getInstance().getUser().setCurrentEvent(myEvent);
            onClickNewIntent(this,UploadPhotoActivity.class);
        }else
            Toast.makeText(this,"Please select an event type",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickNewIntent(Context context, Class<?> myClass) {
        Intent i= new Intent(context,myClass);
        startActivity(i);
    }
}