package rs.elfak.mosis.greenforce.interfaces;
import java.util.ArrayList;

import rs.elfak.mosis.greenforce.models.UserData;

public interface IGetUsersCallback extends IGetDataCallback {
    void onUsersReceived(ArrayList<UserData> allUsers);
    void onUserReceived(UserData user);
}
