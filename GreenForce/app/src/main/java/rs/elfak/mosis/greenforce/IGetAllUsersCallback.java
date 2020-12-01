package rs.elfak.mosis.greenforce;
import java.util.ArrayList;
public interface IGetAllUsersCallback
{
    void onUsersReceived(ArrayList<UserData> allUsers);
}
