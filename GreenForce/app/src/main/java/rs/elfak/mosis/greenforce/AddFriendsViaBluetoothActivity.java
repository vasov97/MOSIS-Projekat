package rs.elfak.mosis.greenforce;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BlendMode;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class AddFriendsViaBluetoothActivity extends AppCompatActivity implements View.OnClickListener,IComponentInitializer {

    Toolbar toolbar;
    FloatingActionButton refreshFAB;
    BluetoothAdapter bluetoothAdapter;
    Switch bluetoothSwitch;
    Intent enablingInent;
    ListView pairedDevice;
    ArrayList<String> devices = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    int requestCode;
    boolean isToogleOn;
    private Object AddFriendsViaBluetoothActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends_via_bluetooth);
        initializeComponents();


        /*IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);*/

        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    enableBluetooth();
                    Set<BluetoothDevice> nearbyDevices = bluetoothAdapter.getBondedDevices();
                    String[] strings=new String[nearbyDevices.size()];
                    int index=0;
                    if(nearbyDevices.size()>0)
                    {
                        for(BluetoothDevice device:nearbyDevices)
                        {
                            strings[index]=device.getName();
                            index++;
                            //Log.d("b", nearbyDevices.toString());
                            // Toast.makeText(device.getName().toString(),Toast.LENGTH_SHORT).show();

                        }
                       /*arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.nearby_device,R.id.textViewDevice,strings);
                        pairedDevice.setAdapter(arrayAdapter);*/
                        MyListViewAdapter adapter=new MyListViewAdapter(getApplicationContext(),strings,R.id.textViewDevice,R.drawable.add_friends_icon,R.layout.nearby_device);
                        pairedDevice.setAdapter(adapter);
                       /* MyListViewAdapter myListViewAdapter = new MyListViewAdapter(getApplicationContext(),strings);
                        pairedDevice.setAdapter(myListViewAdapter);
                        myListViewAdapter.addAll(strings);*/
                    }
                }
                else disableBluetooth();
            }
        });

    }
    private void disableBluetooth()
    {
        bluetoothAdapter.disable();
        //arrayAdapter.clear();
    }
   private final BroadcastReceiver myReceiver=new BroadcastReceiver()
   {
       @Override
       public void onReceive(Context context, Intent intent) {
           String action=intent.getAction();
           if(BluetoothDevice.ACTION_FOUND.equals(action))
           {
               BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
               devices.add(device.getName());
               //pairedDevice.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,devices));

           }
       }
   };
    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();


        // Don't forget to unregister the ACTION_FOUND receiver.
        //unregisterReceiver(receiver);
    }

    @Override
    public void initializeComponents()
    {
        toolbar = findViewById(R.id.bluetoothToolbar);
        refreshFAB = findViewById(R.id.fabRefresh);

        isToogleOn = false;
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        getSupportActionBar().setTitle("Bluetooth");
        refreshFAB.setOnClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver,filter);
        bluetoothSwitch = findViewById(R.id.switchBluetooth);
        bluetoothSwitch.setOnClickListener(this);

        requestCode=1;

        enablingInent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        pairedDevice=findViewById(R.id.listViewDevic);
        //pairedDevice=findViewById(R.id.listViewNearbyDevice);
    }

    @Override
    public void onClick(View v)
    {

        /*if(v.getId()==R.id.switchBluetooth)
        {
            isToogleOn=!isToogleOn;
            if(isToogleOn)
                enableBluetooth();
            else
                disableBluetooth(arrayAdapter);
        }*/


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

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        if(requestCode==this.requestCode)
        {
            if(resultCode==RESULT_OK)
                Toast.makeText(getApplicationContext(),"Bluetooth enabled",Toast.LENGTH_SHORT).show();
            else if(resultCode==RESULT_CANCELED)
                Toast.makeText(getApplicationContext(),"Bluetooth enabling canceling",Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}