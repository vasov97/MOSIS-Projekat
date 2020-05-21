package rs.elfak.mosis.greenforce;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentRegistrationImage extends Fragment implements IFragmentComponentInitializer,View.OnClickListener
{
    private TextView uploadPhoto;
    private Button finish;
    private CircleImageView profilePhoto;
    private Uri imageUri;
    private Bitmap imageBitmap;

    private static final int PICK_IMAGE=1;

    private FragmentRegistrationImageListener listener;

    public interface  FragmentRegistrationImageListener
    {
        void onInputImageSent(Bitmap imageBitmap);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View registrationImageView = inflater.inflate(R.layout.fragment_registration_image,container,false);

        initializeComponents(registrationImageView);

        finish.setOnClickListener(this);
        uploadPhoto.setOnClickListener(this);

        return registrationImageView;
    }

    @Override
    public void initializeComponents(View v)
    {
        uploadPhoto = v.findViewById(R.id.textViewUploadPhoto);
        finish = v.findViewById(R.id.FinishButton);
        profilePhoto = v.findViewById(R.id.registrationImage);
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.textViewUploadPhoto)
        {
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(gallery,"Select image"),PICK_IMAGE);
        }
        else if(v.getId() == R.id.FinishButton)
        {
            listener.onInputImageSent(imageBitmap);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK)
        {
            imageUri = data.getData();
            try
            {
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),imageUri);
                profilePhoto.setImageBitmap(imageBitmap);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof FragmentRegistrationImageListener)
            listener=(FragmentRegistrationImageListener)context;
        else{
            throw new RuntimeException(context.toString()+" must implement FragmentRegistrationImageListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener=null;
    }
}
