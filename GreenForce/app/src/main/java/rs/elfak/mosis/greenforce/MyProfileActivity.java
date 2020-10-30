package rs.elfak.mosis.greenforce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MyProfileActivity extends AppCompatActivity implements IComponentInitializer {

    FragmentMyProfileMain myProfileMainFragment;
    FragmentMyProfileEdit myProfileEditFragment;
    Toolbar toolbar;
    boolean edit=false;
    TextView userName;
    TextView fullName;
    ImageView myProfileImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        initializeComponents();
        setUpActionBar(String.valueOf(R.string.my_profile));
        setUpUserData();
        myProfileMainFragment=new FragmentMyProfileMain();
        myProfileEditFragment=new FragmentMyProfileEdit();
        setUpFragment(R.id.MyProfileActivityFragmentContainer,myProfileMainFragment,true);


    }

    private void setUpActionBar(String msg) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(msg);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);

    }

    private void setUpUserData(){
        UserData user=MyUserManager.getInstance().getUser();
        fullName.setText(user.getName()+" "+user.getSurname());
        userName.setText(user.getUsername());
        myProfileImage.setImageBitmap(user.getUserImage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        selectMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         super.onPrepareOptionsMenu(menu);
         selectMenu(menu);
         return true;
    }

    private void selectMenu(Menu menu) {
        if(edit)
            getMenuInflater().inflate(R.menu.menu_myprofile_edit,menu);
        else
            getMenuInflater().inflate(R.menu.menu_myprofile_main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(item.getItemId()==R.id.menu_edit_profile)
        {
            edit=true;
            getSupportActionBar().setTitle(R.string.EditProfile);
            invalidateOptionsMenu();
            setUpFragment(R.id.MyProfileActivityFragmentContainer,myProfileEditFragment,true);

        }
        else if(item.getItemId()==R.id.menu_save_profile_changes)
        {
            edit=false;
            MyUserManager.getInstance().editProfileChanges(myProfileEditFragment.getUserData());
            getSupportActionBar().setTitle(R.string.my_profile);
            invalidateOptionsMenu();
            setUpFragment(R.id.MyProfileActivityFragmentContainer,myProfileMainFragment,true);
        }



        return super.onOptionsItemSelected(item);
    }
    @Override
    public void initializeComponents() {
        toolbar = findViewById(R.id.myProfileToolbar);

        myProfileImage=findViewById(R.id.my_profile_image);
        userName=findViewById(R.id.my_username);
        fullName=findViewById(R.id.account_full_name);

    }

    private void setUpFragment(int container, Fragment fragment, Boolean addToBackStack) {
        if(addToBackStack)
            getSupportFragmentManager().beginTransaction().replace(container,fragment).addToBackStack(null).commit();
        else
            getSupportFragmentManager().beginTransaction().replace(container,fragment).commit();
    }


}
