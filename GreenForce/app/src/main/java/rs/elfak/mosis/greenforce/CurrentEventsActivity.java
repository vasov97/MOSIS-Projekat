package rs.elfak.mosis.greenforce;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.mosis.greenforce.activities.MyFriendsActivity;
import rs.elfak.mosis.greenforce.enums.VolunteerType;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.MyEvent;

public class CurrentEventsActivity extends AppCompatActivity implements IComponentInitializer {
    Toolbar toolbar;
    TabLayout tabLayout;
    ListView eventsToDisplayListView;
    ArrayList<MyEvent> leaderEvents,volunteerEvents;
    HashMap<String, EventVolunteer> eventsMap;
    GetEventsCallback eventsCallback;
    ProgressDialog progressDialog;
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
            }
        }

        @Override
        public void onEventImagesReceived(ArrayList<Bitmap> images) {

        }

        @Override
        public void onEventVolunteersReceived(ArrayList<EventVolunteer> volunteers) {

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
        MyUserManager.getInstance().getAllCurrentEventsEvents(MyUserManager.getInstance().getCurrentUserUid(),eventsCallback);

    }

    @Override
    public void initializeComponents() {
        toolbar=findViewById(R.id.currentEvents_toolbar);
        tabLayout=findViewById(R.id.currentEvents_tabLayout);
        eventsToDisplayListView=findViewById(R.id.currentEvents_eventsList);
        volunteerEvents=new ArrayList<>();
        leaderEvents=new ArrayList<>();
        eventsCallback=new GetEventsCallback();
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
        Toast.makeText(this,"Leader events",Toast.LENGTH_SHORT).show();

    }

    private void displayEventsStatusLeader() {
        leaderList=true;
        Toast.makeText(this,"Volunteer events",Toast.LENGTH_SHORT).show();
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

    //OVDE SAM STAO..Treba da se naprave adapteri za listView da bi se prikazale one 2 liste u funkcijama gde je sad toast
    //*****imas takve layoute za listview samo include gde treba****
    //Treba da se napravi posle toga za notifikacije isti fazon kao kod friends
    // trebalo bi da je sa current gotovo posle --> finishedEvents treba da se smisle
    // NISAM DODAO ON CLICK NA DUGME CURRENT U PROFILE

}