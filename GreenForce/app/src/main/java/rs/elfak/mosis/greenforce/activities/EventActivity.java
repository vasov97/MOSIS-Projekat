package rs.elfak.mosis.greenforce.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Layout;
import android.view.Menu;
import android.view.View;
import android.widget.ViewFlipper;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;

public class EventActivity extends AppCompatActivity implements IComponentInitializer, View.OnClickListener
{
     private Toolbar toolbar;
     private ViewFlipper viewFlipper;
     private TabLayout tabLayout;
     private TabItem tabEventInfoItem;
     private TabItem tabEventPhotosItem;
     private TabItem tabEventVolunteersItem;
    View eventInfoView;
    View eventPhotosView;
    View eventVolunteersView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_layout);
        initializeComponents();
        setUpActionBar(R.string.event);
        /*tabEventInfoItem.setOnClickListener(this);
        tabEventPhotosItem.setOnClickListener(this);
        tabEventVolunteersItem.setOnClickListener(this);*/
        eventInfoView = getLayoutInflater().inflate(R.layout.event_info_layout,viewFlipper,false);
        eventPhotosView = getLayoutInflater().inflate(R.layout.event_photos_layout,viewFlipper,false);
        eventVolunteersView = getLayoutInflater().inflate(R.layout.event_volunteers_layout,viewFlipper,false);
        viewFlipper.addView(eventInfoView);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition())
                {
                    case 0:previousView(eventInfoView);break;
                    case 1:nextView(eventPhotosView);break;
                    case 2:nextView(eventVolunteersView);break;
                    default:break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
               switch (tab.getPosition())
                {
                    case 0:
                        viewFlipper.addView(eventInfoView);
                        /*nextView(eventPhotosView);
                        nextView(eventVolunteersView);*/
                        //nextView(eventInfoView);
                       break;
                    case 1:
                        viewFlipper.addView(eventPhotosView);
                        previousView(eventInfoView);
                        nextView(eventVolunteersView);
                        //nextView(eventPhotosView);break;
                    case 2:
                        viewFlipper.addView(eventVolunteersView);
                        previousView(eventPhotosView);
                        nextView(eventInfoView);
                        //nextView(eventVolunteersView);break;
                    default:break;
                }

            }
        });

        //viewFlipper.setFlipInterval(2000);
        //viewFlipper.startFlipping();

    }

    public void previousView(View view)
    {
        viewFlipper.setInAnimation(this,R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this,R.anim.slide_out_left);
        viewFlipper.showPrevious();;
    }

    public void nextView(View view)
    {
        viewFlipper.setInAnimation(this,R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this,R.anim.slide_out_left);
        viewFlipper.showNext();
    }


    @Override
    public void initializeComponents() {
        toolbar=findViewById(R.id.events_toolbar);
        viewFlipper=findViewById(R.id.view_flipper);
        tabLayout=findViewById(R.id.tabLayout);
        tabEventInfoItem=findViewById(R.id.tabItemInfo);
        tabEventPhotosItem=findViewById(R.id.tabItemPhotos);
        tabEventVolunteersItem=findViewById(R.id.tabItemVolunteers);

    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.tabItemInfo)
              nextView(eventInfoView);
        else if(v.getId()==R.id.tabItemPhotos)
            nextView(eventPhotosView);
            else if(v.getId()==R.id.tabItemVolunteers)
                nextView(eventVolunteersView);

    }
    private void setUpActionBar(int rid)
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_rankings,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_rankings,menu);
        return true;
    }


}
