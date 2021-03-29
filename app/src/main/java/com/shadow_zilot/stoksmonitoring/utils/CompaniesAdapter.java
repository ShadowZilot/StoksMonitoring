package com.shadow_zilot.stoksmonitoring.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.shadow_zilot.stoksmonitoring.R;
import com.shadow_zilot.stoksmonitoring.data_model.Company;
import com.shadow_zilot.stoksmonitoring.data_model.CompanyHolder;
import com.shadow_zilot.stoksmonitoring.database.CompaniesLab;

import java.util.ArrayList;

public class CompaniesAdapter extends RecyclerView.Adapter<CompanyHolder> {
    private static final String LOG_TAG = CompaniesAdapter.class.getSimpleName();

    private ArrayList<Company> mListOfCompanies = new ArrayList<>();
    private ChangeObservable mChangeObserver;
    private Context mContext;
    private FavoriteEvent mListener;

    public CompaniesAdapter(Context _context, boolean _isFavorite, ChangeObservable _observer) {
        CompaniesLab _database = CompaniesLab.get(_context);
        mChangeObserver = _observer;

        if (_isFavorite) {
            _database.addFavoriteObserver(_companies -> {
                mListOfCompanies = _companies;
                callObserver();
            });
        } else {
            _database.addCommonObserver(_companies -> {
                mListOfCompanies = _companies;
                callObserver();
            });
        }

        mContext = _context;
        mListener = _database::changeFavoriteState;
    }

    private void callObserver() {
        if (!mListOfCompanies.isEmpty()) {
            mChangeObserver.notifyChanges(CompaniesLab.OK_RESULT);
        } else {
            mChangeObserver.notifyChanges(CompaniesLab.EMPTY_RESULT);
        }
    }

    public void trySearch(String searchQuery) {
        Log.d(LOG_TAG, String.format("Search query = %1s", searchQuery));
        CompaniesLab lab = CompaniesLab.get(mContext);
        lab.performSearch(searchQuery);
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public CompanyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item_dark,
                    parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item,
                    parent, false);
        }
        return new CompanyHolder(view);
    }

    @Override
    public void onBindViewHolder(CompanyHolder holder, int position) {
        holder.bind(mListOfCompanies.get(position), mContext, mListener);
    }

    @Override
    public int getItemCount() {
        return mListOfCompanies.size();
    }
}
