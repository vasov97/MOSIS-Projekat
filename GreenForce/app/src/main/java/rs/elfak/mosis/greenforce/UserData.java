package rs.elfak.mosis.greenforce;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserData
{
    String email;
    String name;
    String phoneNumber;
    String points;
    String surname;
    String username;

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
    this.points="0";
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

    public String getPoints(){return points;}

    public void setEmail(String email){ this.email=email;}

    public void setName(String name){ this.name=name;}

    public void setPhoneNumber(String phoneNumber){ this.phoneNumber=phoneNumber;}

    public void setPoints(String points){this.points=points;}

    public void setSurname(String surname){ this.surname=surname;}

    public void setUsername(String username){ this.username=username;}
    @Exclude
    public void setUserImage(Bitmap userImage){this.userImage=userImage;}

    @Exclude
    public Bitmap getUserImage() {
        return userImage;
    }


}
