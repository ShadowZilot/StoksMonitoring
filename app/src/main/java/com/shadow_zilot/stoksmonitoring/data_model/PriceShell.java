package com.shadow_zilot.stoksmonitoring.data_model;

import android.provider.ContactsContract;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class PriceShell {
    @SerializedName("high")
    private float mHighPrice;
    @SerializedName("low")
    private float mLowPrice;
    @SerializedName("date")
    private long mData;

    private float mPrice;

    public PriceShell(float _price, long _data) {
        mPrice = _price;
        mData = _data;
    }

    public PriceShell(float _highPrice, float _lowPrice, long _data) {
        mPrice = (_highPrice + _lowPrice) / 2;
        mData = _data;
    }

    public PriceShell(String _savedInstance) {
        try {
            String[] separatedInstance = _savedInstance.split(":");
            try {
                mData = Long.parseLong(separatedInstance[0]);
                mPrice = Float.parseFloat(separatedInstance[1].replace(",", "."));
            } catch (NumberFormatException e) {
                mData = 0;
                mPrice = 0;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            mData = 0;
            mPrice = 0;
        }
    }

    public float getPrice() {
        if (mHighPrice > 0 && mLowPrice > 0) {
            return (mHighPrice + mLowPrice) / 2;
        } else {
            return mPrice;
        }
    }

    public String getFormattedPrice() {
        String currencyPrefix = "$";
        return String.format("%1s %2.2f", currencyPrefix, mPrice);
    }

    public Date getData() {
        return new Date(mData);
    }

    public String getSavedInstanceString() {
        return String.format("%1d:%2.2f", mData, getPrice()).replace(",", ".");
    }
}
