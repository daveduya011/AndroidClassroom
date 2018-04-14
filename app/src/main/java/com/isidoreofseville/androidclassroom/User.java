package com.isidoreofseville.androidclassroom;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Dave on 1/21/2018.
 */

public class User implements Serializable{

    private String bio, contact, email, firstname, grade, id, lastname, picture, role, section, username;
    private String key;
    private HashMap<String, Object> birthdate;
    private HashMap<String, Object> favorites;
    private HashMap<String, Object> lastreadposts;

    public User() {
    }

    public String getBio() {
        return bio;
    }

    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {

        if (firstname == null){
            firstname = "";
        }
        return firstname;
    }

    public String getGrade() {
        return grade;
    }

    public String getId() {
        return id;
    }

    public String getLastname() {

        if (lastname == null){
            lastname = "";
        }

        return lastname;
    }

    public String getPicture() {
        return picture;
    }

    public String getRole() {
        return role;
    }

    public String getSection() {
        return section;
    }

    public String getUsername() {
        return username;
    }

    public HashMap<String, Object> getBirthdate() {
        if (birthdate == null){
            HashMap<String, String> birth = new HashMap<String, String>();
            birth.put("day", "0");
            birth.put("month", "0");
            birth.put("year", "0");

            HashMap<String, Object> fullbirth = new HashMap<String, Object>();
            fullbirth.put("birthdate", birth);

            return fullbirth;
        }
        return birthdate;
    }

    public HashMap<String, Object> getFavorites() {
        return favorites;
    }

    public HashMap<String, Object> getLastreadposts() {
        return lastreadposts;
    }

    public void setLastreadposts(HashMap<String, Object> lastreadposts) {
        this.lastreadposts = lastreadposts;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
