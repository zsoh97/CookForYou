package com.example.cookforyou;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.example.cookforyou.auth.RegisterFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements Dialog.AddIngredientDialogListener {

    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private IngredientAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Ingredient> ingredientList;
    private Button queryBtn;
    private Button addIngredientBtn;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("CookForYou");

        queryBtn = getActivity().findViewById(R.id.queryBtn);
        addIngredientBtn = getActivity().findViewById(R.id.addIngredientBtn);
        mAuth = FirebaseAuth.getInstance();

        //change to login fragment if user not logged in
        if(mAuth.getCurrentUser()==null){
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, new RegisterFragment());
            ft.commit();
        }

        populateIngredientList();
        buildRecyclerView();

        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAdapter.checkedIngredients.isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(), "No ingredient selected", Toast.LENGTH_SHORT).show();
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Ingredient ingredient : mAdapter.checkedIngredients) {
                        sb.append(ingredient.getmText().toLowerCase().trim() + " ");
                    }
                    Toast.makeText(getContext().getApplicationContext(), sb.toString().trim(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        addIngredientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
    }

    //Initial population of list of ingredients that will be display in RecyclerView. Will have to
    //populate a json to make the database continuously update
    public void populateIngredientList(){
        ingredientList = new ArrayList<>();
        ingredientList.add(new Ingredient("banana"));
        ingredientList.add(new Ingredient("butter"));
        ingredientList.add(new Ingredient("cheese"));
        ingredientList.add(new Ingredient("chicken breast"));
        ingredientList.add(new Ingredient("duck"));
        ingredientList.add(new Ingredient("eggs"));
        ingredientList.add(new Ingredient("garlic"));
        ingredientList.add(new Ingredient("milk"));
        ingredientList.add(new Ingredient("salmon"));
        ingredientList.add(new Ingredient("tomato"));
    }

    //Constructs RecyclerView to be used
    public void buildRecyclerView(){
        mRecyclerView = getActivity().findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mAdapter = new IngredientAdapter(ingredientList);

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
        mAdapter.addNewIngredient(ingredient);
        mAdapter.notifyDataSetChanged();
    }
}
