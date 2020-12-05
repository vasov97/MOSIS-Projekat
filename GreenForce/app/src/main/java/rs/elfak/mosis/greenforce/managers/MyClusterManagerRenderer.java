package rs.elfak.mosis.greenforce.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.models.ClusterMarker;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker>
{
    private final IconGenerator iconGenerator;
    private ImageView imageView;
    private final int markerHeight;
    private final int markerWidth;

    public IconGenerator getIconGenerator() {
        return iconGenerator;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public int getMarkerHeight() {
        return markerHeight;
    }

    public int getMarkerWidth() {
        return markerWidth;
    }



    public MyClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager)
    {
        super(context, map, clusterManager);

        iconGenerator=new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        markerWidth=(int)context.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight=(int)context.getResources().getDimension(R.dimen.custom_marker_image);

        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth,markerHeight));
        int topBottomPadding = (int)context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(0,topBottomPadding,0,topBottomPadding);

        iconGenerator.setContentView(imageView);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions)
    {
        imageView.setImageBitmap(item.getUserData().getUserImage());
        Bitmap icon = iconGenerator.makeIcon();
        //icon.setWidth(27);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).
                title(item.getTitle());
    }


    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMarker> cluster)
    {
        return  false;
    }
    /**
     * Update the GPS coordinate of a ClusterItem
     * @param clusterMarker
     */
    public void setUpdateMarker(ClusterMarker clusterMarker, LatLng newLatlng) {
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            clusterMarker.setPosition(newLatlng);
            marker.setPosition(clusterMarker.getPosition());
        }
    }
    public void setMarkerVisibility(ClusterMarker clusterMarker,boolean visible){
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setVisible(visible);
        }
    }

    public void setTag(ClusterMarker clusterMarker)
    {
        Marker marker = getMarker(clusterMarker);
        if(marker!=null)
            marker.setTag(clusterMarker.getUserData());
    }


}
