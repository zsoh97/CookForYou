package com.example.cookforyou;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements Dialog.AddIngredientDialogListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView mRecyclerView;
    private IngredientAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Ingredient> ingredientList;
    private ArrayList<String> ingredientString;
    private Button queryBtn;
    private Button addIngredientBtn;
    private ImageButton removeBtn;
    private String uid;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("CookForYou");

        queryBtn = getActivity().findViewById(R.id.queryBtn);
        addIngredientBtn = getActivity().findViewById(R.id.addIngredientBtn);
        removeBtn = getActivity().findViewById(R.id.removeBtn);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //change to welcome fragment if user not logged in
        if(mAuth.getCurrentUser()!=null) {
            ingredientList = new ArrayList<>();
            ingredientString = new ArrayList<>();
            //Get unique uid for each user
            uid = mAuth.getCurrentUser().getUid();
            //Getting of array of ingredients stored under uid
            buildRecyclerView();
            populateLists();
            queryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAdapter.checkedIngredients.isEmpty()) {
                        Toast.makeText(getActivity().getApplicationContext(), "No ingredient selected", Toast.LENGTH_SHORT).show();
                    } else {
                        String[] ingredients = new String[mAdapter.checkedIngredients.size()];
                        for (int i = 0; i < ingredients.length; i++) {
                            ingredients[i] = mAdapter.checkedIngredients.get(i).getmText();
                        }
                        Fragment resultsFragment = ResultsFragment.newInstance(ingredients);
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        fm.beginTransaction()
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.content_frame, resultsFragment)
                                .addToBackStack("ResultsFragment")
                                .commit();
                    }
                }
            });

            addIngredientBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialog();
                }
            });
            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mAdapter.checkedIngredients.isEmpty()){
                        Toast.makeText(getActivity().getApplicationContext(), "No ingredient selected", Toast.LENGTH_SHORT).show();
                    }else {
                        List<Ingredient> toDelete = mAdapter.checkedIngredients;
                        StringBuilder sb = new StringBuilder();
                        for(int j = 0; j < toDelete.size() - 1; j++){
                            Ingredient ing = toDelete.get(j);
                            String ingredientName = ing.getmText();
                            deleteIngredient(ingredientName);
                            sb.append(ingredientName + ", ");
                            ingredientString.remove(ing);
                            for(Ingredient i : ingredientList){
                                if( i.getmText().equals(ing)){
                                    ingredientList.remove(i);
                                }
                            }
                        }
                        Ingredient ing = toDelete.get(toDelete.size()-1);
                        String ingredientName = ing.getmText();
                        deleteIngredient(ingredientName);
                        sb.append(ingredientName);
                        ingredientString.remove(ing);
                        for(Ingredient i : ingredientList){
                            if( i.getmText().equals(ing)){
                                ingredientList.remove(i);
                            }
                        }
                        String ingredientsDeleted = sb.toString().trim();
                        Toast.makeText(getActivity().getApplicationContext(), ingredientsDeleted + " successfully deleted", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, WelcomeFragment.newInstance());
            ft.addToBackStack("WelcomeFragment");
            ft.commit();
        }
    }

    //Initial population of list of ingredients that will be display in RecyclerView. Will have to
    //populate a json to make the database continuously update
    //Constructs RecyclerView to be used
    public void buildRecyclerView() {
        mRecyclerView = getActivity().findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mAdapter = new IngredientAdapter(ingredientList, getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    //Opens Dialog for user input of new ingredients
    public void openDialog(){
        Dialog dialog = new Dialog();
        dialog.setTargetFragment(HomeFragment.this, 1);
        dialog.show(getFragmentManager(), "Dialog");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.search_action);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                mAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    @Override
    public void applyText(String ingredient) {
        db.collection("UserDetails").document(uid).update("Ingredients", FieldValue.arrayUnion(ingredient));
        mAdapter.addNewIngredient(ingredient);
        mAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity().getApplicationContext(), ingredient + " successfully added", Toast.LENGTH_SHORT).show();
    }

    public void populateLists(){
        db.collection("UserDetails").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ingredientString = (ArrayList<String>) documentSnapshot.get("Ingredients");
                Collections.sort(ingredientString);
                for(String s : ingredientString){
                    ingredientList.add(new Ingredient(s));
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void deleteIngredient(String ingredient){
        db.collection("UserDetails").document(uid).update("Ingredients", FieldValue.arrayRemove(ingredient));
        mAdapter.removeIngredient(ingredient);
        ingredientString.remove(ingredient);
        for(Ingredient i : ingredientList){
            if( i.getmText().equals(ingredient)){
                ingredientList.remove(i);
            }
        }
    }
}
