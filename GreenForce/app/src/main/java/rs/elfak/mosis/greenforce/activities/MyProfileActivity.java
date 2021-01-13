package rs.elfak.mosis.greenforce.activities;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;

import rs.elfak.mosis.greenforce.interfaces.IGetCurrentRankCallback;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.UserData;
import rs.elfak.mosis.greenforce.fragments.FragmentMyProfileEdit;
import rs.elfak.mosis.greenforce.fragments.FragmentMyProfileMain;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;

public class MyProfileActivity extends AppCompatActivity implements IComponentInitializer {

    private static final int PICK_IMAGE = 1;
    private static final String MAIN_TAG="MainFragment";
    private static final String EDIT_TAG="EditFragment";
    FragmentMyProfileMain myProfileMainFragment;
    FragmentMyProfileEdit myProfileEditFragment;
    Toolbar toolbar;
    boolean edit=false,visitor=false;
    TextView userName;
    TextView fullName;
    TextView changePhoto;
    ImageView myProfileImage;
    Bitmap imageBitmap;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        initializeComponents();
        setUpActionBar(R.string.my_profile);
        String visit = getIntent().getStringExtra("Visit");
        if(visit.equals("Visit"))
            visitor=true;
        choseUserToDisplay();
        Bundle bundle=new Bundle();
        bundle.putString("Visit",visit);
        myProfileMainFragment=new FragmentMyProfileMain();
        myProfileMainFragment.setArguments(bundle);
        myProfileEditFragment=new FragmentMyProfileEdit();
        setUpFragment(R.id.MyProfileActivityFragmentContainer,new FragmentMyProfileMain(),true,MAIN_TAG);
    }

    private void choseUserToDisplay() {
        UserData displayUser;
        if(visitor){
            displayUser=MyUserManager.getInstance().getVisitProfile();
        }
        else{
            displayUser=MyUserManager.getInstance().getUser();
        }
        setUpUserData(displayUser);
    }

    private void setUpActionBar(int rid) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


    }

    private void setUpUserData(UserData user){
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
        else{
            if(visitor==false)
                getMenuInflater().inflate(R.menu.menu_myprofile_main,menu);
            else
                getMenuInflater().inflate(R.menu.menu_main,menu);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_edit_profile)
        {
            setUpEditToolbar();
            setUpFragment(R.id.MyProfileActivityFragmentContainer,myProfileEditFragment,true,EDIT_TAG);

        }
        else if(item.getItemId()==R.id.menu_save_profile_changes)
        {

            MyUserManager.getInstance().editProfileChanges(myProfileEditFragment.getUserData());
            if(imageBitmap!=null){
                MyUserManager.getInstance().getUser().setUserImage(imageBitmap);
                MyUserManager.getInstance().saveUserImage();
            }
            setUpAccountToolbar();
            setUpUserData(MyUserManager.getInstance().getUser());
            setUpFragment(R.id.MyProfileActivityFragmentContainer,new FragmentMyProfileMain(),true,MAIN_TAG);
        }


        return super.onOptionsItemSelected(item);
    }
    @Override
    public void initializeComponents() {
        toolbar = findViewById(R.id.myProfileToolbar);
        changePhoto=findViewById(R.id.changePhoto);
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choseImage();
            }
        });
        myProfileImage=findViewById(R.id.my_profile_image);
        userName=findViewById(R.id.my_username);
        fullName=findViewById(R.id.account_full_name);
    }

    private void choseImage() {
        Intent gallery = new Intent();
        gallery.setType("image/*");
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gallery, "Select image"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                myProfileImage.setImageBitmap(imageBitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpFragment(int container, Fragment fragment, Boolean addToBackStack,String tag) {

        if (getSupportFragmentManager().findFragmentByTag ( tag ) == null) {
                if (addToBackStack)
                    getSupportFragmentManager().beginTransaction().replace(container, fragment).addToBackStack(tag).commit();
                else
                    getSupportFragmentManager().beginTransaction().replace(container, fragment).commit();

        }

    }

    public void setUpAccountToolbar(){
        edit=false;
        getSupportActionBar().setTitle(R.string.my_profile);
        invalidateOptionsMenu();
    }

    public void setUpEditToolbar(){
        edit=true;
        getSupportActionBar().setTitle(R.string.EditProfile);
        invalidateOptionsMenu();
    }

    public void setInvisible(){
        fullName.setVisibility(TextView.INVISIBLE);
        userName.setVisibility(TextView.INVISIBLE);
        changePhoto.setVisibility(TextView.VISIBLE);
    }

    public void setVisible(){
        changePhoto.setVisibility(View.INVISIBLE);
        fullName.setVisibility(TextView.VISIBLE);
        userName.setVisibility(TextView.VISIBLE);
    }

    public boolean getVisitor(){return visitor;}
}
