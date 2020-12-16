package rs.elfak.mosis.greenforce.models;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Comparator;

@IgnoreExtraProperties
public class UserData
{
    String email;
    String name;
    String phoneNumber;
    String surname;
    String username;
    int points;



    @Exclude
    MyEvent currentEvent;
    @Exclude
    Bitmap userImage;
    @Exclude
    String uuid;
    @Exclude
    MyLatLong myLatLong;



    @Exclude
    long currentRank;


    public UserData(){}

    public UserData(String email, String name, String surname, String username, String phoneNumber,Bitmap userImage) {
    this.email = email;
    this.name = name;
    this.surname = surname;
    this.username = username;
    this.phoneNumber = phoneNumber;
    this.userImage = userImage;
    this.points=0;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getPoints(){return points;}

    @Exclude
    public long getCurrentRank() { return currentRank; }

    @Exclude public MyEvent getCurrentEvent() { return currentEvent; }

    @Exclude public void setCurrentEvent(MyEvent currentEvent) { this.currentEvent = currentEvent; }

    public void setEmail(String email){ this.email=email;}

    public void setName(String name){ this.name=name;}

    public void setPhoneNumber(String phoneNumber){ this.phoneNumber=phoneNumber;}

    public void setPoints(int points){this.points=points;}

    public void setSurname(String surname){ this.surname=surname;}

    @Exclude
    public void setCurrentRank(long currentRank) { this.currentRank = currentRank; }

    public void setUsername(String username){ this.username=username;}
    @Exclude
    public void setUserImage(Bitmap userImage){this.userImage=userImage;}

    @Exclude
    public Bitmap getUserImage() {
        return userImage;
    }

    @Exclude
    public void setUserUUID(String uuid){this.uuid=uuid;}

    @Exclude
    public String getUserUUID() {
        return uuid;
    }
    @Exclude
    public MyLatLong getMyLatLong(){return this.myLatLong;}
    @Exclude
    public void setMyLatLong(MyLatLong myLatLong){this.myLatLong=myLatLong;}

    @Exclude
    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof UserData)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        UserData c = (UserData) o;

        // Compare the data members and return accordingly
        return this.uuid.equals(c.getUserUUID());
    }

    @Exclude
    public static Comparator<UserData> UserPointsComparatorDesc=new Comparator<UserData>() {
        @Override
        public int compare(UserData o1, UserData o2) {
            return o2.getPoints()-o1.getPoints();
        }
    };



}
