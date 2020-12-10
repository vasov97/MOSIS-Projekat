package rs.elfak.mosis.greenforce.managers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.renderscript.Sampler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.activities.HomePageActivity;
import rs.elfak.mosis.greenforce.activities.LoginActivity;
import rs.elfak.mosis.greenforce.activities.RegisterActivity;
import rs.elfak.mosis.greenforce.dialogs.DisplayUserInformationOnMapDialog;
import rs.elfak.mosis.greenforce.enums.DataRetriveAction;
import rs.elfak.mosis.greenforce.interfaces.IGetNotifications;
import rs.elfak.mosis.greenforce.interfaces.IGetUsersCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetDataCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetFriendsCallback;
import rs.elfak.mosis.greenforce.models.FriendsRequestNotification;
import rs.elfak.mosis.greenforce.models.MyLatLong;
import rs.elfak.mosis.greenforce.models.UserData;
import rs.elfak.mosis.greenforce.services.LocationService;

public class MyUserManager {

    private final FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private final DatabaseReference databaseReference;
    private final DatabaseReference databaseFriendsReference;
    private final DatabaseReference databaseCoordinatesReference;
    private final DatabaseReference databaseNotificationsReference;
    private final StorageReference storageReference;
    private static final String USER = "user";
    private static final String FRIENDS = "friends";
    private static final String COORDINATES="coordinates";
    private static final String IMAGE = "profileImage/";
    private static final String NOTIFICATIONS="notifications";
    private static final String FRIEND_REQUESTS="friendRequests";
    private UserData userData,visitProfile;
    ArrayList<UserData> myFriends;


    private MyUserManager() {
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(USER);
        databaseFriendsReference=firebaseDatabase.getReference(FRIENDS);
        databaseCoordinatesReference=firebaseDatabase.getReference(COORDINATES);
        databaseNotificationsReference=firebaseDatabase.getReference(NOTIFICATIONS);
        storageReference = FirebaseStorage.getInstance().getReference();

    }




    private static class SingletonHolder{
        public static final MyUserManager instance=new MyUserManager();
    }
   public UserData getVisitProfile(){return visitProfile;}
   public void setVisitProfile(UserData visitProfile){this.visitProfile=visitProfile;}
   public UserData getUser(){return userData;}
   public String getCurrentUserUid(){return firebaseAuth.getCurrentUser().getUid();}
   public static MyUserManager getInstance(){
        return SingletonHolder.instance;
    }
   public ArrayList<UserData> getMyFriends(){return myFriends;}
   public void setMyFriends(ArrayList<UserData> friends){myFriends=friends;}
   public DatabaseReference getDatabaseCoordinatesReference(){return databaseCoordinatesReference;}
   public DatabaseReference getDatabaseFriendsReference(){return databaseFriendsReference;}

    public void loginUser(String emailText, String passwordText, final Activity enclosingActivity){
        firebaseAuth.signInWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(enclosingActivity,
                new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(!task.isSuccessful()) {
                            FirebaseAuthException e=(FirebaseAuthException)task.getException();
                            Toast.makeText(enclosingActivity,"Login failed", Toast.LENGTH_SHORT).show();
                            Log.e("LoginActivity","Login Registration",e);
                        }
                        else {
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            String uid=user.getUid();
                            getUserData(uid,DataRetriveAction.GET_SELF,0,null,null,enclosingActivity);
                            //getUserData(uid,enclosingActivity);
                            //enclosingActivity.startActivity(new Intent(enclosingActivity, HomePageActivity.class));
                        }
                    }
                }
        );
    }

    public void createUserProfile(final HashMap<String,String> params, final Bitmap imageBitmap, final Activity enclosingActivity) {

           firebaseAuth.createUserWithEmailAndPassword(params.get("email"),params.get("password")).addOnCompleteListener(enclosingActivity,
                    new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(!task.isSuccessful()) {
                                FirebaseAuthException e=(FirebaseAuthException)task.getException();
                                Log.e("RegistrationActivity","Failed Registration",e);
                                createUserProfileFailed(enclosingActivity);
                            }
                            else {
                                userData = new UserData(params.get("email"),params.get("name"),params.get("surname"),params.get("username"),params.get("phoneNumber"),imageBitmap);
                                createUserProfileSuccess(enclosingActivity);
                            }
                        }
                    });
    }

    private void createUserProfileSuccess(final Activity enclosingActivity) {
        writeToDatabase();
        saveUserImage();
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(Toast.LENGTH_SHORT);
                    enclosingActivity.finish();
                    enclosingActivity.startActivity(new Intent(enclosingActivity, LoginActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Toast.makeText(enclosingActivity, R.string.registrationCompleted,Toast.LENGTH_SHORT).show();
        thread.start();
    }

    public void saveUserImage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        userData.getUserImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);

        String uid = firebaseAuth.getCurrentUser().getUid();
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImage")
                .child(uid + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        getDownloadUrl(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("UploadPhoto","onFailure",e.getCause());
                    }
                });
    }

    private void getDownloadUrl(StorageReference reference) {
       reference.getDownloadUrl()
               .addOnSuccessListener(new OnSuccessListener<Uri>() {
                   @Override
                   public void onSuccess(Uri uri) {
                       Log.d("UploadPhoto","onSuccess"+ uri);
                       setUserProfileUrl(uri);
                   }
               });
    }

    private void setUserProfileUrl(Uri uri) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri).build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("UploadPhoto","onSuccess");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Profile image failed","Profile image failed",e.getCause());
            }

        });
    }

    private void createUserProfileFailed(final Activity enclosingActivity) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(Toast.LENGTH_SHORT);
                    ((RegisterActivity)enclosingActivity).restart();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Toast.makeText(enclosingActivity,R.string.registrationFailed,Toast.LENGTH_SHORT).show();
        thread.start();
    }

    public void recoverPassword(String email, final Activity enclosingActivity) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful()) {
                            Toast.makeText(enclosingActivity,R.string.emailSent,Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(enclosingActivity,R.string.emailFailed,Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(enclosingActivity,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean validateInfo(ArrayList<String> stringsToCheck, ArrayList<EditText> errorHolders) {
        boolean valid=true;
        for(int i=0;i<stringsToCheck.size();i++)
        {
            if(stringsToCheck.get(i).isEmpty()){
                errorHolders.get(i).setError("Please enter "+errorHolders.get(i).getHint());
                valid=false;
            }
        }
        return valid;
    }

    private void writeToDatabase() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        databaseReference.child(uid).setValue(userData);
    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    public void editProfileChanges(HashMap<String,String> userData) {
        this.userData.setEmail(userData.get("email"));
        this.userData.setName(userData.get("name"));
        this.userData.setSurname(userData.get("surname"));
        this.userData.setPhoneNumber(userData.get("phoneNumber"));
        saveProfileChangesToDatabase(userData);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void saveProfileChangesToDatabase(final HashMap<String, String> userData) {
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for(String key:userData.keySet())
                {
                    databaseReference.child(uid).child(key).setValue(userData.get(key));
                }
            }
        });
    }

    public void updatePassword(String oldPassword, final String newPassword, final Activity myActivity) {
        final FirebaseUser user = firebaseAuth.getCurrentUser();


        AuthCredential credential = EmailAuthProvider
                .getCredential(userData.getEmail(), oldPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(myActivity,"Password updated",Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(myActivity,"Update not successful",Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(myActivity,"Authentication failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getFriends(String uid, final IGetFriendsCallback callback)
    {
        databaseFriendsReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<UserData> friends=new ArrayList<UserData>();
                if(dataSnapshot.exists())
                {
                    long childrenCount=dataSnapshot.getChildrenCount();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String uid = snapshot.getKey();
                        getUserData(uid,DataRetriveAction.GET_FRIENDS,childrenCount,callback,friends,null);
                        //getUserData(uid,childrenCount,callback,friends);
                   }
                }else{
                    callback.onFriendsReceived(new ArrayList<UserData>());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void getAllUsers(final IGetUsersCallback callback)
    {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                databaseReference.removeEventListener(this);
                ArrayList<UserData> allUsers= new ArrayList<UserData>();
                long childrenCount=dataSnapshot.getChildrenCount();
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    UserData user=snapshot.getValue(UserData.class);
                    user.setUserUUID(snapshot.getKey());
                    try {
                       // getUserImageBitmap(user,childrenCount-1,callback,allUsers);
                        getUserImageBitmap(DataRetriveAction.GET_USERS,user,childrenCount-1,callback,allUsers,null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void getRankedUsers(final IGetUsersCallback callback,final long totalCount)
    {
        databaseReference.orderByChild("points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                long totalChildren = dataSnapshot.getChildrenCount();
                long i = 0;
                long usersToReturn = Math.min(totalChildren, totalCount);
                ArrayList<UserData> rankedUsers=new ArrayList<UserData>();
                // loop through dataSnapshot
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserData user=childSnapshot.getValue(UserData.class);
                    if (user.getUserUUID().equals(getCurrentUserUid())){
                        long userPlace = totalChildren - i;
                        userData.setCurrentRank(userPlace);
                        if(usersToReturn<userPlace){
                            callback.onUserReceived(user);
                            break;
                        }else{
                            try {
                                getUserImageBitmap(DataRetriveAction.GET_RANKED_USERS,user,usersToReturn,callback,rankedUsers,null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        i++;
                        user.setCurrentRank(i);
                        if(i<=usersToReturn) {
                            try {
                                getUserImageBitmap(DataRetriveAction.GET_RANKED_USERS,user,usersToReturn,callback,rankedUsers,null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

//    private void getUserImageBitmap(final UserData user, final long count, final IGetAllUsersCallback callback, final ArrayList<UserData> allUsers) throws IOException {
//        StorageReference imageReference=storageReference.child(IMAGE+user.getUserUUID()+".jpeg");
//
//        final File localFile=File.createTempFile(user.getUserUUID(),".jpeg");
//        imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                Bitmap bm=BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                user.setUserImage(bm);
//                getUserLocation(user,count,callback,allUsers);
//            }
//        });
//    }
//    private void getUserImageBitmap(final UserData newUserData) throws IOException {
//        String uid=newUserData.getUserUUID();
//        StorageReference imageReference=storageReference.child(IMAGE+uid+".jpeg");
//
//        final File localFile=File.createTempFile(uid,".jpeg");
//        imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                Bitmap bm=BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                newUserData.setUserImage(bm);
//            }
//        });
//    }
//    private void getUserImageBitmap(final UserData newUserData, final long count, final IGetFriendsCallback callback, final ArrayList<UserData> friends) throws IOException {
//        String uid=newUserData.getUserUUID();
//        StorageReference imageReference=storageReference.child(IMAGE+uid+".jpeg");
//
//        final File localFile=File.createTempFile(uid,".jpeg");
//        imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                Bitmap bm=BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                newUserData.setUserImage(bm);
//                friends.add(newUserData);
//                if(friends.size()==count)
//                    callback.onFriendsReceived(friends);
//            }
//        });
//    }

    private void getUserImageBitmap(final DataRetriveAction action, final UserData user, final long count, final IGetDataCallback callback, final ArrayList<UserData> usersArray, final Activity enclosingActivity) throws IOException {
        String uid=user.getUserUUID();
        StorageReference imageReference=storageReference.child(IMAGE+uid+".jpeg");

        final File localFile=File.createTempFile(uid,".jpeg");
        imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bm = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                user.setUserImage(bm);
                switch (action) {
                    case GET_SELF:
                        enclosingActivity.startActivity(new Intent(enclosingActivity, HomePageActivity.class));
                        break;
                    case GET_FRIENDS:
                        usersArray.add(user);
                        if (usersArray.size() == count)
                            ((IGetFriendsCallback) callback).onFriendsReceived(usersArray);
                        break;
                    case GET_USERS:
                        getUserLocation(user, count,(IGetUsersCallback)callback, usersArray);
                        break;
                    case GET_USER:
                        ((IGetUsersCallback)callback).onUserReceived(user);
                        break;
                    case GET_RANKED_USERS:
                        usersArray.add(user);
                        if (usersArray.size() == count)
                            ((IGetUsersCallback) callback).onUsersReceived(usersArray);
                        break;

                }
            }
        });
    }

    private void getUserLocation(final UserData user, final long count, final IGetUsersCallback callback, final ArrayList<UserData> allUsers) {
        databaseCoordinatesReference.child(user.getUserUUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseCoordinatesReference.child(user.getUserUUID()).removeEventListener(this);
                if(dataSnapshot.exists()){
                    if(!user.getUserUUID().equals(getCurrentUserUid())){
                        MyLatLong userLatLong=dataSnapshot.getValue(MyLatLong.class);
                        user.setMyLatLong(userLatLong);
                        allUsers.add(user);

                        if(allUsers.size()==count)
                           callback.onUsersReceived(allUsers);
                    }
                }else{
                    allUsers.add(user);
                    if(allUsers.size()==count)
                       callback.onUsersReceived(allUsers);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

//    private void getUserData(final String uid, final long count, final IGetFriendsCallback callback, final ArrayList<UserData> friends) {
//        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                UserData newUserData=dataSnapshot.getValue(UserData.class);
//                newUserData.setUserUUID((uid));
//                try {
//                   // getUserImageBitmap(newUserData,count,callback,friends);
//                    getUserImageBitmap(DataRetriveAction.GET_FRIENDS,newUserData,count,callback,friends,null);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) { }
//        });
//    }
//    private void getUserData(final String uid, final Activity enclosingActivity){
//
//        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                userData=dataSnapshot.getValue(UserData.class);
//                userData.setUserUUID(uid);
//                try {
//                    getUserImageBitmap(DataRetriveAction.GET_SELF,userData,0,null,null,enclosingActivity);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) { }
//        });
//    }
    public void getSingleUser(DataRetriveAction action, String uid, IGetUsersCallback clb) {
        getUserData(uid,action,0,clb,null,null);
    }
    private void getUserData(final String uid,final DataRetriveAction action, final long count, final IGetDataCallback callback, final ArrayList<UserData> friends, final Activity enclosingActivity) {
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserData userToSend;
                if(action==DataRetriveAction.GET_SELF){
                    userData=dataSnapshot.getValue(UserData.class);
                    userData.setUserUUID(uid);
                    userToSend=userData;
                }else{
                    userToSend=dataSnapshot.getValue(UserData.class);
                    userToSend.setUserUUID((uid));
                }
                try {
                    getUserImageBitmap(action,userToSend,count,callback,friends,enclosingActivity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }




    public void addFriend(String friendUid){
        String uid = firebaseAuth.getCurrentUser().getUid();
        databaseFriendsReference.child(uid).child(friendUid).child("status").setValue("friend");
        databaseFriendsReference.child(friendUid).child(uid).child("status").setValue("friend");
    }
    public void saveUserCoordinates(MyLatLong myLatLong){
        String uid = firebaseAuth.getCurrentUser().getUid();
        userData.setMyLatLong(myLatLong);
       // MyLatLong latlong=userData.getMyLatLong();
        databaseCoordinatesReference.child(uid).setValue(myLatLong);
    }


    public void startLocationService(Activity serviceHolder){
        if(!isLocationServiceRunning(serviceHolder)){
            Intent serviceIntent = new Intent(serviceHolder, LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                serviceHolder.startForegroundService(serviceIntent);
            }else{
                serviceHolder.startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning(Activity serviceHolder) {
        ActivityManager manager = (ActivityManager) serviceHolder.getSystemService(serviceHolder.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("LocationService".equals(service.service.getClassName())) {
                Log.d("LocationSERVICE", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("LocationSERVICE", "isLocationServiceRunning: location service is not running.");
        return false;
    }

    public void removeFriend(String uuid){
        databaseFriendsReference.child(getCurrentUserUid()).child(uuid).removeValue();
        databaseFriendsReference.child(uuid).child(getCurrentUserUid()).removeValue();
    }

    public void checkForFriendRequestAndSendRequest(final String receiver, final Object objectToDismiss){
        databaseNotificationsReference.child(getCurrentUserUid()).child(FRIEND_REQUESTS).child(receiver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseNotificationsReference.child(getCurrentUserUid()).child(FRIEND_REQUESTS).child(receiver).removeEventListener(this);
                if(dataSnapshot.exists()){
                    addFriend(receiver);
                    deleteNotifications(receiver);
                    if(objectToDismiss!=null)
                        ((DisplayUserInformationOnMapDialog)objectToDismiss).dismissDialog();
                }else{
                    sendRequest(receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void sendRequest(final String receiver){
        databaseNotificationsReference.child(receiver).child(FRIEND_REQUESTS).child(getCurrentUserUid()).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseNotificationsReference.child(receiver).child(FRIEND_REQUESTS).child(getCurrentUserUid()).removeEventListener(this);
                if(!dataSnapshot.exists()){
                    FriendsRequestNotification notification=new FriendsRequestNotification(userData.getUsername(),false);
                    databaseNotificationsReference.child(receiver).child(FRIEND_REQUESTS).child(getCurrentUserUid()).setValue(notification);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void checkIfRequestSent(final String receiver,final Button btn){
        databaseNotificationsReference.child(receiver).child(FRIEND_REQUESTS).child(getCurrentUserUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    btn.setEnabled(false);
                    btn.setText(R.string.pending);
                }else{
                    btn.setText(R.string.sendFriendRequest);
                    btn.setEnabled(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void getFriendRequestNotifications(String uuid, final IGetNotifications notificationsClb) {
        databaseNotificationsReference.child(uuid).child(FRIEND_REQUESTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ArrayList<Object> notifications=new ArrayList<Object>();
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        FriendsRequestNotification notification=child.getValue(FriendsRequestNotification.class);
                        notification.setSenderUid(child.getKey());
                        notifications.add(notification);
                    }
                    notificationsClb.onFriendRequestsReceived(notifications);
                }else
                {
                    notificationsClb.onFriendRequestsReceived(null);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
    public void deleteNotifications(String receiver) {
        databaseNotificationsReference.child(receiver).child(FRIEND_REQUESTS).child(getCurrentUserUid()).removeValue();
        databaseNotificationsReference.child(getCurrentUserUid()).child(FRIEND_REQUESTS).child(receiver).removeValue();
    }

}
