package com.example.cookforyou.model;

import android.support.annotation.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Encapsulates a recipe object. Part of the model.
 *
 * <p>
 *     This class encapsulates a recipe and includes a title,
 *     a recipe url, an array of ingredients and a thumbnail url.
 * </p>
 */
public class Recipe {

    public static final String TITLE_FIELD = "title";
    public static final String RECIPE_URL_FIELD = "recipeUrl";
    public static final String INGREDIENTS_FIELD = "ingredients";
    public static final String THUMBNAIL_URL_FIELD = "thumbnailUrl";

    private String mId;
    private String mTitle;
    private String mRecipeUrl;
    private List<String> mIngredients;
    private String mThumbnailUrl;

    public Recipe() {

    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getRecipeUrl() {
        return mRecipeUrl;
    }

    public void setRecipeUrl(String recipeUrl) {
        mRecipeUrl = recipeUrl;
    }

    public List<String> getIngredients() {
        return mIngredients;
    }

    public void setIngredients(List<String> ingredients) {
        mIngredients = ingredients;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
    }

    public String getId() {
        return mId;
    }

    /**
     * Sets a unique ID for each recipe object.
     *
     * <p>
     *     Note that if you pass in a null value as argument,
     *     the id is then randomly generated using the variant 2 UUID PRNG.
     *     Otherwise the id is set to the string representation of the UUID
     *     passed in as the argument. Refer to documentation of android studio
     *     for BNF of the UUID string form.
     * </p>
     * @param id String representation of the UUID.
     */
    public void setId(@Nullable String id) {
        if(id == null) {
            mId = UUID.randomUUID().toString();
        } else {
            this.mId = id;
        }
    }

    @Override
    public String toString() {
        return "Recipe: " + mTitle
                + ", URL: " + mRecipeUrl
                + ", Ingredients: " + mIngredients.toString();
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Recipe)) {
            throw new IllegalArgumentException("Recipe passed in is not of type Recipe in Recipe.java");
        }
        Recipe other = (Recipe) o;
        return this.mId.equals(other.mId);
    }
}
