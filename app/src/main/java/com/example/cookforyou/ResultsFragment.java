package com.example.cookforyou;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cookforyou.database.Database;
import com.example.cookforyou.model.Recipe;
import com.example.cookforyou.network.ThumbnailDownloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResultsFragment extends Fragment {
    private static final String TAG = "ResultsFragment";

    private static final String QUERY_KEY = "ingredientQuery";


    private RecyclerView mRecyclerView;
    private List<Recipe> mRecipeList = new ArrayList<>();
    private ThumbnailDownloader<ResultsHolder> mThumbnailDownloader;

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
        String[] ingredients = getArguments().getStringArray(QUERY_KEY);
        Log.i(TAG, "Queried ingredients: " + Arrays.toString(ingredients));
        new FetchRecipeTask().execute(ingredients);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<ResultsHolder>() {
                    @Override
                    public void onThumbnailDownloaded(ResultsHolder target, Bitmap thumbnail) {
                        Log.i(TAG, "Setting downloaded thumbnail");
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
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
            mRecyclerView.setAdapter(new ResultsAdapter(mRecipeList));
        }
    }

    private class FetchRecipeTask extends AsyncTask<String , Void, Database> {
        @Override
        protected Database doInBackground(String... strings) {
            return Database.getInstance().query(Arrays.asList(strings));
        }

        @Override
        protected void onPostExecute(Database db) {
            db.setOnQueryCompleteListener(new Database.OnQueryCompleteListener() {
                @Override
                public void onComplete(List<Recipe> recipes) {
                    mRecipeList = recipes;
                    setupAdapter();
                }
            });
        }
    }

    private class ResultsHolder extends RecyclerView.ViewHolder {

        private TextView mItemTextView;
        private ImageView mItemImageView;

        public ResultsHolder(@NonNull View itemView) {
            super(itemView);

            mItemImageView = itemView.findViewById(R.id.item_image_view);
            mItemTextView = itemView.findViewById(R.id.item_text_view);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindTitle(String title) {
            mItemTextView.setText(title);
        }
    }

    private class ResultsAdapter extends RecyclerView.Adapter<ResultsHolder> {

        private List<Recipe> mRecipeList;

        public ResultsAdapter(List<Recipe> recipeList){
            mRecipeList = recipeList;
        }

        @NonNull
        @Override
        public ResultsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater
                    .from(getActivity())
                    .inflate(
                            R.layout.list_item_results,
                            viewGroup,
                            false
                            );
            return new ResultsHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ResultsHolder resultsHolder, int i) {
            Recipe recipe = mRecipeList.get(i);
            Drawable placeholder = getResources().getDrawable(R.drawable.banana);
            resultsHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(resultsHolder, recipe.getThumbnailUrl());
            resultsHolder.bindTitle(recipe.getTitle());
        }

        @Override
        public int getItemCount() {
            return mRecipeList.size();
        }
    }
}
