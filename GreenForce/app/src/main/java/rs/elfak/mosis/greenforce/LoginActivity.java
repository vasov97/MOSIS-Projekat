package rs.elfak.mosis.greenforce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    Button loginButton;
    TextView dontHaveAnAccount;
    TextView forgotPassword;
    EditText email;
    EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    @Override
    public void onClick(View v) {

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
