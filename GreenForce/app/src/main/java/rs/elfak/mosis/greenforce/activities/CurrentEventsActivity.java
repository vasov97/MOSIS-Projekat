package rs.elfak.mosis.greenforce.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.EventsAdapter;
import rs.elfak.mosis.greenforce.enums.NotificationType;
import rs.elfak.mosis.greenforce.enums.VolunteerType;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetNotifications;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.EventRequestNotification;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.FriendsRequestNotification;
import rs.elfak.mosis.greenforce.models.LikeDislike;
import rs.elfak.mosis.greenforce.models.MyEvent;

public class CurrentEventsActivity extends AppCompatActivity implements IComponentInitializer,View.OnClickListener {
    Toolbar toolbar;
    TabLayout tabLayout;
    ListView eventsToDisplayListView;
    ImageView notificationIcon;
    TextView notificationNumber;
    ArrayList<MyEvent> leaderEvents,volunteerEvents,displayedEvents;
    EventsAdapter eventsAdapter;
    HashMap<String, EventVolunteer> eventsMap;
    GetEventsCallback eventsCallback;
    GetNotificationsCallback notificationsClb;
    ProgressDialog progressDialog;
    ArrayList<Object> eventRequestNotifications;
    boolean leaderList;

    TabLayout.OnTabSelectedListener myTabSelectedListener=new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
                case 0:displayEventsStatusLeader();break;
                case 1:displayEventsStatusVolunteer();break;
                default:break;
            }
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) { }
        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };



    public class GetEventsCallback implements IGetEventsCallback{
        @Override
        public void onEventsReceived(ArrayList<MyEvent> events) { }

        @Override
        public void onSingleEventReceived(MyEvent event) {
            if(eventsMap.containsKey(event.getEventID())){
                EventVolunteer type=eventsMap.get(event.getEventID());
                if(type.getType()== VolunteerType.LEADER)
                    leaderEvents.add(event);
                else
                    volunteerEvents.add(event);
                checkIfAllEventsReceived();
            }
        }

        @Override
        public void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {
            if(currentEventsRole!=null){
                eventsMap=currentEventsRole;
                for(String key : eventsMap.keySet()){
                    MyUserManager.getInstance().getSingleEvent(key,eventsCallback);
                }
            }else
                progressDialog.dismiss();
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
    public class GetNotificationsCallback implements IGetNotifications {
        @Override
        public void onFriendRequestsReceived(ArrayList<Object> notifications) {
            if(notifications!=null){
                eventRequestNotifications=notifications;
                notificationIcon.setEnabled(true);
                int notSeenCount=0;
                for(Object notification :notifications){
                    if(notification instanceof EventRequestNotification)
                    {
                        if(!((EventRequestNotification) notification).getSeen())
                            notSeenCount++;
                    }

                }
                if(notSeenCount!=0) {
                    notificationNumber.setText(String.valueOf(notSeenCount));
                    notificationNumber.setVisibility(View.VISIBLE);
                }
            }else{
                notificationNumber.setVisibility(View.INVISIBLE);
                notificationIcon.setEnabled(false);
            }

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_events);
        initializeComponents();
        setUpActionBar(R.string.currentEvents);
        loadAllCurrentEvents();
        tabLayout.addOnTabSelectedListener(myTabSelectedListener);
        notificationIcon.setOnClickListener(this);
        String userID = getIntent().getStringExtra("UserID");
        if(!userID.equals("")){
            MyUserManager.getInstance().getEventRequestNotifications(userID,notificationsClb);
            MyUserManager.getInstance().getAllCurrentEvents(userID,eventsCallback);
        }


    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.notificationIcon){
            //Toast.makeText(this, "Bell "+notificationNumber.getText(),Toast.LENGTH_SHORT).show();
            Intent i= new Intent(this,NotificationsActivity.class);
            i.putExtra("Type", NotificationType.EVENT.toString());
            startActivity(i);
        }
    }

    @Override
    public void initializeComponents() {
        toolbar=findViewById(R.id.currentEvents_toolbar);
        tabLayout=findViewById(R.id.currentEvents_tabLayout);
        eventsToDisplayListView=findViewById(R.id.currentEvents_eventsList);
        notificationIcon=findViewById(R.id.notificationIcon);
        notificationNumber=findViewById(R.id.notificationNumber);
        volunteerEvents=new ArrayList<>();
        leaderEvents=new ArrayList<>();
        eventsCallback=new GetEventsCallback();
        notificationsClb=new GetNotificationsCallback();
        leaderList=true;
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

    private void displayEventsStatusVolunteer() {
        leaderList=false;
        //Toast.makeText(this,"Leader events",Toast.LENGTH_SHORT).show();

            addElementsToDisplay(volunteerEvents);
        if(eventsAdapter==null){
            eventsAdapter=new EventsAdapter(this,displayedEvents);
            eventsToDisplayListView.setAdapter(eventsAdapter);
        }
        else
        {
            eventsAdapter.notifyDataSetChanged();
        }




    }

    private void displayEventsStatusLeader() {
        leaderList=true;
//

            addElementsToDisplay(leaderEvents);
            if(eventsAdapter==null){
                eventsAdapter=new EventsAdapter(this,displayedEvents);
                eventsToDisplayListView.setAdapter(eventsAdapter);
            }
            else
            {
                eventsAdapter.notifyDataSetChanged();
            }



    }

    private void addElementsToDisplay(ArrayList<MyEvent> list) {
        if(displayedEvents==null)
            displayedEvents=new ArrayList<>();
        displayedEvents.clear();
        for(MyEvent e : list)
            displayedEvents.add(e);
    }

    private void loadAllCurrentEvents()
    {
        progressDialog=new ProgressDialog(CurrentEventsActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

    }

    private void checkIfAllEventsReceived() {

        if(leaderEvents.size()+volunteerEvents.size()==eventsMap.size()){
            progressDialog.dismiss();
            displayListOfEvents();
                }




    }

    private void displayListOfEvents() {
        if(leaderList)
            displayEventsStatusLeader();
        else
            displayEventsStatusVolunteer();
    }

    //nisam dodao on click!!
    // trebalo bi da je sa current gotovo posle --> finishedEvents treba da se smisle



}