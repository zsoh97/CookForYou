package com.example.cookforyou.auth;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cookforyou.HomeFragment;
import com.example.cookforyou.R;
import com.example.cookforyou.animation.Animations;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment  {

    private EditText emailEditText, passwordEditText;
    private ImageView mWelcomeImage;
    private TextView mWelcomeBackText;
    private Button loginBtn;
    private TextView forgotPw;
    private LinearLayout mLoginPageLayout, mLoginWaitingPageLayout, mWelcomeBackPageLayout;

    private FirebaseAuth mAuth;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Login");
        mAuth = FirebaseAuth.getInstance();

        //instantiate component here
        emailEditText = getActivity().findViewById(R.id.loginEmailEditText);
        passwordEditText = getActivity().findViewById(R.id.loginPasswordEditText);
        loginBtn = getActivity().findViewById(R.id.loginBtn);
        forgotPw = getActivity().findViewById(R.id.fpTextView);
        mLoginPageLayout = getActivity().findViewById(R.id.login_page_view);
        mLoginWaitingPageLayout = getActivity().findViewById(R.id.login_waiting_page_view);
        mWelcomeBackPageLayout = getActivity().findViewById(R.id.welcome_back_page_view);
        mWelcomeBackText = getActivity().findViewById(R.id.welcome_back_text_view);
        mWelcomeImage = getActivity().findViewById(R.id.welcome_back_image_view);

        Glide.with(getActivity())
                .load(R.drawable.welcome_back_image)
                .into(mWelcomeImage);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });

        forgotPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                ft.replace(R.id.content_frame, new ResetPasswordFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });


    }

    private void loginUserAccount() {
        String email, password;
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        if (email.isEmpty() && password.isEmpty()) {
            emailEditText.setError("This field cannot be blank");
            passwordEditText.setError("This field cannot be blank");
            return;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("This field cannot be blank");
            return;
        }
        if(email.isEmpty()) {
            emailEditText.setError("This field cannot be blank");
            return;
        }

        beginSigninView();
        //your login code here....
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            completeSignin();
                        } else {
                            Log.d("testing ", "onComplete: " + task.toString());
                            failedSigninView();
                        }
                    }
                });
    }

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        return view;
    }

    private void beginSigninView() {
        Animations.crossfade(mLoginPageLayout, mLoginWaitingPageLayout,
                getResources().getInteger(android.R.integer.config_shortAnimTime), null);
    }

    private void completeSignin() {
        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginWaitingPageLayout.setVisibility(View.GONE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                        ft.replace(R.id.content_frame, new HomeFragment());
                        ft.commit();
                    }
                }, 2000);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        mWelcomeBackText.setText(getString(R.string.welcome_message, mAuth.getCurrentUser().getDisplayName()));
        Animations.crossfade(mLoginWaitingPageLayout,
                mWelcomeBackPageLayout,
                getResources().getInteger(android.R.integer.config_shortAnimTime),
                listener);

    }

    private void failedSigninView() {
        Toast.makeText(getActivity().getApplicationContext(), "Login Failed. Incorrect username or password", Toast.LENGTH_SHORT).show();
        Animations.crossfade(mLoginWaitingPageLayout, mLoginPageLayout,
                getResources().getInteger(android.R.integer.config_shortAnimTime),
                null);
    }


}
