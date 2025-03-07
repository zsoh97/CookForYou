package com.example.cookforyou;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.cookforyou.database.Database;
import com.example.cookforyou.database.Ranker;
import com.example.cookforyou.model.Recipe;
import com.example.cookforyou.network.RecipeFetcher;
import com.example.cookforyou.network.ThumbnailDownloader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.cookforyou.database.Database.getInstance;

public class ResultsFragment extends Fragment implements ResultsAdapter.OnRecipeClickListener {
    private static final String TAG = "ResultsFragment";
    private static final String QUERY_KEY = "ingredientQuery";

    private RecyclerView mRecyclerView;
    private List<Recipe> mRecipeList = new ArrayList<>();
    private List<String> favIds = new ArrayList<>();
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> mQuery;
    private Group mLoadingGroup, mRecyclerGroup, mFailGroup;
    private ResultsAdapter mResultsAdapter;
    public static ThumbnailDownloader<ResultsAdapter.ResultsHolder> mThumbnailDownloader;


    public static ResultsFragment newInstance(String[] ingredientQuery) {
        Bundle bundle = new Bundle();
        bundle.putStringArray(QUERY_KEY, ingredientQuery);

        ResultsFragment fragment = new ResultsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        String[] ingredients = getArguments().getStringArray(QUERY_KEY);
        mQuery = Arrays.asList(ingredients);
        Log.i(TAG, "Queried ingredients: " + Arrays.toString(ingredients));
        new FetchRecipeTask().execute(ingredients);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(getActivity(), responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<ResultsAdapter.ResultsHolder>() {
                    @Override
                    public void onThumbnailDownloaded(ResultsAdapter.ResultsHolder target, Bitmap thumbnail) {
                        Log.i(TAG, "Setting downloaded thumbnail");
                        Bitmap roundedCornerBitmap = ImageHelper.getRoundedCornerBitmap(thumbnail, 20);
                        Drawable drawable = new BitmapDrawable(getResources(), roundedCornerBitmap);
                        target.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread for thumbnail download started");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_results, container, false);

        mRecyclerView = v.findViewById(R.id.results_recycler_view);
        mLoadingGroup = v.findViewById(R.id.results_loading_group);
        mRecyclerGroup = v.findViewById(R.id.results_recycler_group);
        mFailGroup = v.findViewById(R.id.results_fail_group);

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

        setupAdapter();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_results, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sort_by_query :
                Ranker.rankOnQuery(mRecipeList, mQuery, Ranker.DESCENDING);
                Log.i(TAG, "Sorting by query: " + mRecipeList.toString());
                mResultsAdapter.notifyDataSetChanged();
                return true;
            case R.id.sort_by_visits :
                Ranker.rankOnVisits(mRecipeList, Ranker.DESCENDING);
                Log.i(TAG, "Sorting by visits: " + mRecipeList.toString());
                mResultsAdapter.notifyDataSetChanged();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread for thumbnail download destroyed");
    }

    private void setupAdapter() {
        if(isAdded()) {
            mResultsAdapter = new ResultsAdapter(mRecipeList, favIds, getContext(), this);
            mRecyclerView.setAdapter(mResultsAdapter);
        }
    }

    private class FetchRecipeTask extends AsyncTask<String , Void, Database> {

        private String[] ingredients;

        @Override
        protected Database doInBackground(String... strings) {
            ingredients = strings;
            db.collection("UserDetails").document(uid).collection("favourites")
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                    List<String> temp = new ArrayList<>();
                    for(DocumentSnapshot snapshot: snapshots){
                        temp.add(snapshot.getId());
                    }
                    favIds = new ArrayList<>(temp);
                }
            });
            return getInstance().query(Arrays.asList(strings));
        }

        @Override
        protected void onPostExecute(Database db) {
            db.setOnQueryCompleteListener(new Database.OnQueryCompleteListener() {
                @Override
                public void onComplete(List<Recipe> recipes) {
                    if(recipes.isEmpty()) {
                        Log.d(TAG, "Recipe not found in firestore database");
                        UpdateDatabaseTask task = new UpdateDatabaseTask();
                        task.execute(ingredients);
                        return;
                    }
                    mRecipeList = recipes;
                    setupAdapter();
                    completeLoadingScreen();
                }
            });
        }
    }

    private class UpdateDatabaseTask extends AsyncTask<String, Void, Void> {

        private String[] ingredients;
        private List<Recipe> recipeFetched;

        @Override
        protected Void doInBackground(String... strings) {
            ingredients = strings;
            Log.d(TAG, "Querying puppy API with ingredients: " + Arrays.toString(strings));
            recipeFetched = new RecipeFetcher().fetch(strings, "", 1);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Task<Void> recipeAdded = Database.getInstance().addAllRecipe(recipeFetched);
            if(recipeAdded == null) {
                Log.d(TAG, "No recipe fetched");
                completeNoResultsScreen();
            } else {
                recipeAdded.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Fetched recipes added to firestore database. Beginning fetch task again");
                        FetchRecipeTask fetchTask = new FetchRecipeTask();
                        fetchTask.execute(ingredients);
                    }
                });
            }

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

    private void completeLoadingScreen() {
        mLoadingGroup.setVisibility(View.GONE);
        mRecyclerGroup.setVisibility(View.VISIBLE);
    }

    private void completeNoResultsScreen() {
        mLoadingGroup.setVisibility(View.GONE);
        mFailGroup.setVisibility(View.VISIBLE);
    }
}
