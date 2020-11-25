package rs.elfak.mosis.greenforce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.nio.channels.FileLock;
import java.util.ArrayList;



public class MyFriendsActivity extends AppCompatActivity implements View.OnClickListener,IComponentInitializer,IOnClickNewIntent{
    FloatingActionButton fabFriends;
    FloatingActionButton fabBluetooth;
    FloatingActionButton fabMaps;
    Toolbar toolbar;
    ArrayList<UserData> friends;
    IGetFriendsCallback clb;
    ListView friendslist;
    ProgressDialog progressDialog;
    MyFriendsAdapter friendsAdapter;


    OvershootInterpolator overshootInterpolator= new OvershootInterpolator();

    boolean isFABOpen=false;
    float translationY;

    public class GetFriendsCallback implements IGetFriendsCallback{
        @Override
        public void onFriendsReceived(ArrayList<UserData> myFriends) {
            friends=myFriends;
            progressDialog.dismiss();
            displayFriends();
        }
    }

    private void displayFriends() {
        friendsAdapter=new MyFriendsAdapter(this,friends);
        friendslist.setAdapter(friendsAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        clb=new GetFriendsCallback();
        MyUserManager.getInstance().getFriends(MyUserManager.getInstance().getCurrentUserUid(),clb);
        initializeComponents();
        setUpActionBar(R.string.friends_text);
        loadFriendsList();

        friendslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserData user=friendsAdapter.getClickedItem(position);
                viewClickedUserProfile(user);

            }
        });

    }

    private void viewClickedUserProfile(UserData user) {
        MyUserManager.getInstance().setVisitProfile(user);
        Intent i=new Intent(this,MyProfileActivity.class);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
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
        friendslist=findViewById(R.id.my_friends_listview);
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
           onClickNewIntent(this,AddFriendsViaBluetoothActivity.class);
            /*Intent i=new Intent(this,AddFriendsViaBluetoothActivity.class);
            startActivity(i);*/
        }
        else if(v.getId()==R.id.fabMaps)
            onClickNewIntent(this,AddFriendsViaMapsActivity.class);
            /*Intent i=new Intent(this,AddFriendsViaMapsActivity.class);
            startActivity(i);*/

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_friends_list,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_friends_list,menu);
        return true;
    }


}