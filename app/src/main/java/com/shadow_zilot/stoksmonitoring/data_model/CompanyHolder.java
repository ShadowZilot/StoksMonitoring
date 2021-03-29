package com.shadow_zilot.stoksmonitoring.data_model;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.shadow_zilot.stoksmonitoring.R;
import com.shadow_zilot.stoksmonitoring.ui.CompanyDetailActivity;
import com.shadow_zilot.stoksmonitoring.utils.FavoriteEvent;

import static com.shadow_zilot.stoksmonitoring.ui.CompanyDetailActivity.sTICKER_ARG;

public class CompanyHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = CompanyHolder.class.getSimpleName();
    private Company mCompany;
    private ViewGroup mItemStockView;
    private ImageView mCompanyLogo;
    private TextView mTickerText;
    private TextView mNameText;
    private TextView mStockPriceText;
    private TextView mDeltaPriceText;
    private ImageButton mFavoriteButton;

    public CompanyHolder(View itemView) {
        super(itemView);
        mItemStockView = itemView.findViewById(R.id.item_stock_container);
        mCompanyLogo = itemView.findViewById(R.id.company_logo);
        mTickerText = itemView.findViewById(R.id.ticker_text);
        mNameText = itemView.findViewById(R.id.company_name);
        mStockPriceText = itemView.findViewById(R.id.stock_price_view);
        mDeltaPriceText = itemView.findViewById(R.id.delta_price_view);
        mFavoriteButton = itemView.findViewById(R.id.make_favorite);
    }

    public void bind(Company company, Context _context, FavoriteEvent _listener) {
        mCompany = company;
        mCompanyLogo.setImageResource(R.drawable.ic_no_network);
        mTickerText.setText(company.getTicker());
        mNameText.setText(company.getName());
        mStockPriceText.setText(company.getFormattedStockPrice());
        mDeltaPriceText.setText(String.valueOf(company.getFormattedDeltaPrice()));
        mDeltaPriceText.setTextColor(_context.getResources().getColor(company.getDeltaColor()));
        setFavoriteBack(company.isFavorite());
        mFavoriteButton.setOnClickListener(v -> {
            mCompany.setIsFavorite(!mCompany.isFavorite());
            setFavoriteBack(mCompany.isFavorite());
            _listener.makeFavorite(company.getTicker());
        });

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CompanyDetailActivity.class);
            intent.putExtra(sTICKER_ARG, mCompany.getTicker());
            v.getContext().startActivity(intent);
        });
    }

    private void setFavoriteBack(boolean _isFavorite) {
        if (_isFavorite) {
            mFavoriteButton.setBackgroundResource(R.drawable.ic_favorite_active);
        } else {
            mFavoriteButton.setBackgroundResource(R.drawable.ic_favorite);
        }
    }
}
