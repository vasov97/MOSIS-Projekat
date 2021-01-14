package rs.elfak.mosis.greenforce.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.managers.MyUserManager;

public class EnableCustomServicesActivity extends AppCompatActivity implements IComponentInitializer {

    Switch customServiceSwitch;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_custom_services_acitivity);
        initializeComponents();
        setUpActionBar(R.string.services);
        customServiceSwitch.setChecked(MyUserManager.getInstance().checkIfLocationServiceActive());

        customServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    MyUserManager.getInstance().stopLocationService();
                    MyUserManager.getInstance().setEnableService(false);
                }
                else{
                    MyUserManager.getInstance().setEnableService(true);
                    MyUserManager.getInstance().startLocationService();
                }
            }
        });
    }


    private void setUpActionBar(int rid) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
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
    public void onBackPressed()
    {
        //Intent i=new Intent(this,HomePageActivity.class);
        //startActivity(i);
        finish();
        super.onBackPressed();
    }

    @Override
    public void initializeComponents()
    {
      customServiceSwitch=findViewById(R.id.switchCustomService);
      toolbar =findViewById(R.id.services_toolbar);
    }
}