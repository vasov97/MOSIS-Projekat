package rs.elfak.mosis.greenforce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.FriendsRequestNotification;

public class NotificationsAdapter extends ArrayAdapter<Object>
{
    ArrayList<Object> notifications;
    Context context;

    public NotificationsAdapter(@NonNull Context context, @NonNull ArrayList<Object> notifications)
    {
        super(context,0, notifications);
        this.context=context;
        this.notifications=notifications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.nearby_device, parent, false);
        TextView notification = row.findViewById(R.id.textViewDevice);
        Object display=notifications.get(position);
        if(display instanceof FriendsRequestNotification)
        {
            notification.setText(((FriendsRequestNotification) display).getUsername() + "would like to add you as a friend");
        }
        //dodaj ostale

        return row;
    }

    public Object getNotification(int position){return notifications.get(position);}
}
