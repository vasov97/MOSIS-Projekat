package rs.elfak.mosis.greenforce;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import java.nio.channels.FileLock;

/********************************
MyFriends ToDo:
 - open myfriends activity *
 - dimen xml *
 - fab menu *
 - listview design
 -
 - add static listview
 - scrolable
 - toolbar design
 - bluetooth toolbar
 - bluetooth acitivity
 - scan nearby devices
 - adding to friends list
**********************************/

public class MyFriendsActivity extends AppCompatActivity implements View.OnClickListener,IComponentInitializer{
    FloatingActionButton fabFriends;
    FloatingActionButton fabBluetooth;
    FloatingActionButton fabMaps;
    Toolbar toolbar;

    OvershootInterpolator overshootInterpolator= new OvershootInterpolator();

    boolean isFABOpen=false;
    float translationY;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        initializeComponents();

    }


    private void showFABMenu()
    {
        isFABOpen = !isFABOpen;
        setAnimationInterpolator(0,1);
        fabFriends.setImageDrawable(getResources().getDrawable(R.drawable.close_icon));

    }
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
        toolbar = findViewById(R.id.toolbar);
        setFABAlpha();
        setFABListeners();
        setFABTranslations();
        setSupportActionBar(toolbar);
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
    public void onClick(View v)
    {
        if(v.getId()==R.id.fabFriends)
        {
            if(isFABOpen) closeFABMenu();
            else showFABMenu();
        }
        else if(v.getId()==R.id.fabBluetooth)
        {
            Intent i=new Intent(this,AddFriendsViaBluetoothActivity.class);
            startActivity(i);
        }
        else if(v.getId()==R.id.fabMaps)
        {
            //
        }
    }
}