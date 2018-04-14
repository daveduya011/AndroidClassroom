package com.isidoreofseville.androidclassroom;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.validator.Validator;
import org.apache.commons.validator.routines.EmailValidator;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class Profile extends AppCompatActivity{

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int THUMBNAIL_SIZE = 100;

    private LinearLayout countpostsLayout;

    private int postsCount = 0;
    private int likesCount = 0;
    Handler handler; Runnable LongPressed;
    SimpleDraweeView profilePicture;
    TextView fullname, section, position, countposts, countlikes, formFullName, formGrade, formSection, formIDNum, formUsername,
        formEmail, formBirthdate,bio, formcontact;
    Button btnEditProfile, btnBack;
    User currentUser;
    String currentAuthorUID;

    Boolean isOwned;
    PopupMenu popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        isOwned = getIntent().getBooleanExtra("isOwned", false);

        if (isOwned){
            currentUser = MainActivity.userInfo;
            currentAuthorUID = MainActivity.getUser();
        } else {
            currentUser = (User) getIntent().getSerializableExtra("user");
            currentAuthorUID = getIntent().getStringExtra("UID");
        }

        profilePicture = findViewById(R.id.profilepicture);
        fullname = findViewById(R.id.fullname);
         section = findViewById(R.id.section);
         position = findViewById(R.id.position);
         countposts = findViewById(R.id.countposts);
         countlikes = findViewById(R.id.countlikes);
         formFullName = findViewById(R.id.formFullName);
         formGrade = findViewById(R.id.formGrade);
         formSection = findViewById(R.id.formSection);
         formIDNum = findViewById(R.id.formIDNum);
         formUsername = findViewById(R.id.formusername);
         bio = findViewById(R.id.bio);
         countpostsLayout = findViewById(R.id.countpostsLayout);

        formEmail = findViewById(R.id.formemail);
        formcontact = findViewById(R.id.formcontact);
        formBirthdate = findViewById(R.id.formbirthdate);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(btnBackListener);

        popup = new PopupMenu(this, btnEditProfile);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.editprofile, popup.getMenu());

        popup.setOnMenuItemClickListener(menuClickListener);

        countpostsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, YourPosts.class).putExtra("UID", currentAuthorUID));
            }
        });


        if (isOwned) {

            handler = new Handler();
            LongPressed = new Runnable() {
                @Override
                public void run() {
                    System.out.println("LONG PRESSED!");
                    changeProfilePicture();
                }
            };

            profilePicture.setOnTouchListener(touchListener);

            btnEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup(view);
                }
            });

        }
            if (currentUser != null) {


                fullname.setText(currentUser.getFirstname() + " " + currentUser.getLastname());
                section.setText(currentUser.getSection());
                position.setText(currentUser.getRole());
                countposts.setText(String.valueOf(postsCount));
                countlikes.setText(String.valueOf(likesCount));
                bio.setText(currentUser.getBio());
                formFullName.setText(currentUser.getFirstname() + " " + currentUser.getLastname());
                formGrade.setText(currentUser.getGrade());
                formSection.setText(currentUser.getSection());
                formIDNum.setText(currentUser.getId());
                formUsername.setText(currentUser.getUsername());
                formEmail.setText(currentUser.getEmail());
                formcontact.setText(currentUser.getContact());
                formBirthdate.setText(currentUser.getBirthdate().get("month") + "/"
                        + currentUser.getBirthdate().get("day") + "/"
                        + currentUser.getBirthdate().get("year")
                );

            }


            if (!isOwned){
                btnEditProfile.setVisibility(View.GONE);

            }

        Uri uri = Uri.parse(currentUser.getPicture());
        profilePicture.setImageURI(uri);
        getPostsCount();
    }

    private void getPostsCount() {
        //get the likes and posts count
        Database.ref_posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (post.getAuthor().equals(currentAuthorUID)){
                        postsCount++;

                        if (post.getLikes() != null) {
                            likesCount += post.getLikes().size();
                        }
                    }
                }

                countposts.setText(String.valueOf(postsCount));
                countlikes.setText(String.valueOf(likesCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void changeProfilePicture() {
        if (DateRetriever.isConnected()){

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Photos"), RESULT_LOAD_IMAGE);
        } else {
            Toast.makeText(this, "CAN'T CHANGE PROFILE PICTURE: NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            if (data.getData() != null) {
                final int MAX_IMAGE_SIZE = 100 * 100;
                ;
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                Uri fileUri = data.getData();

                byte[] byteData = new byte[0];
                try {
                    Bitmap bitmap = getThumbnail(fileUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byteData = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String key = Database.ref_users.push().getKey();
                StorageReference fileToUpload = Storage.profiles.child(key);
                final UploadTask task = fileToUpload.putBytes(byteData);
                task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        Database.ref_users.child(MainActivity.getUser()).child("picture").setValue(taskSnapshot.getDownloadUrl().toString());
                        Toast.makeText(Profile.this, "Profile updated. Please restart your app to accept changes.", Toast.LENGTH_LONG).show();
                        profilePicture.setImageURI(taskSnapshot.getDownloadUrl().toString());
                    }
                });

            }
        }
    }

    private View.OnClickListener btnBackListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnTouchListener  touchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    profilePicture.setActualImageResource(R.drawable.ic_add_a_photo_white_48dp);
                    handler.postDelayed(LongPressed, ViewConfiguration.getLongPressTimeout() + 300);
                    break;
                case MotionEvent.ACTION_UP:
                    profilePicture.setImageURI(currentUser.getPicture());
                    handler.removeCallbacks(LongPressed);
                    break;

            }
            return true;
        }
    };


    public void showPopup(View v) {
        popup.show();
    }

    private PopupMenu.OnMenuItemClickListener menuClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_changeprofilepic:
                    changeProfilePicture();
                    System.out.println("ON CHANGE PROFILE PIC");
                    return true;
                case R.id.menu_editcontact:
                    showInputPopup("Edit Contact", "contact");
                    return true;
                    case R.id.menu_editbio:
                    showInputPopup("Write your bio.", "bio");
                    return true;
                case R.id.menu_updateemail:
                    showInputPopup("Update Email", "email");
                    return true;
                case R.id.menu_changebirthdate:
                    showInputPopup("Birthday: (use format yyyy/MM/dd)", "birthdate");
                    return true;
                case R.id.menu_changeusername:
                    showInputPopup("Change Username", "username");
                    return true;
                case R.id.menu_changepassword:
                    showInputPopup("Change Password", "password");
                    return true;
                default:
                    return false;
            }
        }
    };

    private void showInputPopup(String title, final String databaseattr){
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(title);

        final EditText input = new EditText(this);
        if (databaseattr.equals("contact")){
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        if (databaseattr.equals("password")){
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (databaseattr.equals("birthdate")){
            input.setHint("YYYY/MM/DD");
        }


        alertBuilder.setView(input);

        alertBuilder.setPositiveButton("Save", null);

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        final AlertDialog alertDialog = alertBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String value = input.getText().toString().trim();
                        if (DateRetriever.isConnected()){
                            if (value.length() < 3 && value.length() < 50){
                                Toast.makeText(Profile.this, "Number of characters can't be less than 3 or more than 50", Toast.LENGTH_LONG).show();
                            } else {

                                if (databaseattr.equals("username")){

                                    if (value.length() >= 5 && value.length() < 20) {


                                        Database.ref_users.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                boolean hasExistingUser = false;
                                                for (DataSnapshot snaphshot : dataSnapshot.getChildren()){
                                                    User user = snaphshot.getValue(User.class);

                                                    if (user.getUsername().equals(value)){
                                                        hasExistingUser = true;
                                                    }
                                                }

                                                if (hasExistingUser){
                                                    Toast.makeText(Profile.this, "Existing user has this username. Please choose another username.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Database.ref_users.child(MainActivity.getUser()).child(databaseattr).setValue(value);
                                                    Toast.makeText(Profile.this, "Username has been successfully updated. Please restart your app.", Toast.LENGTH_LONG).show();
                                                    alertDialog.cancel();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                    } else {
                                        Toast.makeText(Profile.this, "Username must contain 5 characters and less than 20 characters", Toast.LENGTH_LONG).show();
                                    }

                                }


                                else if (databaseattr.equals("password")){

                                    if (value.length() >= 5 && value.length() < 20) {
                                        MainActivity.user.updatePassword(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    alertDialog.cancel();
                                                    FirebaseAuth.getInstance().signOut();
                                                    finish();
                                                } else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(Profile.this, databaseattr+ " change FAILED. " + error, Toast.LENGTH_LONG).show();
                                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);

                                                }
                                            }
                                        });
                                        Toast.makeText(Profile.this, "Updating password. Please wait...", Toast.LENGTH_LONG).show();
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);

                                    } else {
                                        Toast.makeText(Profile.this, "Password must contain 5 characters and less than 20 characters", Toast.LENGTH_LONG).show();
                                    }

                                } else if (databaseattr.equals("contact")){
                                    if (value.length() >= 10 && value.length() < 14){

                                        Database.ref_users.child(MainActivity.getUser()).child(databaseattr).setValue(value);
                                        Toast.makeText(Profile.this, databaseattr+ " successfully updated. Please restart your app to see changes.", Toast.LENGTH_LONG).show();

                                        alertDialog.cancel();
                                    } else {
                                        Toast.makeText(Profile.this, "Contact numeber must contain 10 numbers or more", Toast.LENGTH_LONG).show();
                                    }

                                } else if (databaseattr.equals("email")){
                                    boolean valid = EmailValidator.getInstance(true).isValid(value);
                                    if (valid){
                                        MainActivity.user.updateEmail(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Database.ref_users.child(MainActivity.getUser()).child(databaseattr).setValue(value);
                                                    alertDialog.cancel();
                                                    FirebaseAuth.getInstance().signOut();
                                                    finish();
                                                } else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(Profile.this, databaseattr+ " change FAILED. " + error, Toast.LENGTH_LONG).show();
                                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);

                                                }
                                            }
                                        });
                                        Toast.makeText(Profile.this, "Updating email. Please wait...", Toast.LENGTH_LONG).show();
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                                    } else {
                                        Toast.makeText(Profile.this, "Invalid email. Please put a valid email address.", Toast.LENGTH_LONG).show();
                                    }

                                }
                                else if (databaseattr.equals("birthdate")){

                                    String date = value.replaceAll("\\D+","");

                                    if (isValidDate(date)){
                                        //yyyyMMdd
                                        int year = Integer.parseInt(date.substring(0,4));
                                        System.out.println("Year: " + year);
                                        int month = Integer.parseInt(date.substring(4,6));
                                        System.out.println("Month: " + month);
                                        int day = Integer.parseInt(date.substring(6,8));
                                        System.out.println("Day: " + day);

                                        HashMap<String, String> birth = new HashMap<String, String>();
                                        birth.put("day", String.valueOf(day));
                                        birth.put("month", String.valueOf(month));
                                        birth.put("year", String.valueOf(year));


                                        Database.ref_users.child(MainActivity.getUser()).child(databaseattr).setValue(birth);
                                        Toast.makeText(Profile.this, "Birthdate has been successfully updated. Please restart your app to see changes.", Toast.LENGTH_LONG).show();

                                        alertDialog.cancel();
                                    } else {
                                        Toast.makeText(Profile.this, "Invalid date.", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                else {
                                    if (databaseattr.equals("bio") && value.length() > 300){
                                        int exceededLimit = 300 - value.length();
                                        Toast.makeText(Profile.this, "Bio must contain a maximum of only 300 characters. Remove atleast " + exceededLimit + " characters", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Database.ref_users.child(MainActivity.getUser()).child(databaseattr).setValue(value);
                                    Toast.makeText(Profile.this, databaseattr+ " successfully updated. Please restart your app to see changes.", Toast.LENGTH_LONG).show();

                                    alertDialog.cancel();
                                }

                            }
                        } else {
                            Toast.makeText(Profile.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        alertDialog.show();
    }

    //DATE VALIDATION CREDITS ALL GOES TO SLANEC
    public static boolean isValidDate(String dateString) {
        //remove everything other than numbers
        if (dateString == null || dateString.length() != "yyyyMMdd".length()) {
            return false;
        }

        int date;
        try {
            date = Integer.parseInt(dateString);
        } catch (NumberFormatException e) {
            return false;
        }

        int year = date / 10000;
        int month = (date % 10000) / 100;
        int day = date % 100;

        // leap years calculation not valid before 1581
        boolean yearOk = (year >= 1581) && (year <= 2500);
        boolean monthOk = (month >= 1) && (month <= 12);
        boolean dayOk = (day >= 1) && (day <= daysInMonth(year, month));

        return (yearOk && monthOk && dayOk);
    }

    private static int daysInMonth(int year, int month) {
        int daysInMonth;
        switch (month) {
            case 1: // fall through
            case 3: // fall through
            case 5: // fall through
            case 7: // fall through
            case 8: // fall through
            case 10: // fall through
            case 12:
                daysInMonth = 31;
                break;
            case 2:
                if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                    daysInMonth = 29;
                } else {
                    daysInMonth = 28;
                }
                break;
            default:
                // returns 30 even for nonexistant months
                daysInMonth = 30;
        }
        return daysInMonth;
    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }
}


