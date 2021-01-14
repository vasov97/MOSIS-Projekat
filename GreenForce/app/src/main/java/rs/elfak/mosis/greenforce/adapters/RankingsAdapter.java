package rs.elfak.mosis.greenforce.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.collection.LLRBNode;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.UserData;

public class RankingsAdapter extends ArrayAdapter<UserData>
{
    Context context;
    ArrayList<UserData> rankings;

    public RankingsAdapter(@NonNull Context context, @NonNull ArrayList<UserData> rankings)
    {
        super(context,0,rankings);
        this.context=context;
        this.rankings=rankings;
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.ranked_user_layout, parent, false);

        //final Object display=notifications.get(position);
        ImageView rankedUserImage=row.findViewById(R.id.ranked_user_image);
        TextView rank=row.findViewById(R.id.textViewRank);
        TextView username = row.findViewById(R.id.textViewRankedUsername);
        TextView points = row.findViewById(R.id.textViewUserPoints);

        UserData display=getItem(position);
        assert display != null;
        rankedUserImage.setImageBitmap(display.getUserImage());
        rank.setText(Long.toString(position+1));
        username.setText(display.getUsername());
        points.setText(Integer.toString(display.getPoints()));
        if(display.getUserUUID().equals(MyUserManager.getInstance().getCurrentUserUid()))
            row.setBackgroundResource(R.drawable.my_ranked_list_view);




        return row;
    }

    public UserData getRank(int position){return rankings.get(position);}


}
