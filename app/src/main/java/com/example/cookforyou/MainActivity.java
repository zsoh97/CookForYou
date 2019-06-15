package com.example.cookforyou;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
//    private FirebaseDatabase mDatabase;
    private FirebaseFirestore db;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Handle drawer.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final View headerView = navigationView.getHeaderView(0);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);



        // choose which screen u want to show first.
        displaySelectedScreen(R.id.nav_home);


        //Firestore Setup
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        ArrayList<String> arrayIngredients = new ArrayList<>();
        arrayIngredients.addAll(Arrays.asList(getResources().getStringArray(R.array.array_ingredients)));
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayIngredients);

        // add auth change listener
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                // if logged in, remove from nav bar items
                if(firebaseAuth.getCurrentUser()!=null) {

                    Menu navMenuLogIn = navigationView.getMenu();
                    navMenuLogIn.findItem(R.id.nav_login).setVisible(false);
                    navMenuLogIn.findItem(R.id.nav_register).setVisible(false);
                    navMenuLogIn.findItem(R.id.nav_logout).setVisible(true);
                    navMenuLogIn.findItem(R.id.nav_update_profile).setVisible(true);

                    final TextView displayName =headerView.findViewById(R.id.displayNameTextView);
                    final TextView displayMatric = headerView.findViewById(R.id.displayMatricTextView);
                    final ImageView profilePic = headerView.findViewById(R.id.profileImageView);

                    String userID = mAuth.getCurrentUser().getUid();

                    db.collection("UserDetails").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String name = documentSnapshot.getString("Name");
                            String matricNum = documentSnapshot.getString("MatriculationNumber");
                            displayName.setText(name);
                            displayMatric.setText(matricNum);
                        }
                    });

                    StorageReference mStorageReference = mStorage.getReference();
                    mStorageReference.child(userID).child("images/Profile Picture").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(profilePic);
                        }
                    });


                } else {
                    // show items in nav bar
                    Menu navMenuLogIn = navigationView.getMenu();
                    navMenuLogIn.findItem(R.id.nav_login).setVisible(true);
                    navMenuLogIn.findItem(R.id.nav_register).setVisible(true);
                    navMenuLogIn.findItem(R.id.nav_logout).setVisible(false);
                    navMenuLogIn.findItem(R.id.nav_update_profile).setVisible(false);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //displaying the selected screen
    private void displaySelectedScreen(int id) {

        // creating the fragment
        Fragment fragment = null;


        if (id == R.id.nav_home) {

            fragment = new HomeFragment();
        } else if (id == R.id.nav_register) {
//            fragment = new RegisterFragment();

        } else if (id == R.id.nav_login) {
//            fragment = new LoginFragment();

        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            fragment = new HomeFragment();

        }else if (id == R.id.nav_update_profile){
//            fragment = new ProfileFragment();
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
    }


        @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.


        int id = item.getItemId();

        displaySelectedScreen(id);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    check loginExample for this
}
