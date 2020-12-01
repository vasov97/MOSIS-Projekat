package rs.elfak.mosis.greenforce;
import java.util.ArrayList;
import java.util.Map;

public interface IGetAllUsersCallback
{
    void onUsersReceived(ArrayList<UserData> allUsers);
}
