package com.isidoreofseville.androidclassroom;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddStudentsActivity extends AppCompatActivity {

    private static int num;
    private static boolean savedAll;

    FontCustomizer fontCustomizer;
    FirebaseAuth mAuth;

    LinearLayout formLayout;

    private ArrayList<NewUser> newUsers;
    private ArrayList<LinearLayout> inflatedForms;


    private TextView formFirstName;
    private TextView formLastName;
    private TextView formID;

    private ScrollView scrollView;

    private Button btnBack;
    private Button btnPost;
    private Button btnAddMore;

    private ProgressBar progressBar;

    private AlertDialog.Builder alertBuilder;

    private boolean isPosted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students);
        mAuth = FirebaseAuth.getInstance();
        Auth_SignUp auth_signUp = new Auth_SignUp(getApplicationContext(), "anything");

        newUsers = new ArrayList<>();
        inflatedForms = new ArrayList<>();

        num = 0;

        formLayout = findViewById(R.id.formLayout);
        scrollView = findViewById(R.id.scrollView2);

        progressBar = findViewById(R.id.progressbar_addstudents);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(btnBackListener);
        btnPost = findViewById(R.id.btnPost);
        btnPost.setOnClickListener(btnPostListener);
        btnAddMore = findViewById(R.id.btnAddMore);
        btnAddMore.setOnClickListener(btnAddMoreListener);

        LinearLayout inflateForm = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.add_form, formLayout, false);
        formLayout.addView(inflateForm);
        inflatedForms.add(inflateForm);

        formFirstName = formLayout.findViewById(R.id.formFirstName);
        formLastName = formLayout.findViewById(R.id.formLastName);
        formID = formLayout.findViewById(R.id.formId);

        formID.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    btnAddMore.callOnClick();
                    return true;
                }

                return false;
            }
        });


        setFonts();
        formOnLoad();


        alertBuilder  = new AlertDialog.Builder(this);


    }

    private void formOnLoad() {
            formFirstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        formFirstName.setHint("");
                    else
                        formFirstName.setHint(R.string.txt_first_name);
                }
            });

            formID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        formID.setHint("");
                    else
                        formID.setHint(R.string.txtid);
                }
            });

            formLastName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus)
                        formLastName.setHint("");
                    else
                        formLastName.setHint(R.string.txt_last_name);
                }
            });
    }

    private OnClickListener btnPostListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            //make sure users has been added in array
            newUsers.clear();

            boolean hasBlankField = false;
            boolean hasFilledForm = false;
            for (LinearLayout inflatedform: inflatedForms){
                TextView viewFirstName = inflatedform.findViewById(R.id.formFirstName);
                TextView viewLastName = inflatedform.findViewById(R.id.formLastName);
                TextView viewID = inflatedform.findViewById(R.id.formId);

                String firstname = viewFirstName.getText().toString().trim();
                String lastname = viewLastName.getText().toString().trim();
                String id = viewID.getText().toString().trim();

                NewUser newUser = new NewUser(firstname, lastname, id);
                newUser.setInflatedform(inflatedform);
                newUsers.add(newUser);

                //IF ALL USER FIELD HAS BEEN FILLED, THEN SET HASFIELD TO TRUE
                 if ( (TextUtils.isEmpty(firstname) && TextUtils.isEmpty(lastname) && TextUtils.isEmpty(id))
                         || (firstname.equals("") && lastname.equals("") && id.equals(""))) {
                     continue;
                 } else {
                     hasFilledForm = true;
                 }

                 if ((TextUtils.isEmpty(firstname) || TextUtils.isEmpty(lastname) || TextUtils.isEmpty(id))
                        || (firstname.equals("") || lastname.equals("") || id.equals(""))){
                    hasBlankField = true;
                }
            }

            if (!hasBlankField && hasFilledForm){
                post();
                progressBar.setVisibility(View.VISIBLE);
                btnPost.setEnabled(false);
                btnPost.setVisibility(View.GONE);
                btnAddMore.setEnabled(false);
                btnAddMore.setVisibility(View.GONE);
            }

            else if (hasBlankField){
                Toast.makeText(AddStudentsActivity.this, "Please fill all the blank forms.", Toast.LENGTH_SHORT).show();
            } else if (!hasFilledForm){
                Toast.makeText(AddStudentsActivity.this, "Forms cannot be blank", Toast.LENGTH_SHORT).show();

            }


        }
    };

    private OnClickListener btnAddMoreListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            scrollView.fullScroll(View.FOCUS_DOWN);
            LinearLayout inflateForm = (LinearLayout) LayoutInflater.from(AddStudentsActivity.this).inflate(R.layout.add_form, formLayout, false);
            TextView formIdGenerated = inflateForm.findViewById(R.id.formId);
            inflatedForms.add(inflateForm);

            formIdGenerated.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_NEXT) {
                        btnAddMore.callOnClick();
                        return true;
                    }

                    return false;
                }
            });
            formLayout.addView(inflateForm);
            inflateForm.startAnimation(AnimationUtils.loadAnimation(AddStudentsActivity.this, R.anim.abc_grow_fade_in_from_bottom));

            inflateForm.requestFocus();

        }
    };

    private void post(){

        if (num < newUsers.size()){


            final String firstName = newUsers.get(num).getFirstname();
            final String lastName = newUsers.get(num).getLastname();
            final String ID = newUsers.get(num).getID();
            final String role = ROLES.STUDENT;
            final String grade = MainActivity.userInfo.getGrade();
            final String section = MainActivity.userInfo.getSection();
            final String picture = "https://firebasestorage.googleapis.com/v0/b/spcf-class-announcement-system.appspot.com/o/profiles%2FKNUCKLES.jpg?alt=media&token=d7055c3d-65f7-41ec-bb90-dc0465faab93";
            final String username = "";
            final String contact = "";


            if ((TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && TextUtils.isEmpty(ID))
                    || (firstName.equals("") && lastName.equals("") && ID.equals(""))){
                //JUST IGNORE THE TAB
                num++;
                post();
            }
            else {

                    if (DateRetriever.isConnected()){

                        final String generatedEmail = getSaltString(10)+"@gmail.com";
                        final String generatedPassword = getSaltString(6);
                        Auth_SignUp.getAuth_signup().createUserWithEmailAndPassword(generatedEmail, generatedPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                String key = task.getResult().getUser().getUid();

                                DatabaseReference directory = Database.ref_users.child(key);
                                Map<String, Object> map = new HashMap<>();
                                map.put("firstname", firstName);
                                map.put("lastname", lastName);
                                map.put("contact", contact);
                                map.put("email", generatedEmail);
                                map.put("id", ID);
                                map.put("role", role);
                                map.put("grade", grade);
                                map.put("picture", picture);
                                map.put("section", section);
                                map.put("username", username);
                                Map<String, String> birthdate = new HashMap<>();
                                birthdate.put("day", "0");
                                birthdate.put("month", "0");
                                birthdate.put("year", "0");
                                map.put("birthdate", birthdate);

                                directory.setValue(map);

                                directory.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Toast.makeText(AddStudentsActivity.this, "Successfully Registered: " + firstName, Toast.LENGTH_LONG).show();
                                        newUsers.get(num).getInflatedform().findViewById(R.id.img_check).setVisibility(View.VISIBLE);
                                        writeToFile("Name: " + firstName + " " + lastName + "\n"
                                            + "ID Number: " + ID + "\n" +
                                                "Password: " + generatedPassword + "\n\n\n"
                                        );
                                        Auth_SignUp.getAuth_signup().signOut();
                                        num++;
                                        post();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        });

                    } else {
                        Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
                    }
            }


        } else {
            num = 0;
            progressBar.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
            savedAll = true;
        }


    }



    private OnClickListener btnBackListener = new OnClickListener(){

        @Override
        public void onClick(View v) {
                alertBuilder.setMessage("All of your data will not be saved. Do you really want to go back?").setTitle("Go back");
                alertBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddStudentsActivity.super.onBackPressed();
                    }
                });

                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DO NOTHING
                    }
                });

                AlertDialog dialog = alertBuilder.create();
                dialog.show();

        }
    };

    @Override
    public void onBackPressed() {

        if (!savedAll){
            alertBuilder.setMessage("All of your data will not be saved. Do you really want to go back?").setTitle("Go back");
            alertBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    AddStudentsActivity.super.onBackPressed();
                }

            });

            alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //DO NOTHING
                }
            });

            AlertDialog dialog = alertBuilder.create();
            dialog.show();
        } else {
            super.onBackPressed();
        }


    }

    private void setFonts(){
//        TextView[] tv1 = {formFirstName, formID};
        TextView[] tv2 = {btnBack, btnPost};

        fontCustomizer = new FontCustomizer(this);
//        fontCustomizer.setToQuickSand(tv1);
        fontCustomizer.setToQuickSandBold(tv2);
    }

    private String getSaltString(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public void writeToFile(String data)
    {
        if (canWriteOnExternalStorage()){
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/SPCFCLASSROOM/");
            dir.mkdirs();
            File file = new File(dir, "users.txt");

            try {
                FileWriter writer = new FileWriter(file, true);
                writer.append(data);
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "ERROR CREATING FILE IN SD CARD.", Toast.LENGTH_LONG);
        }

    }

    public static boolean canWriteOnExternalStorage() {
        // get the state of your external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // if storage is mounted return true
            Log.v("sTag", "Yes, can write to external storage." );
            return true;
        }
        return false;
    }

}
