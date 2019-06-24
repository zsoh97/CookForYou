package com.example.cookforyou;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.cookforyou.auth.LoginFragment;
import com.example.cookforyou.auth.RegisterFragment;

public class WelcomeFragment extends Fragment {

    private Button mLoginButton;
    private TextView mNewUser;

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome, container, false);

        mLoginButton = v.findViewById(R.id.fragment_welcome_login_button);
        mNewUser = v.findViewById(R.id.fragment_welcome_new_user);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFragmentTransaction(new LoginFragment());
            }
        });

        mNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFragmentTransaction(new RegisterFragment());
            }
        });

        return v;
    }

    private void handleFragmentTransaction(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up,
                        R.anim.slide_out_up,
                        R.anim.slide_in_down,
                        R.anim.slide_out_down)
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }
}
