package com.example.cookforyou.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cookforyou.R;
import com.example.cookforyou.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";

    private static int PICK_IMAGE = 123;

    private ImageView profilePic;
    private EditText emailEditText, passwordEditText, nameEditText;
    private Button regBtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private StorageReference mStorageReference;
    Uri imagePath;

    String email, password, name;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Register");
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        //initialise components and look for them according to their IDs
        profilePic = getActivity().findViewById(R.id.profileImageView);
        emailEditText = getActivity().findViewById(R.id.emailEditText);
        passwordEditText = getActivity().findViewById(R.id.password);
        nameEditText = getActivity().findViewById(R.id.name);

        regBtn = getActivity().findViewById(R.id.register);
        progressBar = getActivity().findViewById(R.id.progressBar);
        mStorageReference = mStorage.getReference();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData()!=null){
            imagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imagePath);
                profilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void registerNewUser() {
        progressBar.setVisibility(View.VISIBLE);
        //get the actual String or text that the user type
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        name = nameEditText.getText().toString();
        final StorageReference mRef = mStorage.getReference();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity().getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity().getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getActivity().getApplicationContext(), "Please enter name!", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity().getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            String uid = mAuth.getCurrentUser().getUid();
                            UserDetails currentUser = new UserDetails(name, uid);
                            currentUser.createEntry();
                            sendUserData();
                            mAuth.signOut();
                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.content_frame, new LoginFragment());
                            ft.commit();
                        }
                        else {

                            Log.d("testing ", "onComplete: " + task.toString());
                            Toast.makeText(getActivity().getApplicationContext(), "Registration failed!" + task.getException().toString(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }


    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        return view;
    }

    private void sendUserData(){
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mDatabase.getReference(uid);
        StorageReference imageReference = mStorageReference.child(uid).child("images").child("Profile Picture");
        imageReference.putFile(imagePath);
    }

    private void sendEmailVerification(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(mAuth!=null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity().getApplicationContext(), "Successfully registered, verification email has been sent", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        UserDetails currentUser = new UserDetails(name);
                        currentUser.createEntry();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, new LoginFragment());
                        ft.commit();

                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Verification mail not sent", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
