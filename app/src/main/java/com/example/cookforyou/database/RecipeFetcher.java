package com.example.cookforyou.database;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.cookforyou.model.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fetches recipes from the recipe puppy api.
 *
 * <p>
 * This class encapsulates all the methods related to
 * fetching from the recipe puppy API using REST architecture(Only GET)
 * The data is downloaded using HTTP connections as the API does not support
 * HTTPS and above Android version 7, HTTP clear text transfers are disabled by default.
 * Look at the res/xml/network_security_config.xml file for the subdomains that have been
 * added to allow for clear text transfers. Also included in the android manifest is the
 * above mentioned XML file that will allow for HTTP transfers to occur on the device with
 * version 7 and above.
 * </p>
 * <p>
 *     The public facing methods include a fetch method that allows you to fetch directly
 *     from the recipe puppy API by including 3 search parameters, ingredients list, search query
 *     and page number. No null references are allowed in any of the search parameters and empty
 *     search queries or empty ingredients list are perfectly fine.
 * </p>
 */
public class RecipeFetcher {

    private static final String TAG = "RecipeFetcher";
    private static final String INGREDIENT_PARAM = "i";
    private static final String SEARCH_PARAM = "q";
    private static final String PAGE_PARAM = "p";

    private static final Uri RECIPE_PUPPY_API_ENDPOINT =
            Uri.parse("http://www.recipepuppy.com/api/");

    /**
     * Fetches JSON data from the recipe puppy API.
     *
     * This method opens a HTTP connection with the API and attempts to
     * search for the query using the passed in parameters. As per the
     * specifications on Recipe Puppy API website, their api only accepts
     * 4 types of optional parameters:
     * <p>
     *     1. i - comma delimited ingredients
     * </p>
     * <p>
     *     2. q - normal search query
     * </p>
     * <p>
     *     3. p - page number
     * </p>
     * <p>
     *     4. format=xml - if data queried needs to be XML
     * </p>
     *
     * The method chooses to ignore the 4th optional parameter and the
     * data received back from the call is in JSON format which is then parsed.
     *
     * @param ingredients An array of all the ingredients to search for
     * @param searchQuery The search query to perform
     * @param pageNum The page number of the API to receive from
     */
    public void fetch(@NonNull String[] ingredients, @NonNull String searchQuery,
                        int pageNum) {
       String ingredientQuery = buildIngredientsQuery(ingredients);
       String queryUrl = RECIPE_PUPPY_API_ENDPOINT
               .buildUpon()
               .appendQueryParameter(INGREDIENT_PARAM, ingredientQuery)
               .appendQueryParameter(SEARCH_PARAM, searchQuery)
               .appendQueryParameter(PAGE_PARAM, "" + pageNum)
               .build()
               .toString();
       Log.i(TAG, "Fetching from: " + queryUrl);

       try {
           //This method will open HTTP connection and fetch the json data.
           String jsonString = getUrlString(queryUrl);
           Log.i(TAG, "Successfully fetched json: " + jsonString);

           //This method attempts to parse the json data that is received.
           List<Recipe> parsedData = parseJsonString(jsonString);
           //TODO Right now this method simply prints out the recipes that have been fetched. Make it return and do something with it.
           for(Recipe s : parsedData) {
               Log.i(TAG, s + "\n");
           }
       } catch (IOException e) {
           Log.e(TAG, "Unable to retrieve json data", e);
       } catch (JSONException e) {
           Log.e(TAG, "Unable to parse json data", e);
       }
    }

    /**
     * Parses the JSON string received back from the recipe puppy API.
     *
     * <p>
     * The data received back from the API has all the results in the aptly named
     * "results" value in the json string. This "results" value is a JSON array
     * where each object in that array has the following attributes.
     * </p>
     * <p>
     *     1. Recipe Title
     * </p>
     * <p>
     *     2. href link to recipe website
     * </p>
     * <p>
     *     3. Ingredients list of recipe
     * </p>
     * <p>
     *     4. Thumbnail link to recipe
     * </p>
     *
     * <p>
     *     The parsed json string will return a list of recipes that store the
     *     4 things mentioned above.
     * </p>
     * @param jsonString Takes in the json string returned by recipe puppy API
     * @return List of recipes with all necessary attributes listed above
     * @throws JSONException If the name of the json values are not as expected.
     */
    private List<Recipe> parseJsonString(String jsonString) throws JSONException {
        List<Recipe> recipes = new ArrayList<>();

        JSONObject object = new JSONObject(jsonString);
        //The main results page. All other data is regarding the API
        JSONArray resultArray = object.getJSONArray("results");

        for(int i = 0; i < resultArray.length(); i++) {
            Recipe r = new Recipe();
            JSONObject current = resultArray.getJSONObject(i);

            //The four main attributes of each item of the results array
            r.setTitle(cleanTitleData(current.getString("title")));
            r.setRecipeUrl(current.getString("href"));
            r.setIngredients(parseIngredientJson(current.getString("ingredients")));
            r.setThumbnailUrl(current.getString("thumbnail"));

            recipes.add(r);
        }
        return recipes;
    }

    /**
     * Cleans up extra escape characters that may be present.
     *
     * This method helps to clean up title data
     * which sometimes have extra tabs or newline characters
     *
     * @param title The title to clean up
     * @return The cleaned up title with no escape characters.
     */
    private String cleanTitleData(String title) {
        return title
                .replaceAll("\n", "")
                .replaceAll("\r", "")
                .replaceAll("\t", "");
    }

    /**
     * Parses a string of json representing a list of ingredients.
     *
     * <p>
     *     The returned Recipe Puppy API json data contains
     *     the ingredients as a CSV type of string. This method
     *     helps to parse that into a list of ingredient strings.
     * </p>
     * @param jsonIngredient The json string representing the ingredient list
     * @return A list of ingredients
     */
    private List<String> parseIngredientJson(String jsonIngredient) {
        return new ArrayList<>(Arrays.asList(jsonIngredient.split(",")));
    }

    /**
     * A utility method to open a connection to API.
     *
     * @param urlSpec The specification of the url
     * @return A string of data received from opening a connection
     * @throws IOException If there is some error in connection.
     */
    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    /**
     * Opens a HTTP connection to API and requests the data.
     *
     * @param urlSpec The specification of the url.
     * @return A byte[] array of data from the output stream.
     * @throws IOException If connection fails, HTTP response code not 200.
     */
    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        //Opens up a HTTP connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //Opens up the input stream of the connection.
            InputStream in = connection.getInputStream();

            //If API is down/unresponsive, it will throw a exception
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with" +
                        urlSpec);
            }

            //Buffers to read in data for faster speed.
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Utility method to build a query for ingredients being passed into API call.
     *
     * <p>
     *     As the REST API requires comma separated values of ingredients,
     *     this method helps to append commas between each ingredient except
     *     the last one. If the passed in ingredients array is empty, it simply
     *     returns a empty string which the API still accepts.
     * </p>
     * @param ingredients Array of ingredients to query the API for.
     * @return A string to be passed into the API call.
     */
    private String buildIngredientsQuery(String[] ingredients) {
        if(ingredients.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(String s : ingredients) {
            sb.append(s);
            sb.append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
