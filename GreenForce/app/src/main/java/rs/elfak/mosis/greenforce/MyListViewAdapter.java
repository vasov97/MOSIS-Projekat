package rs.elfak.mosis.greenforce;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyListViewAdapter extends ArrayAdapter<String>
{
    Context context;
    String[] titles;
    int id;
    int layout;
    int textID;

    public MyListViewAdapter(Context context, String[] titles,int textID,int id,int layout)
    {
        super(context, R.layout.nearby_device, titles);
        this.context=context;
        this.titles=titles;
        this.id=id;
        this.layout=layout;
        this.textID=textID;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.nearby_device, parent, false);
        ImageView images = row.findViewById(R.id.imageViewNearbyDevice);
        TextView myTitle = row.findViewById(R.id.textViewDevice);
        //TextView myDescription = row.findViewById(R.id.textView2);

        // now set our resources on views
        images.setImageResource(R.drawable.add_friends_icon);
        myTitle.setText(titles[position]);
        //myDescription.setText(rDescription[position]);




        return row;
    }
}
