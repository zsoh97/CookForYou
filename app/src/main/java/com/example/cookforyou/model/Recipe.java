package com.example.cookforyou.model;

import java.util.Arrays;
import java.util.List;

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

    @Override
    public String toString() {
        return "Recipe: " + mTitle
                + ", URL: " + mRecipeUrl
                + ", Ingredients: " + mIngredients.toString();
    }
}
