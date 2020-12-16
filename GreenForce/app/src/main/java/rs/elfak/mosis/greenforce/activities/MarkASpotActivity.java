package rs.elfak.mosis.greenforce.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.google.android.material.tabs.TabLayout;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;

public class MarkASpotActivity extends AppCompatActivity implements IComponentInitializer
{
    private Toolbar toolbar;
    private ViewFlipper viewFlipper;
    private TabLayout tabLayout;
    private CheckBox waterPollution,landPollution,reforestation,other;
    private EditText eventPointsSum,description;
    private View markASpotInfoView;
    private View markASportAboutView;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_a_spot_acitvity);
        setUpActionBar(R.string.mark_a_spot);

        markASpotInfoView = getLayoutInflater().inflate(R.layout.activity_mark_a_spot_acitvity,viewFlipper,false);
        markASportAboutView = getLayoutInflater().inflate(R.layout.activity_mark_a_spot_acitvity,viewFlipper,false);


        viewFlipper.addView(markASpotInfoView);
        viewFlipper.addView(markASportAboutView);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition())
                {
                    case 0:viewFlipper.setDisplayedChild(0);break;
                    case 1:viewFlipper.setDisplayedChild(1);break;
                    default:break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

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
    public void initializeComponents() {
        toolbar=findViewById(R.id.events_toolbar_mark_a_spot);
        viewFlipper=findViewById(R.id.view_flipper_mark_a_spot);
    }
}