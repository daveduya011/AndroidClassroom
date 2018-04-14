package com.isidoreofseville.androidclassroom;

/**
 * Created by Dave on 1/14/2018.
 */

public class LatestPost {

    public boolean hasPost;
    public int date;
    public String key;

    public LatestPost() {
        date = 0;
    }

    public void getLatestPost(int lastQuery, int currentQuery, String modelKey, String postKey){
        if (currentQuery >= lastQuery) {
            //If posted the same date, then compare between their keys
            if (currentQuery == lastQuery){
                if (modelKey.compareTo(postKey) >= 0){
                    date = currentQuery;
                    key = modelKey;
                }
                //If not, then just get the post that has the higher post date
            } else {
                date = currentQuery;
                key = modelKey;
            }
        }
        hasPost = true;
    }

}

