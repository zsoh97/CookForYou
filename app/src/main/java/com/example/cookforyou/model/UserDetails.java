package com.example.cookforyou.model;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDetails implements Serializable {

    // Database Keys
    public static final String userDetailsKey = "UserDetails";
    public static final String nameKey = "Name";
    public static final String ingredientKey = "Ingredients";
    //maybe can implement the number of clicks of recipe using this class
    private FirebaseFirestore db;
    private String uid;
    private List<String> ingredients;
    public String name;

    public UserDetails(String name, String uid){
        this.name = name;
        this.uid = uid;
        this.ingredients = new ArrayList<>();
    }

    public UserDetails(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // call to update entry In database.

    public void updateEntry () {

        db = FirebaseFirestore.getInstance();

        Map<String,Object> data = new HashMap<>();

        data.put(nameKey,name);

        db.collection(userDetailsKey).document(uid).update(data);
    }


    // call to create entry In database.
    public void createEntry () {

        db = FirebaseFirestore.getInstance();
        Map<String,Object> data = new HashMap<>();

        data.put(nameKey,name);
        Collections.addAll(ingredients, "banana",
                "butter", "flour", "cheese", "chicken breast",
                "duck", "eggs", "garlic", "milk", "salmon", "tomato");
        data.put(ingredientKey, ingredients);

        db.collection(userDetailsKey).document(uid).set(data);
//        db.collection(userDetailsKey).document(uid).collection("Personal List").document(ingredientKey).set(ingredients);
    }

    public void deleteEntry() {
        db = FirebaseFirestore.getInstance();
        Map<String,Object> data = new HashMap<>();

        data.put(nameKey,name);
        db.collection(userDetailsKey).document(uid).delete();
    }

}
