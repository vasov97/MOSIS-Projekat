package rs.elfak.mosis.greenforce.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.EventsAdapter;
import rs.elfak.mosis.greenforce.adapters.RecyclerAdapter;
import rs.elfak.mosis.greenforce.enums.EventImageType;
import rs.elfak.mosis.greenforce.enums.ReviewType;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.IGetEventsCallback;
import rs.elfak.mosis.greenforce.managers.MyUserManager;
import rs.elfak.mosis.greenforce.models.EventVolunteer;
import rs.elfak.mosis.greenforce.models.LikeDislike;
import rs.elfak.mosis.greenforce.models.MyEvent;

public class LikeDislikeEvent extends AppCompatActivity implements IComponentInitializer, View.OnClickListener {
    String eventID;
    ArrayList<String> eventVolunteers;
    ArrayList<Bitmap> beforePhotos,afterPhotos,displayedPhotos;
    GetBeforeImages getEventVolunteersAndBeforeImagesCallback;
    GetAfterImagesCallback getAfterImagesCallback;
    RecyclerAdapter photosAdapter;
    boolean volunteer;
    int beforeCount,afterCount;
    int likeCount=0,dislikeCount=0;
    private Button likeButton,dislikeButton;
    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private LinearLayout buttonsLayout,likesLayout;
    private TextView likeView,dislikeView;


    TabLayout.OnTabSelectedListener myTabSelectedListener=new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
                case 0:displayBeforePhotos();break;
                case 1:displayAfterPhotos();break;
                default:break;
            }
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) { }
        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };



    public class GetBeforeImages implements IGetEventsCallback{
        @Override
        public void onEventsReceived(ArrayList<MyEvent> events) {

        }

        @Override
        public void onSingleEventReceived(MyEvent event) {

        }

        @Override
        public void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {

        }

        @Override
        public void onCompletedEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {

        }

        @Override
        public void onEventImagesReceived(ArrayList<Bitmap> images) {
             beforePhotos=images;
            displayBeforePhotos();
        }

        @Override
        public void onEventVolunteersReceived(ArrayList<EventVolunteer> volunteers) {
            if(volunteers!=null){
               for(EventVolunteer v : volunteers)
                   eventVolunteers.add(v.getId());
            }
            if(eventVolunteers.contains(MyUserManager.getInstance().getCurrentUserUid())){
                volunteer=true;
               buttonsLayout.setVisibility(View.INVISIBLE);
               likesLayout.setVisibility(View.VISIBLE);
            }else
                volunteer=false;
           MyUserManager.getInstance().getEventReviews(eventID,getEventVolunteersAndBeforeImagesCallback);

        }

        @Override
        public void onLikeDislikeReceived(ArrayList<LikeDislike> list) {
            if(volunteer){
                calculateLikesAndDislikes(list);
            }else{
                checkIfAlreadyVoted(list);
            }

        }
    }




    public class GetAfterImagesCallback implements IGetEventsCallback{

        @Override
        public void onEventsReceived(ArrayList<MyEvent> events) {

        }

        @Override
        public void onSingleEventReceived(MyEvent event) {

        }

        @Override
        public void onCurrentEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {

        }

        @Override
        public void onCompletedEventsMapReceived(HashMap<String, EventVolunteer> currentEventsRole) {

        }

        @Override
        public void onEventImagesReceived(ArrayList<Bitmap> images) {
             afterPhotos=images;
        }

        @Override
        public void onEventVolunteersReceived(ArrayList<EventVolunteer> volunteers) {

        }

        @Override
        public void onLikeDislikeReceived(ArrayList<LikeDislike> list) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_dislike_event);
        initializeComponents();
        eventID=getIntent().getStringExtra("eventID");
        afterCount= Integer.parseInt(getIntent().getStringExtra("afterCount"));
        beforeCount= Integer.parseInt(getIntent().getStringExtra("beforeCount"));
        setUpActionBar(R.string.review);
        MyUserManager.getInstance().getEventVolunteers(eventID,getEventVolunteersAndBeforeImagesCallback);
        try{
            MyUserManager.getInstance().getEventImages(eventID, EventImageType.BEFORE,beforeCount,getEventVolunteersAndBeforeImagesCallback);
        }catch(IOException e){

        }
        try{
            MyUserManager.getInstance().getEventImages(eventID, EventImageType.AFTER,afterCount,getAfterImagesCallback);
        }catch(IOException e){

        }



        tabLayout.addOnTabSelectedListener(myTabSelectedListener);
        dislikeButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);


    }
    private void setUpActionBar(int rid)
    {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(rid);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }


    @Override
    public void initializeComponents() {
        recyclerView=findViewById(R.id.likeDislike_photos_recycler);
        toolbar=findViewById(R.id.likeDislike_toolbar);
        likeButton=findViewById(R.id.likeDislike_btn_like);
        dislikeButton=findViewById(R.id.likeDislike_btn_dislike);
        buttonsLayout=findViewById(R.id.likeDislike_layout_buttons);
        likesLayout=findViewById(R.id.likeDislike_layout_likeDislike);
        tabLayout=findViewById(R.id.likeDislike_tabLayout);
        likeView=findViewById(R.id.likeDislike_likes);
        dislikeView=findViewById(R.id.likeDislike_dislikes);
        getEventVolunteersAndBeforeImagesCallback=new GetBeforeImages();
        getAfterImagesCallback=new GetAfterImagesCallback();
        eventVolunteers=new ArrayList<>();
        displayedPhotos=new ArrayList<>();
        likeView.setText("Likes: "+likeCount);
        dislikeView.setText("Dislikes: "+dislikeCount);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.likeDislike_btn_dislike){
         MyUserManager.getInstance().reviewEvent(eventID,MyUserManager.getInstance().getCurrentUserUid(),ReviewType.DISLIKE);
        }
        else if(v.getId()==R.id.likeDislike_btn_like){
            MyUserManager.getInstance().reviewEvent(eventID,MyUserManager.getInstance().getCurrentUserUid(),ReviewType.LIKE);
        }

    }


    private void addElementsToDisplay(ArrayList<Bitmap> list) {
        if(displayedPhotos==null)
            displayedPhotos=new ArrayList<>();
        displayedPhotos.clear();
        for(Bitmap bitmap : list)
            displayedPhotos.add(bitmap);
    }

    private void displayBeforePhotos() {
        addElementsToDisplay(beforePhotos);
        setAdapter();
    }

    private void displayAfterPhotos() {
        addElementsToDisplay(afterPhotos);
        setAdapter();
    }
    private void setAdapter(){
        if(photosAdapter==null){
            photosAdapter=new RecyclerAdapter(displayedPhotos);
            recyclerView.setAdapter(photosAdapter);
        }
        else
        {
            photosAdapter.notifyDataSetChanged();
        }
    }

    private void checkIfAlreadyVoted(ArrayList<LikeDislike> list) {
        boolean voted=false;
        if(list!=null){
            for(LikeDislike likeDislike : list){
                if(likeDislike.getUserID().equals(MyUserManager.getInstance().getCurrentUserUid()))
                    voted=true;
            }
        }

        if(voted){
            calculateLikesAndDislikes(list);
            buttonsLayout.setVisibility(View.INVISIBLE);
            likesLayout.setVisibility(View.VISIBLE);
        }
    }

    private void calculateLikesAndDislikes(ArrayList<LikeDislike> list) {
        if(list!=null){
            likeCount=0;
            dislikeCount=0;
            for(LikeDislike likeDislike : list){
                if(likeDislike.getType()== ReviewType.LIKE)
                    likeCount++;
                else if(likeDislike.getType()==ReviewType.DISLIKE)
                    dislikeCount++;
            }
        }
        likeView.setText("Likes: "+likeCount);
        dislikeView.setText("Dislikes: "+dislikeCount);
    }

}