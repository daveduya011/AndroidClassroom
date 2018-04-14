package com.isidoreofseville.androidclassroom;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Dave on 3/13/2018.
 */

public class Auth_SignUp {

    private FirebaseOptions firebaseOptions;
    private static FirebaseApp app_signup;
    private static FirebaseAuth auth_signup;
    private static boolean hasAppInitianalize;

    public Auth_SignUp(Context context, String name) {


        if (!hasAppInitianalize){

            firebaseOptions = new FirebaseOptions.Builder()
                    .setDatabaseUrl("https://spcf-class-announcement-system.firebaseio.com")
                    .setApiKey("AIzaSyCCPgm0qcVv4Qr_HLRi6s5YcQLaNildKWY")
                    .setApplicationId("spcf-class-announcement-system").build();


            app_signup = FirebaseApp.initializeApp(context, firebaseOptions, name);

            auth_signup = FirebaseAuth.getInstance(app_signup);
            hasAppInitianalize = true;
        }
    }

    public static FirebaseAuth getAuth_signup() {
        return auth_signup;
    }

    public static void setAuth_signup(FirebaseAuth auth_signup) {
        Auth_SignUp.auth_signup = auth_signup;
    }

    public static FirebaseApp getApp_signup() {
        return app_signup;
    }

    public static void setHasAppInitianalize(boolean hasAppInitianalize) {
        Auth_SignUp.hasAppInitianalize = hasAppInitianalize;
    }
}
