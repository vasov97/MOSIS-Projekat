package rs.elfak.mosis.greenforce.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import java.util.HashMap;

import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.fragments.FragmentRegistrationData;
import rs.elfak.mosis.greenforce.fragments.FragmentRegistrationImage;

public class RegisterActivity extends AppCompatActivity implements FragmentRegistrationImage.FragmentRegistrationImageListener, FragmentRegistrationData.FragmentRegistrationDataListener{

    private FragmentRegistrationImage imageFragment;
    private FragmentRegistrationData dataFragment;
    private HashMap<String,String> data;
    private Bitmap imageBitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dataFragment=new FragmentRegistrationData();
        imageFragment=new FragmentRegistrationImage();
        setUpFragment(R.id.RegisterActivityFragmentContainer,dataFragment,true);






    }

    private void setUpFragment(int container, Fragment fragment,Boolean addToBackStack) {
        if(addToBackStack)
          getSupportFragmentManager().beginTransaction().replace(container,fragment).addToBackStack(null).commit();
        else
            getSupportFragmentManager().beginTransaction().replace(container,fragment).commit();
    }

    private void createUserProfile()
    {
        MyUserManager.getInstance().createUserProfile(data,imageBitmap,this);


    }


    @Override
    public void onInputDataSent(HashMap<String,String> userData) {
        data=userData;
        setUpFragment(R.id.RegisterActivityFragmentContainer,imageFragment,false);
    }

    @Override
    public void onInputImageSent(Bitmap imageBitmap) {
        this.imageBitmap=imageBitmap;
        createUserProfile();


    }

    @Override
    public void finish() {
        super.finish();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void restart() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
