package rs.elfak.mosis.greenforce.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.MyListViewAdapter;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.services.BluetoothConnectionService;

public class AddFriendsViaBluetoothActivity extends AppCompatActivity implements View.OnClickListener, IComponentInitializer
{

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
    boolean isToggleOn,isServer;

    Button btsnd,btnconnect;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_via_bluetooth);
        initializeComponents();
        setUpActionBar(R.string.bluetooth);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(myReceiverBondStateChanged,filter);//njemu 4 receiver

        LocalBroadcastManager.getInstance(this).registerReceiver(friendRequestReceiver,new IntentFilter("incomingMessage"));

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isToggleOn=isChecked;
                enableDisableBluetooth();
                //getPairedDevices();
                //discoverDevices();
            }
        });
        checkIfBluetoothIsEnabled();

        pairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //@RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
               bluetoothAdapter.cancelDiscovery();
                Log.d("AddFriendsViaBT","You clicked on a device");
                String deviceName = listNearbyDevices.get(position).getName();
                String deviceAddress = listNearbyDevices.get(position).getAddress();
                Log.d("AddFriendsViaBT","Device name:"+deviceName);
                Log.d("AddFriendsViaBT","Device address:"+deviceAddress);
                listNearbyDevices.get(position).createBond();
                bluetoothDevice=listNearbyDevices.get(position);
                startConnection();
            }
        });

        btnconnect=findViewById(R.id.btn_connect_via_bt);
        btsnd=findViewById(R.id.btn_send_fried_request);
        btnconnect.setOnClickListener(this);
        btsnd.setOnClickListener(this);
        bluetoothConnectionService=new BluetoothConnectionService(AddFriendsViaBluetoothActivity.this);

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

    private final BroadcastReceiver myReceiverForDiscovering = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            ArrayList<String> strings=new ArrayList<String>();
            Log.d("AddFriendsViaBT","onReceive: Action found");

            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listNearbyDevices.add(device);
                strings.add(device.getName());
                Log.d("AddFriendsViaBT","onReceive:"+device.getName()+":"+device.getAddress());


                adapter=new MyListViewAdapter(getApplicationContext(),strings,R.id.textViewDevice,R.drawable.add_friends_icon,R.layout.nearby_device);
                pairedDevice.setAdapter(adapter);
            }
        }
    };

    private final BroadcastReceiver myReceiverBondStateChanged = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState()==BluetoothDevice.BOND_BONDED)
                {
                    Log.d("AddFriendsViaBT","BRecv: State_Bonded");
                    bluetoothDevice=device;
                }
                else if(device.getBondState()==BluetoothDevice.BOND_BONDING)
                {
                    Log.d("AddFriendsViaBT","BRecv: State_Bonding");
                }
                else if(device.getBondState()==BluetoothDevice.BOND_NONE)
                {
                    Log.d("AddFriendsViaBT","BRecv: State_None");
                }
            }
        }
    };

    private final BroadcastReceiver friendRequestReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String text = intent.getStringExtra("requestMessage");
            displayRequest(text);
        }
    };
    private void setUpActionBar(int rid)
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
    }

    public void sendFriendRequest() {
        isServer=true;
        String username= MyUserManager.getInstance().getUser().getUsername();
        String id=MyUserManager.getInstance().getCurrentUserUid();
        String myData=username+"*"+id;
        byte[] bytes=myData.getBytes(Charset.defaultCharset());
        bluetoothConnectionService.write(bytes);
    }
    private void checkIfBluetoothIsEnabled()
    {
        if(bluetoothAdapter.isEnabled())
        {
            isToggleOn =true;
            bluetoothSwitch.setChecked(true);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPairedDevices()
    {
        nearbyDevices = bluetoothAdapter.getBondedDevices();
        listNearbyDevices.clear();
        ArrayList<String> strings=new ArrayList<String>();
        if(nearbyDevices.size()>0)
        {
            for(BluetoothDevice device:nearbyDevices)
            {
                strings.add(device.getName());
                listNearbyDevices.add(device);
            }

            adapter=new MyListViewAdapter(getApplicationContext(),strings,R.id.textViewDevice,R.drawable.add_friends_icon,R.layout.nearby_device);
            pairedDevice.setAdapter(adapter);
        }
    }
    private void enableDisableBluetooth()
    {
        if(isToggleOn)
            enableBluetooth();
        else disableBluetooth();
    }

    private void disableBluetooth()
    {
        if(bluetoothAdapter.isEnabled()){
            if(adapter!=null){
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
            bluetoothAdapter.disable();
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(myReceiver,filter);
        }
    }
    private void enableBluetooth()
    {
        if(bluetoothAdapter==null)
            Toast.makeText(getApplicationContext(),"Bluetooth isn't supported on this device",Toast.LENGTH_LONG).show();
        else
        {
            if(!bluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(enableBtIntent, this.requestCode);
            }
        }
    }
    private void enableDiscoverable()
    {
        Intent discoverableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);
        //IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
       // registerReceiver(myReceiver2,filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void discoverDevices()
    {
        Log.d("AddFriendsViaBT","Looking for unpaired devices");
        if(bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();
            Log.d("AddFriendsViaBT","Canceling discovery...");
            bluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(myReceiverForDiscovering,discoverDevicesIntent);
        }
        if(!bluetoothAdapter.isDiscovering())
        {
            bluetoothPermissions();
            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(myReceiverForDiscovering,discoverDevicesIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void bluetoothPermissions()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
           int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
           permissionCheck+=this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
           if(permissionCheck!=0)
               this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1001);
           else
               Log.d("AddFriendsViaBT","SDK version < LOLLIPOP");

        }
    }
    public void displayRequest(String data){
        final String[] strings = data.split("[*]");
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        MyUserManager.getInstance().addFriend(strings[1]);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Accept", dialogClickListener)
                .setNegativeButton("Decline", dialogClickListener)
                .setTitle("Friend request")
                .setMessage("User "+strings[0]+" would like to add you.")
                .create().show();




    }
    public void startConnection(){
             startBTConnection(bluetoothDevice,myUUID);
    }
    public void startBTConnection(BluetoothDevice device,UUID uuid)
    {
        Log.d("AddFriendsViewBT","startBTConnection: Initializing BT Connection");
        isServer=false;
        bluetoothConnectionService.startClient(device,uuid,isServer);
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
    protected void onDestroy()
    {
        try
        {
            unregisterReceiver(myReceiver);
            unregisterReceiver(myReceiver2);
            unregisterReceiver(myReceiverForDiscovering);
            unregisterReceiver(myReceiverBondStateChanged);
            unregisterReceiver(friendRequestReceiver);
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }


        super.onDestroy();
    }
    @Override
    public void initializeComponents()
    {
        toolbar = findViewById(R.id.bluetoothToolbar);
        refreshFAB = findViewById(R.id.fabRefresh);
        pairedDevice=findViewById(R.id.listViewDevic);
        bluetoothSwitch = findViewById(R.id.switchBluetooth);

        myUUID=UUID.fromString(getResources().getString(R.string.app_uuid));
        isToggleOn = false;
        isServer=true;
        requestCode=1;

        refreshFAB.setOnClickListener(this);
        bluetoothSwitch.setOnClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();

        enablingIntent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        listNearbyDevices=new ArrayList<BluetoothDevice>();

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fabRefresh)
        {
            if(bluetoothAdapter.isEnabled())
                 discoverDevices();
        }else if(v.getId()==R.id.btn_connect_via_bt){
            startConnection();
        }else if(v.getId()==R.id.btn_send_fried_request){
            sendFriendRequest();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        if(requestCode==this.requestCode)
        {
            if(resultCode==RESULT_OK)
            {
                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(myReceiver,filter);
                Toast.makeText(getApplicationContext(),"Bluetooth enabled",Toast.LENGTH_SHORT).show();

                discoverDevices();

            }
            else if(resultCode==RESULT_CANCELED)
                Toast.makeText(getApplicationContext(),"Bluetooth enabling canceling",Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}