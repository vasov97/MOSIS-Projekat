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
    CardView friendsListCard;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initializeComponents();
        profileCard.setOnClickListener(this);
        friendsListCard.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {
        if(v.getId()==R.id.profile_card)
        {
            Intent i=new Intent(this,MyProfileActivity.class);
            i.putExtra("Visit", "MyProfile");
            startActivity(i);
        }
        else if(v.getId()==R.id.friends_card)
        {
            Intent i=new Intent(this,MyFriendsActivity.class);
            startActivity(i);
        }
    }


    @Override
    public void initializeComponents()
    {
        profileCard=findViewById(R.id.profile_card);
        friendsListCard = findViewById(R.id.friends_card);
;    }
}
