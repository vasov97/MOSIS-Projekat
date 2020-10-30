package rs.elfak.mosis.greenforce;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener,IComponentInitializer {

    CardView profileCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initializeComponents();
        profileCard.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.profile_card)
        {
            Intent i=new Intent(this,MyProfileActivity.class);
            startActivity(i);
        }
    }


    @Override
    public void initializeComponents() {
        profileCard=findViewById(R.id.profile_card);

    }
}