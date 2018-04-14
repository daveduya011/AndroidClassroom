package com.isidoreofseville.androidclassroom;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Dave on 1/8/2018.
 */

public class Database {

    public static String section;

    public static FirebaseDatabase database;
    public static DatabaseReference ref_root;
    public static DatabaseReference ref_posts;
    public static DatabaseReference ref_time;
    public static DatabaseReference ref_connection;
    public static DatabaseReference ref_info;
    public static DatabaseReference ref_users;
    public static DatabaseReference ref_reports;
    public static DatabaseReference ref_user;

    public static FirebaseStorage storage;
    public static StorageReference storage_root;
    public static StorageReference storage_profiles;

    public Database() {

        section = "";

        if (database == null){
            database = FirebaseDatabase.getInstance();
            //To enable offline compatibilities
            database.getInstance().setPersistenceEnabled(true);
        }

        if (storage == null){
            storage = FirebaseStorage.getInstance();
        }


        ref_root = database.getReference();
        ref_posts = ref_root.child(section).child("Posts");
        ref_time = ref_root.child(".info/serverTimeOffset");
        ref_connection = ref_root.child(".info/connected");
        ref_info = ref_root.child(".info");
        ref_users = ref_root.child("Users");
        ref_reports = ref_root.child("Reports");

        storage_root = storage.getReference();
        storage_profiles = storage_root.child("Profiles");

//
//        if (MainActivity.user != null){
//            ref_user = ref_root.child("Users").child(MainActivity.getUser());
//        }
    }

    public static void setSection(String section) {
        Database.section = section;
        ref_posts = ref_root.child(section).child("Posts");
    }
}
