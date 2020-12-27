package rs.elfak.mosis.greenforce.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Timer;
import java.util.TimerTask;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.activities.MyProfileActivity;
import rs.elfak.mosis.greenforce.enums.EventStatus;
import rs.elfak.mosis.greenforce.enums.VolunteerType;
import rs.elfak.mosis.greenforce.interfaces.ICheckEventData;
import rs.elfak.mosis.greenforce.interfaces.IFragmentComponentInitializer;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.MyEvent;
import rs.elfak.mosis.greenforce.models.UserData;

public class DisplayEventInformationOnMapDialog extends BottomSheetDialog implements IFragmentComponentInitializer, View.OnClickListener {
    private MyEvent eventToView;
    private UserData createdByUser;
    private BottomSheetDialog bottomSheetDialog;
    private boolean onScreen;
    private CheckEventData clb;
    Button applyToEvent,viewEventData;
    TextView username,eventPoints,eventStatus;
    ImageView createdByImage;

    public class CheckEventData implements ICheckEventData{

        @Override
        public void onCheckIfVolunteer(boolean volunteer, VolunteerType type) {
            if(volunteer){
                applyToEvent.setEnabled(false);
                applyToEvent.setText(type.toString());
            }else{
                applyToEvent.setEnabled(true);
                applyToEvent.setText(R.string.apply);
            }
            bottomSheetDialog.show();
        }

        @Override
        public void onCheckIfRequestSent(boolean sent) {
            if(sent){
                applyToEvent.setEnabled(false);
                applyToEvent.setText(R.string.pending);
            }else{
                applyToEvent.setText(R.string.apply);
                applyToEvent.setEnabled(true);
            }
            bottomSheetDialog.show();
        }
    }
    public DisplayEventInformationOnMapDialog(@NonNull Context context, UserData createdBy, MyEvent eventToView) {
        super(context);
        this.onScreen=false;
        this.createdByUser=createdBy;
        this.eventToView=eventToView;
        clb=new CheckEventData();
        bottomSheetDialog=new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_display_event_info_on_map,(LinearLayout)findViewById(R.id.bottom_sheet_container_map_event_info));
        initializeComponents(bottomSheetView);
        applyToEvent.setOnClickListener(this);
        viewEventData.setOnClickListener(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                   dismissDialog();
                }
                return true;
            }
        });
    }
    public boolean getOnScreen(){return this.onScreen;}
    public void setEventToView(MyEvent eventToView) {
        this.eventToView = eventToView;
    }

    public void setCreatedByUser(UserData createdByUser) {
        this.createdByUser = createdByUser;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.viewEventApply)
        {
            MyUserManager.getInstance().applyForEvent(eventToView.getEventID());
            applyToEvent.setText(R.string.sent);
            applyToEvent.setEnabled(false);
        }
        else if(v.getId()==R.id.viewEventView)
        {
            //view event info vec pravljen dizajn
        }
    }

    @Override
    public void initializeComponents(View v) {
        applyToEvent=v.findViewById(R.id.viewEventApply);
        viewEventData=v.findViewById(R.id.viewEventView);
        username=v.findViewById(R.id.viewEventCreateByUsername);
        eventPoints=v.findViewById(R.id.viewEventPoint);
        eventStatus=v.findViewById(R.id.viewEventStatus);
        createdByImage=v.findViewById(R.id.viewEventCreateByImage);
    }

    public void showDialog(){
        onScreen=true;
        displayEventData();
    }

    public void dismissDialog(){
        onScreen=false;
        bottomSheetDialog.dismiss();
    }

    public void refreshDialogInformation(){
        displayEventData();
    }
    private void displayEventData() {
        applyToEvent.setEnabled(true);
        applyToEvent.setText(R.string.apply);
        MyUserManager.getInstance().findEventLeaderAndCheckRequest(eventToView.getEventID(),clb);
        username.setText("Created by: "+createdByUser.getUsername());
        eventPoints.setText("Reward: "+eventToView.getEventPoints());
        eventStatus.setText("Event status: "+eventToView.getEventStatus().toString());
        createdByImage.setImageBitmap(createdByUser.getUserImage());
    }
}