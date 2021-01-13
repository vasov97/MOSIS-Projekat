package rs.elfak.mosis.greenforce.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import rs.elfak.mosis.greenforce.R;
import rs.elfak.mosis.greenforce.interfaces.IEditEmailListener;
import rs.elfak.mosis.greenforce.interfaces.IFragmentComponentInitializer;
import rs.elfak.mosis.greenforce.interfaces.ILogOutListener;

public class EditEmailDialog extends AppCompatDialogFragment implements IFragmentComponentInitializer
{
    EditText password;
    String newEmail;
    AlertDialog editEmailDialog;
    private IEditEmailListener listener;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_find_user_on_map,null);


        builder.setView(view)
                .setTitle("Email change request")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                }).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!password.getText().toString().equals(""))
                  listener.onEmailEdit(password.getText().toString(),newEmail);
                else
                    Toast.makeText(getActivity(),"Please enter password!",Toast.LENGTH_LONG).show();

            }
        });
        initializeComponents(view);
        editEmailDialog=builder.create();
        return editEmailDialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener=(IEditEmailListener) context;
    }

    @Override
    public void initializeComponents(View v) {
        password=v.findViewById(R.id.dialog_find_user_username);
        password.setHint("Password");
        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}