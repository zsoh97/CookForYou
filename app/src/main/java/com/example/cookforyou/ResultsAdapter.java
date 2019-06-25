package com.example.cookforyou;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cookforyou.model.Recipe;

import java.util.List;

import static com.example.cookforyou.ResultsFragment.mThumbnailDownloader;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultsHolder> {

    private List<Recipe> mRecipeList;
    private Context mContext;
    private OnRecipeClickListener mOnRecipeClickListener;

    public ResultsAdapter(List<Recipe> recipeList, Context context, OnRecipeClickListener onRecipeClickListener){
        mRecipeList = recipeList;
        mContext = context;
        mOnRecipeClickListener = onRecipeClickListener;
    }

    @NonNull
    @Override
    public ResultsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(
                        R.layout.list_item_results,
                        viewGroup,
                        false
                );
        return new ResultsHolder(v, mOnRecipeClickListener);
    }
    @Override
    public void onBindViewHolder(@NonNull ResultsHolder resultsHolder, int i) {
        Recipe recipe = mRecipeList.get(i);
        mThumbnailDownloader.queueThumbnail(resultsHolder, recipe.getThumbnailUrl());
        resultsHolder.bindTitle(recipe.getTitle());
    }

    @Override
    public int getItemCount() {
        return mRecipeList.size();
    }

    public class ResultsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mItemTextView;
        private ImageView mItemImageView;
        OnRecipeClickListener onRecipeClickListener;

        public ResultsHolder(@NonNull View itemView, OnRecipeClickListener onRecipClickListener) {
            super(itemView);

            mItemImageView = itemView.findViewById(R.id.item_image_view);
            mItemTextView = itemView.findViewById(R.id.item_text_view);
            onRecipeClickListener = onRecipClickListener;
            itemView.setOnClickListener(this);
        }
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindTitle(String title) {
            mItemTextView.setText(title);
        }

        @Override
        public void onClick(View v) {
            onRecipeClickListener.onRecipeClick(getAdapterPosition());
        }
    }

    public interface OnRecipeClickListener{
        void onRecipeClick(int position);
    }
}
