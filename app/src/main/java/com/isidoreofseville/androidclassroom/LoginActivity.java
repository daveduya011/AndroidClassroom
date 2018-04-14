package com.isidoreofseville.androidclassroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends AppCompatActivity {

    private TextView title1, title2;
    private EditText formUsername, formPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean hasLoginClicked;

    private FontCustomizer fontCustomizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPref= getSharedPreferences("sharedpref", 0);
        SharedPreferences.Editor editor = sharedPref.edit();

        boolean isFreshInstalled = sharedPref.getBoolean("isFreshInstalled", true);

        if (isFreshInstalled){
            //Show splashscreen
            Intent intent = new Intent(this, Splashscreen.class);
            startActivity(intent);

            System.out.println("FRESHLY INSTALLED");
            editor.putBoolean("isFreshInstalled", false);
            editor.commit();
        } else {
            System.out.println("NOT FRESHLY INSTALLED");

        }

        title1 = findViewById(R.id.title1);
        title2 = findViewById(R.id.title2);
        formUsername = findViewById(R.id.formPostTitle);
        formPassword = findViewById(R.id.formPostMessage);
        btnLogin = findViewById(R.id.btnPost);
        progressBar = findViewById(R.id.progressBar);
        setFonts();

        formOnLoad();

        //FIREBASE
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
                    Toast.makeText(LoginActivity.this, "LOGGED IN", Toast.LENGTH_LONG).show();
                    //MainActivity.refreshAll();
                    finish();
                }
            }
        };


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!hasLoginClicked){

                    //fetch for some internet
                    Database.ref_root.child("coonn").setValue(true);

                    hasLoginClicked = true;

                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    hasLoginClicked = false;
                                    cancel();
                                }
                            },
                            3000
                    );
                    String username = formUsername.getText().toString().trim();
                    String password = formPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
                        Toast.makeText(LoginActivity.this, "Please fill in the fields.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (DateRetriever.isConnected())
                        checkLogin(username, password);
                        else
                            Toast.makeText(LoginActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();

                    }
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();


        //fetch for some internet
        Database.ref_root.child("coonn").setValue(true);
        mAuth.addAuthStateListener(mAuthListener);

    }

    private void checkLogin(final String username, final String password){

        progressBar.setVisibility(View.VISIBLE);

        final boolean[] hasUserName = {false};
        final boolean[] hasId = {false};

        //Check if it is email
        if (Patterns.EMAIL_ADDRESS.matcher(username).matches()){
            mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        //Or a username
        else {
            final ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final DatabaseReference ref = dataSnapshot.getRef();

                    //DETECT IF USERNAME HAS A MATCH USERNAME IN THE DATABASE
                    ref.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String refUsername = dataSnapshot.getValue(String.class);
                            if (refUsername.equals(username)){
                                System.out.println("Perform Username Login");
                                hasUserName[0] = true;

                                //Get email
                                ref.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String email = dataSnapshot.getValue(String.class);
                                        System.out.println(email);
                                        checkLogin(email, password);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //DETECT IF ID HAS A MATCH ID IN THE DATABASE
                    ref.child("id").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String refId = dataSnapshot.getValue(String.class);
                            if (refId.equals(username)){
                                System.out.println("Perform Id Login");
                                hasId[0] = true;

                                //Get email
                                ref.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String email = dataSnapshot.getValue(String.class);
                                        System.out.println(email);
                                        checkLogin(email, password);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            //Get the email associated with the username
            Database.ref_users.addChildEventListener(childEventListener);

            Database.ref_users.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Remove Listener
                    Database.ref_users.removeEventListener(childEventListener);

                    List<DatabaseReference> refs = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                         refs.add(snapshot.getRef());
                    }

                    // TO ADD LISTENER TO LAST DATABASE REFERENCE
                    // TO DETECT IF EVERYTHING HAS BEEN QUERIED
                    refs.get(refs.size() - 1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check if there has been an id or username
                            if (hasId[0] == false && hasUserName[0] == false){
                                //If there is none,
                                //LOGIN WITH JUST DUMMY EMAIL THAT IS IMPOSSIBLE TO BE OWNED BY ANYONE
                                System.out.println("Logging in with just random");
                                String email = "dummy321421@gmail.com";
                                checkLogin(email, password);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void formOnLoad(){
        formUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    formUsername.setHint("");
                else
                    formUsername.setHint(R.string.formUsername);
            }
        });

        formPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    formPassword.setHint("");
                else
                    formPassword.setHint(R.string.formPassword);
            }
        });
    }

    private void setFonts(){
        fontCustomizer = new FontCustomizer(this);

        TextView[] tv = {title1, title2, formUsername, formPassword, btnLogin};
        fontCustomizer.setToQuickSandBold(tv);
    }
}
