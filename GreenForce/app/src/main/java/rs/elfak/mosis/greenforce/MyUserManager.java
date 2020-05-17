package rs.elfak.mosis.greenforce;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import java.util.ArrayList;

public class MyUserManager {

    FirebaseAuth firebaseAuth;
    private MyUserManager()
    {
        firebaseAuth=FirebaseAuth.getInstance();
    }
    private static class SingletonHolder{
        public static final MyUserManager instance=new MyUserManager();
    }

    public static MyUserManager getInstance(){
        return SingletonHolder.instance;
    }

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
                            enclosingActivity.startActivity(new Intent(enclosingActivity,HomePageActivity.class));
                        }
                    }
                }
        );
    }

    public void createUserProfile(ArrayList<String> params, final Activity enclosingActivity) {
            firebaseAuth.createUserWithEmailAndPassword(params.get(0),params.get(1)).addOnCompleteListener(enclosingActivity,
                    new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(!task.isSuccessful())
                            {
                                FirebaseAuthException e=(FirebaseAuthException)task.getException();
                                Toast.makeText(enclosingActivity,"Registration failed", Toast.LENGTH_SHORT).show();
                                Log.e("LoginActivity","Failed Registration",e);
                            }
                            else
                            {
                                enclosingActivity.finish();
                                enclosingActivity.startActivity(new Intent(enclosingActivity,LoginActivity.class));
                            }
                        }
                    }
            );
    }


}
