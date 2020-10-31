package rs.elfak.mosis.greenforce;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



public class FragmentMyProfileMain extends Fragment implements IFragmentComponentInitializer{

      TextView myProfileRank;
      TextView myProfilePoints;
      TextView myProfileEmailAddress;
      TextView myProfilePhone;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myProfileMain = inflater.inflate(R.layout.fragment_my_profile_main,container,false);
        initializeComponents(myProfileMain);
        setUpUserData();
        return myProfileMain;
    }

    private void setUpUserData() {
        UserData user=MyUserManager.getInstance().getUser();
        myProfilePhone.setText(user.getPhoneNumber());
        myProfileEmailAddress.setText(user.getEmail());
        ((MyProfileActivity)getActivity()).setVisible();
        //ranking
    }

    @Override
    public void initializeComponents(View v) {
        myProfileRank=v.findViewById(R.id.my_profile_rank);
        myProfilePoints=v.findViewById(R.id.my_profile_points);
        myProfileEmailAddress=v.findViewById(R.id.my_profile_email_address);
        myProfilePhone=v.findViewById(R.id.my_profile_phone_number);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.getActivity().finish();
    }
}
