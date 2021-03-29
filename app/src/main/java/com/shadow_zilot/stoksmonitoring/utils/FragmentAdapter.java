package com.shadow_zilot.stoksmonitoring.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.shadow_zilot.stoksmonitoring.ui.StockListFragment;

import java.util.ArrayList;

public class FragmentAdapter extends FragmentStatePagerAdapter {
    private ArrayList<StockListFragment> mListOfFragments;

    public FragmentAdapter(FragmentManager fm, ArrayList<StockListFragment> _listOfFragments) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mListOfFragments = _listOfFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mListOfFragments.get(position);
    }

    @Override
    public int getCount() {
        return mListOfFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Stocks";
        } else {
            return "Favorite";
        }
    }
}
