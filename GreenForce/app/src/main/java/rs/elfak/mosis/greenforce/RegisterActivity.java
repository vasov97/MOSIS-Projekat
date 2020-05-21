package rs.elfak.mosis.greenforce;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements FragmentRegistrationImage.FragmentRegistrationImageListener,FragmentRegistrationData.FragmentRegistrationDataListener{

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
