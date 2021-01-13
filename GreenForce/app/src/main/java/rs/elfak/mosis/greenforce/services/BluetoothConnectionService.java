package rs.elfak.mosis.greenforce.services;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import rs.elfak.mosis.greenforce.activities.AddFriendsViaBluetoothActivity;

public class BluetoothConnectionService {
    private static final String TAG="BluetoothConnectionServ";
    private static final String appName="GreenForce";
    private static final UUID myUUID=UUID.fromString("a3d56928-fe48-4ad7-83d4-bf0d72ec9538");
    private BluetoothAdapter bluetoothAdapter;
    private Context context;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private BluetoothDevice bluetoothDevice;
    private UUID deviceUUID;
    private boolean isServer=true;
    public BluetoothConnectionService(Context context){
        this.context=context;
        this.bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        start();
    }

   private class AcceptThread extends Thread{
        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp=null;

            try {
                tmp=bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,myUUID);
                Log.d(TAG,"AcceptThread: Setting up server using: "+myUUID);
            } catch (IOException e) {
                Log.e(TAG,"AcceptThread: Failed to create RFCOM using UUID: "+myUUID);
            }
            bluetoothServerSocket=tmp;
        }
        public void run(){
            Log.d(TAG,"run: AcceptThread running");
            BluetoothSocket socket=null;
            //while (true){}
            try {
                Log.d(TAG,"run: RFCOM server socket start.....");
                socket=bluetoothServerSocket.accept();
                Log.d(TAG,"run: RFCOM server socket accepted connection");
            } catch (IOException e) {
                Log.e(TAG,"AcceptThread: IOException"+e.getMessage());
            }
            if(socket!=null){
                connected(socket,bluetoothDevice);
            }
            Log.d(TAG,"END AcceptThread");
            acceptThread.start();

        }
        public void cancel(){
            Log.d(TAG,"cancel: Canceling AcceptThread");
            try{
                bluetoothServerSocket.close();
            }catch(IOException e){
                Log.e(TAG,"cancel:AcceptThread IOException Canceling Server Socket Failed"+e.getMessage());
            }
        }
   }
   private class ConnectThread extends Thread {
        private BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device,UUID uuid){
            Log.d(TAG,"ConnectThread: started");
            bluetoothDevice=device;
            deviceUUID=uuid;
        }
        public void run(){
            BluetoothSocket tmp=null;
            Log.d(TAG,"RUN ConnectThread");
            try {
                Log.d(TAG,"ConnectThread: Trying to create RFCOM using UUID: "+deviceUUID);
                tmp=bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG,"ConnectThread: Could not create RFCOM "+e.getMessage());
            }
            socket=tmp;
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
                Log.d(TAG,"run: ConnectThread Connected");
                connected(socket,bluetoothDevice);
            } catch (IOException e) {
                try {
                    socket.close();
                    Log.d(TAG,"run: ConnectThread socket closed"+e.getMessage());
                } catch (IOException ioException) {
                    Log.e(TAG,"run: ConnectThread could not close the socket "+e.getMessage());
                }
                Log.e(TAG,"run: ConnectThread could not connect to UUID "+myUUID);
            }

        }

       public void cancel(){
           Log.d(TAG,"cancel: Canceling ConnectThread Client Socket");
           try{
               socket.close();
           }catch(IOException e){
               Log.e(TAG,"cancel:ConnectThread IOException Canceling Client Socket Failed"+e.getMessage());
           }
       }
   }



    public synchronized void start(){
        Log.d(TAG,"Start");
        if(connectThread!=null){
            connectThread.cancel();
            connectThread=null;
        }
        if(acceptThread==null){
            acceptThread=new AcceptThread();
            acceptThread.start();
        }
   }

   public void startClient(BluetoothDevice device,UUID uuid,boolean isServer){
        Log.d(TAG,"StartClient: started");
        this.isServer=isServer;
//       progressDialog=new ProgressDialog(context);
//       progressDialog.show();
//       progressDialog.setContentView(R.layout.progress_dialog);
//       progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//       progressDialog.setCancelable(false);
//       progressDialog.setCanceledOnTouchOutside(false);

       connectThread=new ConnectThread(device,uuid);
       connectThread.start();
   }

   private class ConnectedThread extends Thread
   {
       private final BluetoothSocket bluetoothSocket;
       private final InputStream inputStream;
       private final OutputStream outputStream;


       public ConnectedThread(BluetoothSocket bluetoothSocket)
       {
           Log.d(TAG,"Connected thread: Starting.");
           this.bluetoothSocket=bluetoothSocket;
           InputStream tmpIn=null;
           OutputStream tmpOut=null;
//           try{
//               progressDialog.dismiss();
//           }catch(NullPointerException e){
//                e.printStackTrace();
//           }


           try {
               tmpIn=bluetoothSocket.getInputStream();
               tmpOut=bluetoothSocket.getOutputStream();
           } catch (IOException e) {
               e.printStackTrace();
           }

           inputStream=tmpIn;
           outputStream=tmpOut;

       }
       public void run() {
           byte[] buffer= new byte[1024];
           int numBytes;
           while (true)
           {
               try {
                   numBytes = inputStream.read(buffer);
                   String incomingMessage = new String(buffer,0,numBytes);
                   Log.d(TAG,"InputStream"+incomingMessage);
                   Intent incomingMessageIntent=new Intent("incomingMessage");
                   incomingMessageIntent.putExtra("requestMessage",incomingMessage);
                   LocalBroadcastManager.getInstance(context).sendBroadcast(incomingMessageIntent);

                  // ((AddFriendsViaBluetoothActivity)context).displayRequest(incomingMessage);

               } catch (IOException e) {
                   Log.e(TAG,"Error reading inputstream"+e.getMessage());
                   break;
               }
           }
       }

       public void cancel()
       {
           try {
               bluetoothSocket.close();
           } catch (IOException e) {
               Log.e(TAG, "Could not close the connect socket" + e.getMessage());
           }
       }

       public void write(byte[] bytes)
       {
          String text=new String(bytes, Charset.defaultCharset());
          Log.d(TAG,"write: Writing to outputstream"+text);
           try {
               outputStream.write(bytes);
           } catch (IOException e) {
               Log.e(TAG,"Error writing to outputstream"+e.getMessage());
           }
       }
   }

    private void connected(BluetoothSocket socket, BluetoothDevice bluetoothDevice) {
        Log.d(TAG,"Connected: Starting.");
        connectedThread=new ConnectedThread(socket);
        connectedThread.start();
        if(!isServer){
            ((AddFriendsViaBluetoothActivity)context).sendFriendRequest();
            isServer=true;
        }


    }
    public void write(byte[] out)
    {
        Log.d(TAG,"write: Write called");
        connectedThread.write(out);
    }


    public synchronized void closeAllThreads(){
        if(acceptThread!=null)
            acceptThread.cancel();
        if(connectThread!=null)
            connectThread.cancel();
        if(connectedThread!=null)
            connectedThread.cancel();
    }





}
