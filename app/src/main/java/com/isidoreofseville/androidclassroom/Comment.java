package com.isidoreofseville.androidclassroom;

import android.net.Uri;

/**
 * Created by Dave on 1/23/2018.
 */

public class Comment {
    private String message, author, dateCreated, timeCreated, authorName, key;
    private Uri authorPictureUri;
    private boolean isOwnedPost;

    public Comment() {
    }

    public Comment(String message, String author, String dateCreated, String timeCreated) {
        this.message = message;
        this.author = author;
        this.dateCreated = dateCreated;
        this.timeCreated = timeCreated;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Uri getAuthorPictureUri() {
        return authorPictureUri;
    }

    public void setAuthorPictureUri(Uri authorPictureUri) {
        this.authorPictureUri = authorPictureUri;
    }

    public boolean isOwnedPost() {
        return isOwnedPost;
    }

    public void setOwnedPost(boolean ownedPost) {
        isOwnedPost = ownedPost;
    }
}
