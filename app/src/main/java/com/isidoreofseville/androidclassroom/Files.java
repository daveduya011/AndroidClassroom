package com.isidoreofseville.androidclassroom;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class Files extends AppCompatActivity {

    private LinearLayout filesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        filesLayout = findViewById(R.id.filesLayout);

        Database.ref_posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final Post post = snapshot.getValue(Post.class);
                    Database.ref_posts.child(post.getKey()).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                final String fileId = snapshot1.getKey();

                                LinearLayout inflateLayout = (LinearLayout) LayoutInflater.from(Files.this).inflate(R.layout.post_file, filesLayout, false);
                                filesLayout.addView(inflateLayout);
                                TextView textview = inflateLayout.findViewById(R.id.filename);
                                textview.setText(post.getFiles().get(fileId).get("filename"));
                                ImageButton dlButton = inflateLayout.findViewById(R.id.btnFileDownload);
                                dlButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String filename = post.getFiles().get(fileId).get("filename");
                                        String fileuri = post.getFiles().get(fileId).get("uri");
                                        if (DateRetriever.isConnected())
                                            downloadFile(filename, fileuri);
                                        else
                                            Toast.makeText(Files.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT);
                                    }
                                });
                            }
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


    private void downloadFile(String filename, String fileuri) {
        final NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "M_CH_ID");
        mBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_file_download_white_36dp)
                .setTicker("Downloading")
                .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                .setContentTitle("Downloading: " + filename)
                .setProgress(100,0,true);

        Toast.makeText(Files.this, "DOWNLOAD STARTED", Toast.LENGTH_SHORT).show();
        mNotifyManager.notify(1, mBuilder.build());

        final StorageReference storageref = FirebaseStorage.getInstance().getReferenceFromUrl(fileuri);

        final File rootPath = new File(Environment.getExternalStorageDirectory(), "SPCFCLASSROOM");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }


        File localFile = new File(rootPath,filename);
        String defaultFileName = filename;
        if (localFile.exists()){
            for (int i = 0; i < 30; i++){
                int extension = filename.lastIndexOf(".");
                filename = filename.substring(0, extension) + "(" + i + ")" + filename.substring(extension, filename.length());
                localFile = new File(rootPath,filename);
                if (localFile.exists()){
                    filename = defaultFileName;
                    localFile = new File(rootPath, filename);
                    continue;
                } else {
                    break;
                }
            }
        }

        final File finalLocalFile = localFile;
        final String finalFilename = filename;
        storageref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " + finalLocalFile.toString());

                mBuilder.setContentTitle("Download Complete: " + finalFilename)
                        .setProgress(0,0,false)
                        .setContentText("STORAGE/SPCFCLASSROOM/" + finalFilename)
                        .setTicker("Download Complete");

                mNotifyManager.notify(1, mBuilder.build());
                Toast.makeText(Files.this, "DOWNLOAD COMPLETED", Toast.LENGTH_SHORT).show();


            }
        });
    }
}
