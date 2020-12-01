package rs.elfak.mosis.greenforce;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MyUserManager {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseFriendsReference;
    private DatabaseReference databaseCoordinatesReference;
    private StorageReference storageReference;
    private static final String USER = "user";
    private static final String FRIENDS = "friends";
    private static final String COORDINATES="coordinates";
    private static final String IMAGE = "profileImage/";
    private UserData userData,visitProfile;
    ArrayList<UserData> myFriends;

    private MyUserManager()
    {
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(USER);
        databaseFriendsReference=firebaseDatabase.getReference(FRIENDS);
        databaseCoordinatesReference=firebaseDatabase.getReference(COORDINATES);
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

    public void loginUser(String emailText, String passwordText, final Activity enclosingActivity){
        firebaseAuth.signInWithEmailAndPassword(emailText,passwordText).addOnCompleteListener(enclosingActivity,
                new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(!task.isSuccessful())
                        {
                            FirebaseAuthException e=(FirebaseAuthException)task.getException();
                            Toast.makeText(enclosingActivity,"Login failed", Toast.LENGTH_SHORT).show();
                            Log.e("LoginActivity","Login Registration",e);
                        }
                        else
                        {
                            FirebaseUser user=firebaseAuth.getCurrentUser();
                            String uid=user.getUid();
                            userData=new UserData();
                            getUserData(uid,userData);
                            enclosingActivity.startActivity(new Intent(enclosingActivity,HomePageActivity.class));
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
                            if(!task.isSuccessful())
                            {
                                FirebaseAuthException e=(FirebaseAuthException)task.getException();
                                Log.e("RegistrationActivity","Failed Registration",e);
                                createUserProfileFailed(enclosingActivity);
                            }
                            else
                            {


                                userData = new UserData(params.get("email"),params.get("name"),params.get("surname"),params.get("username"),params.get("phoneNumber"),imageBitmap);
                                createUserProfileSuccess(enclosingActivity);

                            }
                        }
                    }
            );


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
                    enclosingActivity.startActivity(new Intent(enclosingActivity,LoginActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Toast.makeText(enclosingActivity,R.string.registrationCompleted,Toast.LENGTH_SHORT).show();
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
                        if(task.isSuccessful())
                        {
                            Toast.makeText(enclosingActivity,R.string.emailSent,Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
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

    private void getUserData(final String uid,final UserData newUserData){
        final ArrayList<String> userString=new ArrayList<String>();
        //final String uid = user.getUid();
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userString.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                    userString.add(snapshot.getValue().toString());

                createUserFromList(newUserData,userString);

                try {
                    getUserImageBitmap(uid,newUserData);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void createUserFromList(UserData user,ArrayList<String> userString){
        user.setEmail(userString.get(0));
        user.setName(userString.get(1));
        user.setPhoneNumber(userString.get(2));
        user.setPoints(userString.get(3));
        user.setSurname(userString.get(4));
        user.setUsername(userString.get(5));

    }
    private void getUserImageBitmap(String uid, final UserData newUserData) throws IOException {

        //String uid=user.getUid();
        StorageReference imageReference=storageReference.child(IMAGE+uid+".jpeg");

        final File localFile=File.createTempFile(uid,".jpeg");
        imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bm=BitmapFactory.decodeFile(localFile.getAbsolutePath());
                newUserData.setUserImage(bm);
            }
        });


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
                    //friends.clear();
                    long childrenCount=dataSnapshot.getChildrenCount();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String uid = snapshot.getKey();
                        UserData newUser=new UserData();
                        childrenCount--;
                        if(childrenCount==0)
                            getUserData(uid,newUser,true,callback,friends);
                        else
                            getUserData(uid,newUser,false,callback,friends);
                   }
                }else
                  callback.onFriendsReceived(new ArrayList<UserData>());

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getAllUsers(final IGetAllUsersCallback callback)
    {
       // final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference(USER);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                ArrayList<UserData> allUsers=new ArrayList<>();
                long childrenCount=dataSnapshot.getChildrenCount();
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    UserData user=ds.getValue(UserData.class);
                    if(!user.getUserUUID().equals(getCurrentUserUid()))
                    {
                        allUsers.add(user);
                        childrenCount--;
                        if(childrenCount==0)
                            getSingleUserData(user.getUserUUID(),user,true,callback,allUsers);
                        else
                            getSingleUserData(user.getUserUUID(),user,false,callback,allUsers);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }
    private void getSingleUserData(final String uid, final UserData newUserData,final boolean isLast, final IGetAllUsersCallback callback, final ArrayList<UserData> users)
    {
        final ArrayList<String> userString=new ArrayList<String>();
        //final String uid = user.getUid();
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userString.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                    userString.add(snapshot.getValue().toString());

                newUserData.setUserUUID((uid));
                createUserFromList(newUserData,userString);



                /*try {
                    getUserImageBitmap(uid,newUserData,isLast,callback,users);

                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserData(final String uid, final UserData newUserData, final boolean isLast, final IGetFriendsCallback callback, final ArrayList<UserData> friends) {
        final ArrayList<String> userString=new ArrayList<String>();
        //final String uid = user.getUid();
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userString.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                    userString.add(snapshot.getValue().toString());

                newUserData.setUserUUID((uid));
                createUserFromList(newUserData,userString);

                try {
                    getUserImageBitmap(uid,newUserData,isLast,callback,friends);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserImageBitmap(String uid, final UserData newUserData, final boolean isLast, final IGetFriendsCallback callback, final ArrayList<UserData> friends) throws IOException {
        StorageReference imageReference=storageReference.child(IMAGE+uid+".jpeg");

        final File localFile=File.createTempFile(uid,".jpeg");
        imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bm=BitmapFactory.decodeFile(localFile.getAbsolutePath());
                newUserData.setUserImage(bm);
                if(isLast)
                    addLastFriendToList(newUserData,friends,callback);
                else
                    addFriendToList(newUserData,friends);

            }
        });
    }

    public void addFriendToList(UserData friend, ArrayList<UserData> friends) {
        friends.add(friend);
    }
    public void addLastFriendToList(UserData lastFriend, ArrayList<UserData> friends, IGetFriendsCallback callback) {
        friends.add(lastFriend);
        callback.onFriendsReceived(friends);
    }

    public void addFriend(String friendUid){
        String uid = firebaseAuth.getCurrentUser().getUid();
        databaseFriendsReference.child(uid).child(friendUid).child("status").setValue("friend");
        databaseFriendsReference.child(friendUid).child(uid).child("status").setValue("friend");
    }
    public void saveUserCoordinates(double lat,double lon){
        String uid = firebaseAuth.getCurrentUser().getUid();
        MyLatLong latlong=new MyLatLong(lat,lon);
        databaseCoordinatesReference.child(uid).setValue(latlong);
    }
}
