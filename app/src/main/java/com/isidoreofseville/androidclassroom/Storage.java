package com.isidoreofseville.androidclassroom;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Dave on 3/4/2018.
 */

public class Storage {

    private static String section = "none";
    private static StorageReference storageRef;
    public static StorageReference images;
    public static StorageReference files;
    public static StorageReference profiles;

    public Storage() {
        storageRef = FirebaseStorage.getInstance().getReference();
        images = storageRef.child(section).child("images");
        files = storageRef.child(section).child("files");
        profiles = storageRef.child("profiles");
    }

    public static void loadSectionFiles(String section) {
        Storage.section = section;
        images = storageRef.child(section).child("images");
        files = storageRef.child(section).child("files");
    }

}
