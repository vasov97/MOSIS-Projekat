package rs.elfak.mosis.greenforce.dialogs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import de.hdodenhof.circleimageview.CircleImageView;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.activities.MyProfileActivity;
import rs.elfak.mosis.greenforce.interfaces.IFragmentComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IRemoveUserFromFriends;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.UserData;

public class DisplayUserInformationOnMapDialog extends BottomSheetDialog implements IFragmentComponentInitializer, View.OnClickListener
{

    TextView username,name,userPoints,userPhoneNumber,userEmail,userDistance;
    CircleImageView userImage;
    Button sendRequestButton,removeUseButton,viewUserButton;
    IRemoveUserFromFriends listener;
    BottomSheetDialog bottomSheetDialog;

    UserData user;
    public DisplayUserInformationOnMapDialog(@NonNull Context context)
    {
        super(context);
        listener=(IRemoveUserFromFriends)context;

    }
    public void showDialog(UserData user,boolean isFriend){
        this.user=user;
        bottomSheetDialog=new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_bottom_sheet,(LinearLayout)findViewById(R.id.bottom_sheet_container));
        initializeComponents(bottomSheetView);
        selectButtons(isFriend);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void selectButtons(boolean isFriend)
    {
        if(isFriend)
        {
            sendRequestButton.setVisibility(View.INVISIBLE);
            sendRequestButton.setEnabled(false);
            removeUseButton.setOnClickListener(this);
            viewUserButton.setOnClickListener(this);
        }
        else
        {
            removeUseButton.setVisibility(View.INVISIBLE);
            removeUseButton.setEnabled(false);
            viewUserButton.setVisibility(View.INVISIBLE);
            viewUserButton.setEnabled(false);
            sendRequestButton.setOnClickListener(this);
        }
        setUpData(isFriend);
    }

    private void setUpData(boolean isFriend) {
        username.setText(user.getUsername());
        name.setText(user.getName()+" "+user.getSurname());
        userPoints.setText(user.getPoints().toString()+" points");
        userImage.setImageBitmap(user.getUserImage());
        if(isFriend){
            userEmail.setText("Mail: "+user.getEmail());
            userPhoneNumber.setText("Phone Number: "+user.getPhoneNumber());
        }
    }

    @Override
    public void onClick(View v)
    {
       if(v.getId()==R.id.sendRequestButton)
       {
           Toast.makeText(getContext(), "Send request ", Toast.LENGTH_SHORT).show();
       }
       else if(v.getId()==R.id.viewUserButton)
       {
           MyUserManager.getInstance().setVisitProfile(user);
           Intent i=new Intent(getContext(), MyProfileActivity.class);
           i.putExtra("Visit","Visit");
           getContext().startActivity(i);
       }
       else if(v.getId()==R.id.removeUserButton)
           removeUserFromFriends();
    }

    private void removeUserFromFriends()
    {
       listener.onUserRemoved(user);
    }
    public void dismissDialog(){
        bottomSheetDialog.dismiss();
    }

    @Override
    public void initializeComponents(View v) {
        name=v.findViewById(R.id.viewName);
        username=v.findViewById(R.id.viewUsername);
        userPoints=v.findViewById(R.id.viewUserPoints);
        userPhoneNumber=v.findViewById(R.id.viewUserPhoneNumber);
        userEmail=v.findViewById(R.id.viewUserEmail);
        userDistance=v.findViewById(R.id.bottom_sheet_dialog_time);
        userImage=v.findViewById(R.id.viewUserImage);
        sendRequestButton=v.findViewById(R.id.sendRequestButton);
        removeUseButton=v.findViewById(R.id.removeUserButton);
        viewUserButton=v.findViewById(R.id.viewUserButton);
    }
}
