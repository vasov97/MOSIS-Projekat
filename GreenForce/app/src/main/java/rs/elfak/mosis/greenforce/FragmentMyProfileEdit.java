package rs.elfak.mosis.greenforce;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;


public class FragmentMyProfileEdit extends Fragment implements IFragmentComponentInitializer{

    EditText myProfileName;
    EditText myProfileSurname;
    EditText myProfileEmailAddress;
    EditText myProfilePhone;
    Button changePassword;
    FragmentMyProfileEditListener listener;

    public interface  FragmentMyProfileEditListener
    {
        void onInputDataSent(HashMap<String,String> userData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myProfileEdit= inflater.inflate(R.layout.fragment_my_profile_edit, container, false);
        initializeComponents(myProfileEdit);
        setUpUserData();
        return myProfileEdit;
    }

    private void setUpUserData() {
        UserData user=MyUserManager.getInstance().getUser();
        myProfileName.setText(user.getName(), TextView.BufferType.EDITABLE);
        myProfileSurname.setText(user.getSurname(), TextView.BufferType.EDITABLE);
        myProfilePhone.setText(user.getPhoneNumber(), TextView.BufferType.EDITABLE);
        myProfileEmailAddress.setText(user.getEmail(), TextView.BufferType.EDITABLE);
    }

    @Override
    public void initializeComponents(View v) {
        myProfileName=v.findViewById(R.id.edit_profile_name);
        myProfileSurname=v.findViewById(R.id.edit_profile_surname);
        myProfileEmailAddress=v.findViewById(R.id.edit_profile_email);
        myProfilePhone=v.findViewById(R.id.edit_profile_numnber);
        changePassword=v.findViewById(R.id.button_change_password);
    }
    public HashMap<String,String> getUserData()
    {
        String emailText = myProfileEmailAddress.getText().toString();
        String surnameText = myProfileSurname.getText().toString();
        String nameText = myProfileName.getText().toString();
        String phoneNumberText = myProfilePhone.getText().toString();
        HashMap<String,String> userData = new HashMap<String, String>();
        userData.put("email",emailText);
        userData.put("surname",surnameText);
        userData.put("name",nameText);
        userData.put("phoneNumber",phoneNumberText);
       return userData;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof FragmentMyProfileEditListener)
            listener=(FragmentMyProfileEditListener)context;
        else{
            throw new RuntimeException(context.toString()+" must implement FragmentMyProfileEditListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener=null;
    }
}
