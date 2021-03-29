package com.shadow_zilot.stoksmonitoring.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.shadow_zilot.stoksmonitoring.utils.FragmentAdapter;
import com.shadow_zilot.stoksmonitoring.R;
import com.shadow_zilot.stoksmonitoring.database.CompaniesLab;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String sARG_IS_LOADED = "isLoaded";

    private FragmentManager mManager;
    private FragmentAdapter mNavigationAdapter;
    private ViewPager mViewPager;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = getSupportFragmentManager();
        mViewPager = findViewById(R.id.pager);
        Log.d(LOG_TAG, "MainActivity is recreated!");
        mNavigationAdapter = new FragmentAdapter(mManager, getListOfTabsFragment());
        mViewPager.setAdapter(mNavigationAdapter);

        TabLayout tabs = findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(mViewPager);
    }

    @Override
    public void onResume() {
        super.onResume();
        CompaniesLab.get(this).resetSearchQuery();
        try {
            mSearchView.setQuery("", false);
            mSearchView.onActionViewCollapsed();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        mSearchView = (SearchView) menu.findItem(R.id.search_item).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                StockListFragment fragment = (StockListFragment) mNavigationAdapter.getItem(0);
                fragment.performSearch(String.valueOf(query));
                fragment = (StockListFragment) mNavigationAdapter.getItem(1);
                fragment.performSearch(String.valueOf(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                StockListFragment fragment = (StockListFragment) mNavigationAdapter.getItem(0);
                fragment.performSearch(String.valueOf(newText));
                fragment = (StockListFragment) mNavigationAdapter.getItem(1);
                fragment.performSearch(String.valueOf(newText));
                return true;
            }
        });
        return true;
    }


    private ArrayList<StockListFragment> getListOfTabsFragment() {
        ArrayList<StockListFragment> listFragments = new ArrayList<>();
        listFragments.add(StockListFragment.getInstance(false));
        listFragments.add(StockListFragment.getInstance(true));
        return listFragments;
    }
}