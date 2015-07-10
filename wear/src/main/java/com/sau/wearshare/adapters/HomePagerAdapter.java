package com.sau.wearshare.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.util.List;

/**
 * Created by saurabh on 15-07-10.
 */
public class HomePagerAdapter extends FragmentGridPagerAdapter {
    List<Fragment> fragments;

    public HomePagerAdapter(FragmentManager fm, List<Fragment> fragments){
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getFragment(int row, int column) {
        return fragments.get(column);
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return fragments == null ? 0 : fragments.size();
    }
}
