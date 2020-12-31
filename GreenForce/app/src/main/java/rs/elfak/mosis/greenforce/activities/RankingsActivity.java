package rs.elfak.mosis.greenforce.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.NotificationsAdapter;
import rs.elfak.mosis.greenforce.adapters.RankingsAdapter;
import rs.elfak.mosis.greenforce.enums.DataRetriveAction;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetFriendsCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetUsersCallback;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.UserData;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Collections;

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
    GetFriendsCallback getFriendsCallback;
    GetUsersCallback getUsersCallback;
    RankingsAdapter rankingsAdapter;
    ProgressDialog progressDialog;

    public class GetFriendsCallback implements IGetFriendsCallback{

        @Override
        public void onFriendsReceived(ArrayList<UserData> myFriends) {
                rankedFriends=new ArrayList<UserData>();
                for(UserData emptyUser : myFriends){
                    if(rankedUsers.contains(emptyUser)){
                        UserData fullUser=getUserFromList(rankedUsers,emptyUser.getUserUUID());
                        rankedFriends.add(fullUser);
                    }
                }
                sortArray(rankedFriends);
                progressDialog.dismiss();
                if(radioButtonFriends.isChecked())
                    displayRankedUsers(rankedFriends);
        }
    }



    public class GetUsersCallback implements IGetUsersCallback
    {

        @Override
        public void onUsersReceived(ArrayList<UserData> allUsers) {
                rankedUsers=allUsers;
                MyUserManager.getInstance().getFriends(MyUserManager.getInstance().getCurrentUserUid(),getFriendsCallback,DataRetriveAction.GET_RANKED_USERS,false);
                sortArray(rankedUsers);
                if(radioButtonAll.isChecked())
                    displayRankedUsers(allUsers);
        }

        @Override
        public void onUserReceived(UserData user) {
            displayMyRank(user);

        }
    }

    @Override
    public void initializeComponents()
    {
        radioGroup=findViewById(R.id.radio_group);
        radioButtonAll=findViewById(R.id.radio_button_all);
        radioButtonFriends=findViewById(R.id.radio_button_friends);
        rankingsListView=findViewById(R.id.rankings_listview);
        currentPoints=findViewById(R.id.your_curent_points);
        currentRank=findViewById(R.id.your_current_rank);
        toolbar=findViewById(R.id.rankings_toolbar);
        radioButtonAll.setChecked(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings);
        initializeComponents();
        setUpActionBar(R.string.rankings);

        getFriendsCallback=new GetFriendsCallback();
        getUsersCallback=new GetUsersCallback();
        MyUserManager.getInstance().getAllUsers(getUsersCallback, DataRetriveAction.GET_RANKED_USERS);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                radioButtonCheckedChanged(checkedId);
            }
        });
        loadUsers();
    }

    private void radioButtonCheckedChanged(int checkedId) {
        if(rankedFriends!=null && rankedUsers!=null){
            if(checkedId==R.id.radio_button_all)
                displayRankedUsers(rankedUsers);
            else
                displayRankedUsers(rankedFriends);
        }
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

    private void displayRankedUsers(ArrayList<UserData> rankedUsers)
    {

        if(rankedUsers!=null)
        {
            rankingsAdapter =new RankingsAdapter(this,rankedUsers);
            rankingsListView.setAdapter(rankingsAdapter);
            //showRank();
        }

        displayMyRank(MyUserManager.getInstance().getUser());


    }

    private void showRank()
    {
        if(radioButtonAll.isSelected())
            displayRankedUsers(rankedUsers);
        else if(radioButtonFriends.isSelected())
            displayRankedUsers(rankedFriends);
    }

    private void displayMyRank(UserData user) {
         currentRank.setText("Your rank: "+user.getCurrentRank()+"");
         currentPoints.setText("Points: "+user.getPoints()+"");
    }
    private void setRanks(ArrayList<UserData> userList){
        long rank=1;
        for(UserData user:userList){
            user.setCurrentRank(rank);//mora da sacekam da se napuni fon malo
            if(user.getUserUUID().equals(MyUserManager.getInstance().getCurrentUserUid())){
                 MyUserManager.getInstance().getUser().setCurrentRank(rank);
            }
             rank++;
        }
    }
    private void sortArray(ArrayList<UserData> userList){
        Collections.sort(userList,UserData.UserPointsComparatorDesc);
        setRanks(userList);
    }

    private UserData getUserFromList(ArrayList<UserData> allUsers, String uuid) {
        for(UserData u: allUsers){
            if(u.getUserUUID().equals(uuid))
                return u;
        }
        return null;
    }
    private void loadUsers()
    {
        progressDialog=new ProgressDialog(RankingsActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        if(id==R.id.rankings_refresh){
            MyUserManager.getInstance().getAllUsers(getUsersCallback,DataRetriveAction.GET_RANKED_USERS);
            progressDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

}