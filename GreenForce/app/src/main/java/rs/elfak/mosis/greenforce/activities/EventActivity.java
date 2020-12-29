package rs.elfak.mosis.greenforce.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.MyFriendsAdapter;
import rs.elfak.mosis.greenforce.adapters.RecyclerAdapter;
import rs.elfak.mosis.greenforce.dialogs.DisplayEventInformationOnMapDialog;
import rs.elfak.mosis.greenforce.dialogs.EventFiltersDialog;
import rs.elfak.mosis.greenforce.enums.DataRetriveAction;
import rs.elfak.mosis.greenforce.enums.EventImageType;
import rs.elfak.mosis.greenforce.enums.EventTypes;
import rs.elfak.mosis.greenforce.enums.VolunteerType;
import rs.elfak.mosis.greenforce.fragments.FragmentMyProfileMain;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetUsersCallback;
import rs.elfak.mosis.greenforce.interfaces.IViewFlipperHandler;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.MyEvent;
import rs.elfak.mosis.greenforce.models.MyLatLong;
import rs.elfak.mosis.greenforce.models.UserData;

public class EventActivity extends AppCompatActivity implements IComponentInitializer, IViewFlipperHandler
{
     private Toolbar toolbar;
     private ViewFlipper viewFlipper;
     private TabLayout tabLayout;
     private View eventInfoView;
     private View eventPhotosView;
     private View eventVolunteersView;
     private RecyclerView recyclerView;
     private ListView volunteerListView;
     private RecyclerView.LayoutManager layoutManager;
     private RecyclerAdapter recyclerAdapter;
     private MyFriendsAdapter volunteersAdapter;
     private TextView eventDescription;
     private TextView eventType;
     private TextView eventPoints,eventStatus,eventLocation;
     private TextView eventCreatedBy,eventCreatedFullName;
     private TextView eventLeader;
     private CircleImageView eventCreatedByImage;
     EventsCallback eventsCallback;
     UserCallback userCallback;
     private MyEvent eventToView;
     private UserData createdByUser;
     String viewEventID;
     ArrayList<UserData> myVolunteers;
     ArrayList<EventVolunteer> myVolunteersKeys;
     VolunteersCallback volunteersCallback;
     boolean isLeader=false;


     public class VolunteersCallback implements IGetUsersCallback{

         @Override
         public void onUsersReceived(ArrayList<UserData> allUsers) {

         }

         @Override
         public void onUserReceived(UserData user) {

             myVolunteers.add(user);
             if(user.getUserUUID().equals(eventLeader.getText().toString()))
                 eventLeader.setText(user.getUsername());
             if(myVolunteers.size()==myVolunteersKeys.size()){
                 volunteersAdapter=new MyFriendsAdapter(getApplicationContext(),myVolunteers);
                 volunteerListView.setAdapter(volunteersAdapter);

             }


         }
     }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_layout);
        initializeComponents();
        setUpActionBar(R.string.event);
        Bundle bundle = getIntent().getExtras();
        viewEventID = bundle.getString("eventId");
        MyUserManager.getInstance().getSingleEvent(viewEventID,eventsCallback);
        MyUserManager.getInstance().getEventVolunteers(viewEventID,eventsCallback);


       viewFlipperSetup();


       //MyUserManager.getInstance().getSingleEvent();



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

               switch (tab.getPosition())
                {
                    case 0:viewFlipper.setDisplayedChild(0);break;
                    case 1: viewFlipper.setDisplayedChild(1);break;
                    case 2:viewFlipper.setDisplayedChild(2);break;
                    default:break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        volunteerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData user=volunteersAdapter.getClickedItem(position);
                viewClickedUserProfile(user);

            }
        });
    }

    private void viewClickedUserProfile(UserData user) {
        Intent i=new Intent(this, MyProfileActivity.class);
       if(user.getUserUUID().equals(MyUserManager.getInstance().getCurrentUserUid())){
           i.putExtra("Visit", "MyProfile");

       }else{
           MyUserManager.getInstance().setVisitProfile(user);
           i.putExtra("Visit","Visit");
       }

        startActivity(i);
    }


    public void viewFlipperSetup()
    {
        eventInfoView = getLayoutInflater().inflate(R.layout.event_info_layout,viewFlipper,false);
        eventPhotosView = getLayoutInflater().inflate(R.layout.event_photos_layout,viewFlipper,false);
        eventVolunteersView = getLayoutInflater().inflate(R.layout.event_volunteers_layout,viewFlipper,false);

        viewFlipper.addView(eventInfoView);
        viewFlipper.addView(eventPhotosView);
        viewFlipper.addView(eventVolunteersView);
    }

    @Override
    public void initializeComponents()
    {
        toolbar=findViewById(R.id.events_toolbar);
        viewFlipper=findViewById(R.id.view_flipper);
        tabLayout=findViewById(R.id.tabLayout);
        eventCreatedByImage=findViewById(R.id.event_created_user_image);
        eventLeader=findViewById(R.id.event_leader);
        recyclerView=findViewById(R.id.event_photos_recycler);
        volunteerListView=findViewById(R.id.event_volunteers_listview);
        layoutManager=new GridLayoutManager(this,2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        eventDescription=findViewById(R.id.event_info_description);
        eventType = findViewById(R.id.event_info_type);
        eventStatus=findViewById(R.id.event_info_status);
        eventPoints=findViewById(R.id.event_info_points);
        eventLocation=findViewById(R.id.event_location_city);
        eventCreatedBy=findViewById(R.id.event_created_username);
        eventCreatedFullName=findViewById(R.id.event_created_fullname);
        eventsCallback= new EventsCallback();
        volunteersCallback=new VolunteersCallback();
        userCallback=new UserCallback();
        myVolunteers=new ArrayList<UserData>();



    }




    private void setUpActionBar(int rid)
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_view_event,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_view_event,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.submit_event)
        {
            if(myVolunteersKeys!=null){
                if(isLeader)
                    Toast.makeText(this,"later",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"Only the event leader can submit this event.",Toast.LENGTH_SHORT).show();

            }

                else Toast.makeText(this,"Please try again later",Toast.LENGTH_SHORT).show();

        }


        return super.onOptionsItemSelected(item);
    }
    public String EventAddressCity(double latitude,double longitude) throws IOException
    {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if(addresses.get(0).getLocality()==null)
            return addresses.get(0).getAddressLine(0)+", "+addresses.get(0).getCountryName();
        else
            return addresses.get(0).getLocality();
    }

   public class EventsCallback implements IGetEventsCallback
   {

       @Override
       public void onEventsReceived(ArrayList<MyEvent> events) {

       }

       @SuppressLint({"ResourceAsColor", "SetTextI18n"})
       @Override
       public void onSingleEventReceived(MyEvent event)
       {
           eventToView=event;
           MyUserManager.getInstance().getSingleUser(DataRetriveAction.GET_USER,event.getCreatedByID(),userCallback);
           try {
               MyUserManager.getInstance().getEventImages(viewEventID, EventImageType.BEFORE, eventToView.getImagesBeforeCount(),eventsCallback);
           } catch (IOException e) {
               e.printStackTrace();
           }

           eventDescription.setText(event.getDescription());
           StringBuilder viewEventTypes = new StringBuilder();
           for(EventTypes type: event.getEventTypes())
           {
               viewEventTypes.append(" ");
               viewEventTypes.append(type.toString());
           }
           eventType.setText(viewEventTypes);

           MyLatLong latLong=event.getEventLocation();
           String myCity="";
           try { myCity= EventAddressCity(latLong.getLatitude(),latLong.getLongitude()); }
           catch(Exception e) { e.getMessage(); }
           eventLocation.setText(myCity);
           eventPoints.setText(event.getEventPoints()+"");
           eventStatus.setText(event.getEventStatus().toString());
           eventStatus.setTextColor(R.color.greenBlue); //stavi u zavisnosti od statusa color da se menja









       }

       @Override
       public void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {

       }

       @Override
       public void onEventImagesReceived(ArrayList<Bitmap> images)
       {
           if(images!=null){
               eventToView.setEventPhotos(images);
               recyclerAdapter=new RecyclerAdapter(eventToView.getEventPhotos());
               recyclerView.setAdapter(recyclerAdapter);
           }

       }

       @Override
       public void onEventVolunteersReceived(ArrayList<EventVolunteer> volunteers) {

           if(volunteers!=null)
           {
               myVolunteersKeys=volunteers;
               for(EventVolunteer v : volunteers){

                   if(v.getType()== VolunteerType.LEADER)
                   {
                       eventLeader.setText(v.getId());
                       if(v.getId().equals(MyUserManager.getInstance().getCurrentUserUid()))
                           isLeader=true;
                   }
                   MyUserManager.getInstance().getSingleUser(DataRetriveAction.GET_USER,v.getId(),volunteersCallback);

               }
           }



       }
   }


   public class UserCallback implements IGetUsersCallback
   {

       @Override
       public void onUsersReceived(ArrayList<UserData> allUsers) {

       }

       @SuppressLint("SetTextI18n")
       @Override
       public void onUserReceived(UserData user)
       {
           createdByUser=user;
           //neka fukcija koja sve postavi
           eventCreatedFullName.setText(user.getName()+" "+user.getSurname());
           eventCreatedBy.setText(user.getUsername());
           eventCreatedByImage.setImageBitmap(user.getUserImage());

       }
   }
}
