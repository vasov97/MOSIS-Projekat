package rs.elfak.mosis.greenforce.interfaces;
import java.util.ArrayList;

import rs.elfak.mosis.greenforce.models.UserData;

public interface IGetAllUsersCallback extends IGetDataCallback {
    void onUsersReceived(ArrayList<UserData> allUsers);
}
