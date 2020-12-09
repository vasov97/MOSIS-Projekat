package rs.elfak.mosis.greenforce.interfaces;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.models.FriendsRequestNotification;

public interface IGetNotifications {
   void onFriendRequestsReceived(ArrayList<FriendsRequestNotification> notifications);
}
