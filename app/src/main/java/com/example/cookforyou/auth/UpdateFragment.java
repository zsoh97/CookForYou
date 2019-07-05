package com.example.cookforyou.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cookforyou.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class UpdateFragment extends Fragment {

    private ImageView profilePic;
    private EditText newName;
    private Button saveBtn;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private FirebaseFirestore db;
    private StorageReference mStorageReference;
    private String name, uid;
    private static int PICK_IMAGE = 1234;
    Uri imagePath;

    public static final String userDetailsKey = "UserDetails";
    public static final String nameKey = "Name";
    private boolean profileChanged = false;

    public UpdateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Update Profile");

        profilePic = getActivity().findViewById(R.id.updateImageView);
        newName = getActivity().findViewById(R.id.updateNameEditText);
        saveBtn = getActivity().findViewById(R.id.saveBtn);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mDatabase = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = mAuth.getUid();

        mStorageReference.child(uid).child("images/Profile Picture").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profilePic);
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { updateDetails();}
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData()!=null){
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imagePath);
                profilePic.setImageBitmap(bitmap);
                profileChanged = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void updateDetails(){

        name = newName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "Please enter new Name...", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String,Object> data = new HashMap<>();

        data.put(nameKey,name);

        db.collection(userDetailsKey).document(uid).update(data);

        if(profileChanged) {
            mStorageReference.child(uid).child("images/Profile Picture").delete();
            StorageReference imageReference = mStorageReference.child(uid).child("images").child("Profile Picture");
            imageReference.putFile(imagePath);
        }

        Toast.makeText(getActivity().getApplicationContext(), "Profile Update Successful!", Toast.LENGTH_SHORT).show();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new ProfileFragment());
        ft.commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_update, container, false);
        return view;
    }
}

