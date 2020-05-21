package rs.elfak.mosis.greenforce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,IComponentInitializer{

    Button loginButton;
    TextView dontHaveAnAccount;
    TextView forgotPassword;
    EditText email;
    EditText password;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeComponents();


        dontHaveAnAccount.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        loginButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {
       if(v.getId() == R.id.dontHaveAnAccount)
       {
           Intent i = new Intent(this,RegisterActivity.class);
           startActivity(i);
       }
       else if(v.getId() == R.id.forgotPassword)
       {
           showRecoverPasswordDialog();
       }
       else if(v.getId() == R.id.loginButton)
       {
//           Toast.makeText(this, "LoginButton", Toast.LENGTH_SHORT).show();
//           Intent i = new Intent(this,HomePageActivity.class);
//           startActivity(i);
           loginUser();
       }
    }
    @Override
    public void initializeComponents() {
        loginButton = findViewById(R.id.loginButton);
        dontHaveAnAccount = findViewById(R.id.dontHaveAnAccount);
        forgotPassword = findViewById(R.id.forgotPassword);
        email = findViewById(R.id.editTextEmailLoginActivity);
        password = findViewById(R.id.editTextPasswordLoginActivity);
    }
    private void showRecoverPasswordDialog()
    {
        AlertDialog.Builder forgotPasswordDialog = new AlertDialog.Builder(this);
        forgotPasswordDialog.setTitle(R.string.alertDialog);

        LinearLayout linearLayout = new LinearLayout(this);

        final EditText emailRecover = new EditText(this);
        emailRecover.setHint(R.string.email);
        emailRecover.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        linearLayout.addView(emailRecover);
        linearLayout.setPadding(10,10,10,10);

        forgotPasswordDialog.setView(linearLayout);

        forgotPasswordDialog.setPositiveButton(R.string.recoverPassword, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              String emailRecoverText = emailRecover.getText().toString().trim();
              MyUserManager.getInstance().recoverPassword(emailRecoverText,LoginActivity.this);
            }
        });
        forgotPasswordDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                  dialog.dismiss();
            }
        });

        forgotPasswordDialog.create().show();


    }



    private boolean validateInfo(String emailText, String passwordText) {
        boolean valid=true;
        if(emailText.isEmpty())
        {
            email.setError("Please enter email");
            email.requestFocus();
            valid=false;
        }
        else if(passwordText.isEmpty())
        {
            password.setError("Please enter password");
            password.requestFocus();
            valid=false;
        }
        else if(emailText.isEmpty() && passwordText.isEmpty())
        {
            valid=false;
            Toast.makeText(this, "Fields are empty", Toast.LENGTH_SHORT).show();
        }
        else if(passwordText.length()<6)
        {
            valid=false;
            Toast.makeText(this, "Password must be longer than 6 characters", Toast.LENGTH_SHORT).show();
        }
        return valid;
    }
    private void loginUser()
    {
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        if(validateInfo(emailText,passwordText))
        {
           MyUserManager.getInstance().loginUser(emailText,passwordText,LoginActivity.this);
        }
    }


}
