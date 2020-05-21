package rs.elfak.mosis.greenforce;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserData
{
    String email;
    String name;
    String surname;
    String username;
    String phoneNumber;
    @Exclude
    Bitmap userImage;


    public UserData(){}

    public UserData(String email, String name, String surname, String username, String phoneNumber,Bitmap userImage) {
    this.email = email;
    this.name = name;
    this.surname = surname;
    this.username = username;
    this.phoneNumber = phoneNumber;
    this.userImage = userImage;
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
    @Exclude
    public Bitmap getUserImage() {
        return userImage;
    }


}
