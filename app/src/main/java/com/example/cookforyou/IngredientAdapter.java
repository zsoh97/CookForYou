package com.example.cookforyou;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> implements Filterable {
    private List<Ingredient> ingredientList;
    private Context mContext;
    private List<Ingredient> ingredientListFull;
    protected List<Ingredient> checkedIngredients = new ArrayList<>();

    public static class IngredientViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView;
        CheckBox checkBox;
        ItemClickListener itemClickListener;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            checkBox = itemView.findViewById(R.id.checkbox);
            checkBox.setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener icl){
            this.itemClickListener = icl;
        }

        @Override
        public void onClick(View v) {
            this.itemClickListener.onItemClick(v, getLayoutPosition());
        }
        interface ItemClickListener{
            void onItemClick(View v, int pos);
        }
    }

    public IngredientAdapter(List<Ingredient> ingredients, Context context){
        this.mContext = context;
        this.ingredientList = ingredients;
        this.ingredientListFull = new ArrayList<>(ingredientList);
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ingredient, viewGroup, false);
        IngredientViewHolder ivh = new IngredientViewHolder(v);
        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull final IngredientViewHolder ingredientViewHolder, int i) {
        final Ingredient currentIngredient = ingredientList.get(i);
        ingredientViewHolder.textView.setText(currentIngredient.getmText().toLowerCase());
        ingredientViewHolder.checkBox.setChecked(currentIngredient.isSelected());
        ingredientViewHolder.setItemClickListener(new IngredientViewHolder.ItemClickListener(){
            @Override
            public void onItemClick(View v, int pos){
                CheckBox checkBox = (CheckBox) v;
                if(checkBox.isChecked()){
                    currentIngredient.setSelected(true);
                    checkedIngredients.add(currentIngredient);
                } else if (!checkBox.isChecked()){
                    currentIngredient.setSelected(false);
                    checkedIngredients.remove(currentIngredient);
                }
            }
        });
    }

    protected void addNewIngredient(String ingredient){
        Ingredient ing = new Ingredient(ingredient);
        ingredientList.add(ing);
        Collections.sort(ingredientList);
        ingredientListFull.add(ing);
        Collections.sort(ingredientListFull);
        notifyDataSetChanged();
    }

    protected void removeIngredient(String ingredient){
        int position = 0;
        for(int i = 0; i<ingredientList.size(); i++){
            if(ingredientList.get(i).getmText().equals(ingredient)){
                ingredientList.remove(i);
                position = i;
            }
        }
        for(int i = 0; i < ingredientListFull.size(); i++) {
            if (ingredientListFull.get(i).getmText().equals(ingredient)) {
                ingredientListFull.remove(i);
            }
        }
        notifyItemRemoved(position);
    }

    public void notifySuccessfulDeletion() {
        //Removes all the checked ingredients that were held by the adapter
        checkedIngredients = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    @Override
    public Filter getFilter() {
        return ingredientFilter;
    }

    private Filter ingredientFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Ingredient> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() ==0){
                filteredList.addAll(ingredientListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(Ingredient ingredient: ingredientListFull){
                    if (ingredient.getmText().contains(filterPattern)){
                        filteredList.add(ingredient);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ingredientList.clear();
            ingredientList.addAll((List<Ingredient>)results.values);
            notifyDataSetChanged();
        }
    };
}
