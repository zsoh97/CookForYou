package com.example.cookforyou.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cookforyou.HomeFragment;
import com.example.cookforyou.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;

public class PasswordFragment extends Fragment {



    private EditText password, currentPw, currentEmail;
    private Button updateBtn;
    private FirebaseUser mUser;
    private String curEmail, curPw, newPw;

    public PasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Update Password");

        currentEmail = getActivity().findViewById(R.id.currentEmailEditText);
        currentPw = getActivity().findViewById(R.id.currentPwEditText);
        password = getActivity().findViewById(R.id.newPwEditText);
        updateBtn = getActivity().findViewById(R.id.savePwBtn);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });
    }

    public void updatePassword(){
        curEmail = currentEmail.getText().toString();
        curPw = currentPw.getText().toString();
        newPw = password.getText().toString();

        if(TextUtils.isEmpty(curEmail)){
            Toast.makeText(getContext().getApplicationContext(), "Current email incorrect", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(curPw)){
            Toast.makeText(getContext().getApplicationContext(), "Current password incorrect", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(newPw)){
            Toast.makeText(getContext().getApplicationContext(), "Please enter a valid new password", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential;

        mUser.updatePassword(newPw).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Password Successfully Changed", Toast.LENGTH_SHORT).show();
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, new HomeFragment());
                    ft.commit();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                    AuthCredential credential = EmailAuthProvider.getCredential(curEmail, curPw);

                    mUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Re-authentication was successful
                                    // Re-attempt secure password update action
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext().getApplicationContext(), "Password update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update_password, container, false);
        return view;
    }

}