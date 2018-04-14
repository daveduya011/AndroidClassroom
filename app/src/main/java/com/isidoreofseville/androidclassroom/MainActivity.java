package com.isidoreofseville.androidclassroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements
        TabFragment1.OnFragmentInteractionListener {
//
//    FileInputStream serviceAccount = new FileInputStream("path/to/serviceAccountKey.json");
//
//    FirebaseOptions options = new FirebaseOptions.Builder()
//            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//            .setDatabaseUrl("https://spcf-class-announcement-system.firebaseio.com")
//            .build();
//
//    FirebaseApp.initializeApp(options);

    TabFragment1 currentfrag;
    private FontCustomizer fontCustomizer;
    private PostsLoader postsLoader;

    public static User userInfo;

    private ViewPager viewPager;

    public static String[] ARRAYCATEGORIES;

    private FirebaseAuth auth;
    public static FirebaseUser user;
    public static boolean user_hasEditPermission;
    public static boolean user_hasDeletePermission;
    public static boolean user_canPostToPublic;

    private PagerAdapter adapter;

    private TabLayout tablayout;

    private Button addPost;
    private Button menuBtn;
    private Database database;
    private Storage storage;

    private DateRetriever dateRetriever;

    //for exiting the app by clicking the back button twice
    private boolean backPressedOnce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        //Authentication
        user = auth.getCurrentUser();
        database = new Database();

        Log.d("Firebase", "token "+ FirebaseInstanceId.getInstance().getToken());

        dateRetriever = new DateRetriever(this);
        storage = new Storage();
        ARRAYCATEGORIES = getResources().getStringArray(R.array.categories);
        CATEGORIES.ROOMANNOUNCEMENT = ARRAYCATEGORIES[0];
        CATEGORIES.HOMEWORK = ARRAYCATEGORIES[1];
        CATEGORIES.PROJECT = ARRAYCATEGORIES[2];
        CATEGORIES.EVENT = ARRAYCATEGORIES[3];
        CATEGORIES.CLASSSUSPENSION = ARRAYCATEGORIES[4];
        CATEGORIES.SCHOOLANNOUNCEMENT = ARRAYCATEGORIES[5];
        CATEGORIES.OTHERS = ARRAYCATEGORIES[6];
        ArrayList<String> listofcategories = new ArrayList<>();

        //add in chosen order
        listofcategories.add(CATEGORIES.RECENT);
        listofcategories.add(CATEGORIES.HOMEWORK);
        listofcategories.add(CATEGORIES.PROJECT);
        listofcategories.add(CATEGORIES.EVENT);
        listofcategories.add(CATEGORIES.ROOMANNOUNCEMENT);
        listofcategories.add(CATEGORIES.SCHOOLANNOUNCEMENT);
        listofcategories.add(CATEGORIES.CLASSSUSPENSION);
        listofcategories.add(CATEGORIES.OTHERS);
        listofcategories.add(CATEGORIES.OWNEDPOSTS);
        listofcategories.add(CATEGORIES.FAVORITES);
        CATEGORIES.LIST = listofcategories;

        if (user != null) {
            Database.ref_users.child(getUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userInfo = dataSnapshot.getValue(User.class);

                    System.out.println("ROLE: " + userInfo.getRole());

                    if (userInfo.getRole().equals(ROLES.TEACHER)){
                        user_hasEditPermission = true;
                        user_hasDeletePermission = true;
                        user_canPostToPublic = true;

                        //SHOW ADMIN TAB WHEN YOU HAvE THE PERMISSION
                        navigationView.getMenu().findItem(R.id.nav_addstudents).setVisible(true);
                    }

                    //set navigation bar profile and name
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View hView =  navigationView.getHeaderView(0);
                    TextView nav_user = hView.findViewById(R.id.txtname);
                    nav_user.setText(userInfo.getFirstname() + " " +userInfo.getLastname());
                    TextView nav_section = hView.findViewById(R.id.txtsection);
                    nav_section.setText(userInfo.getSection());
                    //set profile picture
                    Uri uri = Uri.parse(userInfo.getPicture());
                    SimpleDraweeView draweeView = hView.findViewById(R.id.profilepicture);
                    draweeView.setImageURI(uri);


                    Database.setSection(userInfo.getSection());
                    Storage.loadSectionFiles(userInfo.getSection());
                    System.out.println(Database.section);
                    load();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        //for addbutton
        addPost = findViewById(R.id.btnAddPost);
        menuBtn = findViewById(R.id.btnMenu);
        addPost.setOnClickListener(addPostListener);
        menuBtn.setOnClickListener(menuBtnListener);
        tablayout = findViewById(R.id.tablayout);
        tablayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = findViewById(R.id.pager);

        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.LIST.get(tablayout.getTabCount())));
        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.LIST.get(tablayout.getTabCount()) + "S"));
        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.LIST.get(tablayout.getTabCount()) + "S"));
        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.LIST.get(tablayout.getTabCount()) + "S"));
        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.LIST.get(tablayout.getTabCount()) + "S"));
        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.LIST.get(tablayout.getTabCount()) + "S"));
        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.LIST.get(tablayout.getTabCount()) + "S"));
        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.LIST.get(tablayout.getTabCount())));

        adapter = new PagerAdapter(getSupportFragmentManager(), tablayout.getTabCount(), null);
        currentfrag = (TabFragment1) adapter.instantiateItem(viewPager, 0);

        setFonts();

        //We need to wait for some time to get the connection state
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Connected? " + DateRetriever.isConnected());
                Looper.prepare();
                if (DateRetriever.isConnected()) {
                } else {
                    Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        }, 9000);

    }

    private void load() {
        //tablayout.addTab(tablayout.newTab().setText("PROJECTS"));
        //tablayout.addTab(tablayout.newTab().setText("EVENTS"));
        //tablayout.addTab(tablayout.newTab().setText("OTHERS"));
        //tablayout.addTab(tablayout.newTab().setText("ALL ANNOUNCEMENTS"));

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tablayout));
//        viewPager.setOffscreenPageLimit(3);

        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
                TabFragment1 frag = (TabFragment1) adapter.instantiateItem(viewPager, tab.getPosition());
                currentfrag = frag;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case PostAdapter.MENU.VIEW:
                System.out.println("VIEW SELECTED");
                currentfrag.postsLoader.adapter.openActivity(item.getGroupId());
                System.out.println("GROUP ID: " + item.getGroupId());
                return true;
            case PostAdapter.MENU.DELETE:
                currentfrag.postsLoader.adapter.deletePost(item.getGroupId());
                System.out.println("DELETE SELECTED");
                return true;
            case PostAdapter.MENU.EDIT:
                currentfrag.postsLoader.adapter.editPost(item.getGroupId());
                System.out.println("EDIT SELECTED");
                return true;
            case PostAdapter.MENU.REPORT:
                System.out.println("REPORT SELECTED");
                currentfrag.postsLoader.adapter.reportPost(item.getGroupId());
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

/*
    @Override
    public boolean onContextItemSelected(MenuItem item) {


        switch (item.getItemId()){
            case PostAdapter.MENU.VIEW:
                System.out.println("VIEW SELECTED");
                //postsLoader.adapter.openActivity(item.getGroupId());
                return true;
            case PostAdapter.MENU.DELETE:
                postsLoader.adapter.deletePost(item.getGroupId());
                System.out.println("DELETE SELECTED");
                return true;
            case PostAdapter.MENU.EDIT:
                postsLoader.adapter.editPost(item.getGroupId());
                System.out.println("EDIT SELECTED");
                return true;
            case PostAdapter.MENU.FAV:
                System.out.println("FAV SELECTED");
                return true;
            case PostAdapter.MENU.REPORT:
                System.out.println("REPORT SELECTED");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
         */

    //When add button has been pressed
    private View.OnClickListener addPostListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }
    };

    //When add button has been pressed
    private View.OnClickListener menuBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openDrawer();
        }
    };

    @Override
    protected void onStart() {

        super.onStart();

        user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();

        }

        backPressedOnce = false;
    }

    public static String getUser() {
        String uid;
        if (user != null) {
            uid = user.getUid();
        } else {
            uid = "ANONYMOUS";
        }
        return uid;
    }

    /*@Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case 0:
                System.out.println("PRESSED: " + item.getGroupId());
                removeUser(item.getGroupId());
                break;
            case 1:
                System.out.println("PRESSED: " + item.getGroupId());
                editPost(item.getGroupId());
                break;
        }

        return super.onContextItemSelected(item);
    }*/

    private void setFonts() {
        TextView[] tv1 = {(TextView) findViewById(R.id.Announcements)};

        fontCustomizer = new FontCustomizer(this);
        fontCustomizer.setToQuickSandBold(tv1);
    }

    @Override
    public void onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed();
        }
        backPressedOnce = true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch(id){
            case R.id.nav_profile:
                Intent intent = new Intent(this, Profile.class);
                intent.putExtra("isOwned", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_messages:
                Intent intentM = new Intent(this, UnderConstruction.class);
                startActivity(intentM);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_favorites:
                Intent intentFav = new Intent(this, Favorites.class);
                startActivity(intentFav);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_yourposts:
                Intent intentYP = new Intent(this, YourPosts.class).putExtra("UID", getUser());
                startActivity(intentYP);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_addstudents:
                Intent intent2 = new Intent(this, AddStudentsActivity.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_files:
                Intent intentF = new Intent(this, Files.class);
                startActivity(intentF);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_section:
                Intent intentSec = new Intent(this, Section.class);
                startActivity(intentSec);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_settings:
                Intent intentSet = new Intent(this, UnderConstruction.class);
                startActivity(intentSet);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_about:
                Intent intentAb = new Intent(this, UnderConstruction.class);
                startActivity(intentAb
                );
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                onStart();
                break;
        }


        return super.onNavigationItemSelected(item);
    }
}

