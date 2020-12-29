package rs.elfak.mosis.greenforce.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.NotificationsAdapter;
import rs.elfak.mosis.greenforce.enums.NotificationType;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetDataCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetNotifications;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.FriendsRequestNotification;

public class NotificationsActivity extends AppCompatActivity implements IComponentInitializer
{
    NotificationsAdapter notificationsAdapter;
    Toolbar toolbar;
    ListView notificationsListView;
    ArrayList<Object> notificationsList;
    IGetNotifications notificationsCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        initializeComponents();
        setUpActionBar(R.string.notifications);

        String type = getIntent().getStringExtra("Type");
        if(type.equals(NotificationType.EVENT.toString()))
            MyUserManager.getInstance().getEventRequestNotifications(MyUserManager.getInstance().getCurrentUserUid(),notificationsCallback);
        else if(type.equals(NotificationType.FRIEND.toString()))
           MyUserManager.getInstance().getFriendRequestNotifications(MyUserManager.getInstance().getCurrentUserUid(),notificationsCallback);

    }

    public class GetNotifications implements IGetNotifications
    {

        @Override
        public void onFriendRequestsReceived(ArrayList<Object> notifications)
        {
            if(notifications!=null){
                notificationsList=notifications;
                displayNotifications(notificationsList);
            }


        }
    }
    @Override
    public void initializeComponents()
    {
       notificationsListView=findViewById(R.id.notifications_listView);
       toolbar=findViewById(R.id.notifications_toolbar);
        notificationsCallback=new GetNotifications();
    }

    private void displayNotifications(ArrayList<Object> notifications)
    {
        if(notifications!=null)
        {
            notificationsAdapter=new NotificationsAdapter(this,notifications);
            notificationsListView.setAdapter(notificationsAdapter);
        }

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
}
