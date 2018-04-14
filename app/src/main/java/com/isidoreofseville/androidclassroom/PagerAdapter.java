package com.isidoreofseville.androidclassroom;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Dave on 3/3/2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {

    int numOfTabs;
    String UID;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, String UID) {
        super(fm);
        this.numOfTabs = NumOfTabs;
        if (UID != null){
            this.UID = UID;
        }
    }

    @Override
    public Fragment getItem(int position) {
        String param = CATEGORIES.LIST.get(position);

        if (CATEGORIES.LIST.get(position).equals(CATEGORIES.OWNEDPOSTS)){
            return TabFragment1.newInstance(param, UID);
        }
        return TabFragment1.newInstance(param, "");
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
