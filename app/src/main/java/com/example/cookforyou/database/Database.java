package com.example.cookforyou.database;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.cookforyou.model.Recipe;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A thin wrapper API around Google's FirebaseFirestore database.
 * <p>
 *     This class wraps around the FirebaseFirestore database to allow for easy
 *     addition and query of recipes unique to this app. As per Firebase's documentation,
 *     Firestore is not meant to be subclassed(It is so for all Firebase classes) and thus,
 *     the database uses the same static getInstance() method as FirebaseFirestore to get
 *     a reference to the database. This ensures that only one reference to the database
 *     will exist at any point in time of running the app so it helps to prevent any memory
 *     leakage and security leaks.
 * </p>
 * <p>
 *     For the database structure, the root of the collections of recipes is found at the path
 *     /root/recipes/ingredients/. For each ingredient in the recipe, there is a corresponding
 *     document associated with it. Inside this document associated with the ingredient is
 *     the collection of recipes that have the above mentioned ingredient in it.
 * </p>
 */
public class Database {

    private static final String TAG = "Database";
    private static final String RECIPE_COLLECTION_ROOT = "root/recipes/ingredients/";
    private static final String DEFAULT_INGREDIENT_RECIPE_PATH = "/recipes";

    private FirebaseFirestore mDatabase;
    private OnQueryCompleteListener mOnQueryCompleteListener;
    private HashSet<Recipe> mQueryResult;

    private Database(FirebaseFirestore database) {
        mDatabase = database;
    }

    /**
     * Returns a reference to the firestore database related to this app.
     * @return A reference to the firestore database.
     */
    public static Database getInstance() {
        return new Database(FirebaseFirestore.getInstance());
    }

    /**
     * Adds a recipe to the database.
     *
     * <p>
     *     Adds a recipe to the database in the format described above
     *     in the class description where each ingredient will have be its
     *     own document. If the document does not exist, it will automatically
     *     be created. This is so as to facilitate querying and searching of
     *     the database.
     * </p>
     * @param recipe The recipe to add to the database
     * @return A task regarding the add operation being executed.
     */
    public Task<Void> addRecipe(Recipe recipe) {
        List<String> ingredients = recipe.getIngredients();
        Task<Void> result = null;
        for(String ingredient : ingredients) {
            result = mDatabase
                    .collection(constructIngredientCollectionPath(ingredient))
                    .document(recipe.getTitle().trim())
                    .set(recipe, SetOptions.merge());
        }
        return result;
    }
    /**
     * Adds a collectino of recipe to the database.
     *
     * <p>
     *     Adds a collection of recipes to the database at one go.
     * </p>
     * @param recipes The collection of recipe to add to the database
     * @return A task regarding the add operation being executed.
     */
    public Task<Void> addAllRecipe(Collection<Recipe> recipes) {
        Task<Void> task = null;
        for(Recipe recipe : recipes) {
            task = addRecipe(recipe);
        }
        return task;
    }

    /**
     * Queries the database with a given list of ingredients.
     *
     * <p>
     *     This method will query the firebase database with the
     *     ingredients list passed into it. There will be a separate
     *     query to the database for each ingredient in the list.
     *     This is due to the structure of the database storing the
     *     recipes to allow for quicker retrieval of data from the database.
     * </p>
     *
     * <p>
     *     As the queries are Asynchronous, this query method will not return
     *     you the list of recipes that are queried. In order to be informed
     *     about the completion of the query, you can attach a listener to the database
     *     instance you hold where a callback to
     *     Database.OnQueryCompleteListener.onComplete method is made with the
     *     list of recipes that have been fetched Asynchronously.
     * </p>
     * @param ingredients The list of all the ingredients to query the database about.
     * @return The instance of the database that called this method.
     */
    public Database query(final List<String> ingredients) {
        final List<Task<QuerySnapshot>> taskList = new ArrayList<>();
        //In case the list storing results is null.
        mQueryResult = new HashSet<>();

        for(String ingredient : ingredients) {
            //The query based upon this ingredient
            Task<QuerySnapshot> queryTask = mDatabase
                    .collection(constructIngredientCollectionPath(ingredient))
                    .get();
            taskList.add(queryTask);

            queryTask
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            //Once query is complete, add all recipes into the query result Hashset
                            for(QueryDocumentSnapshot query : task.getResult()) {
                                Recipe recipe = query.toObject(Recipe.class);
                                Log.i(TAG, "Fetched: " + recipe);
                                mQueryResult.add(recipe);
                            }
                        }
                    });
        }

        //When all the query tasks are completed,
        Tasks
                .whenAllComplete(taskList)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {
                        //Perform callback and return the list of recipes retrieved.
                        List<Recipe> queryResults = retrieveQuery();
                        //Passes off the ranking job to static Ranker class.
                        Ranker.rankOnQuery(queryResults, ingredients, Ranker.DESCENDING);
                        mOnQueryCompleteListener.onComplete(queryResults);
                        //Makes a new HashSet so that multiple async queries do not overlap results.
                        mQueryResult = new HashSet<>();
                    }
                });
        return this;
    }

    /**
     * Retrieves a certain query after it has been completed.
     *
     * <p>
     *     Only call this method from within this class when a query
     *     has been completed fully. Otherwise, results maybe missing
     *     from the query result list.
     * </p>
     * <p>
     *     This method will help to retrieve the query results into a new
     *     list so that even if the original list if overwritten by a new
     *     query operation, the existing result list will still be alive.
     * </p>
     * @return A copy of the result list from queries.
     */
    private List<Recipe> retrieveQuery() {
        List<Recipe> newQueryResult = new ArrayList<>();
        for(Recipe recipe : mQueryResult) {
            /*
            Note here that recipe objects are not deep copied as once the list is gone,
            there is no point in keeping the recipe, so deep copy is not required.
             */
            newQueryResult.add(recipe);
        }
        return newQueryResult;
    }

    /**
     * Sets a listener to listen out for query complete callbacks.
     *
     * <p>
     *     Use this method to register a query complete listener to find out
     *     when a query has been completed to be informed about completion.
     *     At any one point in time, an instance to the database will only have
     *     one instance of listener attached to it.
     * </p>
     * @param listener The listener to attach to this instance.
     */
    public void setOnQueryCompleteListener(OnQueryCompleteListener listener) {
        mOnQueryCompleteListener = listener;
    }

    /**
     * Utility method to construct the collection path for a certain ingredient.
     *
     * <p>
     *     This method will construct the collection path for a certain
     *     ingredient according to the following format:
     * </p>
     * <p>
     *     /root/recipes/ingredients/"ingredient"/recipes/
     * </p>
     * @param ingredient The ingredient to construct a path out of
     * @return The constructed path according to format
     */
    private String constructIngredientCollectionPath(String ingredient) {
        StringBuilder sb = new StringBuilder(RECIPE_COLLECTION_ROOT);
        sb.append(ingredient);
        sb.append(DEFAULT_INGREDIENT_RECIPE_PATH);
        return sb.toString();
    }

    /**
     * Represents the listener to be attached when a query is completed.
     *
     * <p>
     *     This interface contains one method which will be called
     *     when a certain query has been completed. The method will
     *     then pass in the resulting list as its argument. Users
     *     of the interface will have to override the onComplete method
     *     to do something after query result is received.
     * </p>
     */
    public interface OnQueryCompleteListener {
        void onComplete(List<Recipe> recipes);
    }
}
