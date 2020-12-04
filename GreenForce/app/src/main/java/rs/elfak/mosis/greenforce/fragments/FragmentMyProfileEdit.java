package rs.elfak.mosis.greenforce.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import rs.elfak.mosis.greenforce.activities.MyProfileActivity;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.UserData;
import rs.elfak.mosis.greenforce.interfaces.IFragmentComponentInitializer;


public class FragmentMyProfileEdit extends Fragment implements IFragmentComponentInitializer {

    EditText myProfileName;
    EditText myProfileSurname;
    EditText myProfileEmailAddress;
    EditText myProfilePhone;
    Button changePassword;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myProfileEdit= inflater.inflate(R.layout.fragment_my_profile_edit, container, false);
        initializeComponents(myProfileEdit);
        setUpUserData();
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangePasswordDialog();
            }
        });


        return myProfileEdit;
    }

    private void openChangePasswordDialog() {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_my_profile_change_password,null);
        final EditText oldPassword= view.findViewById(R.id.editTextOldPassword);
        final EditText newPassword= view.findViewById(R.id.editTextNewPassword);
        final EditText confirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        Button confirm = view.findViewById(R.id.buttonConfirmNewPassword);
        Button cancel = view.findViewById(R.id.buttonCancelNewPassword);



        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setView(view);
        final AlertDialog dialog= builder.create();
        dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPasswordText = oldPassword.getText().toString();
                String newPasswordText = newPassword.getText().toString();
                String confirmPasswordText = confirmPassword.getText().toString();
                ArrayList<String> stringsToCheck=new ArrayList<String>();
                Collections.addAll(stringsToCheck,oldPasswordText,newPasswordText,confirmPasswordText);
                ArrayList<EditText> errorHolders=new ArrayList<EditText>();
                Collections.addAll(errorHolders,oldPassword,newPassword,confirmPassword);
                if(MyUserManager.getInstance().validateInfo(stringsToCheck,errorHolders))
                {
                    if(newPasswordText.equals(confirmPasswordText)){
                        dialog.dismiss();
                        updatePassword(oldPasswordText,newPasswordText);
                    }
                    else
                        Toast.makeText(getActivity(),"Passwords don't match",Toast.LENGTH_SHORT).show();

                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private void updatePassword(String oldPasswordText, String newPasswordText) {

       MyUserManager.getInstance().updatePassword(oldPasswordText,newPasswordText,getActivity());
    }

    private void setUpUserData() {
        UserData user=MyUserManager.getInstance().getUser();
        myProfileName.setText(user.getName(), TextView.BufferType.EDITABLE);
        myProfileSurname.setText(user.getSurname(), TextView.BufferType.EDITABLE);
        myProfilePhone.setText(user.getPhoneNumber(), TextView.BufferType.EDITABLE);
        myProfileEmailAddress.setText(user.getEmail(), TextView.BufferType.EDITABLE);
        ((MyProfileActivity)getActivity()).setInvisible();
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
    public void onDetach() {
        super.onDetach();
        ((MyProfileActivity)this.getActivity()).setUpAccountToolbar();
    }


}
