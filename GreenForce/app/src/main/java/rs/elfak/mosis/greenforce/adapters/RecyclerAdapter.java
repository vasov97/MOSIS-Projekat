package rs.elfak.mosis.greenforce.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import rs.elfak.mosis.greenforce.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ImageViewHolder>
{
    ArrayList<Bitmap> images;

    public RecyclerAdapter(ArrayList<Bitmap> images)
    {
        this.images=images;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view_layout,parent,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position)
    {
       Bitmap image = images.get(position);
       holder.eventPhoto.setImageBitmap(image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void removeFromList(int position)
    {
        images.remove(position);
        notifyDataSetChanged();
    }

    public void addToList()
    {
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder
    {
        ImageView eventPhoto;
        public ImageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            eventPhoto=itemView.findViewById(R.id.event_image_view);
        }
    }
}
