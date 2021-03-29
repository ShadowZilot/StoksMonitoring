package com.shadow_zilot.stoksmonitoring.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shadow_zilot.stoksmonitoring.utils.CompaniesAdapter;
import com.shadow_zilot.stoksmonitoring.R;
import com.shadow_zilot.stoksmonitoring.database.CompaniesLab;

public class StockListFragment extends Fragment {
    private static final String sARG_IS_FAVORITE = "is_favorite_arg";

    private ViewGroup mErrorsContainers;
    private RecyclerView mListStock;
    private CompaniesAdapter mAdapter;

    private boolean mIsFavoriteView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsFavoriteView = requireArguments().getBoolean(sARG_IS_FAVORITE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragmnet_stock_list, container,
                false);
        CompaniesLab lab = CompaniesLab.get(requireActivity());

        mErrorsContainers = rootView.findViewById(R.id.error_indicator_container);
        mListStock = rootView.findViewById(R.id.stocks_list);
        mListStock.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mAdapter = new CompaniesAdapter(requireActivity(), mIsFavoriteView, _resultCode -> {
            if (_resultCode == CompaniesLab.OK_RESULT) {
                mErrorsContainers.setVisibility(View.GONE);
                mListStock.setVisibility(View.VISIBLE);
                mListStock.getAdapter().notifyDataSetChanged();
            } else if (_resultCode == CompaniesLab.EMPTY_RESULT) {
                mErrorsContainers.setVisibility(View.VISIBLE);
                mListStock.setVisibility(View.GONE);
            }
        });
        mListStock.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            CompaniesLab.get(requireActivity()).updateUI();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void performSearch(String searchQuery) {
        if (mAdapter != null) {
            mAdapter.trySearch(searchQuery);
        }
    }

    public static StockListFragment getInstance(boolean _isFavoriteList) {
        Bundle args = new Bundle();
        args.putBoolean(sARG_IS_FAVORITE, _isFavoriteList);
        StockListFragment fragment = new StockListFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
