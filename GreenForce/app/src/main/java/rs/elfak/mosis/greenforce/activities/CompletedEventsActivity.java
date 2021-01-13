package rs.elfak.mosis.greenforce.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.EventsAdapter;
import rs.elfak.mosis.greenforce.enums.VolunteerType;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.LikeDislike;
import rs.elfak.mosis.greenforce.models.MyEvent;

public class CompletedEventsActivity extends AppCompatActivity implements IComponentInitializer {

    Toolbar toolbar;
    ListView eventsToDisplayListView;
    ArrayList<MyEvent> leaderEvents,volunteerEvents,displayedEvents;
    EventsAdapter eventsAdapter;
    TabLayout tabLayout;
    HashMap<String, EventVolunteer> eventsMap;
    EventsCallback eventsCallback;
    ProgressDialog progressDialog;
    boolean leaderList;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_events);
        initializeComponents();
        setUpActionBar(R.string.completed_events);
        loadAllCurrentEvents();
        tabLayout.addOnTabSelectedListener(myTabSelectedListener);
        String userID = getIntent().getStringExtra("UserIDCompl");
        if(!userID.equals("")){
            MyUserManager.getInstance().getAllCompletedEvents(userID,eventsCallback);
        }
    }
    private void loadAllCurrentEvents()
    {
        progressDialog=new ProgressDialog(CompletedEventsActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

    }
    TabLayout.OnTabSelectedListener myTabSelectedListener=new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
                case 0:displayCompletedEvents(true,leaderEvents);break;
                case 1:displayCompletedEvents(false,volunteerEvents);break;
                default:break;
            }
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) { }
        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };

    private void displayCompletedEvents(boolean isLeader,ArrayList<MyEvent> completedEvents)
    {
        leaderList=isLeader;
        addElementsToDisplay(completedEvents);
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
        displayedEvents.addAll(list);

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
        toolbar=findViewById(R.id.completedEvents_toolbar);
        eventsToDisplayListView=findViewById(R.id.completedEvents_eventsList);
        tabLayout=findViewById(R.id.completedEvents_tabLayout);
        volunteerEvents=new ArrayList<>();
        leaderEvents=new ArrayList<>();
        eventsCallback=new EventsCallback();
        leaderList=true;
    }

    private void checkIfAllEventsReceived()
    {
        if(leaderEvents.size()+volunteerEvents.size()==eventsMap.size())
        {
            progressDialog.dismiss();
            displayCompletedEvents(leaderList, leaderList ? leaderEvents:volunteerEvents);
        }
    }


    public class EventsCallback implements IGetEventsCallback {
        @Override
        public void onEventsReceived(ArrayList<MyEvent> events) { }

        @Override
        public void onSingleEventReceived(MyEvent event) {
            if(eventsMap.containsKey(event.getEventID())){
                EventVolunteer type=eventsMap.get(event.getEventID());
                assert type != null;
                if(type.getType()== VolunteerType.LEADER)
                    leaderEvents.add(event);
                else
                    volunteerEvents.add(event);
                checkIfAllEventsReceived();
            }
        }

        @Override
        public void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {
//            if(currentEventsRole!=null){
//                eventsMap=currentEventsRole;
//                for(String key : eventsMap.keySet()){
//                    MyUserManager.getInstance().getSingleEvent(key,eventsCallback);
//                }
//            }
//            else
//                progressDialog.dismiss();
        }

        @Override
        public void onCompletedEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {
            if(currentEventsRole!=null){
                eventsMap=currentEventsRole;
                for(String key : eventsMap.keySet()){
                    MyUserManager.getInstance().getSingleEvent(key,eventsCallback);
                }
            }else
                progressDialog.dismiss();
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
}