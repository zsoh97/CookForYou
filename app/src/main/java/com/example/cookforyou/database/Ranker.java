package com.example.cookforyou.database;

import com.example.cookforyou.model.Recipe;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * A static utility class that will help rank relevance.
 *
 * <p>
 *     This class represents a utility class that will rank
 *     search results based on conditions specified by the
 *     method.
 * </p>
 */
public class Ranker {

    public static final int ASCENDING = 0;
    public static final int DESCENDING = 1;

    /**
     * Ranks results based on how many matches it has with queries.
     *
     * <p>
     *     This method ranks a list of results based on how many matches
     *     it has with the query. You are allowed to specify the direction
     *     of the order using the static variables declared in this class.
     *     If there is a tie between number of matches, the tiebreaker is
     *     the number of ingredients that exist in the recipe.
     * </p>
     * @param results The results list
     * @param queries The querying list
     * @param option Specifies ascending or descending order.
     */
    public static void rankOnQuery(final List<Recipe> results,
                            final List<String> queries,
                            final int option) {
        final HashSet<String> setQuery = new HashSet<>();
        setQuery.addAll(queries);
        Collections.sort(results, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe o1, Recipe o2) {
                int numOfMatchesForO1 = countNumberOfMatchingQuery(o1.getIngredients(), setQuery);
                int numOfMatchesForO2 = countNumberOfMatchingQuery(o2.getIngredients(), setQuery);
                int diff = numOfMatchesForO1 - numOfMatchesForO2;
                if(diff == 0) {
                    return o1.getIngredients().size() - o2.getIngredients().size();
                }
                if(option == ASCENDING) {
                    return diff;
                } else if(option == DESCENDING) {
                    return -diff;
                } else {
                    throw new IllegalArgumentException("Option passed into ranker not recognized.");
                }
            }
        });
    }

    /**
     * Ranks results based on the number of visits to the recipe link.
     *
     * <p>
     *     This method ranks a list of results based on how many visits the
     *     url has had since the dawn of time.
     *     You are allowed to specify the direction
     *     of the order using the static variables declared in this class.
     *     If there is a tie between number of matches, the in-place order
     *     is used.
     * </p>
     * @param results The results list
     * @param option Specifies ascending or descending order.
     */
    public static void rankOnVisits(final List<Recipe> results, final int option) {
        Collections.sort(results, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe o1, Recipe o2) {
                int diff = o1.getVisits() - o2.getVisits();
                if(option == ASCENDING) {
                    return diff;
                } else if(option == DESCENDING) {
                    return -diff;
                } else {
                    throw new IllegalArgumentException("Option passed into ranker not recognized.");
                }
            }
        });
    }

    /**
     * Convenient utility method to help count the number of
     * matching queries.
     *
     * <p>
     *     This method helps to count the number of
     *     matching queries in a list of strings against
     *     a dictionary of query words.
     * </p>
     */
    private static int countNumberOfMatchingQuery(List<String> ingredientList,
                                                  HashSet<String> query) {
        int count = 0;
        for(String s : ingredientList) {
            if(query.contains(s)) {
                count++;
            }
        }
        return count;
    }
}
