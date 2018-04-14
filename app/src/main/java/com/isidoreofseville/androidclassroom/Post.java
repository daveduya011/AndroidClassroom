package com.isidoreofseville.androidclassroom;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dave on 12/26/2017.
 */

public class Post implements Comparable<Post>{

    private String title, message, dateCreated, author, timeCreated, key;
    private int type;
    private String text, authorName = "";
    private HashMap<String, Boolean> categories;
    private HashMap<String, HashMap<String, String>> images;
    private HashMap<String, Boolean> likes;
    private HashMap<String, HashMap<String, String>> files;
    private boolean isOwnedPost;

    public Post(){

    }

    public Post(String title, String message, String dateCreated, String timeCreated, String author, HashMap<String, Boolean> category) {
        this.title = title;

        this.message = message;
        this.dateCreated = dateCreated;
        this.timeCreated = timeCreated;
        this.author = author;
        this.type = PostAdapter.VIEW_TYPES.NORMAL;
        this.key = "0";
        this.categories = category;
    }
    public Post(String text, int type, String key){
        this.text = text;
        this.type = type;
        this.key = key;
        this.isOwnedPost = false;
    }

    public String getTitle() {
        return title;
    }


    public String getMessage() {
        return message;
    }


    public String getDateCreated() {
        return dateCreated;
    }


    public String getAuthor() {
            return author;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public String getKey() {
        return key;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getText() {
        return text;
    }

    public HashMap<String, Boolean> getCategories() {
        if (categories != null){

            return categories;
        }
        HashMap<String, Boolean> hashMap = new HashMap<>();
        return hashMap;

    }

    public boolean isHeader(){
        if (type == PostAdapter.VIEW_TYPES.HEADER){
            return true;
        }
        return false;
    }


    public boolean isNormal(){
        if (type == PostAdapter.VIEW_TYPES.NORMAL){
            return true;
        }
        return false;
    }


    public boolean isFooter(){
        if (type == PostAdapter.VIEW_TYPES.FOOTER){
            return true;
        }
        return false;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public boolean isOwnedPost() {
        return isOwnedPost;
    }

    public void setOwnedPost(boolean ownedPost) {
        isOwnedPost = ownedPost;
    }

    public HashMap<String, Boolean> getLikes() {
        return likes;
    }

    //TO SORT LISTS IN DATE ORDER
    @Override
    public int compareTo(@NonNull Post post){
        return (this.key).compareTo(post.key);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HashMap<String, HashMap<String, String>> getImages() {
        return images;
    }

    public HashMap<String, HashMap<String, String>> getFiles() {
        return files;
    }
}
