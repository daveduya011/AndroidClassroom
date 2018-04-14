package com.isidoreofseville.androidclassroom;

import android.widget.LinearLayout;

/**
 * Created by Dave on 3/12/2018.
 */

public class NewUser {

    private String firstname;
    private String lastname;
    private String ID;
    private LinearLayout inflatedform;

    public NewUser(String firstname, String lastname, String ID) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.ID = ID;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getID() {
        return ID;
    }

    public void setInflatedform(LinearLayout inflatedform) {
        this.inflatedform = inflatedform;
    }

    public LinearLayout getInflatedform() {
        return inflatedform;
    }
}
