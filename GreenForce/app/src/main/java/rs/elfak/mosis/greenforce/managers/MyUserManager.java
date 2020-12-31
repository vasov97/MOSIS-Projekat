package rs.elfak.mosis.greenforce.managers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import rs.elfak.mosis.greenforce.activities.CurrentEventsActivity;
import rs.elfak.mosis.greenforce.activities.EventActivity;
import rs.elfak.mosis.greenforce.activities.EventsMapActivity;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.activities.HomePageActivity;
import rs.elfak.mosis.greenforce.activities.LoginActivity;
import rs.elfak.mosis.greenforce.activities.RegisterActivity;
import rs.elfak.mosis.greenforce.dialogs.DisplayUserInformationOnMapDialog;
import rs.elfak.mosis.greenforce.enums.DataRetriveAction;
import rs.elfak.mosis.greenforce.enums.EventImageType;
import rs.elfak.mosis.greenforce.enums.EventStatus;
import rs.elfak.mosis.greenforce.enums.VolunteerType;
import rs.elfak.mosis.greenforce.interfaces.ICheckEventData;
import rs.elfak.mosis.greenforce.interfaces.IGetCurrentRankCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetNotifications;
import rs.elfak.mosis.greenforce.interfaces.IGetUsersCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetDataCallback;
import rs.elfak.mosis.greenforce.interfaces.IGetFriendsCallback;
import rs.elfak.mosis.greenforce.models.EventRequestNotification;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.FriendsRequestNotification;
import rs.elfak.mosis.greenforce.models.MyEvent;
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
    private final DatabaseReference databaseEventsReference;
    private final DatabaseReference databaseVolunteersReference;
    private final DatabaseReference databaseUserEventsReference;
    private final StorageReference storageReference;
    private static final String USER = "user";
    private static final String FRIENDS = "friends";
    private static final String EVENTS = "events";
    private static final String COORDINATES="coordinates";
    private static final String IMAGE = "profileImage/";
    private static final String EVENT_IMAGES="eventImage/";
    private static final String NOTIFICATIONS="notifications";
    private static final String FRIEND_REQUESTS="friendRequests";
    private static final String EVENT_REQUEST="eventRequest";
    private static final String VOLUNTEERS="volunteers";
    private static final String USER_EVENTS="userEvents";
    private static final String CURRENT_EVENTS="currentEvents";

    private UserData userData,visitProfile;
    private boolean loggedIn;
    ArrayList<UserData> myFriends;
    Activity locationServiceHolder;
    Intent locationService;


    private MyUserManager() {
        loggedIn=true;
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(USER);
        databaseFriendsReference=firebaseDatabase.getReference(FRIENDS);
        databaseCoordinatesReference=firebaseDatabase.getReference(COORDINATES);
        databaseNotificationsReference=firebaseDatabase.getReference(NOTIFICATIONS);
        databaseEventsReference=firebaseDatabase.getReference(EVENTS);
        databaseVolunteersReference=firebaseDatabase.getReference(VOLUNTEERS);
        databaseUserEventsReference=firebaseDatabase.getReference(USER_EVENTS);
        storageReference = FirebaseStorage.getInstance().getReference();

    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }


    private static class SingletonHolder{
        public static final MyUserManager instance=new MyUserManager();
    }
   public UserData getVisitProfile(){return visitProfile;}
   public void setVisitProfile(UserData visitProfile){this.visitProfile=visitProfile;}
   public UserData getUser(){return userData;}
   public String getCurrentUserUid(){return userData.getUserUUID();}
   public static MyUserManager getInstance(){
        return SingletonHolder.instance;
    }
   public ArrayList<UserData> getMyFriends(){return myFriends;}
   public void setMyFriends(ArrayList<UserData> friends){myFriends=friends;}
   public DatabaseReference getDatabaseCoordinatesReference(){return databaseCoordinatesReference;}
   public DatabaseReference getDatabaseFriendsReference(){return databaseFriendsReference;}
   public DatabaseReference getDatabaseEventsReference(){return databaseEventsReference;}
   public FirebaseAuth getFirebaseAuth(){return firebaseAuth;}


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

    public void getFriends(final String uid, final IGetFriendsCallback callback, final DataRetriveAction action, final boolean removeLIstener)
    {
        databaseFriendsReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(removeLIstener)
                    databaseFriendsReference.child(uid).removeEventListener(this);
                if(action==DataRetriveAction.GET_RANKED_USERS)
                    databaseFriendsReference.child(uid).removeEventListener(this);
                final ArrayList<UserData> friends=new ArrayList<UserData>();
                if(dataSnapshot.exists())
                {
                    long childrenCount=dataSnapshot.getChildrenCount();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        if(action==DataRetriveAction.GET_FRIENDS)
                           getUserData(key,action,childrenCount,callback,friends,null);
                        else{
                            childrenCount--;
                            UserData emptyUser=new UserData();
                            emptyUser.setUserUUID(key);
                            friends.add(emptyUser);
                            if(childrenCount==0)
                                callback.onFriendsReceived(friends);
                        }
                   }
                }else{
                    callback.onFriendsReceived(new ArrayList<UserData>());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void getAllUsers(final IGetUsersCallback callback, final DataRetriveAction action)
    {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                databaseReference.removeEventListener(this);
                ArrayList<UserData> allUsers= new ArrayList<UserData>();
                long childrenCount=dataSnapshot.getChildrenCount();
                if(action!=DataRetriveAction.GET_RANKED_USERS)
                    childrenCount--;
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    UserData user=snapshot.getValue(UserData.class);
                    user.setUserUUID(snapshot.getKey());
                    try {
                        getUserImageBitmap(action,user,childrenCount,callback,allUsers,null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }



    public void getUsersCurrentRank(final String uid, final IGetCurrentRankCallback callback){
        databaseReference.orderByChild("points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseReference.removeEventListener(this);
                long childrenCount=dataSnapshot.getChildrenCount();
                long myRank=childrenCount;
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    if(child.getKey().equals(uid)){
                        callback.onRankRetrieved(myRank);
                    }else{
                        myRank--;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
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
                        if(!usersArray.contains(user))
                           usersArray.add(user);
                        else
                        {
                            usersArray.remove(user);
                            usersArray.add(user);
                        }
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
                        if(!usersArray.contains(user))
                         usersArray.add(user);
                        else{
                            usersArray.remove(user);
                            usersArray.add(user);
                        }
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
                if(dataSnapshot.exists()) {
//                    if(!user.getUserUUID().equals(getCurrentUserUid()))
                    MyLatLong userLatLong = dataSnapshot.getValue(MyLatLong.class);
                    user.setMyLatLong(userLatLong);
                    allUsers.add(user);

                    if (allUsers.size() == count)
                        callback.onUsersReceived(allUsers);

//                }else{
//                    allUsers.add(user);
//                    if(allUsers.size()==count)
//                       callback.onUsersReceived(allUsers);
//                }
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
        String uid = getCurrentUserUid();
        databaseFriendsReference.child(uid).child(friendUid).child("status").setValue("friend");
        databaseFriendsReference.child(friendUid).child(uid).child("status").setValue("friend");
    }
    public void saveUserCoordinates(MyLatLong myLatLong){
        String uid = getCurrentUserUid();
        userData.setMyLatLong(myLatLong);
        databaseCoordinatesReference.child(uid).setValue(myLatLong);
    }

    public void startLocationService(){
        startLocationService(locationServiceHolder);
    }
    public void startLocationService(Activity serviceHolder){
        locationServiceHolder=serviceHolder;
        if(!isLocationServiceRunning(serviceHolder)){
             locationService = new Intent(serviceHolder, LocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                serviceHolder.startForegroundService(locationService);
            }else{
                serviceHolder.startService(locationService);
            }
        }
    }


    public void stopLocationService(){
        locationServiceHolder.stopService(locationService);
//        ActivityManager manager = (ActivityManager) serviceHolder.getSystemService(serviceHolder.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
//            if("LocationService".equals(service.service.getClassName())) {
//                android.os.Process.killProcess(service.pid);
//            }
//        }
    }

    public boolean checkIfLocationServiceActive(){
        return isLocationServiceRunning(locationServiceHolder);
    }


    private boolean isLocationServiceRunning(Activity serviceHolder) {
        ActivityManager manager = (ActivityManager) serviceHolder.getSystemService(serviceHolder.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            String serviceName=service.service.getClassName();
            if(LocationService.class.getName().equals(serviceName)) {
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

    public void checkIfFriendRequestSent(final String receiver,final Button btn){
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

    public void getEventRequestNotifications(String userID, final IGetNotifications notificationsClb) {
        databaseNotificationsReference.child(userID).child(EVENT_REQUEST).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ArrayList<Object> notifications=new ArrayList<Object>();
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        for(DataSnapshot childOfChild: child.getChildren()){
                            EventRequestNotification notification=childOfChild.getValue(EventRequestNotification.class);
                            notification.setSenderUid(childOfChild.getKey());
                            notification.setEventID(child.getKey());
                            notifications.add(notification);
                        }
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveEvent(Activity activity) {
        MyEvent event=userData.getCurrentEvent();
        event.setCreatedByID(getCurrentUserUid());
        //userData.setUserUUID(getCurrentUserUid());
      //  event.setCreatedByFullName(userData.getName()+" "+userData.getSurname());
      //  event.setCreatedByUsername(userData.getUsername());

       // LocalDateTime sentDateTime=LocalDateTime.now();
      //  String dateTime=sentDateTime.toString();
       // event.setDateTime(dateTime);
       // String eventDate=LocalDate.now().toString();
        LocalDate eventDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        //LocalDate myEventDate = LocalDate.parse(eventDate,dateTimeFormatter);
        String eventDateToWrite = eventDate.format(dateTimeFormatter);
        Calendar calendar = Calendar.getInstance();
        String eventTime= setCurrentTime(calendar);

        event.setDate(eventDateToWrite);
        event.setTime(eventTime);
        String eventID=databaseEventsReference.push().getKey();
        assert eventID != null;
        databaseEventsReference.child(eventID).setValue(event);
        saveEventBeforePhotos(eventID,event.getEventPhotos());
    }
    public String setCurrentTime(Calendar myCalendar)
    {
        myCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));//gmt+8 probaj
        int currentHour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = myCalendar.get(Calendar.MINUTE);

        return String.format("%s:%s", currentHour, currentMinute);
    }
    private String setFormattedTime(Calendar myCalendar)//ako ne radi fja iznad ovo bi trebalo
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm",Locale.getDefault());
        return simpleDateFormat.format(myCalendar.getTime());
    }

    private void saveEventBeforePhotos(String eventID, ArrayList<Bitmap> eventPhotos) {
        String imageName="image";
        int count=1;
        for(Bitmap image : eventPhotos){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final StorageReference reference; reference = FirebaseStorage.getInstance().getReference()
                    .child("eventImage")
                    .child(eventID)
                    .child("before")
                    .child(imageName+count+".jpeg");
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            count++;
            reference.putBytes(baos.toByteArray())
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("UploadPhoto","onFailure",e.getCause());
                        }
                    });

        }
    }
    public void getAllEvents(final IGetEventsCallback callback) {
        databaseEventsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseEventsReference.removeEventListener(this);
                final ArrayList<MyEvent> events=new ArrayList<MyEvent>();
                if(dataSnapshot.exists())
                {
                    long childrenCount=dataSnapshot.getChildrenCount();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        childrenCount--;
                        MyEvent event = snapshot.getValue(MyEvent.class);
                        assert event != null;
                        event.setEventID(key);
                        events.add(event);
//                            if(childrenCount==0)
//                                callback.onFriendsReceived(friends);
                       }
                    callback.onEventsReceived(events);
                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void applyForEvent(final String eventID) {
        databaseEventsReference.child(eventID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseEventsReference.child(eventID).removeEventListener(this);
                MyEvent event=dataSnapshot.getValue(MyEvent.class);
                if(event.getEventStatus()== EventStatus.AVAILABLE){
                    databaseEventsReference.child(eventID).child("eventStatus").setValue(EventStatus.IN_PROGRESS);
                    addLeaderToEvent(eventID,getCurrentUserUid());
                }else if(event.getEventStatus()==EventStatus.IN_PROGRESS){
                    sendEventRequestToLeader(eventID,event.getEventLocation());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendEventRequestToLeader(final String eventID, final MyLatLong eventLocation) {
        databaseVolunteersReference.child(eventID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseVolunteersReference.child(eventID).removeEventListener(this);
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    EventVolunteer volunteer=child.getValue(EventVolunteer.class);
                    if(volunteer.getType()== VolunteerType.LEADER){
                        sendEventRequest(child.getKey(),eventID,eventLocation);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void sendEventRequest(final String receiver, final String eventID, final MyLatLong eventLocation){
        databaseNotificationsReference.child(receiver).child(EVENT_REQUEST).child(eventID).child(getCurrentUserUid()).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseNotificationsReference.child(receiver).child(EVENT_REQUEST).child(eventID).child(getCurrentUserUid()).removeEventListener(this);
                if(!dataSnapshot.exists()){
                    EventRequestNotification notification=new EventRequestNotification(userData.getUsername(),false,eventLocation);
                    databaseNotificationsReference.child(receiver).child(EVENT_REQUEST).child(eventID).child(getCurrentUserUid()).setValue(notification);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void addLeaderToEvent(String eventID, String currentUserUid) {
        EventVolunteer volunteer=new EventVolunteer();
        volunteer.setType(VolunteerType.LEADER);
        databaseVolunteersReference.child(eventID).child(currentUserUid).setValue(volunteer);
        addEventToUsersCurrent(eventID,currentUserUid,volunteer);
    }

    public void findEventLeaderAndCheckRequest(final String eventID, final ICheckEventData clb){
        databaseVolunteersReference.child(eventID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseVolunteersReference.child(eventID).removeEventListener(this);
                boolean found=false;
                EventVolunteer leader = null;
                VolunteerType myType = null;
                for (DataSnapshot child : dataSnapshot.getChildren()){
                    EventVolunteer volunteer=child.getValue(EventVolunteer.class);
                    volunteer.setId(child.getKey());
                    if(child.getKey().equals(getCurrentUserUid())){
                        myType=volunteer.getType();
                        found=true;
                    }
                    if(volunteer.getType()== VolunteerType.LEADER){
                        leader=child.getValue(EventVolunteer.class);
                        leader.setId(child.getKey());
                        //checkIfEventRequestSent(child.getKey(),eventID,clb);
                    }
                }
                if(found){
                    if(myType!=null)
                        clb.onCheckIfVolunteer(true,myType);
                }
                else{
                    if(leader!=null)
                      checkIfEventRequestSent(leader.getId(),eventID,clb);
                    else
                        clb.onCheckIfRequestSent(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    public void checkIfEventRequestSent(final String receiver, final String eventID, final ICheckEventData clb){
        databaseNotificationsReference.child(receiver).child(EVENT_REQUEST).child(eventID).child(getCurrentUserUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseNotificationsReference.child(receiver).child(EVENT_REQUEST).child(eventID).child(getCurrentUserUid()).removeEventListener(this);
                if(dataSnapshot.exists()){
                    clb.onCheckIfRequestSent(true);
                }else{
                    clb.onCheckIfRequestSent(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }


    public void addEventToUsersCurrent(String eventID,String userID,EventVolunteer status){
        //dodaj vreme za lidera za 21 dan countdown? ako da treba nova klasa a ne EventVolunteer
        databaseUserEventsReference.child(userID).child(CURRENT_EVENTS).child(eventID).setValue(status);

    }

    public void getAllCurrentEvents(String userID, final IGetEventsCallback eventsCallback) {
        databaseUserEventsReference.child(userID).child(CURRENT_EVENTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    HashMap<String,EventVolunteer> map=new HashMap<String,EventVolunteer>();
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        EventVolunteer myType=child.getValue(EventVolunteer.class);
                        myType.setId(child.getKey());
                        map.put(child.getKey(),myType);
                    }
                    eventsCallback.onCurrentEventsMapReceived(map);

                }else
                    eventsCallback.onCurrentEventsMapReceived(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void getSingleEvent(final String eventID, final IGetEventsCallback eventsCallback) {
        databaseEventsReference.child(eventID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                databaseEventsReference.child(eventID).removeEventListener(this);
                MyEvent event=dataSnapshot.getValue(MyEvent.class);
                event.setEventID(dataSnapshot.getKey());
                eventsCallback.onSingleEventReceived(event);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void getEventImages(String eventID, EventImageType type, final int imageCount, final IGetEventsCallback callback) throws IOException {

        final ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        StorageReference imageReference = storageReference.child(EVENT_IMAGES+eventID+type.toString());
        for (int i = 1; i <= imageCount; i++) {
            final File localFile = File.createTempFile("image"+i, ".jpeg");
            StorageReference myImage=imageReference.child("image"+i+".jpeg");
            myImage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bm = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    images.add(bm);
                    if(images.size()==imageCount)
                        callback.onEventImagesReceived(images);
                }
            });
        }
    }

    public void getEventVolunteers(String eventID, final IGetEventsCallback callback){
        databaseVolunteersReference.child(eventID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ArrayList<EventVolunteer> volunteers=new ArrayList<EventVolunteer>();
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        EventVolunteer volunteer=child.getValue(EventVolunteer.class);
                        volunteer.setId(child.getKey());
                        volunteers.add(volunteer);
                    }
                    callback.onEventVolunteersReceived(volunteers);
                }else
                    callback.onEventVolunteersReceived(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void acceptEventRequest(String senderUid, String eventID) {
        EventVolunteer volunteer=new EventVolunteer();
        volunteer.setType(VolunteerType.FOLLOWER);
        databaseVolunteersReference.child(eventID).child(senderUid).setValue(volunteer);
        addEventToUsersCurrent(eventID,senderUid,volunteer);

    }

    public void deleteEventNotification(String senderUid, String eventID) {
        databaseNotificationsReference.child(getCurrentUserUid()).child(EVENT_REQUEST).child(eventID).child(senderUid).removeValue();
    }








}
