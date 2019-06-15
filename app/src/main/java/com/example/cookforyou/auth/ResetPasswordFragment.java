package com.example.cookforyou.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cookforyou.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordFragment extends Fragment {

    private EditText emailEditText;
    private Button resetBtn;
//    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Reset Password");

        emailEditText = getActivity().findViewById(R.id.resetEmailEditText);
        resetBtn = getActivity().findViewById(R.id.resetBtn);

        mAuth = FirebaseAuth.getInstance();

//        progressBar = getActivity().findViewById(R.id.resetprogressBar);


        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetUserAccount();
            }
        });


    }

    private void resetUserAccount() {
//        progressBar.setVisibility(View.VISIBLE);

        String email;
        email = emailEditText.getText().toString().trim();


        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity().getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }

        //your resetpasswordcode here
        if(TextUtils.isEmpty(email)){
            Toast.makeText(getActivity().getApplicationContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getActivity().getApplicationContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, new LoginFragment());
                        ft.commit();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Error in sending password reset", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public ResetPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_resetpassword, container, false);

        return view;
    }


}
