package rs.elfak.mosis.greenforce;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    EditText email;
    EditText password;
    EditText username;
    EditText surname;
    EditText name;
    EditText phoneNumber;
    Button next;
    Button finish;
    ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    public void onClick(View v) {

    }
    private void createUserProfile()
    {
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        String usernameText = username.getText().toString();
        String surnameText = surname.getText().toString();
        String nameText = name.getText().toString();
        String phoneNumberText = phoneNumber.getText().toString();
        ArrayList<String> stringsToCheck=new ArrayList<String>();
        Collections.addAll(stringsToCheck,emailText,passwordText,usernameText,surnameText,nameText,phoneNumberText);
        ArrayList<EditText> errorHolders=new ArrayList<EditText>();
        Collections.addAll(errorHolders,email,password,username,surname,name,phoneNumber);
        if(validateInfo(stringsToCheck,errorHolders))
        {
           MyUserManager.getInstance().createUserProfile(stringsToCheck,this);
        }

    }

    private boolean validateInfo(ArrayList<String> stringsToCheck, ArrayList<EditText> errorHolders) {
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




}
