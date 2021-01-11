package rs.elfak.mosis.greenforce.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.interfaces.IFragmentComponentInitializer;
import rs.elfak.mosis.greenforce.models.UserData;

public class FragmentRegistrationData extends Fragment implements IFragmentComponentInitializer
{
    private EditText registrationEmail;
    private EditText registrationName;
    private EditText registrationSurname;
    private EditText registrationUsername;
    private EditText registrationPhoneNumber;
    private EditText registrationPassword;

    private Button next;

    private FragmentRegistrationDataListener listener;

    public interface  FragmentRegistrationDataListener
    {
        void onInputDataSent(HashMap<String,String> userData);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View registrationDataView = inflater.inflate(R.layout.fragment_registration_data,container,false);
        initializeComponents(registrationDataView);

        next.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onButtonNextClick();
            }
        });

        return registrationDataView;
    }

    @Override
    public void initializeComponents(View v) {
        registrationEmail = v.findViewById(R.id.editTextEmailRegistrationFragment);
        registrationName = v.findViewById(R.id.editTextNameRegistrationFragment);
        registrationSurname = v.findViewById(R.id.editTextSurnameRegistrationFragment);
        registrationUsername = v.findViewById(R.id.editTextUsernameRegistrationFragment);
        registrationPhoneNumber = v.findViewById(R.id.editTextPhoneNumberRegistrationFragment);
        registrationPassword= v.findViewById(R.id.editTextPasswordRegistrationFragment);
        next = v.findViewById(R.id.NextButton);
    }

    private void onButtonNextClick() {
        String emailText = registrationEmail.getText().toString();
        String passwordText = registrationPassword.getText().toString();
        String usernameText = registrationUsername.getText().toString();
        String surnameText = registrationSurname.getText().toString();
        String nameText = registrationName.getText().toString();
        String phoneNumberText = registrationPhoneNumber.getText().toString();
        ArrayList<String> stringsToCheck=new ArrayList<String>();
        Collections.addAll(stringsToCheck,emailText,passwordText,usernameText,surnameText,nameText,phoneNumberText);
        ArrayList<EditText> errorHolders=new ArrayList<EditText>();
        Collections.addAll(errorHolders,registrationEmail,registrationPassword,registrationUsername,registrationSurname,registrationName,registrationPhoneNumber);
        if(MyUserManager.getInstance().validateInfo(stringsToCheck,errorHolders))
        {

            HashMap<String,String> userData = new HashMap<String, String>();
            userData.put("email",emailText);
            userData.put("username",usernameText);
            userData.put("surname",surnameText);
            userData.put("name",nameText);
            userData.put("phoneNumber",phoneNumberText);
            userData.put("password",passwordText);
            checkIFUserAlreadyExists(userData);

        }
    }

    private void checkIFUserAlreadyExists(final HashMap<String, String> userData) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference("user");
        databaseReference.orderByChild("username").equalTo(userData.get("username")).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                databaseReference.removeEventListener(this);
                if(dataSnapshot.exists()){
                    registrationUsername.setError("Username already in use!");
                }
                else{
                    listener.onInputDataSent(userData);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof FragmentRegistrationDataListener)
            listener=(FragmentRegistrationDataListener)context;
        else{
            throw new RuntimeException(context.toString()+" must implement FragmentRegistrationDataListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener=null;
        this.getActivity().finish();
    }

}
