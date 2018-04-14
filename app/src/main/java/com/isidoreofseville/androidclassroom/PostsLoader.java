package com.isidoreofseville.androidclassroom;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dave on 1/8/2018.
 */

public class PostsLoader {

    private DateTime dateToday;

    private RecyclerView recyclerview;
    public PostAdapter adapter;
    private List<Post> list_posts;

    private Activity context;
    private View view;

    private LatestPost postToday;
    private LatestPost postYesterday;
    private LatestPost postThisWeek;
    private LatestPost postNoRecent;
    private LatestPost postLastWeek;

    private String category;


    private boolean isDoneLoading;

    private final int MAX_POSTS_NUMBER = 15;
    private int currentPostNumber;

    private String param2;

    public PostsLoader(Activity context, View view, String category, String UID) {
        this.context = context;
        this.view = view;
        this.category = category;
        this.param2 = UID;

        System.out.println("CATEGORY:" + category);

        dateToday = new DateTime();

        if (category == CATEGORIES.RECENT){
            postToday = new LatestPost();
            postYesterday = new LatestPost();
            postThisWeek = new LatestPost();
            postLastWeek = new LatestPost();
            postNoRecent = new LatestPost();
        }
        load();
        //Start requesting for posts
        startListening();
    }

    public void startListening() {
        Database.ref_posts.orderByKey().limitToLast(MAX_POSTS_NUMBER).addChildEventListener(postListener);
        organizePosts();
    }

    private void load(){
        list_posts = new ArrayList<>();
        adapter = new PostAdapter(list_posts, context);

        recyclerview = view.findViewById(R.id.recycleview_posts);

        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(context));
        recyclerview.setAdapter(adapter);
    }


    private ChildEventListener postListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            final Post model;
            System.out.println("On child added");
            //IF NO ERROR, THEN SET THE VALUES
            model = dataSnapshot.getValue(Post.class);

            //If post is owned by user, we'll let them edit their posts

            if (model.getAuthor().equals(MainActivity.getUser())){
                model.setOwnedPost(true);
            }

            //Update the author names
            Database.ref_users.child(model.getAuthor()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User author = dataSnapshot.getValue(User.class);
                    String name;

                    if (author != null) {


                        //display the name of the author
                        if (model.isOwnedPost()) {
                            name = "You";
                        } else {
                            name = author.getFirstname() + " " + author.getLastname();
                        }

                        model.setAuthorName(name);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            int dayCreated = Integer.parseInt(model.getDateCreated().substring(3, 5));
            int monthCreated = Integer.parseInt(model.getDateCreated().substring(0, 2));
            int yearCreated = Integer.parseInt(model.getDateCreated().substring(6, 10));


            DateTime postDate = new DateTime(yearCreated, monthCreated, dayCreated, 0, 0);
            int daysBetween = Days.daysBetween(dateToday, postDate).getDays();

            if (category == CATEGORIES.RECENT){
                //Order by date
                //if post is created today
                if (daysBetween == 0){
                    int lastQuery = postToday.date;
                    int currentQuery = dayCreated;
                    String modelKey = model.getKey();
                    String postKey = postToday.key;
                    postToday.getLatestPost(lastQuery, currentQuery, modelKey, postKey);

                } else if (daysBetween == -1){
                    int lastQuery = postYesterday.date;
                    int currentQuery = dayCreated;
                    String modelKey = model.getKey();
                    String postKey = postYesterday.key;
                    postYesterday.getLatestPost(lastQuery, currentQuery, modelKey, postKey);
                } else if (daysBetween >= -7){
                    int lastQuery = postThisWeek.date;
                    int currentQuery = dayCreated;
                    String modelKey = model.getKey();
                    String postKey = postThisWeek.key;
                    postThisWeek.getLatestPost(lastQuery, currentQuery, modelKey, postKey);
                } else if (daysBetween >= -14){
                    int lastQuery = postLastWeek.date;
                    int currentQuery = dayCreated;
                    String modelKey = model.getKey();
                    String postKey = postLastWeek.key;
                    postLastWeek.getLatestPost(lastQuery, currentQuery, modelKey, postKey);

                } else {
                    int lastQuery = postNoRecent.date;
                    int currentQuery = dayCreated;
                    String modelKey = model.getKey();
                    String postKey = postNoRecent.key;
                    postNoRecent.getLatestPost(lastQuery, currentQuery, modelKey, postKey);
                }

                list_posts.add(model);
            }
            //IF CATEGORY IS "ROOM ANNOUNCEMENT"
            else if (category == CATEGORIES.HOMEWORK){
                if (model.getCategories().containsKey(CATEGORIES.HOMEWORK)){
                    list_posts.add(model);
                }
            }else if (category == CATEGORIES.ROOMANNOUNCEMENT){
                if (model.getCategories().containsKey(CATEGORIES.ROOMANNOUNCEMENT)){
                    list_posts.add(model);
                }
            }else if (category == CATEGORIES.PROJECT){
                if (model.getCategories().containsKey(CATEGORIES.PROJECT)){
                    list_posts.add(model);
                }
            }else if (category == CATEGORIES.CLASSSUSPENSION){
                if (model.getCategories().containsKey(CATEGORIES.CLASSSUSPENSION)){
                    list_posts.add(model);
                }
            }else if (category == CATEGORIES.SCHOOLANNOUNCEMENT){
                if (model.getCategories().containsKey(CATEGORIES.SCHOOLANNOUNCEMENT)){
                    list_posts.add(model);
                }
            }else if (category == CATEGORIES.EVENT){
                if (model.getCategories().containsKey(CATEGORIES.EVENT)){
                    list_posts.add(model);
                }
            }else if (category == CATEGORIES.OTHERS){
                if (model.getCategories().containsKey(CATEGORIES.OTHERS)){
                    list_posts.add(model);
                }
            } else if (category == CATEGORIES.OWNEDPOSTS){
                if (model.getAuthor().equals(param2)){
                    list_posts.add(model);
                }
            } else if (category == CATEGORIES.FAVORITES){

                if (MainActivity.userInfo.getFavorites() != null){
                    for (String key : MainActivity.userInfo.getFavorites().keySet()){
                        if (model.getKey().equals(key)){
                            list_posts.add(model);
                        }
                    }
                }

            }

            //load changes when there is one or two new posts has been added while screen is on
            if (isDoneLoading){

                //Update the author names
                Database.ref_users.child(model.getAuthor()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User author = dataSnapshot.getValue(User.class);
                        String name;

                        //display the name of the author
                        if (model.isOwnedPost()){
                            name = "You";
                        } else {
                            name = author.getFirstname() + " " + author.getLastname();
                        }

                        model.setAuthorName(name);

                            //SORT RESULT BY DATE POSTED
                            Collections.sort(list_posts);
                            Collections.reverse(list_posts);
                            if (category.equals(CATEGORIES.RECENT))
                            refreshAll();
                            else refresh();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            refreshAll();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
//            Post model = dataSnapshot.getValue(Post.class);
//
//            int index = getItemIndex(model);
//
//            //SOMETIMES, OTHER DEVICES RETURNS INDEX VALUE OF -1,
//            //SO TO AVOID GETTING ERROR,
//            if (index != -1){
//
//                if (index <= list_posts.size()-1){
//                    list_posts.remove(index);
//                    adapter.notifyItemRemoved(index);
//                } else if (index <= result_yesterday.size()-1){
//                    result_yesterday.remove(index - list_posts.size());
//                    adapter_yesterday.notifyItemRemoved(index - list_posts.size() - 1);
//                } else if (index <= result_past.size()-1){
//                    result_past.remove(index - list_posts.size() - result_yesterday.size());
//                    adapter_yesterday.notifyItemRemoved(index - list_posts.size() - result_yesterday.size() - 2);
//                }
//
//            }
            refreshAll();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    public void organizePosts(){
        Database.ref_posts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post modelHeader;
                if (category == CATEGORIES.RECENT){
                    if (postToday.hasPost){
                        modelHeader = new Post("Today's Announcements", PostAdapter.VIEW_TYPES.HEADER, postToday.key);
                        list_posts.add(modelHeader);
                    }

                    if (postYesterday.hasPost) {
                        modelHeader = new Post("Yesterday", PostAdapter.VIEW_TYPES.HEADER, postYesterday.key);
                        list_posts.add(modelHeader);
                    }

                    if (postThisWeek.hasPost){
                        modelHeader = new Post("This Week", PostAdapter.VIEW_TYPES.HEADER, postThisWeek.key);
                        list_posts.add(modelHeader);
                    }

                    if (postLastWeek.hasPost){
                        modelHeader = new Post("Last Week", PostAdapter.VIEW_TYPES.HEADER, postLastWeek.key);
                        list_posts.add(modelHeader);
                    }

                    if (postNoRecent.hasPost){
                        modelHeader = new Post("Long Time Ago", PostAdapter.VIEW_TYPES.HEADER, postNoRecent.key);
                        list_posts.add(modelHeader);
                    }
                }

                //Update posts
                Database.ref_posts.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        refresh();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //SORT RESULT BY DATE POSTED
                Collections.sort(list_posts);
                Collections.reverse(list_posts);
                isDoneLoading = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void refreshAll(){

        //Remove listeners
        Database.ref_posts.removeEventListener(postListener);

        list_posts.clear();
        adapter.notifyDataSetChanged();

        if (category.equals(CATEGORIES.RECENT)){
            postToday.hasPost = false;
            postYesterday.hasPost = false;
            postThisWeek.hasPost = false;
            postLastWeek.hasPost = false;
            postNoRecent.hasPost = false;
        }

        isDoneLoading = false;

        startListening();
    }

    public void refresh(){
        //stopListening();
        //startListening();
        adapter.notifyDataSetChanged();
    }

    private int getItemIndex(Post model){
        int index = 0;

        for (int i = 0; i < list_posts.size(); i++){
            if (list_posts.get(i).getKey().equals(model.getKey())){
                index = i;
                break;
            }
        }

        return index;
    }


    /*private void removeUser(final int position){
        for (final DatabaseReference category : categories){
            category.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String key = list_posts.get(position).getKey();
                    if (dataSnapshot.hasChild(key)){
                        category.child(key).removeValue();
                        System.out.println("Removed " + key + "under " + category.getKey());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void editPost(final int position){
        final Post post = list_posts.get(position);
        post.setMessage("TROLLOLOLOL");

        for (final DatabaseReference category : categories){
            category.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String key = list_posts.get(position).getKey();
                    if (dataSnapshot.hasChild(key)){
                        category.child(key).child("message").setValue(post.getMessage());
                        System.out.println("Edited " + key + "under " + category.getKey());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }*/


}
