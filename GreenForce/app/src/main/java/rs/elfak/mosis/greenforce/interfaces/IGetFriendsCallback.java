package rs.elfak.mosis.greenforce.interfaces;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.models.UserData;

public interface IGetFriendsCallback extends IGetDataCallback {
    void onFriendsReceived(ArrayList<UserData> myFriends);
}
