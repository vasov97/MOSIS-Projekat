package rs.elfak.mosis.greenforce.activities;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

import rs.elfak.mosis.greenforce.interfaces.IGetNotifications;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.FriendsRequestNotification;
import rs.elfak.mosis.greenforce.models.UserData;
import rs.elfak.mosis.greenforce.adapters.MyFriendsAdapter;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetFriendsCallback;
import rs.elfak.mosis.greenforce.interfaces.IOnClickNewIntent;


public class MyFriendsActivity extends AppCompatActivity implements View.OnClickListener,Serializable, IComponentInitializer,
        IOnClickNewIntent {
    FloatingActionButton fabFriends;
    FloatingActionButton fabBluetooth;
    FloatingActionButton fabMaps;
    Toolbar toolbar;
    ArrayList<UserData> friends;
    IGetFriendsCallback friendsClb;
    IGetNotifications notificationsClb;
    ListView friendsList;
    ProgressDialog progressDialog;
    MyFriendsAdapter friendsAdapter;
    SearchView searchView;
    ImageView notificationIcon;
    TextView notificationNumber;
    ArrayList<FriendsRequestNotification> friendsRequestNotifications;

    OvershootInterpolator overshootInterpolator= new OvershootInterpolator();

    boolean isFABOpen=false;
    float translationY;


    public class GetFriendsCallback implements IGetFriendsCallback{
        @Override
        public void onFriendsReceived(ArrayList<UserData> myFriends) {
            friends=myFriends;
            progressDialog.dismiss();
            if(searchView==null || searchView.getQuery().toString().equals(""))
               displayFriends(friends);
        }
    }

    public class GetNotificationsCallback implements IGetNotifications{
        @Override
        public void onFriendRequestsReceived(ArrayList<FriendsRequestNotification> notifications) {
            if(notifications!=null){
                friendsRequestNotifications=notifications;
                notificationIcon.setEnabled(true);
                int notSeenCount=0;
                for(FriendsRequestNotification notification :notifications){
                    if(notification.getSeen()==false)
                        notSeenCount++;
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

    SearchView.OnQueryTextListener mySearchListener=new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText)
        {
            if(newText.isEmpty())
                displayFriends(friends);
            else{
                ArrayList<UserData> resultData = new ArrayList<UserData>();
                for(UserData user:friends)
                {
                    if(user.getUsername().toLowerCase().contains(newText.toLowerCase()))
                    {
                        resultData.add(user);
                    }
                }
                displayFriends(resultData);
            }


            return true;
        }
    };

    private void displayFriends(ArrayList<UserData> friends) {
        friendsAdapter=new MyFriendsAdapter(this,friends);
        friendsList.setAdapter(friendsAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        initializeComponents();
        friendsClb =new GetFriendsCallback();
        notificationsClb=new GetNotificationsCallback();
        MyUserManager.getInstance().getFriends(MyUserManager.getInstance().getCurrentUserUid(), friendsClb);
        MyUserManager.getInstance().getFriendRequestNotifications(MyUserManager.getInstance().getCurrentUserUid(),notificationsClb);

        setUpActionBar(R.string.friends_text);
        loadFriendsList();

        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData user=friendsAdapter.getClickedItem(position);
                viewClickedUserProfile(user);

            }
        });

    }

    private void viewClickedUserProfile(UserData user) {
        MyUserManager.getInstance().setVisitProfile(user);
        Intent i=new Intent(this, MyProfileActivity.class);
        i.putExtra("Visit","Visit");
        startActivity(i);
    }

    private void loadFriendsList()
    {
        progressDialog=new ProgressDialog(MyFriendsActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

    }

    private void setUpActionBar(int rid) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);*/
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showFABMenu() {
        isFABOpen = !isFABOpen;
        setAnimationInterpolator(0,1);
        fabFriends.setImageDrawable(getResources().getDrawable(R.drawable.close_icon));

    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void closeFABMenu()
    {
        isFABOpen = !isFABOpen;
        setAnimationInterpolator(translationY,0);
        fabFriends.setImageDrawable(getResources().getDrawable(R.drawable.add_friends_icon));

    }
    private void setAnimationInterpolator(float translation,float alpha)
    {
        fabBluetooth.animate().translationY(translation).alpha(alpha).setInterpolator(overshootInterpolator).setDuration(300).start();
        fabMaps.animate().translationY(translation).alpha(alpha).setInterpolator(overshootInterpolator).setDuration(300).start();
    }
    @Override
    public void initializeComponents()
    {
        translationY=100;
        fabFriends = findViewById(R.id.fabFriends);
        fabBluetooth = findViewById(R.id.fabBluetooth);
        fabMaps = findViewById(R.id.fabMaps);
        toolbar = findViewById(R.id.friends_toolbar);
        friendsList=findViewById(R.id.my_friends_listview);
        notificationIcon=findViewById(R.id.notificationIcon);
        notificationNumber=findViewById(R.id.notificationNumber);
        notificationIcon.setOnClickListener(this);
        setFABAlpha();
        setFABListeners();
        setFABTranslations();
        //setSupportActionBar(toolbar);
    }
    private void setFABAlpha()
    {
        fabBluetooth.setAlpha(0f);
        fabMaps.setAlpha(0f);
    }
    private void setFABListeners()
    {
        fabFriends.setOnClickListener(this);
        fabBluetooth.setOnClickListener(this);
        fabMaps.setOnClickListener(this);
    }
    private void setFABTranslations()
    {
        fabFriends.setTranslationY(translationY);
        fabBluetooth.setTranslationY(translationY);
        fabFriends.setTranslationY(translationY);
    }

    @Override
    public void onClickNewIntent(Context context,Class<?> myClass)
    {
        Intent i=new Intent(context,myClass);
        startActivity(i);
    }
    @Override
    public void onClick(View v)
    {
        if(v.getId()==R.id.fabFriends)
        {
            if(isFABOpen) closeFABMenu();
            else showFABMenu();
        }
        else if(v.getId()==R.id.fabBluetooth)
        {
           onClickNewIntent(this, AddFriendsViaBluetoothActivity.class);
        }
        else if(v.getId()==R.id.fabMaps)
        {

            Intent i=new Intent(this, AddFriendsViaMapsActivity.class);
            MyUserManager.getInstance().setMyFriends(friends);
            startActivity(i);
        }
        else if(v.getId()==R.id.notificationIcon){
            Toast.makeText(this, "Bell", Toast.LENGTH_SHORT).show();
        }



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_friends_list,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if(id==R.id.app_bar_search)
            return  true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_friends_list,menu);
        MenuItem searchBar = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView)searchBar.getActionView();
        searchView.setOnQueryTextListener(mySearchListener);
        return true;
    }


}