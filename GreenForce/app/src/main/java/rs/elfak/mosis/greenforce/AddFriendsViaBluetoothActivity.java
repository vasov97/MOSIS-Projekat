package rs.elfak.mosis.greenforce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class AddFriendsViaBluetoothActivity extends AppCompatActivity implements View.OnClickListener,IComponentInitializer {

    Toolbar toolbar;
    FloatingActionButton refreshFAB;
    ListView pairedDevice;

    int requestCode;
    BluetoothAdapter bluetoothAdapter;
    BluetoothConnectionService bluetoothConnectionService;
    BluetoothDevice bluetoothDevice;
    Switch bluetoothSwitch;
    Intent enablingIntent;
    Set<BluetoothDevice> nearbyDevices;
    ArrayList<BluetoothDevice> listNearbyDevices;
    MyListViewAdapter adapter;

    UUID myUUID;
    String receivedUser;
    boolean isToggleOn;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_via_bluetooth);
        initializeComponents();
        setUpActionBar(R.string.bluetooth);


        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isToggleOn=isChecked;
                enableDisableBluetooth();
            }
        });
        checkIfBluetoothIsEnabled();

        pairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }
    private final BroadcastReceiver myReceiver=new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,bluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("AddFriendsViaBT","receiver: state off");
                        Toast.makeText(getApplicationContext(),"Bluetooth disabled",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("AddFriendsViaBT","receiver: state turning off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("AddFriendsViaBT","receiver: state on");
                        //enableDiscoverable();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("AddFriendsViaBT","receiver: state turning on");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver myReceiver2=new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int state=intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,bluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d("AddFriendsViaBT","Discoverability enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("AddFriendsViaBT","Able to receive conncetion");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("AddFriendsViaBT","Not able to receive conncetion");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("AddFriendsViaBT","Connected.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("AddFriendsViaBT","Connecting...");
                        break;
                }
            }
        }
    };
    private void setUpActionBar(int rid) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
    }

    private void sendFriendRequest()
    {
        String name=MyUserManager.getInstance().getUser().getName();
        String surname=MyUserManager.getInstance().getUser().getSurname();
        String username=MyUserManager.getInstance().getUser().getUsername();
        String id=MyUserManager.getInstance().getCurrentUserUid();
        String myData=name+" "+surname+" "+username+" "+id;
    }
    private void checkIfBluetoothIsEnabled() {
        if(bluetoothAdapter.isEnabled())
        {
            isToggleOn =true;
            bluetoothSwitch.setChecked(true);
            //getNearbyDevices();
        }
    }
    private void getNearbyDevices() {
        nearbyDevices = bluetoothAdapter.getBondedDevices();
        listNearbyDevices.clear();
        ArrayList<String> strings=new ArrayList<String>();
        if(nearbyDevices.size()>0)
        {
            for(BluetoothDevice device:nearbyDevices) {
                strings.add(device.getName());
                listNearbyDevices.add(device);
            }

            adapter=new MyListViewAdapter(getApplicationContext(),strings,R.id.textViewDevice,R.drawable.add_friends_icon,R.layout.nearby_device);
            pairedDevice.setAdapter(adapter);
        }
    }
    private void enableDisableBluetooth() {
        if(isToggleOn)
            enableBluetooth();
        else disableBluetooth();
    }

    private void disableBluetooth() {
        if(bluetoothAdapter.isEnabled()){
            if(adapter!=null){
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
           // isToggleOn =false;
            bluetoothAdapter.disable();
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(myReceiver,filter);
        }
    }
    private void enableBluetooth() {
        if(bluetoothAdapter==null)
            Toast.makeText(getApplicationContext(),"Bluetooth isn't supported on this device",Toast.LENGTH_LONG).show();
        else
        {
            if(!bluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivity(enableBtIntent);
                startActivityForResult(enableBtIntent, this.requestCode);
//                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//                registerReceiver(myReceiver,filter);
            }
        }
    }
    private void enableDiscoverable(){
        Intent discoverableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);
        //IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
       // registerReceiver(myReceiver2,filter);
    }
    private void createServer(){

    }
    public void startBTConnection(BluetoothDevice device,UUID uuid){
        Log.d("AddFriendsViewBT","startBTConnection: Initializing BT Connection");
        bluetoothConnectionService.startClient(device,uuid);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }
    @Override
    public void initializeComponents() {
        toolbar = findViewById(R.id.bluetoothToolbar);
        refreshFAB = findViewById(R.id.fabRefresh);
        pairedDevice=findViewById(R.id.listViewDevic);
        bluetoothSwitch = findViewById(R.id.switchBluetooth);

        myUUID=UUID.fromString(getResources().getString(R.string.app_uuid));
        isToggleOn = false;
        requestCode=1;

        refreshFAB.setOnClickListener(this);
        bluetoothSwitch.setOnClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();

        enablingIntent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        listNearbyDevices=new ArrayList<BluetoothDevice>();
        //pairedDevice=findViewById(R.id.listViewNearbyDevice);
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fabRefresh)
            enableDisableBluetooth();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        if(requestCode==this.requestCode)
        {
            if(resultCode==RESULT_OK)
            {
                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(myReceiver,filter);
                Toast.makeText(getApplicationContext(),"Bluetooth enabled",Toast.LENGTH_SHORT).show();
                //getNearbyDevices();
            }
            else if(resultCode==RESULT_CANCELED)
                Toast.makeText(getApplicationContext(),"Bluetooth enabling canceling",Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}