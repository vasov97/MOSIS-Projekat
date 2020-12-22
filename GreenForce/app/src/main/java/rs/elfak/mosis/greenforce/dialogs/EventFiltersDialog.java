package rs.elfak.mosis.greenforce.dialogs;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.xeoh.android.checkboxgroup.CheckBoxGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.enums.EventTypes;
import rs.elfak.mosis.greenforce.interfaces.IApplyEventFilters;
import rs.elfak.mosis.greenforce.interfaces.IFragmentComponentInitializer;
import rs.elfak.mosis.greenforce.models.MyEvent;
import rs.elfak.mosis.greenforce.models.MyLatLong;


public class EventFiltersDialog extends BottomSheetDialog implements IFragmentComponentInitializer, View.OnClickListener {

    IApplyEventFilters listener;
    TextView clearFilters;
    CheckBox waterPollution,landPollution,reforestation,other;
    Button apply;
    EditText postedBy,from,to,city;
    BottomSheetDialog bottomSheetDialog;
    ArrayList<String> eventValues;
    ArrayList<EventTypes> filteredTypes;
    ArrayList<MyEvent> events;
    HashMap<String, EventTypes> nameToTypesMap;
    CheckBoxGroup<String> checkBoxGroup;

    CheckBoxGroup.CheckedChangeListener<String> myCheckBoxListener = new CheckBoxGroup.CheckedChangeListener<String>() {
        @Override
        public void onCheckedChange(ArrayList<String> values)
        {
            eventValues=values;
            ArrayList<EventTypes> newTypes=new ArrayList<EventTypes>();
            for(String i:values){
                EventTypes type=nameToTypesMap.get(i);
                if(type!=null){
                    newTypes.add(type);
                }
            }
            filteredTypes=newTypes;
        }
    };

    public EventFiltersDialog(@NonNull Context context,ArrayList<MyEvent> events) {
        super(context);
        listener=(IApplyEventFilters)context;
        this.events=events;
        bottomSheetDialog=new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
        View bottomSheetView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_events_filters,(LinearLayout)findViewById(R.id.bottom_sheet_container_events_filter));
        initializeComponents(bottomSheetView);
        checkBoxesSetup();
        bottomSheetDialog.setContentView(bottomSheetView);
        clearFilters.setOnClickListener(this);
        apply.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
         int id=v.getId();
         if(id==R.id.event_filters_apply){
             applyFilters();
             listener.appliedFilters();
             dismissDialog();
         }else if(id==R.id.clear_all_filters){
             clearAllFilters(events);
         }
    }

    private void applyFilters() {
        for(MyEvent e : events){
            checkAllFilters(e);
        }
    }

    @Override
    public void initializeComponents(View v) {
        apply=v.findViewById(R.id.event_filters_apply);
        clearFilters=v.findViewById(R.id.clear_all_filters);
        postedBy=v.findViewById(R.id.event_filters_posted_by);
        from=v.findViewById(R.id.events_filter_from);
        to=v.findViewById(R.id.events_filter_to);
        city=v.findViewById(R.id.events_filter_city);
        waterPollution=v.findViewById(R.id.filters_checkbox_water_pollution);
        landPollution=v.findViewById(R.id.filters_checkbox_land_pollution);
        reforestation=v.findViewById(R.id.filters_checkbox_reforestation);
        other=v.findViewById(R.id.filters_checkbox_other);
        filteredTypes=new ArrayList<EventTypes>();
    }
    public void showDialog(){
        bottomSheetDialog.show();
    }
    public void dismissDialog(){
        bottomSheetDialog.dismiss();
    }

    private void checkBoxesSetup()
    {
        HashMap<CheckBox,String> checkBoxes = new HashMap<>();
        checkBoxes.put(landPollution,"land");
        checkBoxes.put(waterPollution,"water");
        checkBoxes.put(reforestation,"reforestation");
        checkBoxes.put(other,"other");
        checkBoxGroup= new CheckBoxGroup<>(checkBoxes,myCheckBoxListener);
        nameToTypesMap=new HashMap<String,EventTypes>();
        nameToTypesMap.put("land",EventTypes.LAND_POLLUTION);
        nameToTypesMap.put("water",EventTypes.WATER_POLLUTION);
        nameToTypesMap.put("reforestation",EventTypes.REFORESTATION);
        nameToTypesMap.put("other",EventTypes.OTHER);
    }
    private String getEventAddressCity(double latitude,double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        return addresses.get(0).getLocality();
    }

    private void clearAllFilters(ArrayList<MyEvent> list){
        setDefaultValues();
        for(MyEvent e: list){
           listener.enableMarker(e);
        }
    }

    private void setDefaultValues() {
        postedBy.setText("");
        from.setText("");
        to.setText("");
        city.setText("");
        landPollution.setChecked(false);
        waterPollution.setChecked(false);
        reforestation.setChecked(false);
        other.setChecked(false);
    }

    private void checkAllFilters(MyEvent e){
        checkPostedByFilter(e);
        checkFromFilter(e);
        checkToFilter(e);
        try {
            checkCityFilter(e);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        checkTypesFilter(e);
    }
    private void checkPostedByFilter(MyEvent e){
        String postedByString=postedBy.getText().toString();
        if(!postedByString.equals("")){
            if(!listener.checkIfPostedBy(postedByString,e.getEventID())){
                listener.disableMarker(e);
            }
        }
    }
    private void checkFromFilter(MyEvent e){
        if(!from.getText().toString().equals("")){
            int points=Integer.parseInt(from.getText().toString());
            if(points>0){
                if(e.getEventPoints()<=points)
                    listener.disableMarker(e);
            }
        }
    }
    private void checkToFilter(MyEvent e){
        if(!to.getText().toString().equals("")){
            int points=Integer.parseInt(to.getText().toString());
            if(points>0){
                if(e.getEventPoints()>=points)
                    listener.disableMarker(e);
            }
        }
    }
    private void checkCityFilter(MyEvent e) throws IOException {
        String cityString=city.getText().toString();
        if(!cityString.equals("")){
            MyLatLong latlng=e.getEventLocation();
            String myCity=getEventAddressCity(latlng.getLatitude(),latlng.getLongitude());
            if(!myCity.equals(cityString))
                listener.disableMarker(e);
        }
    }
    private void checkTypesFilter(MyEvent e){
        if(filteredTypes.size()!=0){
            for(EventTypes type : filteredTypes){
                if(!e.getEventTypes().contains(type)){
                    listener.disableMarker(e);
                    break;
                }
            }
        }
    }
}
