package com.isidoreofseville.androidclassroom;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostContent extends AppCompatActivity implements View.OnTouchListener{

    private LinearLayout messageLayout;
    private LinearLayout filesLayout;

    private String key;
    private TextView txtTitle, txtContent, txtCategory, txtAuthorName, txtAuthorPosition, txtPostDate, txtLikeCount,
        txtComments, formComment;
    private ImageView profilePicture;
    private Button btnBack, btnComment;
    private ImageButton btnLike, btnReply, btnFav;

    private User currentPostAuthor;
    private int likeCount;
    private List<String> likers;
    private List<String> likersNames;
    private boolean isLiked;
    private boolean isReplyPanelVisible;
    private boolean isCommentPosted;
    private boolean isFaved;

    private View commentPanel;
    private View postPanel;
    private Snackbar sb;

    private RecyclerView recycleViewComments;
    private CommentAdapter adapter_comments;
    private List<Comment> list_comments;

    private ArrayList<Animatable> animatables;

    private String currentAuthorUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_content);

        animatables = new ArrayList<>();
        ScrollView scrollView = findViewById(R.id.scrollview);
        scrollView.setOnTouchListener(this);

        Intent intent = getIntent();
        key = intent.getStringExtra("key");

        filesLayout = findViewById(R.id.filesLayout);
        messageLayout = findViewById(R.id.messageLayout);
        txtTitle = findViewById(R.id.title);
        txtCategory = findViewById(R.id.category);
        txtAuthorName = findViewById(R.id.authorName);
        txtAuthorPosition = findViewById(R.id.authorPosition);
        txtPostDate = findViewById(R.id.postDate);
        txtLikeCount = findViewById(R.id.command_like_text);
        txtComments = findViewById(R.id.comments);
        profilePicture = findViewById(R.id.authorProfile);
        commentPanel = findViewById(R.id.commentPanel);
        postPanel = findViewById(R.id.postPanel);
        formComment = findViewById(R.id.formComment);

        txtComments.setVisibility(View.INVISIBLE);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(btnBackListener);
        btnLike = findViewById(R.id.command_like);
        btnReply = findViewById(R.id.command_reply);
        btnReply.setOnClickListener(btnReplyListener);
        btnFav = findViewById(R.id.command_fav);
        btnComment = findViewById(R.id.btnComment);
        btnComment.setOnClickListener(btnCommentListener);
        postPanel.setOnClickListener(panelClickListener);
        likeCount = 0;

        likersNames = new ArrayList<>();


        list_comments = new ArrayList<>();
        adapter_comments = new CommentAdapter(list_comments, this);
        recycleViewComments = findViewById(R.id.commentsList);
        recycleViewComments.setHasFixedSize(true);
        recycleViewComments.setLayoutManager(new LinearLayoutManager(this));
        recycleViewComments.setAdapter(adapter_comments);

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewProfile(currentPostAuthor, currentAuthorUID);
            }
        });

        setFonts();
        loadPost();
        loadComments();
    }

    public void viewProfile(User currentPostAuthor, String currentAuthorUID) {
        Intent intent = new Intent(PostContent.this, Profile.class);
        Bundle bundle = new Bundle();
        intent.putExtra("isOwned", false);
        intent.putExtra("UID", currentAuthorUID);
        intent.putExtra("user", currentPostAuthor);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void loadComments() {
        Database.ref_posts.child(key).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() > 0)
                    txtComments.setVisibility(View.VISIBLE);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    final Comment comment = snapshot.getValue(Comment.class);
                    comment.setKey(snapshot.getRef().getKey());
                    list_comments.add(comment);
                    adapter_comments.notifyDataSetChanged();

                    Database.ref_users.child(comment.getAuthor()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            if (user != null){
                                comment.setAuthorName(user.getFirstname() + " " + user.getLastname());
                                Uri uri = Uri.parse(user.getPicture());
                                comment.setAuthorPictureUri(uri);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    if (MainActivity.getUser().equals(comment.getAuthor())){
                        comment.setOwnedPost(true);
                    }

                }

                Database.ref_users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        System.out.println("Comments Updated");
                        adapter_comments.notifyDataSetChanged();
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

    private void loadPost(){
        Database.ref_posts.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Post post = dataSnapshot.getValue(Post.class);
                String message = post.getMessage();
                txtTitle.setText(post.getTitle());
                txtPostDate.setText(post.getDateCreated());
                String category;
                for (String cat : post.getCategories().keySet()){
                    category = cat;
                    txtCategory.setText(category);
                }

                //FOR FILES
                String fileopening = "[file]", fileclosing = "[/file]";
                if (message.contains(fileopening) && message.contains(fileclosing)){

                    int strStart = message.indexOf(fileopening);
                    int strEnd = message.indexOf(fileclosing) + fileclosing.length();


                    SparseIntArray stringLocations = new SparseIntArray();

                    while (strStart >= 0){

                        if (strEnd >= strStart){
                            stringLocations.append(strStart, strEnd);
                            strEnd = message.indexOf(fileclosing, strEnd + fileclosing.length()) + fileclosing.length();
                        }

                        strStart = message.indexOf(fileopening, strStart + fileopening.length());
                    }

                    String nMessage = "";
                    for (int i = 0; i < stringLocations.size(); i++){
                        int start = stringLocations.keyAt(i);
                        int end = stringLocations.valueAt(i);

                        final String fileId = message.substring(start, end).replace(fileopening,"").replace(fileclosing,"");

                        if (i == 0){
                            String text = message.substring(0, start);
                            nMessage += text;
                        } else {
                            String text = message.substring(stringLocations.valueAt(i-1), start);
                            nMessage += text;
                        }
                        if (i == stringLocations.size() - 1){
                            String text = message.substring(stringLocations.valueAt(i), message.length());
                            nMessage += text;
                        }

                        LinearLayout inflateLayout = (LinearLayout) LayoutInflater.from(PostContent.this).inflate(R.layout.post_file, filesLayout, false);
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
                                    Toast.makeText(PostContent.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT);
                            }
                        });
                        
                    }

                    message = nMessage;

                }

                //FOR IMAGES
                //THE TEXTVIEW THAT WE ARE GOING TO DUPLICATE
                //CHECK IF THERE ARE IMAGES
                String imgopening = "[image]", imgclosing = "[/image]";

                if (message.contains(imgopening) && message.contains(imgclosing)){

                    SparseIntArray stringLocations = new SparseIntArray();

                    int strStart = message.indexOf(imgopening);
                    int strEnd = message.indexOf(imgclosing) + imgclosing.length();

                    while (strStart >= 0){

                        if (strEnd >= strStart){
                            stringLocations.append(strStart, strEnd);
                            strEnd = message.indexOf(imgclosing, strEnd + imgclosing.length()) + imgclosing.length();
                        }

                        strStart = message.indexOf(imgopening, strStart + imgopening.length());
                    }

                    for (int i = 0; i < stringLocations.size(); i++){
                        int start = stringLocations.keyAt(i);
                        int end = stringLocations.valueAt(i);

                        if (i == 0){
                            String text = message.substring(0, start);
                            TextView inflateTextView = (TextView) LayoutInflater.from(PostContent.this).inflate(R.layout.post_message, null);
                            inflateTextView.setText(text);
                            messageLayout.addView(inflateTextView);
                        } else {
                            String text = message.substring(stringLocations.valueAt(i-1), start);
                            TextView inflateTextView = (TextView) LayoutInflater.from(PostContent.this).inflate(R.layout.post_message, null);
                            inflateTextView.setText(text);
                            messageLayout.addView(inflateTextView);
                        }

                        String imageId = message.substring(start, end).replace(imgopening,"").replace(imgclosing,"");
                        final SimpleDraweeView draweeView = (SimpleDraweeView) LayoutInflater.from(PostContent.this).inflate(R.layout.post_image, null);
                        messageLayout.addView(draweeView);

                        if(post.getImages() != null){
                            Map<String, HashMap<String, String>> file = post.getImages();
                            String fileUri = (String) file.get(imageId).get("uri");
//                            draweeView.setImageURI(fileUri);
                            ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
                                @Override
                                public void onFinalImageSet(
                                        String id,
                                        @Nullable ImageInfo imageInfo,
                                        @Nullable Animatable anim) {
                                    if (anim != null) {
                                        // app-specific logic to enable animation starting
                                        animatables.add(anim);
                                        anim.start();
                                    }
                                }
                            };
                            DraweeController controller = Fresco.newDraweeControllerBuilder()
                                    .setUri(fileUri)
                                    .setAutoPlayAnimations(false)
                                    .setControllerListener(controllerListener)
                                    .build();
                            draweeView.setController(controller);
                        }

                        if (i == stringLocations.size() - 1){
                            String text = message.substring(stringLocations.valueAt(i), message.length());
                            TextView inflateTextView = (TextView) LayoutInflater.from(PostContent.this).inflate(R.layout.post_message, null);
                            inflateTextView.setText(text);
                            messageLayout.addView(inflateTextView);
                        }

                    }

                } else {
                    TextView inflateTextView = (TextView) LayoutInflater.from(PostContent.this).inflate(R.layout.post_message, null);
                    inflateTextView.setText(message);
                    messageLayout.addView(inflateTextView);
                }


                //for fav
                Database.ref_users.child(MainActivity.getUser()).child("favorites").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null){
                            isFaved = true;
                            updateFaved();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //for likes
                if (post.getLikes() != null){
                    likers = new ArrayList<>(post.getLikes().keySet());
                    likeCount = likers.size();
                    txtLikeCount.setText(String.valueOf(likeCount));

                    for (String liker : likers){
                        //if user has already liked it
                        if (MainActivity.getUser().equals(liker)){
                            if (!isLiked)
                                isLiked = true;
                                updateLike();
                        }

                        Database.ref_users.child(liker).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null){
                                    User user = dataSnapshot.getValue(User.class);
                                    if (user.getFirstname() != null)
                                    likersNames.add(user.getFirstname() + " " + user.getLastname());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                Database.ref_users.child(post.getAuthor()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        currentPostAuthor = user;
                        currentAuthorUID = post.getAuthor();
                        post.setAuthorName(user.getFirstname() + " " + user.getLastname());
                        txtAuthorName.setText(post.getAuthorName());
                        txtAuthorPosition.setText(user.getRole());
                        Uri uri = Uri.parse(user.getPicture());
                        SimpleDraweeView draweeView = findViewById(R.id.authorProfile);
                        draweeView.setImageURI(uri);

                        //Enable listener only after the post has fully loaded
                        btnLike.setOnClickListener(btnLikeListener);
                        btnLike.setOnLongClickListener(btnLikeLongClickListener);

                        btnFav.setOnClickListener(btnFavListener);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                txtAuthorName.setText(post.getAuthorName());

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

        Toast.makeText(PostContent.this, "DOWNLOAD STARTED", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostContent.this, "DOWNLOAD COMPLETED", Toast.LENGTH_SHORT).show();


            }
        });
    }

    public void setLiked(boolean condition){
        isLiked = condition;

        if (isLiked) {
            Database.ref_posts.child(key).child("likes").child(MainActivity.getUser()).setValue(true);
            likeCount++;
            updateLike();
        }
        else {
            Database.ref_posts.child(key).child("likes").child(MainActivity.getUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null){
                        dataSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            likeCount--;
            updateLike();
        }


    }
    public void setFaved(boolean condition){
        isFaved = condition;

        if (isFaved) {
            Database.ref_users.child(MainActivity.getUser()).child("favorites").child(key).setValue(true);
            Toast.makeText(PostContent.this, "Post added to favorites. Changes in fav folder requires app restart.", Toast.LENGTH_LONG).show();
            updateFaved();
        }
        else {
            Database.ref_users.child(MainActivity.getUser()).child("favorites").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null){
                        dataSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            updateFaved();
        }


    }
    public void updateLike(){
        if (isLiked)
            btnLike.setColorFilter(ContextCompat.getColor(this, R.color.lightred), android.graphics.PorterDuff.Mode.MULTIPLY);

        else
            btnLike.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);

        txtLikeCount.setText(String.valueOf(likeCount));
    }
    public void updateFaved(){
            if (isFaved)
                btnFav.setColorFilter(ContextCompat.getColor(this, R.color.lightyellow), android.graphics.PorterDuff.Mode.MULTIPLY);

            else
                btnFav.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);

        }

    private void refreshComments(){
        list_comments.clear();
        loadComments();
    }

    private void postComment(){
        String message = formComment.getText().toString().trim();
        String author = MainActivity.getUser();

        if (isCommentPosted == false){
            if (DateRetriever.isConnected()){
                isCommentPosted = true;

                DatabaseReference directory = Database.ref_posts.child(key).child("comments").push();
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("author", author);
                map.put("dateCreated", DateRetriever.getDateToday());
                map.put("timeCreated", DateRetriever.getTime());
                directory.setValue(map);

                directory.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Toast.makeText(PostContent.this, "Comment posted", Toast.LENGTH_LONG).show();
                        refreshComments();
                        commentPanel.setVisibility(View.GONE);
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

    private void setFonts(){
        FontCustomizer fontCustomizer;
        TextView[] tv1 = {txtPostDate,
                findViewById(R.id.command_fav_text), findViewById(R.id.command_reply_text), txtLikeCount, formComment
        };
        TextView[] tv2 = {txtTitle, txtCategory, btnBack, txtAuthorPosition, txtAuthorName, btnComment, txtComments};

        fontCustomizer = new FontCustomizer(this);
        fontCustomizer.setToQuickSand(tv1);
        fontCustomizer.setToQuickSandBold(tv2);
    }

    private View.OnClickListener btnBackListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener btnLikeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isLiked) {
                setLiked(false);
            }
            else if (!isLiked) {
                setLiked(true);
            }
        }
    };private View.OnClickListener btnFavListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isFaved) {
                setFaved(false);
            }
            else if (!isFaved) {
                setFaved(true);

            }
        }
    };

    private View.OnLongClickListener btnLikeLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            String likersList = "";
            if (likers != null && likersNames != null){
                for (String liker : likersNames){
                    likersList += "\n" + liker;
                }
            }
            String text = "Likes:" + likersList;
            sb = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
            sb.setAction("Close", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sb.dismiss();
                }
            });

            View snackbarView = sb.getView();
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(30);

            if (sb.isShown())
                sb.dismiss();
            else
                sb.show();
            return true;
        }
    };

    private View.OnClickListener btnReplyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            commentPanel.setVisibility(View.VISIBLE);
            isReplyPanelVisible = true;
        }
    };

    private View.OnClickListener btnCommentListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            postComment();
        }
    };

    private View.OnClickListener panelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isReplyPanelVisible) {
                commentPanel.setVisibility(View.GONE);
                isReplyPanelVisible = true;
            }
        }
    };


    @Override
    public void onBackPressed() {
        if (isReplyPanelVisible){
            commentPanel.setVisibility(View.GONE);
            isReplyPanelVisible = false;
        }
        else
            super.onBackPressed();
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case PostAdapter.MENU.DELETE:
                adapter_comments.deletePost(item.getGroupId(), key, this);
                System.out.println("DELETE COMMENT");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE){
            for (Animatable animatable : animatables){
                if (animatable.isRunning())
                animatable.stop();
            }
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_UP){
            for (Animatable animatable : animatables){
                if (!animatable.isRunning())
                    animatable.start();
            }
        }
        return false;
    }
}
