package rs.elfak.mosis.greenforce.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import rs.elfak.mosis.greenforce.activities.MyProfileActivity;
import rs.elfak.mosis.greenforce.interfaces.IGetCurrentRankCallback;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.UserData;
import rs.elfak.mosis.greenforce.interfaces.IFragmentComponentInitializer;


public class FragmentMyProfileMain extends Fragment implements IFragmentComponentInitializer {

      TextView myProfileRank;
      TextView myProfilePoints;
      TextView myProfileEmailAddress;
      TextView myProfilePhone;
      TextView userRank;
      TextView userPoints;
      boolean visitor=false;
    GetUserRank clb;
    public class GetUserRank implements IGetCurrentRankCallback {

        @Override
        public void onRankRetrieved(long rank) {
            userRank.setText(rank+"");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myProfileMain = inflater.inflate(R.layout.fragment_my_profile_main,container,false);
        initializeComponents(myProfileMain);
        visitor=((MyProfileActivity)getActivity()).getVisitor();
        clb=new GetUserRank();
        choseUserToDisplay();
        return myProfileMain;
    }
//    private void choseUserToDisplay() {
//        if(visitor)
//            setUpUserData(MyUserManager.getInstance().getVisitProfile());
//        else
//            setUpUserData(MyUserManager.getInstance().getUser());
//
//    }
    private void choseUserToDisplay() {
        UserData displayUser;
        if(visitor){
            displayUser=MyUserManager.getInstance().getVisitProfile();
        }
        else{
            displayUser=MyUserManager.getInstance().getUser();
        }
        MyUserManager.getInstance().getUsersCurrentRank(displayUser.getUserUUID(),clb);
        setUpUserData(displayUser);
    }

    private void setUpUserData(UserData user) {
        myProfilePhone.setText(user.getPhoneNumber());
        myProfileEmailAddress.setText(user.getEmail());
        userPoints.setText(user.getPoints()+"");
        userRank.setText("");
        ((MyProfileActivity)getActivity()).setVisible();
        //ranking
    }

    @Override
    public void initializeComponents(View v) {
        myProfileRank=v.findViewById(R.id.my_profile_rank);
        myProfilePoints=v.findViewById(R.id.my_profile_points);
        myProfileEmailAddress=v.findViewById(R.id.my_profile_email_address);
        myProfilePhone=v.findViewById(R.id.my_profile_phone_number);
        userRank=v.findViewById(R.id.my_profile_rank);
        userPoints=v.findViewById(R.id.my_profile_points);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        this.getActivity().finish();
    }

}
