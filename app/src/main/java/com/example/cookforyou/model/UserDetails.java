package com.example.cookforyou.model;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserDetails implements Serializable {

    // Database Keys
    public static final String userDetailsKey = "UserDetails";
    public static final String nameKey = "Name";
    //maybe can implement the number of clicks of recipe using this class
    private String uid;


    private String matriculationNumber;
    public String name;

    public UserDetails(String name, String uid){
        this.name = name;
        this.uid = uid;
    }

    public UserDetails(String name){

        this.name = name;

    }

    public String getName() {
        return name;
    }

    // call to update entry In database.

    public void updateEntry(String newName){
        this.name = newName;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Object> data = new HashMap<>();

        data.put(nameKey,name);

        db.collection(userDetailsKey).document(uid).update(data);
    }

    public void updateEntry () {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Object> data = new HashMap<>();

        data.put(nameKey,name);

        db.collection(userDetailsKey).document(uid).update(data);
    }


    // call to create entry In database.
    public void createEntry () {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Object> data = new HashMap<>();

        data.put(nameKey,name);

        db.collection(userDetailsKey).document(uid).set(data);
    }

    public void deleteEntry() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String,Object> data = new HashMap<>();

        data.put(nameKey,name);
        db.collection(userDetailsKey).document(uid).delete();
    }

}
