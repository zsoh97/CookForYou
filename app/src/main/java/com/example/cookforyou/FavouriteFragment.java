package com.example.cookforyou;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cookforyou.database.Database;
import com.example.cookforyou.model.Recipe;
import com.example.cookforyou.network.FavThumbnailDownloader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class FavouriteFragment extends Fragment implements FavouriteAdapter.OnRecipeClickListener{

    private final String TAG = "FavouriteFragment";

    private RecyclerView mRecyclerView;
    private List<Recipe> mRecipeList = new ArrayList<>();
    private Group mLoadingGroup, mRecyclerGroup, mFailGroup;
    private FavouriteAdapter mFavouriteAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    public static FavThumbnailDownloader<FavouriteAdapter.FavouriteHolder> mFavThumbnailDownloader;

    public FavouriteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection("UserDetails").document(uid).collection("favourites")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot snapshot: snapshots){
                    mRecipeList.add(snapshot.toObject(Recipe.class));
                }
                mFavouriteAdapter.notifyDataSetChanged();
                if(mRecipeList.isEmpty()) {
                    completeLoadingScreen(true);
                } else {
                    completeLoadingScreen(false);
                }
            }
        });
        setupAdapter();
        Handler responseHandler = new Handler();
        mFavThumbnailDownloader = new FavThumbnailDownloader<>(getActivity(), responseHandler);
        mFavThumbnailDownloader.setFavThumbnailDownloadListener(
                new FavThumbnailDownloader.FavThumbnailDownloadListener<FavouriteAdapter.FavouriteHolder>() {
                    @Override
                    public void onFavThumbnailDownloaded(FavouriteAdapter.FavouriteHolder target, Bitmap thumbnail) {
                        Log.i(TAG, "Setting downloaded thumbnail");
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                });
        mFavThumbnailDownloader.start();
        mFavThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread for thumbnail download started");
        setupAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favourite, container, false);

        mRecyclerView = v.findViewById(R.id.favourites_recycler_view);
        mLoadingGroup = v.findViewById(R.id.favourites_loading_group);
        mRecyclerGroup = v.findViewById(R.id.favourites_recycler_group);
        mFailGroup = v.findViewById(R.id.favourites_fail_group);

        mRecyclerView
                .addItemDecoration(
                        new DividerItemDecoration(
                                getActivity(),
                                DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(
                        getActivity(),
                        LinearLayoutManager.VERTICAL,
                        false));
        mRecyclerView.setHasFixedSize(true);
        setupAdapter();
        return v;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mFavThumbnailDownloader == null) return;
        mFavThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFavThumbnailDownloader.quit();
        Log.i(TAG, "Background thread for thumbnail download destroyed");
    }

    private void setupAdapter() {
        if(isAdded()) {
            mFavouriteAdapter = new FavouriteAdapter(mRecipeList, getContext(), this);
            mRecyclerView.setAdapter(mFavouriteAdapter);
        }
    }

    public void onRecipeClick(int position){
        Recipe selectedRecipe = mRecipeList.get(position);
        selectedRecipe.incrementVisits();
        Database.getInstance().addRecipe(selectedRecipe);
        String recipeUrl = selectedRecipe.getRecipeUrl();
        Intent intent = new Intent(getActivity(), RecipeActivity.class);
        intent.putExtra("recipeLink", recipeUrl);
        startActivity(intent);
    }

    private void completeLoadingScreen(boolean isDataEmpty) {
        mLoadingGroup.setVisibility(View.GONE);
        if(isDataEmpty) {
            mFailGroup.setVisibility(View.VISIBLE);
        } else {
            mRecyclerGroup.setVisibility(View.VISIBLE);
        }
    }
}
