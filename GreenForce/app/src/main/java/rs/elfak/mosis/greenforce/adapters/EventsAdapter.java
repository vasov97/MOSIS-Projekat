package rs.elfak.mosis.greenforce.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.MyEvent;
import rs.elfak.mosis.greenforce.models.MyLatLong;


public class EventsAdapter extends ArrayAdapter<MyEvent>
{
    Context context;
    ArrayList<MyEvent> events;


    public EventsAdapter(Context context, ArrayList<MyEvent> events)
    {
        super(context, 0,events);
        this.events=events;
        this.context=context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        MyEvent event=getItem(position);
        MyLatLong myLatLong=event.getEventLocation();
        if(convertView==null)
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.listview_events_data,parent,false);
        TextView location = convertView.findViewById(R.id.listView_events_data_location);
        TextView points = convertView.findViewById(R.id.listView_events_data_reward);
        TextView type = convertView.findViewById(R.id.listView_events_data_type);
        try {
            location.setText(getEventAddress(myLatLong.getLatitude(),myLatLong.getLongitude()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        points.setText("Reward: "+event.getEventPoints());
        type.setText(event.getEventTypes().toString());

        return convertView;
    }

    public MyEvent getClickedItem(int position){
        return events.get(position);
    }




    private String getEventAddress(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);

        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        String country = addresses.get(0).getCountryName();
        if(addresses.get(0).getLocality()==null)
            return (address +", "+ country);
        else
           return (address + ", " + city + ", "+ country);

    }

}
