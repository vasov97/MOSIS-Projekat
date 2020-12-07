package rs.elfak.mosis.greenforce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.UserData;

public class MyFriendsAdapter extends ArrayAdapter<UserData> implements Filterable
{
    Context context;
    ArrayList<UserData> users;


    public MyFriendsAdapter(Context context, ArrayList<UserData> users)
    {
        super(context, 0,users);
        this.context=context;
        this.users=users;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        UserData user=getItem(position);
        if(convertView==null)
            convertView=LayoutInflater.from(getContext()).inflate(R.layout.friends_friend,parent,false);
        CircleImageView friendImage = convertView.findViewById(R.id.my_friend_profile_image);
        TextView friendName = convertView.findViewById(R.id.my_friend_fullname);
        TextView friendUsername = convertView.findViewById(R.id.my_friend_username);

        friendImage.setImageBitmap(user.getUserImage());
        friendName.setText(user.getName()+" "+user.getSurname());
        friendUsername.setText(user.getUsername());

        return convertView;
    }

    public UserData getClickedItem(int position){
        return users.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults filterResults = new FilterResults();
                if(constraint==null || constraint.length()==0)
                {
                    filterResults.count=users.size();
                    filterResults.values=users;
                }
                else
                {
                    String searchString = constraint.toString().toLowerCase();
                    ArrayList<UserData> resultData = new ArrayList<UserData>();
                    for(UserData user:users)
                    {
                        if(user.getUsername().toLowerCase().contains(searchString))
                        {
                            resultData.add(user);
                        }
                    }

                    filterResults.count=resultData.size();
                    filterResults.values=resultData;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                users= (ArrayList<UserData>)results.values;
                notifyDataSetChanged();

            }
        };
        return filter;
    }
}
