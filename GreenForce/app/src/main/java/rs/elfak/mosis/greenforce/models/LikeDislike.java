package rs.elfak.mosis.greenforce.models;

import com.google.firebase.database.Exclude;

import rs.elfak.mosis.greenforce.enums.ReviewType;

public class LikeDislike {
    @Exclude
    String userID;
    ReviewType type;

    public ReviewType getType() {
        return type;
    }

    public void setType(ReviewType type) {
        this.type = type;
    }
    @Exclude
    public String getUserID() {
        return userID;
    }
    @Exclude
    public void setUserID(String userID) {
        this.userID = userID;
    }
}
