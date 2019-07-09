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

import java.util.List;

import static com.example.cookforyou.FavouriteFragment.mFavThumbnailDownloader;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.FavouriteHolder> {

    private List<Recipe> mRecipeList;
    private Context mContext;
    private OnRecipeClickListener mOnRecipeClickListener;
    private List<String> userFavId;
    private String uid;
    private FirebaseFirestore db;

    public FavouriteAdapter(List<Recipe> recipeList, List<String> favIds, Context context, OnRecipeClickListener onRecipeClickListener){
        mRecipeList = recipeList;
        userFavId = favIds;
        mContext = context;
        mOnRecipeClickListener = onRecipeClickListener;
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
    }
    @Override
    public void onBindViewHolder(@NonNull FavouriteAdapter.FavouriteHolder favouriteHolder, final int i) {
        final Recipe recipe = mRecipeList.get(i);
        mFavThumbnailDownloader.queueThumbnail(favouriteHolder, recipe.getThumbnailUrl());
        favouriteHolder.bindTitle(recipe.getTitle());
        favouriteHolder.mFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = (ImageView) v;
                    db.collection("UserDetails").document(uid).collection("favourites").document(recipe.getId()).delete();
                    mRecipeList.remove(recipe);
                    imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
                    notifyItemRemoved(i);
                    Toast.makeText(mContext, "Recipe removed from Favourites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public FavouriteHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(
                        R.layout.favourite,
                        viewGroup,
                        false
                );
        return new FavouriteHolder(v, mOnRecipeClickListener);
    }


    @Override
    public int getItemCount() {
        return mRecipeList.size();
    }

    public static class FavouriteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mItemTextView;
        private ImageView mItemImageView;
        private ImageView mFav;
        FavouriteAdapter.OnRecipeClickListener onRecipeClickListener;

        public FavouriteHolder(@NonNull View itemView, FavouriteAdapter.OnRecipeClickListener onRecipClickListener) {
            super(itemView);

            mItemImageView = itemView.findViewById(R.id.item_image_view_fav);
            mItemTextView = itemView.findViewById(R.id.item_text_view_fav);
            mFav = itemView.findViewById(R.id.favIcon);
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

    protected interface OnRecipeClickListener{
        void onRecipeClick(int position);
    }
}
