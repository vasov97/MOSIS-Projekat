package rs.elfak.mosis.greenforce.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Layout;
import android.view.Menu;
import android.view.View;
import android.widget.ViewFlipper;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.RecyclerAdapter;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IViewFlipperHandler;
import rs.elfak.mosis.greenforce.managers.MyUserManager;

public class EventActivity extends AppCompatActivity implements IComponentInitializer, IViewFlipperHandler
{
     private Toolbar toolbar;
     private ViewFlipper viewFlipper;
     private TabLayout tabLayout;
     private View eventInfoView;
     private View eventPhotosView;
     private View eventVolunteersView;
     private RecyclerView recyclerView;
     private RecyclerView.LayoutManager layoutManager;
     Bitmap[] images = new Bitmap[10];
     private RecyclerAdapter recyclerAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_layout);
        initializeComponents();
        setUpActionBar(R.string.event);
        for(int i=0;i<10;i++)
            images[i]= MyUserManager.getInstance().getUser().getUserImage();
        recyclerAdapter=new RecyclerAdapter(images);
        recyclerView.setAdapter(recyclerAdapter);
       viewFlipperSetup();



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

               switch (tab.getPosition())
                {
                    case 0:viewFlipper.setDisplayedChild(0);break;
                    case 1:viewFlipper.setDisplayedChild(1);break;
                    case 2:viewFlipper.setDisplayedChild(2);break;
                    default:break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
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
        recyclerView=findViewById(R.id.event_photos_recycler);
        layoutManager=new GridLayoutManager(this,2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

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
