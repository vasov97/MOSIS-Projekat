package rs.elfak.mosis.greenforce.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.adapters.RecyclerAdapter;
import rs.elfak.mosis.greenforce.enums.EventStatus;
import rs.elfak.mosis.greenforce.interfaces.IComponentInitializer;
import rs.elfak.mosis.greenforce.managers.MyUserManager;

import static rs.elfak.mosis.greenforce.Constants.MAX_IMG_COUNT;

public class UploadPhotoActivity extends AppCompatActivity implements IComponentInitializer, View.OnClickListener
{

   Button uploadPhoto;
   Button cancelUpload;
   Button finishUpload;
   Toolbar toolbar;
   RecyclerView recyclerView;
   private RecyclerView.LayoutManager layoutManager;
   private ArrayList<Bitmap> images;
   private RecyclerAdapter recyclerAdapter;


   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.event_upload_photo);
      initializeComponents();
      setUpActionBar(R.string.upload_photo);
      uploadPhoto.setOnClickListener(this);
      cancelUpload.setOnClickListener(this);
      finishUpload.setOnClickListener(this);
      recyclerAdapter=new RecyclerAdapter(images);
      recyclerView.setAdapter(recyclerAdapter);
   }

   @Override
   public void initializeComponents()
   {
     uploadPhoto=findViewById(R.id.upload_photo_button);
     cancelUpload=findViewById(R.id.mark_spot_cancel);
     finishUpload=findViewById(R.id.mark_spot_finish);
     toolbar=findViewById(R.id.upload_photo_toolbar);
     recyclerView=findViewById(R.id.upload_photos_recycler);
     layoutManager=new GridLayoutManager(this,2);
     recyclerView.setHasFixedSize(true);
     recyclerView.setLayoutManager(layoutManager);
     images = new ArrayList<>();
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
   public void onClick(View v)
   {
      if(v.getId()==R.id.upload_photo_button)
         uploadPhotos();
      else if(v.getId()==R.id.mark_spot_cancel)
         this.finish();
      else if(v.getId()==R.id.mark_spot_finish){
         createEvent();
         startActivity(new Intent(this,HomePageActivity.class));
      }

   }

   private void createEvent() {
      if(images.size()!=0){
         MyUserManager.getInstance().getUser().getCurrentEvent().setEventPhotos(images);
         MyUserManager.getInstance().getUser().getCurrentEvent().setEventStatus(EventStatus.AVAILABLE);
         MyUserManager.getInstance().saveEvent();
      }
   }

   private void uploadPhotos()
   {
      int emptyImages=MAX_IMG_COUNT-images.size();
      if(emptyImages!=0) {
         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                 PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
         }

         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
         intent.setType("image/*");
         startActivityForResult(intent, 1);
      }else{
         Toast.makeText(this, "An event can only have 8 photos, please delete some in order to insert new photos.", Toast.LENGTH_LONG).show();
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == 1 && resultCode == RESULT_OK) {
         ClipData clipData = data.getClipData();
         if (clipData != null) {
            int selectedCount=clipData.getItemCount();
            int emptyCount=MAX_IMG_COUNT-images.size();
            if(emptyCount>=selectedCount){
            for (int i = 0; i <selectedCount; i++) {
               Uri imageURI = clipData.getItemAt(i).getUri();
               try {
                  InputStream inputStream = getContentResolver().openInputStream(imageURI);
                  Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                  images.add(bitmap);
               } catch (FileNotFoundException e) {
                  e.printStackTrace();
               }
             }
            recyclerAdapter.notifyDataSetChanged();
            }else{
               Toast.makeText(this, "You can only add "+emptyCount+" more photos.", Toast.LENGTH_SHORT).show();
            }
         } else {
            Uri imageURI = data.getData();
            int selectedCount=1;
            int emptyCount=MAX_IMG_COUNT-images.size();
            if(emptyCount>=selectedCount){
                  try {
                     InputStream inputStream = getContentResolver().openInputStream(imageURI);
                     Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                     images.add(bitmap);
                  } catch (FileNotFoundException e) {
                     e.printStackTrace();
               }
               recyclerAdapter.notifyDataSetChanged();
            }
         }
      }
   }
}
