package com.isidoreofseville.androidclassroom;

import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import java.lang.reflect.Field;

public class Favorites extends AppCompatActivity implements TabFragment1.OnFragmentInteractionListener {

    TabFragment1 currentfrag;
    private PagerAdapter adapter;
    private TabLayout tablayout;
    private CustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        tablayout = findViewById(R.id.tablayout);
        tablayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager = findViewById(R.id.pager);

        tablayout.addTab(tablayout.newTab().setText(CATEGORIES.FAVORITES));
        adapter = new PagerAdapter(getSupportFragmentManager(), CATEGORIES.LIST.size(), null);
        currentfrag = (TabFragment1) adapter.instantiateItem(viewPager, CATEGORIES.LIST.indexOf(CATEGORIES.FAVORITES));

        Field field = null;
        try {
            field = ViewPager.class.getDeclaredField("mRestoredCurItem");
            field.setAccessible(true);
            field.set(viewPager, CATEGORIES.LIST.indexOf(CATEGORIES.FAVORITES));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        viewPager.setAdapter(adapter);
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
