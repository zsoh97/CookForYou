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
import android.widget.Toast;

import com.example.cookforyou.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.cookforyou.ResultsFragment.mThumbnailDownloader;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultsHolder> {

    private List<Recipe> mRecipeList;
    private OnRecipeClickListener mOnRecipeClickListener;
    private List<String> userFavId;
    private Context mContext;
    private String uid;
    private FirebaseFirestore db;

    public ResultsAdapter(List<Recipe> recipeList, List<String> favIds, Context context, OnRecipeClickListener onRecipeClickListener){
        mRecipeList = recipeList;
        mOnRecipeClickListener = onRecipeClickListener;
        userFavId = favIds;
        mContext = context;
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
    }

    public void addToFavourites(Recipe recipe){
        Map<String, Object> data = new HashMap<>();
        data.put("id", recipe.getId());
        data.put("ingredients", recipe.getIngredients());
        data.put("recipeUrl", recipe.getRecipeUrl());
        data.put("thumbnailUrl", recipe.getThumbnailUrl());
        data.put("title", recipe.getTitle());
        db.collection("UserDetails").document(uid).collection("favourites").document(recipe.getId()).set(data);
        Toast.makeText(mContext, "Recipe Added to Favourites", Toast.LENGTH_SHORT).show();
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
        final Recipe recipe = mRecipeList.get(i);
        mThumbnailDownloader.queueThumbnail(resultsHolder, recipe.getThumbnailUrl());
        resultsHolder.bindTitle(recipe.getTitle());
        if(userFavId.contains(recipe.getId())){
            resultsHolder.mFav.setImageResource(R.drawable.ic_favorite_red_24dp);
        }
        resultsHolder.mFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = (ImageView) v;
                if(!userFavId.contains(recipe.getId())){
                    imageView.setImageResource(R.drawable.ic_favorite_red_24dp);
                    addToFavourites(recipe);
                    userFavId.add(recipe.getId());
                    notifyDataSetChanged();
                }else{
                    db.collection("UserDetails").document(uid).collection("favourites").document(recipe.getId()).delete();
                    imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
                    notifyDataSetChanged();
                    Toast.makeText(mContext, "Recipe removed from Favourites", Toast.LENGTH_SHORT).show();
                    userFavId.remove(recipe.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecipeList.size();
    }

    public static class ResultsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mItemTextView;
        private ImageView mItemImageView;
        private ImageView mFav;
        OnRecipeClickListener onRecipeClickListener;

        public ResultsHolder(@NonNull View itemView, OnRecipeClickListener onRecipClickListener) {
            super(itemView);

            mItemImageView = itemView.findViewById(R.id.item_image_view);
            mItemTextView = itemView.findViewById(R.id.item_text_view);
            mFav = itemView.findViewById(R.id.fav);
            onRecipeClickListener = onRecipClickListener;
            mItemImageView.setOnClickListener(this);

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
