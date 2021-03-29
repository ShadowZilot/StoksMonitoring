package com.shadow_zilot.stoksmonitoring.data_model;

import com.google.gson.annotations.SerializedName;
import com.shadow_zilot.stoksmonitoring.R;

import java.util.ArrayList;

public class Company {

    @SerializedName("symbol")
    private String mTicker;

    @SerializedName("shortName")
    private String mName;

    @SerializedName("financialCurrency")
    private String mCurrencyName;

    @SerializedName("regularMarketPrice")
    private float mStockPrice;

    @SerializedName("regularMarketChangePercent")
    private float mDeltaPrice;

    private ArrayList<PriceShell> mPricesList;

    private boolean mIsFavorite;

    public Company(String mTicker, String mName, float mStockPrice,
                   float mDeltaPrice, String mCurrencyName, boolean mIsFavorite) {
        this(mTicker, mName, "", mStockPrice, mDeltaPrice, mCurrencyName, mIsFavorite);
    }

    public Company(String mTicker, String mName, String savedPricesList, float mStockPrice,
                   float mDeltaPrice, String mCurrencyName, boolean mIsFavorite) {
        this.mTicker = mTicker;
        this.mName = mName;
        setPricesList(savedPricesList);
        this.mStockPrice = mStockPrice;
        this.mDeltaPrice = mDeltaPrice;
        this.mCurrencyName = mCurrencyName;
        this.mIsFavorite = mIsFavorite;
    }

    public String getTicker() {
        return mTicker;
    }

    public String getName() {
        return mName;
    }

    public String getFormattedStockPrice() {
        String currencyPrefix = "$";
        return String.format("%1s %2.2f", currencyPrefix, mStockPrice);
    }

    public String getFormattedDeltaPrice() {
        String prefix;
        if (mDeltaPrice > 0) {
            prefix = "+$";
        } else if (mDeltaPrice < 0) {
            prefix = "-$";
        } else {
            prefix = "$";
        }
        return String.format("%1s%2.2f", prefix, Math.abs(mDeltaPrice));
    }

    public int getDeltaColor() {
        if (mDeltaPrice > 0) {
            return R.color.positive_delta_color;
        } else if (mDeltaPrice < 0) {
            return R.color.negative_delta_color;
        } else {
            return R.color.black;
        }
    }

    public String getPricesSaveString() {
        if (mPricesList != null && !mPricesList.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (PriceShell shell : mPricesList) {
                builder.append(shell.getSavedInstanceString()).append(";");
            }
            return builder.toString();
        } else {
            return "";
        }
    }

    public ArrayList<PriceShell> getPricesList() {
        return mPricesList;
    }

    public void setPricesList(String _savedPricesList) {
        if (mPricesList == null) {
            mPricesList = new ArrayList<>();
        }
        mPricesList.clear();
        String[] separatedSaved = _savedPricesList.split(";");
        for (String shell: separatedSaved) {
            mPricesList.add(new PriceShell(shell));
        }
    }

    public void setPricesList(ArrayList<PriceShell> _pricesList) {
        mPricesList.clear();
        mPricesList = _pricesList;
    }

    public float getStockPrice() {
        return mStockPrice;
    }

    public float getDeltaPrice() {
        return mDeltaPrice;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public String getCurrencyName() {
        return mCurrencyName;
    }

    public void setIsFavorite(boolean mIsFavorite) {
        this.mIsFavorite = mIsFavorite;
    }

    @Override
    public String toString() {
        return String.format("Name=%1s, StockPrice=%2f, Delta=%3f, IsFavorite=%4b",
                mName, mStockPrice, mDeltaPrice, mIsFavorite);
    }
}
