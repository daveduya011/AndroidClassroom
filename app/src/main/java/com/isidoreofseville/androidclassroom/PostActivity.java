package com.isidoreofseville.androidclassroom;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_LOAD_FILE = 2;
    private static final int RESULT_LOAD_FROMCAMERA = 3;
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.isidoreofseville.androidclassroom.fileprovider";
    FontCustomizer fontCustomizer;


    private TextView formTitle;
    private TextView formMessage;
    private TextView txtTitle, txtMessage, txtCategory;

    private Button btnBack;
    private Button btnPost;

    private RecyclerView recycler_uploadList;
    private ImageButton btnUploadFile;
    private ImageButton btnUploadImage;
    private ImageButton btnUploadFromCamera;

    private List<FileUploaded> fileList;
    private UploadListAdapter uploadListAdapter;

    private Spinner cCategories;
    private ArrayAdapter<CharSequence> categoriesStudent, categoriesTeacher;

    private AlertDialog.Builder alertBuilder;

    private boolean isPosted = false;
    private boolean isFilesUploaded = true;

    private ArrayList<UploadTask> filesOnProcess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        formTitle = findViewById(R.id.formPostTitle);
        formMessage = findViewById(R.id.formPostMessage);
        txtTitle = findViewById(R.id.txtTitle);
        txtMessage = findViewById(R.id.txtMessage);
        txtCategory = findViewById(R.id.txtCategory);

        recycler_uploadList = findViewById(R.id.recycler_files_list);
        btnUploadFile = findViewById(R.id.btn_uploadFile);
        btnUploadFile.setOnClickListener(btnUploadFileListener);
        btnUploadImage = findViewById(R.id.btn_uploadImage);
        btnUploadImage.setOnClickListener(btnUploadImageListener);
        btnUploadFromCamera = findViewById(R.id.btn_uploadFromCamera);
        btnUploadFromCamera.setOnClickListener(btnUploadFromCameraListener);

        fileList = new ArrayList<>();
        filesOnProcess = new ArrayList<>();
        uploadListAdapter = new UploadListAdapter(fileList);
        recycler_uploadList.setLayoutManager(new LinearLayoutManager(this));
        recycler_uploadList.setHasFixedSize(true);
        recycler_uploadList.setAdapter(uploadListAdapter);


        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(btnBackListener);
        btnPost = findViewById(R.id.btnPost);
        btnPost.setOnClickListener(btnPostListener);
        setFonts();

        cCategories = findViewById(R.id.cCategories);

        //SHOW ONLY STUDENT VISIBLE CATEGORIES
        if (!MainActivity.user_canPostToPublic){
            categoriesStudent = ArrayAdapter.createFromResource(this, R.array.categories_student_canpost,
                    R.layout.spinner_item);
        } else {
            categoriesStudent = ArrayAdapter.createFromResource(this, R.array.categories,
                    R.layout.spinner_item);
        }

        categoriesStudent.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cCategories.setAdapter(categoriesStudent);
        cCategories.setSelection(0);

        alertBuilder  = new AlertDialog.Builder(this);


    }

    private OnClickListener btnPostListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            int numOfFiles = 0;
            for (FileUploaded file:fileList){
                if (file.isUploaded()){
                    numOfFiles++;
                }

                if (numOfFiles != fileList.size()){
                    isFilesUploaded = false;
                } else {
                    isFilesUploaded = true;
                }
            }
            System.out.println("NUM OF FILES: " + fileList.size());
            System.out.println("FILES UPLOADED SIZE: " + numOfFiles);

            if (!isFilesUploaded){
                alertBuilder.setMessage("Files haven't been completely uploaded yet. Do you want to post announcement now?").setTitle("Post Announcement");
                alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        post();
                    }

                });

                alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DO NOTHING
                    }
                });

                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            } else {
                post();
            }

        }
    };

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

                    DatabaseReference directory = Database.ref_posts.push();
                    Map<String, Object> map = new HashMap<>();
                    map.put("dateCreated", DateRetriever.getDateToday());
                    map.put("timeCreated", DateRetriever.getTime());
                    map.put("key", directory.getKey());
                    map.put("title", title);
                    map.put("message", message);
                    map.put("author", author);
                    Map<String, Boolean> categories = new HashMap<>();
                    categories.put(categoryName,true);
                    map.put("categories", categories);

                    Map<String, Map> images = new HashMap<>();
                    Map<String, Map> files = new HashMap<>();

                    for (FileUploaded file : fileList){

                        Map<String, String> fileDetails = new HashMap<>();
                        fileDetails.put("filename", file.getFileName());
                        fileDetails.put("uri", file.getDownloadUri());

                        if (file.isUploaded() && file.isImage()){
                            String uri = file.getDownloadUri();
                            images.put(file.getKey(), fileDetails);
                        }

                        if (file.isUploaded() && file.isFile()){
                            String uri = file.getDownloadUri();
                            files.put(file.getKey(), fileDetails);
                        }


                    }

                    map.put("images", images);
                    map.put("files", files);

                    directory.setValue(map);

                    directory.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Toast.makeText(PostActivity.this, "You have successfully posted an announcement", Toast.LENGTH_LONG).show();
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

    private OnClickListener btnUploadFileListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Files"), RESULT_LOAD_FILE);

        }
    };
    private OnClickListener btnUploadImageListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Photos"), RESULT_LOAD_IMAGE);

        }
    };

    private OnClickListener btnUploadFromCameraListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            File path = new File(getFilesDir(), "images/captured/");
            if (!path.exists()) path.mkdirs();
            File image = new File(path, "image.jpg");
            Uri imageUri = FileProvider.getUriForFile(PostActivity.this,CAPTURE_IMAGE_FILE_PROVIDER, image);
            Intent intent = new Intent();
            intent.setAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(Intent.createChooser(intent, "CAMERA"), RESULT_LOAD_FROMCAMERA);
            }

        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            if (data.getData() != null){

                final String key = Database.ref_users.push().getKey();

                Uri fileUri = data.getData();
                final FileUploaded file = new FileUploaded(key);
                file.setFileName(getFileName(fileUri));
                file.setKey(key);
                fileList.add(file);

                uploadListAdapter.notifyDataSetChanged();

                StorageReference fileToUpload = Storage.images.child(key);
                final UploadTask task = fileToUpload.putFile(fileUri);
                task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        file.setUploaded(true);
                        file.setDownloadUri(taskSnapshot.getDownloadUrl().toString());
                        file.setImage(true);
                        filesOnProcess.remove(task);
                        uploadListAdapter.notifyDataSetChanged();
                        System.out.println("DONE UPLOADING");

                        CharSequence pastmessage = formMessage.getText();
                        formMessage.setText(pastmessage + "\n" + "[image]" + key + "[/image]");
                    }
                });
                filesOnProcess.add(task);
            }
        }

        else if (requestCode == RESULT_LOAD_FROMCAMERA && resultCode == RESULT_OK) {
            File path = new File(getFilesDir(), "images/captured/");
            File image = new File(path, "image.jpg");
            Uri imageUri = FileProvider.getUriForFile(PostActivity.this,CAPTURE_IMAGE_FILE_PROVIDER, image);
            final String key = Database.ref_users.push().getKey();

            final FileUploaded file = new FileUploaded(key);
            file.setFileName(getFileName(imageUri));
            file.setKey(key);
            fileList.add(file);
            uploadListAdapter.notifyDataSetChanged();

            StorageReference fileToUpload = Storage.images.child(key);
            final UploadTask task = fileToUpload.putFile(imageUri);
            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    file.setUploaded(true);
                    file.setDownloadUri(taskSnapshot.getDownloadUrl().toString());
                    file.setImage(true);
                    filesOnProcess.remove(task);
                    uploadListAdapter.notifyDataSetChanged();
                    System.out.println("DONE UPLOADING");

                    CharSequence pastmessage = formMessage.getText();
                    formMessage.setText(pastmessage + "\n" + "[image]" + key + "[/image]");
                }
            });

            filesOnProcess.add(task);


        } else if (requestCode == RESULT_LOAD_FILE && resultCode == RESULT_OK){

            if (data.getData() != null){

            final String key = Database.ref_users.push().getKey();

            Uri fileUri = data.getData();
            final FileUploaded file = new FileUploaded(key);

            file.setKey(key);
            file.setFileName(getFileName(fileUri));
            fileList.add(file);


            uploadListAdapter.notifyDataSetChanged();

            StorageReference fileToUpload = Storage.files.child(key);
            final UploadTask task = fileToUpload.putFile(fileUri);
            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    file.setUploaded(true);
                    file.setDownloadUri(taskSnapshot.getDownloadUrl().toString());
                    file.setFile(true);
                    filesOnProcess.remove(task);
                    uploadListAdapter.notifyDataSetChanged();
                    System.out.println("DONE UPLOADING");

                    CharSequence pastmessage = formMessage.getText();
                    formMessage.setText(pastmessage + "\n" + "[file]" + key + "[/file]");
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    fileList.remove(file);
                    uploadListAdapter.notifyDataSetChanged();
                    filesOnProcess.remove(task);
                }
            });
                filesOnProcess.add(task);

            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1){
                result = result.substring(cut + 1);
            }
        }
        return result;
    }



    private OnClickListener btnBackListener = new OnClickListener(){

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

                PostActivity.super.onBackPressed();
            }

        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertBuilder.show();


    }

    private void setFonts(){
        TextView[] tv1 = {formTitle, formMessage};
        TextView[] tv2 = {txtTitle, txtMessage, txtCategory, btnBack, btnPost};

        fontCustomizer = new FontCustomizer(this);
        fontCustomizer.setToQuickSand(tv1);
        fontCustomizer.setToQuickSandBold(tv2);
    }

    @Override
    protected void onDestroy() {
        for (UploadTask task : filesOnProcess){
            task.cancel();
            System.out.println("Cancelled upload");
        }

        super.onDestroy();
    }
}
