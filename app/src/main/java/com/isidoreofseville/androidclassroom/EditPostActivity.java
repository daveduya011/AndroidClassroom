package com.isidoreofseville.androidclassroom;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {

    private String key;

    FontCustomizer fontCustomizer;

    private TextView formTitle;
    private TextView formMessage;
    private TextView txtTitle, txtMessage, txtCategory;

    private Button btnBack;
    private Button btnPost;

    private Spinner cCategories;
    private ArrayAdapter<CharSequence> categoriesStudent, categoriesTeacher;

    private AlertDialog.Builder alertBuilder;

    private boolean isPosted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);


        Intent intent = getIntent();
        key = intent.getStringExtra("key");

        formTitle = findViewById(R.id.formPostTitle);
        formMessage = findViewById(R.id.formPostMessage);
        txtTitle = findViewById(R.id.txtTitle);
        txtMessage = findViewById(R.id.txtMessage);
        txtCategory = findViewById(R.id.txtCategory);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(btnBackListener);
        btnPost = findViewById(R.id.btnPost);
        btnPost.setOnClickListener(btnPostListener);
        setFonts();

        cCategories = findViewById(R.id.cCategories);
        categoriesStudent = ArrayAdapter.createFromResource(this, R.array.categories,
                R.layout.spinner_item);

        categoriesStudent.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cCategories.setAdapter(categoriesStudent);
        cCategories.setSelection(0);

        alertBuilder  = new AlertDialog.Builder(this);

        loadPost();
    }

    private View.OnClickListener btnPostListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            post();
        }
    };


    private void loadPost(){
        Database.ref_posts.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post model = dataSnapshot.getValue(Post.class);

                formTitle.setText(model.getTitle());
                formMessage.setText(model.getMessage());
                for (String cat : model.getCategories().keySet()){
                    String category = cat;
                    String categoryName = "";
                    if (category.equals("Classroom Announcements"))
                        categoryName = "Class Announcement";

                    if (category.equals("Homeworks"))
                        categoryName = "Homework";

                    if (category.equals("Projects"))
                        categoryName = "Project";

                    if (category.equals("Bills"))
                        categoryName = "Bill";

                    if (category.equals("Events"))
                        categoryName = "Event";

                    if (category.equals("Others"))
                        categoryName = "Other";
                    cCategories.setSelection(categoriesStudent.getPosition(categoryName));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void post(){

        String title = formTitle.getText().toString().trim();
        String message = formMessage.getText().toString().trim();
        String category = cCategories.getSelectedItem().toString().trim();
        String categoryName = category;
        String author = MainActivity.getUser();


        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Please fill in the title", Toast.LENGTH_SHORT).show();


        }
        else if (TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please fill in the message", Toast.LENGTH_SHORT).show();
        } else {

            if (isPosted == false){
                if (DateRetriever.isConnected()){
                    isPosted = true;

                    DatabaseReference directory = Database.ref_posts.child(key);
                    directory.child("title").setValue(title);
                    directory.child("message").setValue(message);
                    Map<String, Boolean> categories = new HashMap<>();
                    categories.put(categoryName,true);
                    directory.child("categories").setValue(categories);

                    directory.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Toast.makeText(EditPostActivity.this, "You have successfully edited your post.", Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
                }

            }

        }


    }


    private View.OnClickListener btnBackListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            alertBuilder.setMessage("All of your data will not be saved. Do you really want to go back?").setTitle("Go back");
            alertBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
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

        alertBuilder.setMessage("All of your data will not be saved. Do you really want to go back?").setTitle("Go back");
        alertBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditPostActivity.super.onBackPressed();
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

    private void setFonts(){
        TextView[] tv1 = {formTitle, formMessage};
        TextView[] tv2 = {txtTitle, txtMessage, txtCategory, btnBack, btnPost};

        fontCustomizer = new FontCustomizer(this);
        fontCustomizer.setToQuickSand(tv1);
        fontCustomizer.setToQuickSandBold(tv2);
    }

}
