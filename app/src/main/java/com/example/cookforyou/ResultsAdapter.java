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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.cookforyou.ResultsFragment.mThumbnailDownloader;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultsHolder> {

    private List<Recipe> mRecipeList;
    private Context mContext;
    private OnRecipeClickListener mOnRecipeClickListener;
    private List<String> userFavId = new ArrayList<>();
    private String uid;
    private FirebaseFirestore db;

    public ResultsAdapter(List<Recipe> recipeList, Context context, OnRecipeClickListener onRecipeClickListener){
        mRecipeList = recipeList;
        mContext = context;
        mOnRecipeClickListener = onRecipeClickListener;
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        db.collection("UserDetails").document(uid).collection("favourites")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot snapshot: snapshots){
                    userFavId.add(snapshot.getId());
                }
            }
        });
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
    public void onBindViewHolder(final @NonNull ResultsHolder resultsHolder, int i) {
        final Recipe recipe = mRecipeList.get(i);
        if(userFavId.contains(recipe.getId())){
            resultsHolder.mFav.setImageResource(R.drawable.ic_favorite_red_24dp);
        }
        mThumbnailDownloader.queueThumbnail(resultsHolder, recipe.getThumbnailUrl());
        resultsHolder.bindTitle(recipe.getTitle());
        resultsHolder.mFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userFavId.contains(recipe.getId())){
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", recipe.getId());
                    data.put("ingredients", recipe.getIngredients());
                    data.put("recipeUrl", recipe.getRecipeUrl());
                    data.put("thumbnailUrl", recipe.getThumbnailUrl());
                    data.put("title", recipe.getTitle());
                    db.collection("UserDetails").document(uid).collection("favourites").document(recipe.getId()).set(data);
                    resultsHolder.mFav.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_red_24dp));
                    notifyDataSetChanged();
                    Toast.makeText(mContext, "Recipe Added to Favourites", Toast.LENGTH_SHORT).show();
                }else{
                    db.collection("UserDetails").document(uid).collection("favourites").document(recipe.getId()).delete();
                    resultsHolder.mFav.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_favorite_border_white_24dp));
                    notifyDataSetChanged();
                    Toast.makeText(mContext, "Recipe removed from Favourites", Toast.LENGTH_SHORT).show();
                }
            }
        });;

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
