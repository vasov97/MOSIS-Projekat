package rs.elfak.mosis.greenforce.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetFriendsCallback;
import rs.elfak.mosis.greenforce.models.UserData;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RankingsActivity extends AppCompatActivity implements IComponentInitializer
{

    Toolbar toolbar;
    RadioGroup radioGroup;
    RadioButton radioButtonAll;
    RadioButton radioButtonFriends;
    ListView rankingsListView;
    TextView currentRank;
    TextView currentPoints;
    ArrayList<UserData> rankedFriends;
    ArrayList<UserData> rankedUsers;

    public class GetFriendsCallback implements IGetFriendsCallback{

        @Override
        public void onFriendsReceived(ArrayList<UserData> myFriends) {
            if(myFriends!=null){
                rankedFriends=myFriends;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings);
        initializeComponents();
        setUpActionBar(R.string.rankings);
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

    }


}