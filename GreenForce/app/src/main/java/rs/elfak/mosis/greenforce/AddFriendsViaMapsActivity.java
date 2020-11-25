package rs.elfak.mosis.greenforce;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.maps.MapView;

public class AddFriendsViaMapsActivity extends AppCompatActivity implements IComponentInitializer
{

    Spinner addFriendsSpinner;
    EditText radius;
    MapView addFriendsMap;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_via_maps);
        initializeComponents();
        setUpActionBar(R.string.maps);
    }

    @Override
    public void initializeComponents()
    {
         addFriendsSpinner=findViewById(R.id.spinner);
         radius=findViewById(R.id.radius);
         addFriendsMap=findViewById(R.id.addFriendsMapsView);
         toolbar=findViewById(R.id.addFriendsViaMapToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_add_friends_map,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_add_friends_map,menu);
        return true;
    }
    private void setUpActionBar(int rid)
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
    }
}