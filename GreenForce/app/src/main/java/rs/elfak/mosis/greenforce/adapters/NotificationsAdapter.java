package rs.elfak.mosis.greenforce.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.activities.EventActivity;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.EventRequestNotification;
import rs.elfak.mosis.greenforce.models.FriendsRequestNotification;
import rs.elfak.mosis.greenforce.models.MyLatLong;

public class NotificationsAdapter extends ArrayAdapter<Object>
{
    ArrayList<Object> notifications;
    Context context;
    //ArrayList<String> titles;



    public NotificationsAdapter(@NonNull Context context, @NonNull ArrayList<Object> notifications)
    {
        super(context,0, notifications);
        this.context=context;
        this.notifications=notifications;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.notification_layout, parent, false);

        final Object display=notifications.get(position);
        ImageView accept=row.findViewById(R.id.accept_notification);
        ImageView decline=row.findViewById(R.id.decline_notification);
        TextView title = row.findViewById(R.id.notification_title);
        TextView notification = row.findViewById(R.id.notification_text);

        if(display instanceof FriendsRequestNotification)
        {
            final FriendsRequestNotification friendRequest = (FriendsRequestNotification)display;
            title.setText(R.string.friend_request);
            notification.setText(friendRequest.getUsername() + "would like to add as a friend");
            setupFriendRequestRow(accept,decline,position,friendRequest.getSenderUid());
        }
        else if(display instanceof EventRequestNotification){
            final EventRequestNotification eventNotification=(EventRequestNotification)display;
            MyLatLong eventLocation=eventNotification.getEventLocation();
            title.setText(R.string.event_request);
            try {
                notification.setText(eventNotification.getUsername()+" would like to join your event at "+ getEventAddress(eventLocation.getLatitude(),eventLocation.getLongitude()) );
            } catch (IOException e) {
                e.printStackTrace();
            }
            setupEventRequestRow(accept,decline,position,eventNotification.getSenderUid(),eventNotification.getEvenID());
        }
        //dodaj ostale

        return row;
    }



    public Object getNotification(int position){return notifications.get(position);}

    private void removeFromList(int position)
    {
        remove(getItem(position));
        notifyDataSetChanged();
    }

    private void setupFriendRequestRow(ImageView accept, ImageView decline, final int position, final String ID)
    {
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                MyUserManager.getInstance().addFriend(ID);
                MyUserManager.getInstance().deleteNotifications(ID);
                removeFromList(position);
            }
        });
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                MyUserManager.getInstance().deleteNotifications(ID);
                removeFromList(position);
            }
        });
    }
    private void setupEventRequestRow(ImageView accept, ImageView decline, final int position, final String senderUid, final String eventID) {
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                MyUserManager.getInstance().acceptEventRequest(senderUid,eventID);
                MyUserManager.getInstance().deleteEventNotification(senderUid,eventID);
                removeFromList(position);
            }
        });
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                MyUserManager.getInstance().deleteEventNotification(senderUid,eventID);
                removeFromList(position);
            }
        });
    }

    private String getEventAddress(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 3);

        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String country = addresses.get(0).getCountryName();

        return (address + ", " + city + ", "+ country);

    }

}
